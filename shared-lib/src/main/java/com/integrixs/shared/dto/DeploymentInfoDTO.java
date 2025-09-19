package com.integrixs.shared.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * DTO for deployment information of an integration flow
 */
public class DeploymentInfoDTO {

    private String flowId;
    private String endpoint;
    private LocalDateTime deployedAt;
    private String deployedBy;
    private Map<String, Object> metadata;

    // Default constructor
    public DeploymentInfoDTO() {
        this.metadata = new HashMap<>();
    }

    // All args constructor
    public DeploymentInfoDTO(String flowId, String endpoint, LocalDateTime deployedAt, String deployedBy, Map<String, Object> metadata) {
        this.flowId = flowId;
        this.endpoint = endpoint;
        this.deployedAt = deployedAt;
        this.deployedBy = deployedBy;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // Getters
    public String getFlowId() { return flowId; }
    public String getEndpoint() { return endpoint; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public String getDeployedBy() { return deployedBy; }
    public Map<String, Object> getMetadata() { return metadata; }

    // Setters
    public void setFlowId(String flowId) { this.flowId = flowId; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }
    public void setDeployedBy(String deployedBy) { this.deployedBy = deployedBy; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // Builder
    public static DeploymentInfoDTOBuilder builder() {
        return new DeploymentInfoDTOBuilder();
    }

    public static class DeploymentInfoDTOBuilder {
        private String flowId;
        private String endpoint;
        private LocalDateTime deployedAt;
        private String deployedBy;
        private Map<String, Object> metadata = new HashMap<>();

        public DeploymentInfoDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public DeploymentInfoDTOBuilder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public DeploymentInfoDTOBuilder deployedAt(LocalDateTime deployedAt) {
            this.deployedAt = deployedAt;
            return this;
        }

        public DeploymentInfoDTOBuilder deployedBy(String deployedBy) {
            this.deployedBy = deployedBy;
            return this;
        }

        public DeploymentInfoDTOBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public DeploymentInfoDTO build() {
            return new DeploymentInfoDTO(flowId, endpoint, deployedAt, deployedBy, metadata);
        }
    }
}
