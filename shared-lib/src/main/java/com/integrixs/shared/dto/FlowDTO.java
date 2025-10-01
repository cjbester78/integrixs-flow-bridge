package com.integrixs.shared.dto;

import java.time.LocalDateTime;

public class FlowDTO {
    private String id;
    private String name;
    private String uniqueFlowId;
    private String description;
    private String type;
    private String inboundAdapterId;
    private String outboundAdapterId;
    private String sourceBusinessComponentId;
    private String targetBusinessComponentId;
    private String sourceFlowStructureId;
    private String targetFlowStructureId;
    private String status;
    private String configuration;
    private String mappingMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public FlowDTO() {
    }

    public FlowDTO(String id, String name, String uniqueFlowId, String description, String type,
                   String inboundAdapterId, String outboundAdapterId, String sourceBusinessComponentId,
                   String targetBusinessComponentId, String sourceFlowStructureId, String targetFlowStructureId,
                   String status, String configuration, String mappingMode, LocalDateTime createdAt,
                   LocalDateTime updatedAt, String createdBy, String updatedBy) {
        this.id = id;
        this.name = name;
        this.uniqueFlowId = uniqueFlowId;
        this.description = description;
        this.type = type;
        this.inboundAdapterId = inboundAdapterId;
        this.outboundAdapterId = outboundAdapterId;
        this.sourceBusinessComponentId = sourceBusinessComponentId;
        this.targetBusinessComponentId = targetBusinessComponentId;
        this.sourceFlowStructureId = sourceFlowStructureId;
        this.targetFlowStructureId = targetFlowStructureId;
        this.status = status;
        this.configuration = configuration;
        this.mappingMode = mappingMode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getUniqueFlowId() { return uniqueFlowId; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getInboundAdapterId() { return inboundAdapterId; }
    public String getOutboundAdapterId() { return outboundAdapterId; }
    public String getSourceBusinessComponentId() { return sourceBusinessComponentId; }
    public String getTargetBusinessComponentId() { return targetBusinessComponentId; }
    public String getSourceFlowStructureId() { return sourceFlowStructureId; }
    public String getTargetFlowStructureId() { return targetFlowStructureId; }
    public String getStatus() { return status; }
    public String getConfiguration() { return configuration; }
    public String getMappingMode() { return mappingMode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setUniqueFlowId(String uniqueFlowId) { this.uniqueFlowId = uniqueFlowId; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setInboundAdapterId(String inboundAdapterId) { this.inboundAdapterId = inboundAdapterId; }
    public void setOutboundAdapterId(String outboundAdapterId) { this.outboundAdapterId = outboundAdapterId; }
    public void setSourceBusinessComponentId(String sourceBusinessComponentId) { this.sourceBusinessComponentId = sourceBusinessComponentId; }
    public void setTargetBusinessComponentId(String targetBusinessComponentId) { this.targetBusinessComponentId = targetBusinessComponentId; }
    public void setSourceFlowStructureId(String sourceFlowStructureId) { this.sourceFlowStructureId = sourceFlowStructureId; }
    public void setTargetFlowStructureId(String targetFlowStructureId) { this.targetFlowStructureId = targetFlowStructureId; }
    public void setStatus(String status) { this.status = status; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
    public void setMappingMode(String mappingMode) { this.mappingMode = mappingMode; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public static FlowDTOBuilder builder() {
        return new FlowDTOBuilder();
    }

    public static class FlowDTOBuilder {
        private String id;
        private String name;
        private String uniqueFlowId;
        private String description;
        private String type;
        private String inboundAdapterId;
        private String outboundAdapterId;
        private String sourceBusinessComponentId;
        private String targetBusinessComponentId;
        private String sourceFlowStructureId;
        private String targetFlowStructureId;
        private String status;
        private String configuration;
        private String mappingMode;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;

        public FlowDTOBuilder id(String id) { this.id = id; return this; }
        public FlowDTOBuilder name(String name) { this.name = name; return this; }
        public FlowDTOBuilder uniqueFlowId(String uniqueFlowId) { this.uniqueFlowId = uniqueFlowId; return this; }
        public FlowDTOBuilder description(String description) { this.description = description; return this; }
        public FlowDTOBuilder type(String type) { this.type = type; return this; }
        public FlowDTOBuilder inboundAdapterId(String inboundAdapterId) { this.inboundAdapterId = inboundAdapterId; return this; }
        public FlowDTOBuilder outboundAdapterId(String outboundAdapterId) { this.outboundAdapterId = outboundAdapterId; return this; }
        public FlowDTOBuilder sourceBusinessComponentId(String sourceBusinessComponentId) { this.sourceBusinessComponentId = sourceBusinessComponentId; return this; }
        public FlowDTOBuilder targetBusinessComponentId(String targetBusinessComponentId) { this.targetBusinessComponentId = targetBusinessComponentId; return this; }
        public FlowDTOBuilder sourceFlowStructureId(String sourceFlowStructureId) { this.sourceFlowStructureId = sourceFlowStructureId; return this; }
        public FlowDTOBuilder targetFlowStructureId(String targetFlowStructureId) { this.targetFlowStructureId = targetFlowStructureId; return this; }
        public FlowDTOBuilder status(String status) { this.status = status; return this; }
        public FlowDTOBuilder configuration(String configuration) { this.configuration = configuration; return this; }
        public FlowDTOBuilder mappingMode(String mappingMode) { this.mappingMode = mappingMode; return this; }
        public FlowDTOBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public FlowDTOBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public FlowDTOBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public FlowDTOBuilder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }

        public FlowDTO build() {
            return new FlowDTO(id, name, uniqueFlowId, description, type, inboundAdapterId,
                             outboundAdapterId, sourceBusinessComponentId, targetBusinessComponentId,
                             sourceFlowStructureId, targetFlowStructureId, status, configuration,
                             mappingMode, createdAt, updatedAt, createdBy, updatedBy);
        }
    }
}
