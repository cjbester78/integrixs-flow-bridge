package com.integrixs.shared.dto;

import com.integrixs.shared.enums.TransformationType;
import java.time.LocalDateTime;

/**
 * DTO for flow transformation data.
 */
public class FlowTransformationDTO {

    private String id;
    private String flowId;
    private String name;
    private String description;
    private TransformationType transformationType;
    private String configuration;
    private Integer sequence;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public FlowTransformationDTO() {
    }

    // All args constructor
    public FlowTransformationDTO(String id, String flowId, String name, String description, TransformationType transformationType, String configuration, Integer sequence, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.flowId = flowId;
        this.name = name;
        this.description = description;
        this.transformationType = transformationType;
        this.configuration = configuration;
        this.sequence = sequence;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getFlowId() { return flowId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public TransformationType getTransformationType() { return transformationType; }
    public String getConfiguration() { return configuration; }
    public Integer getSequence() { return sequence; }
    public boolean isIsActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setFlowId(String flowId) { this.flowId = flowId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setTransformationType(TransformationType transformationType) { this.transformationType = transformationType; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static FlowTransformationDTOBuilder builder() {
        return new FlowTransformationDTOBuilder();
    }

    public static class FlowTransformationDTOBuilder {
        private String id;
        private String flowId;
        private String name;
        private String description;
        private TransformationType transformationType;
        private String configuration;
        private Integer sequence;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public FlowTransformationDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public FlowTransformationDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public FlowTransformationDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FlowTransformationDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FlowTransformationDTOBuilder transformationType(TransformationType transformationType) {
            this.transformationType = transformationType;
            return this;
        }

        public FlowTransformationDTOBuilder configuration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        public FlowTransformationDTOBuilder sequence(Integer sequence) {
            this.sequence = sequence;
            return this;
        }

        public FlowTransformationDTOBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public FlowTransformationDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FlowTransformationDTOBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public FlowTransformationDTO build() {
            return new FlowTransformationDTO(id, flowId, name, description, transformationType, configuration, sequence, isActive, createdAt, updatedAt);
        }
    }
}
