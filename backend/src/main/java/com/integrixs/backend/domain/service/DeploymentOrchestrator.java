package com.integrixs.backend.domain.service;

import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.FlowStatus;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.shared.enums.AdapterType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain service for orchestrating flow deployment operations
 */
@Service
public class DeploymentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DeploymentOrchestrator.class);


    @Value("${server.host:localhost}")
    private String serverHost;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.protocol:http}")
    private String serverProtocol;

    /**
     * Prepare a flow for deployment
     */
    public void prepareFlowForDeployment(IntegrationFlow flow, String endpoint, Map<String, Object> metadata, UUID deployedBy) {
        flow.setStatus(FlowStatus.DEPLOYED_ACTIVE);
        flow.setDeployedAt(LocalDateTime.now());
        flow.setDeployedBy(deployedBy);
        flow.setDeploymentEndpoint(endpoint);
        flow.setActive(true);

        log.info("Prepared flow {} for deployment with endpoint: {}", flow.getId(), endpoint);
    }

    /**
     * Revert flow deployment on failure
     */
    public void revertDeployment(IntegrationFlow flow) {
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        flow.setActive(false);
        flow.setDeploymentEndpoint(null);
        flow.setDeploymentMetadata(null);
        flow.setDeployedAt(null);
        flow.setDeployedBy(null);

        log.info("Reverted deployment for flow {}", flow.getId());
    }

    /**
     * Generate endpoint URL for flow
     */
    public String generateEndpoint(IntegrationFlow flow, CommunicationAdapter inboundAdapter, String configuredEndpoint) {
        String baseUrl = String.format("%s://%s:%s", serverProtocol, serverHost, serverPort);

        // Use configured endpoint if provided
        if(configuredEndpoint != null && !configuredEndpoint.isEmpty()) {
            log.info("Using adapter configured endpoint: {}", configuredEndpoint);

            // Ensure it starts with /
            if(!configuredEndpoint.startsWith("/")) {
                configuredEndpoint = "/" + configuredEndpoint;
            }

            // Handle SOAP endpoints
            if(inboundAdapter.getType() == AdapterType.SOAP) {
                if(!configuredEndpoint.startsWith("/soap/")) {
                    return baseUrl + "/soap" + configuredEndpoint;
                }
            }

            return baseUrl + configuredEndpoint;
        }

        // Generate default endpoint based on adapter type
        String flowPath = flow.getName().toLowerCase().replaceAll("[^a-zA-Z0-9-]", "-");

        return switch(inboundAdapter.getType()) {
            case HTTP, REST -> String.format("%s/api/integration/%s", baseUrl, flowPath);
            case SOAP -> String.format("%s/soap/%s", baseUrl, flowPath);
            case FILE, FTP, SFTP -> String.format("file:///opt/integrixflowbridge/flows/%s", flowPath);
            default -> String.format("%s/integration/%s", baseUrl, flowPath);
        };
    }

    /**
     * Create deployment metadata
     */
    public Map<String, Object> createDeploymentMetadata(IntegrationFlow flow, CommunicationAdapter inboundAdapter, String endpoint) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("flowName", flow.getName());
        metadata.put("flowId", flow.getId().toString());
        metadata.put("adapterType", inboundAdapter.getType().toString());
        metadata.put("adapterMode", inboundAdapter.getMode().toString());
        metadata.put("endpoint", endpoint);

        // Add adapter-specific metadata
        switch(inboundAdapter.getType()) {
            case SOAP -> {
                metadata.put("wsdlUrl", endpoint + "?wsdl");
                metadata.put("soapVersion", "1.1/1.2");
            }
            case REST -> {
                metadata.put("apiDocsUrl", endpoint + "/docs");
                metadata.put("openApiUrl", endpoint + "/openapi.json");
            }
            case HTTP -> {
                metadata.put("httpMethods", "POST, GET");
                metadata.put("contentType", "application/json, application/xml");
            }
            case FILE, FTP, SFTP -> {
                metadata.put("pollingEnabled", true);
                metadata.put("filePattern", "*.*");
            }
        }

        return metadata;
    }

    /**
     * Prepare flow for undeployment
     */
    public void prepareFlowForUndeployment(IntegrationFlow flow) {
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        flow.setActive(false);
        flow.setDeploymentEndpoint(null);
        flow.setDeploymentMetadata(null);

        log.info("Prepared flow {} for undeployment", flow.getId());
    }
}
