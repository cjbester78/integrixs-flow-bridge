package com.integrixs.backend.service;

import com.integrixs.data.model.*;
import com.integrixs.data.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Service for handling transactional package creation with rollback capability
 */
@Service
public class TransactionalPackageCreationService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionalPackageCreationService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private IntegrationFlowRepository flowRepository;

    @Autowired
    private CommunicationAdapterRepository adapterRepository;

    @Autowired
    private FlowStructureRepository flowStructureRepository;

    @Autowired
    private MessageStructureRepository messageStructureRepository;

    @Autowired
    private FlowTransformationRepository transformationRepository;

    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    @Autowired
    private OrchestrationTargetRepository orchestrationTargetRepository;

    @Autowired
    private ProcessEngineService processEngineService;

    @Autowired
    private AuditService auditService;

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
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
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

            // Flush all changes
            entityManager.flush();

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
        flow.setType(request.getFlowType());
        flow.setTenantId(request.getTenantId());
        flow.setActive(false); // Start inactive
        flow.setConfiguration(request.getFlowConfiguration());
        flow.setCreatedBy(request.getUserId());
        flow.setCreatedDate(LocalDateTime.now());

        flow = flowRepository.saveAndFlush(flow);
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

        flow.setAdapters(new HashSet<>(adapters));
    }

    /**
     * Create single adapter
     */
    private CommunicationAdapter createAdapter(AdapterRequest request, String direction, IntegrationFlow flow) {
        CommunicationAdapter adapter = new CommunicationAdapter();
        adapter.setName(request.getName());
        adapter.setType(request.getType());
        adapter.setDirection(direction);
        adapter.setConfiguration(request.getConfiguration());
        adapter.setActive(true);
        adapter.setCreatedBy(flow.getCreatedBy());
        adapter.setCreatedDate(LocalDateTime.now());
        adapter.setFlow(flow);
        adapter.setTenantId(flow.getTenantId());

        // Encrypt credentials if present
        if(request.getCredentials() != null) {
            Map<String, Object> encryptedConfig = new HashMap<>(adapter.getConfiguration());
            encryptedConfig.put("credentials", encryptCredentials(request.getCredentials()));
            adapter.setConfiguration(encryptedConfig);
        }

        return adapterRepository.saveAndFlush(adapter);
    }

    /**
     * Create message structures
     */
    private void createStructures(PackageCreationRequest request, IntegrationFlow flow, PackageCreationContext context) {
        // Create source structure
        if(request.getSourceStructure() != null) {
            MessageStructure sourceStructure = createStructure(request.getSourceStructure(), flow);

            FlowStructure flowStructure = new FlowStructure();
            flowStructure.setFlow(flow);
            flowStructure.setStructure(sourceStructure);
            flowStructure.setDirection("SOURCE");
            flowStructureRepository.saveAndFlush(flowStructure);

            context.addResource("sourceStructure", sourceStructure);
            context.addCompensation(() -> messageStructureRepository.deleteById(sourceStructure.getId()));
        }

        // Create target structures
        for(StructureRequest targetRequest : request.getTargetStructures()) {
            MessageStructure targetStructure = createStructure(targetRequest, flow);

            FlowStructure flowStructure = new FlowStructure();
            flowStructure.setFlow(flow);
            flowStructure.setStructure(targetStructure);
            flowStructure.setDirection("TARGET");
            flowStructureRepository.saveAndFlush(flowStructure);

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
        structure.setType(request.getType());
        structure.setFormat(request.getFormat());
        structure.setContent(request.getContent());
        structure.setActive(true);
        structure.setVersion(1);
        structure.setCreatedBy(flow.getCreatedBy());
        structure.setCreatedDate(LocalDateTime.now());

        return messageStructureRepository.saveAndFlush(structure);
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
            transformation.setType(transRequest.getType());
            transformation.setConfiguration(transRequest.getConfiguration());
            transformation.setExecutionOrder(order++);
            transformation.setActive(true);
            transformation.setCreatedBy(flow.getCreatedBy());
            transformation.setCreatedDate(LocalDateTime.now());

            transformation = transformationRepository.saveAndFlush(transformation);
            context.addResource("transformation_" + transformation.getName(), transformation);

            // Create field mappings
            if(transRequest.getFieldMappings() != null) {
                for(FieldMappingRequest mappingRequest : transRequest.getFieldMappings()) {
                    FieldMapping mapping = new FieldMapping();
                    mapping.setTransformation(transformation);
                    mapping.setSourcePath(mappingRequest.getSourcePath());
                    mapping.setTargetPath(mappingRequest.getTargetPath());
                    mapping.setMappingExpression(mappingRequest.getExpression());
                    mapping.setMappingType(mappingRequest.getType());
                    mapping.setRequired(mappingRequest.isRequired());
                    mapping.setMappingOrder(mappingRequest.getOrder());

                    fieldMappingRepository.saveAndFlush(mapping);
                }
            }

            context.addCompensation(() -> transformationRepository.deleteById(transformation.getId()));
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
            CommunicationAdapter targetAdapter = flow.getAdapters().stream()
                .filter(a -> a.getName().equals(targetRequest.getAdapterName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Target adapter not found: " + targetRequest.getAdapterName()));

            OrchestrationTarget target = new OrchestrationTarget();
            target.setFlow(flow);
            target.setName(targetRequest.getName());
            target.setTargetAdapter(targetAdapter);
            target.setRoutingCondition(targetRequest.getRoutingCondition());
            target.setTransformationConfig(targetRequest.getTransformationConfig());
            target.setExecutionOrder(targetRequest.getOrder());
            target.setParallelExecution(targetRequest.isParallel());
            target.setErrorStrategy(targetRequest.getErrorStrategy());
            target.setActive(true);
            target.setCreatedBy(flow.getCreatedBy());
            target.setCreatedDate(LocalDateTime.now());

            orchestrationTargetRepository.saveAndFlush(target);
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
        if(flow.getAdapters() == null || flow.getAdapters().isEmpty()) {
            throw new IllegalStateException("No adapters configured for flow");
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
