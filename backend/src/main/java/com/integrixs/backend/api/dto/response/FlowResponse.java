package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;

/**
 * Response object for integration flow
 */
public class FlowResponse {

    private String id;
    private String name;
    private String description;

    private String inboundAdapterId;
    private String inboundAdapterName;
    private String inboundAdapterType;

    private String outboundAdapterId;
    private String outboundAdapterName;
    private String outboundAdapterType;

    private String sourceFlowStructureId;
    private String sourceFlowStructureName;

    private String targetFlowStructureId;
    private String targetFlowStructureName;

    private String status;
    private boolean active;

    private Integer executionCount;
    private Integer successCount;
    private Integer errorCount;

    private LocalDateTime lastExecutionAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String createdBy;

    // Default constructor
    public FlowResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInboundAdapterId() {
        return inboundAdapterId;
    }

    public void setInboundAdapterId(String inboundAdapterId) {
        this.inboundAdapterId = inboundAdapterId;
    }

    public String getInboundAdapterName() {
        return inboundAdapterName;
    }

    public void setInboundAdapterName(String inboundAdapterName) {
        this.inboundAdapterName = inboundAdapterName;
    }

    public String getInboundAdapterType() {
        return inboundAdapterType;
    }

    public void setInboundAdapterType(String inboundAdapterType) {
        this.inboundAdapterType = inboundAdapterType;
    }

    public String getOutboundAdapterId() {
        return outboundAdapterId;
    }

    public void setOutboundAdapterId(String outboundAdapterId) {
        this.outboundAdapterId = outboundAdapterId;
    }

    public String getOutboundAdapterName() {
        return outboundAdapterName;
    }

    public void setOutboundAdapterName(String outboundAdapterName) {
        this.outboundAdapterName = outboundAdapterName;
    }

    public String getOutboundAdapterType() {
        return outboundAdapterType;
    }

    public void setOutboundAdapterType(String outboundAdapterType) {
        this.outboundAdapterType = outboundAdapterType;
    }

    public String getSourceFlowStructureId() {
        return sourceFlowStructureId;
    }

    public void setSourceFlowStructureId(String sourceFlowStructureId) {
        this.sourceFlowStructureId = sourceFlowStructureId;
    }

    public String getSourceFlowStructureName() {
        return sourceFlowStructureName;
    }

    public void setSourceFlowStructureName(String sourceFlowStructureName) {
        this.sourceFlowStructureName = sourceFlowStructureName;
    }

    public String getTargetFlowStructureId() {
        return targetFlowStructureId;
    }

    public void setTargetFlowStructureId(String targetFlowStructureId) {
        this.targetFlowStructureId = targetFlowStructureId;
    }

    public String getTargetFlowStructureName() {
        return targetFlowStructureName;
    }

    public void setTargetFlowStructureName(String targetFlowStructureName) {
        this.targetFlowStructureName = targetFlowStructureName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Integer executionCount) {
        this.executionCount = executionCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public LocalDateTime getLastExecutionAt() {
        return lastExecutionAt;
    }

    public void setLastExecutionAt(LocalDateTime lastExecutionAt) {
        this.lastExecutionAt = lastExecutionAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public static FlowResponseBuilder builder() {
        return new FlowResponseBuilder();
    }

    public static class FlowResponseBuilder {
        private FlowResponse response = new FlowResponse();

        public FlowResponseBuilder id(String id) {
            response.setId(id);
            return this;
        }

        public FlowResponseBuilder name(String name) {
            response.setName(name);
            return this;
        }

        public FlowResponseBuilder description(String description) {
            response.setDescription(description);
            return this;
        }

        public FlowResponseBuilder inboundAdapterId(String inboundAdapterId) {
            response.setInboundAdapterId(inboundAdapterId);
            return this;
        }

        public FlowResponseBuilder inboundAdapterName(String inboundAdapterName) {
            response.setInboundAdapterName(inboundAdapterName);
            return this;
        }

        public FlowResponseBuilder inboundAdapterType(String inboundAdapterType) {
            response.setInboundAdapterType(inboundAdapterType);
            return this;
        }

        public FlowResponseBuilder outboundAdapterId(String outboundAdapterId) {
            response.setOutboundAdapterId(outboundAdapterId);
            return this;
        }

        public FlowResponseBuilder outboundAdapterName(String outboundAdapterName) {
            response.setOutboundAdapterName(outboundAdapterName);
            return this;
        }

        public FlowResponseBuilder outboundAdapterType(String outboundAdapterType) {
            response.setOutboundAdapterType(outboundAdapterType);
            return this;
        }

        public FlowResponseBuilder sourceFlowStructureId(String sourceFlowStructureId) {
            response.setSourceFlowStructureId(sourceFlowStructureId);
            return this;
        }

        public FlowResponseBuilder sourceFlowStructureName(String sourceFlowStructureName) {
            response.setSourceFlowStructureName(sourceFlowStructureName);
            return this;
        }

        public FlowResponseBuilder targetFlowStructureId(String targetFlowStructureId) {
            response.setTargetFlowStructureId(targetFlowStructureId);
            return this;
        }

        public FlowResponseBuilder targetFlowStructureName(String targetFlowStructureName) {
            response.setTargetFlowStructureName(targetFlowStructureName);
            return this;
        }

        public FlowResponseBuilder status(String status) {
            response.setStatus(status);
            return this;
        }

        public FlowResponseBuilder active(boolean active) {
            response.setActive(active);
            return this;
        }

        public FlowResponseBuilder executionCount(Integer executionCount) {
            response.setExecutionCount(executionCount);
            return this;
        }

        public FlowResponseBuilder successCount(Integer successCount) {
            response.setSuccessCount(successCount);
            return this;
        }

        public FlowResponseBuilder errorCount(Integer errorCount) {
            response.setErrorCount(errorCount);
            return this;
        }

        public FlowResponseBuilder lastExecutionAt(LocalDateTime lastExecutionAt) {
            response.setLastExecutionAt(lastExecutionAt);
            return this;
        }

        public FlowResponseBuilder createdAt(LocalDateTime createdAt) {
            response.setCreatedAt(createdAt);
            return this;
        }

        public FlowResponseBuilder updatedAt(LocalDateTime updatedAt) {
            response.setUpdatedAt(updatedAt);
            return this;
        }

        public FlowResponseBuilder createdBy(String createdBy) {
            response.setCreatedBy(createdBy);
            return this;
        }

        public FlowResponse build() {
            return response;
        }
    }
}
