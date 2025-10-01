package com.integrixs.shared.dto;

import java.time.LocalDateTime;

/**
 * DTO for IntegrationFlow entity
 */
public class IntegrationFlowDTO {
    private String id;
    private String name;
    private String description;
    private String inboundAdapterId;
    private String outboundAdapterId;
    private String sourceFlowStructureId;
    private String targetFlowStructureId;
    private String status;
    private boolean isActive;
    private String mappingMode; // WITH_MAPPING or PASS_THROUGH
    private boolean skipXmlConversion; // Skip XML conversion for direct passthrough
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private LocalDateTime lastExecutionAt;
    private Integer executionCount;
    private Integer successCount;
    private Integer errorCount;
    private String businessComponentId;

    // Adapter details
    private String inboundAdapterName;
    private String inboundAdapterType;
    private String outboundAdapterName;
    private String outboundAdapterType;

    public IntegrationFlowDTO() {
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getInboundAdapterId() { return inboundAdapterId; }
    public String getOutboundAdapterId() { return outboundAdapterId; }
    public String getSourceFlowStructureId() { return sourceFlowStructureId; }
    public String getTargetFlowStructureId() { return targetFlowStructureId; }
    public String getStatus() { return status; }
    public boolean isActive() { return isActive; }
    public String getMappingMode() { return mappingMode; }
    public boolean isSkipXmlConversion() { return skipXmlConversion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getLastExecutionAt() { return lastExecutionAt; }
    public Integer getExecutionCount() { return executionCount; }
    public Integer getSuccessCount() { return successCount; }
    public Integer getErrorCount() { return errorCount; }
    public String getBusinessComponentId() { return businessComponentId; }
    public String getInboundAdapterName() { return inboundAdapterName; }
    public String getInboundAdapterType() { return inboundAdapterType; }
    public String getOutboundAdapterName() { return outboundAdapterName; }
    public String getOutboundAdapterType() { return outboundAdapterType; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setInboundAdapterId(String inboundAdapterId) { this.inboundAdapterId = inboundAdapterId; }
    public void setOutboundAdapterId(String outboundAdapterId) { this.outboundAdapterId = outboundAdapterId; }
    public void setSourceFlowStructureId(String sourceFlowStructureId) { this.sourceFlowStructureId = sourceFlowStructureId; }
    public void setTargetFlowStructureId(String targetFlowStructureId) { this.targetFlowStructureId = targetFlowStructureId; }
    public void setStatus(String status) { this.status = status; }
    public void setActive(boolean active) { this.isActive = active; }
    public void setMappingMode(String mappingMode) { this.mappingMode = mappingMode; }
    public void setSkipXmlConversion(boolean skipXmlConversion) { this.skipXmlConversion = skipXmlConversion; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setLastExecutionAt(LocalDateTime lastExecutionAt) { this.lastExecutionAt = lastExecutionAt; }
    public void setExecutionCount(Integer executionCount) { this.executionCount = executionCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    public void setInboundAdapterName(String inboundAdapterName) { this.inboundAdapterName = inboundAdapterName; }
    public void setInboundAdapterType(String inboundAdapterType) { this.inboundAdapterType = inboundAdapterType; }
    public void setOutboundAdapterName(String outboundAdapterName) { this.outboundAdapterName = outboundAdapterName; }
    public void setOutboundAdapterType(String outboundAdapterType) { this.outboundAdapterType = outboundAdapterType; }

    public static IntegrationFlowDTOBuilder builder() {
        return new IntegrationFlowDTOBuilder();
    }

    public static class IntegrationFlowDTOBuilder {
        private String id;
        private String name;
        private String description;
        private String inboundAdapterId;
        private String outboundAdapterId;
        private String sourceFlowStructureId;
        private String targetFlowStructureId;
        private String status;
        private boolean isActive;
        private String mappingMode;
        private boolean skipXmlConversion;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private LocalDateTime lastExecutionAt;
        private Integer executionCount;
        private Integer successCount;
        private Integer errorCount;
        private String businessComponentId;
        private String inboundAdapterName;
        private String inboundAdapterType;
        private String outboundAdapterName;
        private String outboundAdapterType;

        public IntegrationFlowDTOBuilder id(String id) { this.id = id; return this; }
        public IntegrationFlowDTOBuilder name(String name) { this.name = name; return this; }
        public IntegrationFlowDTOBuilder description(String description) { this.description = description; return this; }
        public IntegrationFlowDTOBuilder inboundAdapterId(String inboundAdapterId) { this.inboundAdapterId = inboundAdapterId; return this; }
        public IntegrationFlowDTOBuilder outboundAdapterId(String outboundAdapterId) { this.outboundAdapterId = outboundAdapterId; return this; }
        public IntegrationFlowDTOBuilder sourceFlowStructureId(String sourceFlowStructureId) { this.sourceFlowStructureId = sourceFlowStructureId; return this; }
        public IntegrationFlowDTOBuilder targetFlowStructureId(String targetFlowStructureId) { this.targetFlowStructureId = targetFlowStructureId; return this; }
        public IntegrationFlowDTOBuilder status(String status) { this.status = status; return this; }
        public IntegrationFlowDTOBuilder isActive(boolean isActive) { this.isActive = isActive; return this; }
        public IntegrationFlowDTOBuilder mappingMode(String mappingMode) { this.mappingMode = mappingMode; return this; }
        public IntegrationFlowDTOBuilder skipXmlConversion(boolean skipXmlConversion) { this.skipXmlConversion = skipXmlConversion; return this; }
        public IntegrationFlowDTOBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public IntegrationFlowDTOBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public IntegrationFlowDTOBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public IntegrationFlowDTOBuilder lastExecutionAt(LocalDateTime lastExecutionAt) { this.lastExecutionAt = lastExecutionAt; return this; }
        public IntegrationFlowDTOBuilder executionCount(Integer executionCount) { this.executionCount = executionCount; return this; }
        public IntegrationFlowDTOBuilder successCount(Integer successCount) { this.successCount = successCount; return this; }
        public IntegrationFlowDTOBuilder errorCount(Integer errorCount) { this.errorCount = errorCount; return this; }
        public IntegrationFlowDTOBuilder businessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; return this; }
        public IntegrationFlowDTOBuilder inboundAdapterName(String inboundAdapterName) { this.inboundAdapterName = inboundAdapterName; return this; }
        public IntegrationFlowDTOBuilder inboundAdapterType(String inboundAdapterType) { this.inboundAdapterType = inboundAdapterType; return this; }
        public IntegrationFlowDTOBuilder outboundAdapterName(String outboundAdapterName) { this.outboundAdapterName = outboundAdapterName; return this; }
        public IntegrationFlowDTOBuilder outboundAdapterType(String outboundAdapterType) { this.outboundAdapterType = outboundAdapterType; return this; }

        public IntegrationFlowDTO build() {
            IntegrationFlowDTO dto = new IntegrationFlowDTO();
            dto.id = this.id;
            dto.name = this.name;
            dto.description = this.description;
            dto.inboundAdapterId = this.inboundAdapterId;
            dto.outboundAdapterId = this.outboundAdapterId;
            dto.sourceFlowStructureId = this.sourceFlowStructureId;
            dto.targetFlowStructureId = this.targetFlowStructureId;
            dto.status = this.status;
            dto.isActive = this.isActive;
            dto.mappingMode = this.mappingMode;
            dto.skipXmlConversion = this.skipXmlConversion;
            dto.createdAt = this.createdAt;
            dto.updatedAt = this.updatedAt;
            dto.createdBy = this.createdBy;
            dto.lastExecutionAt = this.lastExecutionAt;
            dto.executionCount = this.executionCount;
            dto.successCount = this.successCount;
            dto.errorCount = this.errorCount;
            dto.businessComponentId = this.businessComponentId;
            dto.inboundAdapterName = this.inboundAdapterName;
            dto.inboundAdapterType = this.inboundAdapterType;
            dto.outboundAdapterName = this.outboundAdapterName;
            dto.outboundAdapterType = this.outboundAdapterType;
            return dto;
        }
    }
}
