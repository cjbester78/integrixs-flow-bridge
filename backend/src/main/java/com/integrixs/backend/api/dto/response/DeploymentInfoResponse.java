package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for deployment information
 */
public class DeploymentInfoResponse {

    private String flowId;
    private String endpoint;
    private LocalDateTime deployedAt;
    private String deployedBy;
    private String status;
    private Map<String, Object> metadata;

    // Default constructor
    public DeploymentInfoResponse() {
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public LocalDateTime getDeployedAt() {
        return deployedAt;
    }

    public void setDeployedAt(LocalDateTime deployedAt) {
        this.deployedAt = deployedAt;
    }

    public String getDeployedBy() {
        return deployedBy;
    }

    public void setDeployedBy(String deployedBy) {
        this.deployedBy = deployedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static DeploymentInfoResponseBuilder builder() {
        return new DeploymentInfoResponseBuilder();
    }

    public static class DeploymentInfoResponseBuilder {
        private DeploymentInfoResponse response = new DeploymentInfoResponse();

        public DeploymentInfoResponseBuilder flowId(String flowId) {
            response.flowId = flowId;
            return this;
        }

        public DeploymentInfoResponseBuilder endpoint(String endpoint) {
            response.endpoint = endpoint;
            return this;
        }

        public DeploymentInfoResponseBuilder deployedAt(LocalDateTime deployedAt) {
            response.deployedAt = deployedAt;
            return this;
        }

        public DeploymentInfoResponseBuilder deployedBy(String deployedBy) {
            response.deployedBy = deployedBy;
            return this;
        }

        public DeploymentInfoResponseBuilder status(String status) {
            response.status = status;
            return this;
        }

        public DeploymentInfoResponseBuilder metadata(Map<String, Object> metadata) {
            response.metadata = metadata;
            return this;
        }

        public DeploymentInfoResponse build() {
            return response;
        }
    }
}
