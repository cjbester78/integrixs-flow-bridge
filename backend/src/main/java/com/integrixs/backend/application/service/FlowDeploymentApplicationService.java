package com.integrixs.backend.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.api.dto.response.DeploymentInfoResponse;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.backend.domain.service.DeploymentOrchestrator;
import com.integrixs.backend.domain.service.DeploymentValidator;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.User;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for flow deployment management
 */
@Service
public class FlowDeploymentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(FlowDeploymentApplicationService.class);


    private final IntegrationFlowSqlRepository flowRepository;
    private final CommunicationAdapterSqlRepository adapterRepository;
    private final DeploymentOrchestrator deploymentOrchestrator;
    private final DeploymentValidator deploymentValidator;
    private final AuditTrailService auditTrailService;
    private final ObjectMapper objectMapper;

    public FlowDeploymentApplicationService(IntegrationFlowSqlRepository flowRepository,
                                          CommunicationAdapterSqlRepository adapterRepository,
                                          DeploymentOrchestrator deploymentOrchestrator,
                                          DeploymentValidator deploymentValidator,
                                          AuditTrailService auditTrailService,
                                          ObjectMapper objectMapper) {
        this.flowRepository = flowRepository;
        this.adapterRepository = adapterRepository;
        this.deploymentOrchestrator = deploymentOrchestrator;
        this.deploymentValidator = deploymentValidator;
        this.auditTrailService = auditTrailService;
        this.objectMapper = objectMapper;
    }

    /**
     * Deploy an integration flow
     */
    public DeploymentInfoResponse deployFlow(String flowId, User deployedBy) {
        log.info("Deploying flow: {} by user: {}", flowId, deployedBy.getUsername());

        try {
            // Get flow
            IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
                .orElseThrow(() -> new ResourceNotFoundException("Flow not found: " + flowId));

            // Get adapters
            CommunicationAdapter inboundAdapter = adapterRepository.findById(flow.getInboundAdapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Source adapter not found"));
            CommunicationAdapter outboundAdapter = adapterRepository.findById(flow.getOutboundAdapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Target adapter not found"));

            // Validate deployment
            deploymentValidator.validateFlowForDeployment(flow, inboundAdapter, outboundAdapter);

            // Extract configured endpoint from adapter config if present
            String configuredEndpoint = extractConfiguredEndpoint(inboundAdapter);

            // Generate endpoint
            String endpoint = deploymentOrchestrator.generateEndpoint(flow, inboundAdapter, configuredEndpoint);

            // Create metadata
            Map<String, Object> metadata = deploymentOrchestrator.createDeploymentMetadata(flow, inboundAdapter, endpoint);

            // Register endpoints and initialize adapters
            registerFlowEndpoint(flow, inboundAdapter);
            initializeAdapters(flow, inboundAdapter);

            // Update flow for deployment
            deploymentOrchestrator.prepareFlowForDeployment(flow, endpoint, metadata, deployedBy.getId());

            // Serialize metadata
            flow.setDeploymentMetadata(objectMapper.writeValueAsString(metadata));

            // Save flow
            flow = flowRepository.save(flow);
            // SQL repository saves are immediate, no flush needed

            // Audit
            auditTrailService.logUserAction(
                deployedBy,
                "IntegrationFlow",
                flowId,
                "DEPLOY"
           );

            log.info("Flow {} deployed successfully with endpoint: {}", flowId, endpoint);

            return DeploymentInfoResponse.builder()
                .flowId(flowId)
                .endpoint(endpoint)
                .deployedAt(flow.getDeployedAt())
                .deployedBy(deployedBy.getUsername())
                .status(flow.getStatus().toString())
                .metadata(metadata)
                .build();

        } catch(Exception e) {
            log.error("Failed to deploy flow: {}", flowId, e);

            // Revert on failure
            try {
                IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId)).orElse(null);
                if(flow != null) {
                    deploymentOrchestrator.revertDeployment(flow);
                    flowRepository.save(flow);
                }
            } catch(Exception revertError) {
                log.error("Failed to revert deployment for flow: {}", flowId, revertError);
            }

            throw new RuntimeException("Failed to deploy flow: " + e.getMessage(), e);
        }
    }

    /**
     * Undeploy an integration flow
     */
    public void undeployFlow(String flowId, User undeployedBy) {
        log.info("Undeploying flow: {} by user: {}", flowId, undeployedBy.getUsername());

        // Get flow
        IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
            .orElseThrow(() -> new ResourceNotFoundException("Flow not found: " + flowId));

        // Validate undeployment
        deploymentValidator.validateFlowForUndeployment(flow);

        // If already undeployed, return success
        if(!deploymentValidator.isFlowDeployed(flow)) {
            log.info("Flow {} is already undeployed", flowId);
            return;
        }

        // Undeploy flow
        deploymentOrchestrator.prepareFlowForUndeployment(flow);
        flowRepository.save(flow);

        // Audit
        auditTrailService.logUserAction(
            undeployedBy,
            "IntegrationFlow",
            flowId,
            "UNDEPLOY"
       );

        log.info("Flow {} undeployed successfully", flowId);
    }

    /**
     * Get deployment information for a flow
     */
    public DeploymentInfoResponse getDeploymentInfo(String flowId) {
        log.debug("Getting deployment info for flow: {}", flowId);

        IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
            .orElseThrow(() -> new ResourceNotFoundException("Flow not found: " + flowId));

        if(!deploymentValidator.isFlowDeployed(flow)) {
            return null;
        }

        Map<String, Object> metadata = null;
        if(flow.getDeploymentMetadata() != null) {
            try {
                metadata = objectMapper.readValue(flow.getDeploymentMetadata(), Map.class);
            } catch(Exception e) {
                log.error("Failed to parse deployment metadata for flow: {}", flowId, e);
            }
        }

        return DeploymentInfoResponse.builder()
            .flowId(flowId)
            .endpoint(flow.getDeploymentEndpoint())
            .deployedAt(flow.getDeployedAt())
            .deployedBy(flow.getDeployedBy() != null ? flow.getDeployedBy().toString() : null)
            .status(flow.getStatus().toString())
            .metadata(metadata)
            .build();
    }

    /**
     * Extract configured endpoint from adapter configuration
     */
    private String extractConfiguredEndpoint(CommunicationAdapter adapter) {
        String configJson = adapter.getConfiguration();
        if(configJson == null || configJson.isEmpty()) {
            return null;
        }

        try {
            Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
            return(String) config.get("serviceEndpointUrl");
        } catch(Exception e) {
            log.warn("Error parsing adapter configuration: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Register flow endpoint
     */
    private void registerFlowEndpoint(IntegrationFlow flow, CommunicationAdapter inboundAdapter) {
        log.info("Registering endpoint for flow: {} with adapter type: {}",
            flow.getName(), inboundAdapter.getType());

        // File - based adapters may need directory setup
        switch(inboundAdapter.getType()) {
            case FILE, FTP, SFTP -> setupFileBasedEndpoint(flow, inboundAdapter);
            default -> log.info("HTTP endpoint registered for flow: {}", flow.getName());
        }
    }

    /**
     * Initialize adapters for the flow
     */
    private void initializeAdapters(IntegrationFlow flow, CommunicationAdapter inboundAdapter) {
        log.info("Initializing adapters for flow: {}", flow.getName());

        // Outbound adapters may need polling setup
        if(inboundAdapter.getMode() == com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterModeEnum.OUTBOUND) {
            log.info("Polling setup would be configured here for adapter: {}", inboundAdapter.getName());
        }
    }

    /**
     * Setup file - based endpoint
     */
    private void setupFileBasedEndpoint(IntegrationFlow flow, CommunicationAdapter adapter) {
        String configJson = adapter.getConfiguration();
        if(configJson == null || configJson.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
            String directory = (String) config.get("directory");

            if(directory != null) {
                try {
                    Files.createDirectories(Paths.get(directory));
                    log.info("Created directory for file adapter: {}", directory);
                } catch(Exception e) {
                    log.error("Failed to create directory: {}", directory, e);
                }
            }
        } catch(Exception e) {
            log.error("Failed to parse adapter configuration", e);
        }
    }
}
