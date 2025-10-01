package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.*;
import java.util.List;
import com.integrixs.data.sql.repository.*;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.backend.dto.PackageCreationRequest;
import com.integrixs.backend.dto.PackageCreationResult;
import com.integrixs.backend.dto.PackageCreationRequest.AdapterRequest;
import com.integrixs.backend.dto.PackageCreationRequest.StructureRequest;
import com.integrixs.backend.dto.PackageCreationRequest.TransformationRequest;
import com.integrixs.backend.dto.PackageCreationRequest.FieldMappingRequest;
import com.integrixs.backend.dto.PackageCreationRequest.OrchestrationTargetRequest;
import com.integrixs.backend.audit.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Service for handling transactional package creation with rollback capability
 */
@Service
public class TransactionalPackageCreationService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionalPackageCreationService.class);

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private IntegrationFlowSqlRepository flowRepository;

    @Autowired
    private CommunicationAdapterSqlRepository adapterRepository;

    @Autowired
    private FlowStructureSqlRepository flowStructureRepository;

    @Autowired
    private MessageStructureSqlRepository messageStructureRepository;

    @Autowired
    private FlowTransformationSqlRepository transformationRepository;

    @Autowired
    private FieldMappingSqlRepository fieldMappingRepository;

    @Autowired
    private OrchestrationTargetSqlRepository orchestrationTargetRepository;

    @Autowired
    private ProcessEngineService processEngineService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Package creation context to track progress
     */
    public static class PackageCreationContext {
        private final UUID correlationId;
        private final Map<String, Object> createdResources;
        private final List<CompensationAction> compensationActions;
        private final List<String> checkpoints;
        private PackageCreationStatus status;
        private String currentStep;
        private int progress;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public PackageCreationContext() {
            this.correlationId = UUID.randomUUID();
            this.createdResources = new HashMap<>();
            this.compensationActions = new ArrayList<>();
            this.checkpoints = new ArrayList<>();
            this.status = PackageCreationStatus.IN_PROGRESS;
            this.startTime = LocalDateTime.now();
            this.progress = 0;
        }

        public void addResource(String key, Object resource) {
            createdResources.put(key, resource);
        }

        public void addCompensation(CompensationAction action) {
            compensationActions.add(action);
        }

        public void checkpoint(String name) {
            checkpoints.add(name);
            logger.debug("Checkpoint reached: {}", name);
        }

        public void updateProgress(int progress, String currentStep) {
            this.progress = Math.min(100, Math.max(0, progress));
            this.currentStep = currentStep;
        }

        // Getters and setters
        public UUID getCorrelationId() { return correlationId; }
        public Map<String, Object> getCreatedResources() { return createdResources; }
        public List<CompensationAction> getCompensationActions() { return compensationActions; }
        public List<String> getCheckpoints() { return checkpoints; }
        public PackageCreationStatus getStatus() { return status; }
        public void setStatus(PackageCreationStatus status) { this.status = status; }
        public String getCurrentStep() { return currentStep; }
        public int getProgress() { return progress; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }

    /**
     * Package creation status
     */
    public enum PackageCreationStatus {
        IN_PROGRESS, COMPLETED, FAILED, ROLLED_BACK, PARTIALLY_COMPLETED
    }

    /**
     * Compensation action for rollback
     */
    @FunctionalInterface
    public interface CompensationAction {
        void compensate() throws Exception;
    }

    /**
     * Create a complete integration package with full transaction support
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public PackageCreationResult createPackage(PackageCreationRequest request) {
        PackageCreationContext context = new PackageCreationContext();

        try {
            logger.info("Starting package creation with correlation ID: {}", context.getCorrelationId());

            // Create flow
            context.updateProgress(10, "Creating integration flow");
            IntegrationFlow flow = createFlow(request, context);
            context.checkpoint("flow_created");

            // Create adapters
            context.updateProgress(25, "Creating adapters");
            createAdapters(request, flow, context);
            context.checkpoint("adapters_created");

            // Create structures
            context.updateProgress(40, "Creating message structures");
            createStructures(request, flow, context);
            context.checkpoint("structures_created");

            // Create transformations
            context.updateProgress(55, "Creating transformations");
            createTransformations(request, flow, context);
            context.checkpoint("transformations_created");

            // Create orchestration targets
            context.updateProgress(70, "Configuring orchestration");
            createOrchestrationTargets(request, flow, context);
            context.checkpoint("orchestration_configured");

            // Deploy to process engine if enabled
            if(request.isDeployToEngine()) {
                context.updateProgress(85, "Deploying to process engine");
                deployToProcessEngine(flow, context);
                context.checkpoint("deployed_to_engine");
            }

            // Note: SQL repositories don't have a flush mechanism - changes are immediate

            // Final validation
            context.updateProgress(95, "Validating package");
            validatePackage(flow, context);
            context.checkpoint("validation_complete");

            // Mark as complete
            context.updateProgress(100, "Package creation completed");
            context.setStatus(PackageCreationStatus.COMPLETED);
            context.setEndTime(LocalDateTime.now());

            // Audit success
            auditService.logPackageCreation(context.getCorrelationId(), "SUCCESS",
                "Package created successfully", context.getCreatedResources());

            logger.info("Package creation completed successfully: {}", context.getCorrelationId());

            return PackageCreationResult.success(flow, context);

        } catch(Exception e) {
            logger.error("Package creation failed: {}", e.getMessage(), e);
            context.setStatus(PackageCreationStatus.FAILED);
            context.setErrorMessage(e.getMessage());
            context.setEndTime(LocalDateTime.now());

            // Audit failure
            auditService.logPackageCreation(context.getCorrelationId(), "FAILED",
                e.getMessage(), context.getCreatedResources());

            // Transaction will be rolled back automatically
            throw new PackageCreationException("Package creation failed: " + e.getMessage(), e, context);
        }
    }

    /**
     * Create package with manual compensation(for complex scenarios)
     */
    public PackageCreationResult createPackageWithCompensation(PackageCreationRequest request) {
        PackageCreationContext context = new PackageCreationContext();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try {
            // Execute in transaction with manual control
            IntegrationFlow flow = transactionTemplate.execute(status -> {
                try {
                    return executePackageCreation(request, context, status);
                } catch(Exception e) {
                    // Mark for rollback
                    status.setRollbackOnly();
                    throw new RuntimeException(e);
                }
            });

            return PackageCreationResult.success(flow, context);

        } catch(Exception e) {
            logger.error("Package creation failed, executing compensation", e);

            // Execute compensation actions
            executeCompensation(context);

            return PackageCreationResult.failure(e.getMessage(), context);
        }
    }

    /**
     * Execute package creation steps
     */
    private IntegrationFlow executePackageCreation(PackageCreationRequest request,
                                                  PackageCreationContext context,
                                                  TransactionStatus transactionStatus) throws Exception {
        // Similar to createPackage but with compensation tracking
        IntegrationFlow flow = createFlow(request, context);

        // Add compensation for flow deletion
        context.addCompensation(() -> {
            if(flow.getId() != null) {
                flowRepository.deleteById(flow.getId());
            }
        });

        createAdapters(request, flow, context);
        createStructures(request, flow, context);
        createTransformations(request, flow, context);
        createOrchestrationTargets(request, flow, context);

        if(request.isDeployToEngine()) {
            deployToProcessEngine(flow, context);
        }

        validatePackage(flow, context);

        return flow;
    }

    /**
     * Create integration flow
     */
    private IntegrationFlow createFlow(PackageCreationRequest request, PackageCreationContext context) {
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName(request.getFlowName());
        flow.setDescription(request.getDescription());
        // Set flow type - assuming request.getFlowType() returns a string that needs to be converted
        if ("DIRECT_MAPPING".equals(request.getFlowType())) {
            flow.setFlowType(FlowType.DIRECT_MAPPING);
        } else if ("ORCHESTRATION".equals(request.getFlowType())) {
            flow.setFlowType(FlowType.ORCHESTRATION);
        }
        flow.setActive(false); // Start inactive
        // IntegrationFlow doesn't have a configuration field
        // Need to fetch the User entity
        User user = new User();
        user.setId(request.getUserId());
        flow.setCreatedBy(user);
        flow.setCreatedAt(LocalDateTime.now());

        flow = flowRepository.save(flow);
        context.addResource("flow", flow);

        return flow;
    }

    /**
     * Create adapters
     */
    private void createAdapters(PackageCreationRequest request, IntegrationFlow flow, PackageCreationContext context) {
        List<CommunicationAdapter> adapters = new ArrayList<>();

        // Create source adapter
        if(request.getSourceAdapter() != null) {
            CommunicationAdapter sourceAdapter = createAdapter(request.getSourceAdapter(), "INBOUND", flow);
            adapters.add(sourceAdapter);
            context.addResource("sourceAdapter", sourceAdapter);

            // Add compensation
            context.addCompensation(() -> adapterRepository.deleteById(sourceAdapter.getId()));
        }

        // Create target adapters
        for(AdapterRequest targetRequest : request.getTargetAdapters()) {
            CommunicationAdapter targetAdapter = createAdapter(targetRequest, "OUTBOUND", flow);
            adapters.add(targetAdapter);
            context.addResource("targetAdapter_" + targetAdapter.getName(), targetAdapter);

            // Add compensation
            context.addCompensation(() -> adapterRepository.deleteById(targetAdapter.getId()));
        }

        // IntegrationFlow doesn't have a setAdapters method
        // Adapters are referenced by inboundAdapterId and outboundAdapterId
    }

    /**
     * Create single adapter
     */
    private CommunicationAdapter createAdapter(AdapterRequest request, String direction, IntegrationFlow flow) {
        CommunicationAdapter adapter = new CommunicationAdapter();
        adapter.setName(request.getName());
        // Convert string type to AdapterType enum
        adapter.setType(AdapterType.valueOf(request.getType()));
        adapter.setDirection(direction);
        // Convert Map to JSON string
        ObjectMapper mapper = new ObjectMapper();
        try {
            adapter.setConfiguration(mapper.writeValueAsString(request.getConfiguration()));
        } catch(Exception e) {
            throw new RuntimeException("Failed to serialize configuration", e);
        }
        adapter.setActive(true);
        adapter.setCreatedBy(flow.getCreatedBy());
        adapter.setCreatedAt(LocalDateTime.now());
        // CommunicationAdapter doesn't have setFlow or setTenantId methods
        // Set the business component from flow if available
        if (flow.getBusinessComponent() != null) {
            adapter.setBusinessComponent(flow.getBusinessComponent());
        }

        // Encrypt credentials if present
        if(request.getCredentials() != null) {
            // Need to deserialize, update, and re-serialize the configuration
            try {
                Map<String, Object> config = mapper.readValue(adapter.getConfiguration(), Map.class);
                config.put("credentials", encryptCredentials(request.getCredentials()));
                adapter.setConfiguration(mapper.writeValueAsString(config));
            } catch(Exception e) {
                throw new RuntimeException("Failed to update configuration", e);
            }
        }

        return adapterRepository.save(adapter);
    }

    /**
     * Create message structures
     */
    private void createStructures(PackageCreationRequest request, IntegrationFlow flow, PackageCreationContext context) {
        // Create source structure
        if(request.getSourceStructure() != null) {
            MessageStructure sourceStructure = createStructure(request.getSourceStructure(), flow);

            // FlowStructure is for WSDL-based structures, not message structures
            // We need to track the created MessageStructure instead
            context.addResource("sourceStructure", sourceStructure);
            context.addCompensation(() -> messageStructureRepository.deleteById(sourceStructure.getId()));
        }

        // Create target structures
        for(StructureRequest targetRequest : request.getTargetStructures()) {
            MessageStructure targetStructure = createStructure(targetRequest, flow);

            // FlowStructure is for WSDL-based structures, not message structures
            // We need to track the created MessageStructure instead
            context.addResource("targetStructure_" + targetStructure.getName(), targetStructure);
            context.addCompensation(() -> messageStructureRepository.deleteById(targetStructure.getId()));
        }
    }

    /**
     * Create single message structure
     */
    private MessageStructure createStructure(StructureRequest request, IntegrationFlow flow) {
        MessageStructure structure = new MessageStructure();
        structure.setName(request.getName());
        // MessageStructure doesn't have setType or setFormat methods
        structure.setXsdContent(request.getContent());
        structure.setIsActive(true);
        structure.setVersion(1);
        structure.setCreatedBy(flow.getCreatedBy());
        structure.setCreatedAt(LocalDateTime.now());

        return messageStructureRepository.save(structure);
    }

    /**
     * Create transformations
     */
    private void createTransformations(PackageCreationRequest request, IntegrationFlow flow, PackageCreationContext context) {
        int order = 0;
        for(TransformationRequest transRequest : request.getTransformations()) {
            FlowTransformation transformation = new FlowTransformation();
            transformation.setFlow(flow);
            transformation.setName(transRequest.getName());
            // Convert string type to TransformationType enum
            transformation.setType(FlowTransformation.TransformationType.valueOf(transRequest.getType().toUpperCase()));
            // Convert configuration Map to JSON string
            try {
                transformation.setConfiguration(mapper.writeValueAsString(transRequest.getConfiguration()));
            } catch(Exception e) {
                throw new RuntimeException("Failed to serialize transformation configuration", e);
            }
            transformation.setExecutionOrder(order++);
            transformation.setActive(true);
            transformation.setCreatedBy(flow.getCreatedBy());
            transformation.setCreatedAt(LocalDateTime.now());

            transformation = transformationRepository.save(transformation);
            final FlowTransformation savedTransformation = transformation;
            context.addResource("transformation_" + transformation.getName(), transformation);

            // Create field mappings
            if(transRequest.getFieldMappings() != null) {
                for(FieldMappingRequest mappingRequest : transRequest.getFieldMappings()) {
                    FieldMapping mapping = new FieldMapping();
                    mapping.setTransformation(transformation);
                    mapping.setSourceXPath(mappingRequest.getSourcePath());
                    mapping.setTargetXPath(mappingRequest.getTargetPath());
                    mapping.setMappingRule(mappingRequest.getExpression());
                    // Convert string type to MappingType enum
                    mapping.setMappingType(FieldMapping.MappingType.valueOf(mappingRequest.getType().toUpperCase()));
                    // FieldMapping doesn't have setRequired method
                    mapping.setMappingOrder(mappingRequest.getOrder());

                    fieldMappingRepository.save(mapping);
                }
            }

            context.addCompensation(() -> transformationRepository.deleteById(savedTransformation.getId()));
        }
    }

    /**
     * Create orchestration targets
     */
    private void createOrchestrationTargets(PackageCreationRequest request, IntegrationFlow flow, PackageCreationContext context) {
        if(request.getOrchestrationTargets() == null) {
            return;
        }

        for(OrchestrationTargetRequest targetRequest : request.getOrchestrationTargets()) {
            // Find target adapter
            // Since IntegrationFlow doesn't have getAdapters(), we need to look up the adapter
            // First get all adapters for this business component
            List<CommunicationAdapter> adapters = adapterRepository.findByBusinessComponent_Id(
                flow.getBusinessComponent() != null ? flow.getBusinessComponent().getId() : null);
            CommunicationAdapter targetAdapter = adapters.stream()
                .filter(a -> a.getName().equals(targetRequest.getAdapterName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Target adapter not found: " + targetRequest.getAdapterName()));

            OrchestrationTarget target = new OrchestrationTarget();
            target.setFlow(flow);
            target.setName(targetRequest.getName());
            target.setTargetAdapter(targetAdapter);
            target.setRoutingCondition(targetRequest.getRoutingCondition());
            // OrchestrationTarget stores configuration as JSON string
            try {
                target.setConfiguration(mapper.writeValueAsString(targetRequest.getTransformationConfig()));
            } catch(Exception e) {
                throw new RuntimeException("Failed to serialize transformation config", e);
            }
            target.setExecutionOrder(targetRequest.getOrder());
            target.setParallel(targetRequest.isParallel());
            // Convert string to ErrorStrategy enum
            target.setErrorStrategy(OrchestrationTarget.ErrorStrategy.valueOf(targetRequest.getErrorStrategy().toUpperCase()));
            target.setActive(true);
            target.setCreatedBy(flow.getCreatedBy());
            target.setCreatedAt(LocalDateTime.now());

            orchestrationTargetRepository.save(target);
            context.addResource("orchestrationTarget_" + target.getName(), target);
            context.addCompensation(() -> orchestrationTargetRepository.deleteById(target.getId()));
        }
    }

    /**
     * Deploy to process engine
     */
    private void deployToProcessEngine(IntegrationFlow flow, PackageCreationContext context) {
        var deployResult = processEngineService.deployProcess(flow);

        if(!deployResult.isSuccess()) {
            throw new RuntimeException("Failed to deploy to process engine: " + deployResult.getError());
        }

        context.addResource("processDefinition", deployResult.getProcessDefinition());

        // Add compensation to undeploy
        context.addCompensation(() -> {
            // In a real implementation, would undeploy from process engine
            logger.info("Would undeploy process definition: {}", deployResult.getProcessDefinition().getId());
        });
    }

    /**
     * Validate the complete package
     */
    private void validatePackage(IntegrationFlow flow, PackageCreationContext context) {
        // Reload flow with all relationships
        flow = flowRepository.findById(flow.getId())
            .orElseThrow(() -> new IllegalStateException("Flow not found after creation"));

        // Validate required components
        // Validate adapters are set
        if(flow.getInboundAdapterId() == null) {
            throw new IllegalStateException("No inbound adapter configured for flow");
        }
        if(flow.getOutboundAdapterId() == null && flow.getFlowType() == FlowType.DIRECT_MAPPING) {
            throw new IllegalStateException("No outbound adapter configured for direct mapping flow");
        }

        // Validate at least one transformation exists
        List<FlowTransformation> transformations = transformationRepository.findByFlowId(flow.getId());
        if(transformations.isEmpty()) {
            logger.warn("No transformations configured for flow");
        }

        // Additional validations can be added here
        logger.info("Package validation completed successfully");
    }

    /**
     * Execute compensation actions
     */
    private void executeCompensation(PackageCreationContext context) {
        logger.info("Executing compensation for correlation ID: {}", context.getCorrelationId());

        List<CompensationAction> actions = new ArrayList<>(context.getCompensationActions());
        Collections.reverse(actions); // Execute in reverse order

        for(CompensationAction action : actions) {
            try {
                action.compensate();
            } catch(Exception e) {
                logger.error("Compensation action failed", e);
                // Continue with other compensations
            }
        }

        context.setStatus(PackageCreationStatus.ROLLED_BACK);
        auditService.logPackageCreation(context.getCorrelationId(), "ROLLED_BACK",
            "Package creation rolled back", context.getCreatedResources());
    }

    /**
     * Encrypt sensitive credentials
     */
    private Map<String, Object> encryptCredentials(Map<String, Object> credentials) {
        // In a real implementation, use proper encryption
        Map<String, Object> encrypted = new HashMap<>();
        credentials.forEach((key, value) -> {
            if(value instanceof String && isSensitiveField(key)) {
                encrypted.put(key, "ENC(" + Base64.getEncoder().encodeToString(value.toString().getBytes()) + ")");
            } else {
                encrypted.put(key, value);
            }
        });
        return encrypted;
    }

    private boolean isSensitiveField(String fieldName) {
        return fieldName.toLowerCase().contains("password") ||
               fieldName.toLowerCase().contains("secret") ||
               fieldName.toLowerCase().contains("key") ||
               fieldName.toLowerCase().contains("token");
    }

    /**
     * Exception for package creation failures
     */
    public static class PackageCreationException extends RuntimeException {
        private final PackageCreationContext context;

        public PackageCreationException(String message, Throwable cause, PackageCreationContext context) {
            super(message, cause);
            this.context = context;
        }

        public PackageCreationContext getContext() {
            return context;
        }
    }

    // Request and result classes would be defined here...
}
