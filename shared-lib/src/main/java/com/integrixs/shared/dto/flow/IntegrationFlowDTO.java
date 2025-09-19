package com.integrixs.shared.dto.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for integration flow information.
 *
 * <p>Represents a complete integration flow including source and target
 * adapters, data transformations, and execution statistics.
 *
 * @author Integration Team
 * @since 1.0.0
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
    private String configuration;
    private String mappingMode;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private LocalDateTime lastExecutionAt;
    private int executionCount;
    private int successCount;
    private int errorCount;
    private List<FlowTransformationDTO> transformations = new ArrayList<>();

    // Default constructor
    public IntegrationFlowDTO() {
    }

    // All args constructor
    public IntegrationFlowDTO(String id, String name, String description, String inboundAdapterId, String outboundAdapterId, String sourceFlowStructureId, String targetFlowStructureId, String status, String configuration, String mappingMode, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, LocalDateTime lastExecutionAt, int executionCount, int successCount, int errorCount, List<FlowTransformationDTO> transformations) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.inboundAdapterId = inboundAdapterId;
        this.outboundAdapterId = outboundAdapterId;
        this.sourceFlowStructureId = sourceFlowStructureId;
        this.targetFlowStructureId = targetFlowStructureId;
        this.status = status;
        this.configuration = configuration;
        this.mappingMode = mappingMode;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.lastExecutionAt = lastExecutionAt;
        this.executionCount = executionCount;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.transformations = transformations != null ? transformations : new ArrayList<>();
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
    public String getConfiguration() { return configuration; }
    public String getMappingMode() { return mappingMode; }
    public boolean isIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getLastExecutionAt() { return lastExecutionAt; }
    public int getExecutionCount() { return executionCount; }
    public int getSuccessCount() { return successCount; }
    public int getErrorCount() { return errorCount; }
    public List<FlowTransformationDTO> getTransformations() { return transformations; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setInboundAdapterId(String inboundAdapterId) { this.inboundAdapterId = inboundAdapterId; }
    public void setOutboundAdapterId(String outboundAdapterId) { this.outboundAdapterId = outboundAdapterId; }
    public void setSourceFlowStructureId(String sourceFlowStructureId) { this.sourceFlowStructureId = sourceFlowStructureId; }
    public void setTargetFlowStructureId(String targetFlowStructureId) { this.targetFlowStructureId = targetFlowStructureId; }
    public void setStatus(String status) { this.status = status; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
    public void setMappingMode(String mappingMode) { this.mappingMode = mappingMode; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setLastExecutionAt(LocalDateTime lastExecutionAt) { this.lastExecutionAt = lastExecutionAt; }
    public void setExecutionCount(int executionCount) { this.executionCount = executionCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    public void setTransformations(List<FlowTransformationDTO> transformations) { this.transformations = transformations; }

    // Builder
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
        private String configuration;
        private String mappingMode;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private LocalDateTime lastExecutionAt;
        private int executionCount;
        private int successCount;
        private int errorCount;
        private List<FlowTransformationDTO> transformations = new ArrayList<>();

        public IntegrationFlowDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public IntegrationFlowDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public IntegrationFlowDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public IntegrationFlowDTOBuilder inboundAdapterId(String inboundAdapterId) {
            this.inboundAdapterId = inboundAdapterId;
            return this;
        }

        public IntegrationFlowDTOBuilder outboundAdapterId(String outboundAdapterId) {
            this.outboundAdapterId = outboundAdapterId;
            return this;
        }

        public IntegrationFlowDTOBuilder sourceFlowStructureId(String sourceFlowStructureId) {
            this.sourceFlowStructureId = sourceFlowStructureId;
            return this;
        }

        public IntegrationFlowDTOBuilder targetFlowStructureId(String targetFlowStructureId) {
            this.targetFlowStructureId = targetFlowStructureId;
            return this;
        }

        public IntegrationFlowDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public IntegrationFlowDTOBuilder configuration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        public IntegrationFlowDTOBuilder mappingMode(String mappingMode) {
            this.mappingMode = mappingMode;
            return this;
        }

        public IntegrationFlowDTOBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public IntegrationFlowDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public IntegrationFlowDTOBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public IntegrationFlowDTOBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public IntegrationFlowDTOBuilder lastExecutionAt(LocalDateTime lastExecutionAt) {
            this.lastExecutionAt = lastExecutionAt;
            return this;
        }

        public IntegrationFlowDTOBuilder executionCount(int executionCount) {
            this.executionCount = executionCount;
            return this;
        }

        public IntegrationFlowDTOBuilder successCount(int successCount) {
            this.successCount = successCount;
            return this;
        }

        public IntegrationFlowDTOBuilder errorCount(int errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public IntegrationFlowDTOBuilder transformations(List<FlowTransformationDTO> transformations) {
            this.transformations = transformations;
            return this;
        }

        public IntegrationFlowDTO build() {
            return new IntegrationFlowDTO(id, name, description, inboundAdapterId, outboundAdapterId, sourceFlowStructureId, targetFlowStructureId, status, configuration, mappingMode, isActive, createdAt, updatedAt, createdBy, lastExecutionAt, executionCount, successCount, errorCount, transformations);
        }
    }
}
