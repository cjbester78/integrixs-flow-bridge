package com.integrixs.shared.dto.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.integrixs.shared.dto.FieldMappingDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for flow transformation configuration.
 *
 * <p>Represents a transformation step within an integration flow,
 * including field mappings, filters, enrichments, and validations.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class FlowTransformationDTO {

    private String id;
    private String flowId;
    private String type;
    private String configuration;
    private int executionOrder;
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FieldMappingDTO> fieldMappings = new ArrayList<>();
    private String name;
    private String description;
    private String errorStrategy = "FAIL";

    // Default constructor
    public FlowTransformationDTO() {
    }

    // All args constructor
    public FlowTransformationDTO(String id, String flowId, String type, String configuration, int executionOrder, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt, List<FieldMappingDTO> fieldMappings, String name, String description, String errorStrategy) {
        this.id = id;
        this.flowId = flowId;
        this.type = type;
        this.configuration = configuration;
        this.executionOrder = executionOrder;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.fieldMappings = fieldMappings != null ? fieldMappings : new ArrayList<>();
        this.name = name;
        this.description = description;
        this.errorStrategy = errorStrategy;
    }

    // Getters
    public String getId() { return id; }
    public String getFlowId() { return flowId; }
    public String getType() { return type; }
    public String getConfiguration() { return configuration; }
    public int getExecutionOrder() { return executionOrder; }
    public boolean isIsActive() { return isActive; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<FieldMappingDTO> getFieldMappings() { return fieldMappings; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getErrorStrategy() { return errorStrategy; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setFlowId(String flowId) { this.flowId = flowId; }
    public void setType(String type) { this.type = type; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
    public void setExecutionOrder(int executionOrder) { this.executionOrder = executionOrder; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setFieldMappings(List<FieldMappingDTO> fieldMappings) { this.fieldMappings = fieldMappings; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setErrorStrategy(String errorStrategy) { this.errorStrategy = errorStrategy; }

    // Builder
    public static FlowTransformationDTOBuilder builder() {
        return new FlowTransformationDTOBuilder();
    }

    public static class FlowTransformationDTOBuilder {
        private String id;
        private String flowId;
        private String type;
        private String configuration;
        private int executionOrder;
        private boolean isActive = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<FieldMappingDTO> fieldMappings = new ArrayList<>();
        private String name;
        private String description;
        private String errorStrategy = "FAIL";

        public FlowTransformationDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public FlowTransformationDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public FlowTransformationDTOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public FlowTransformationDTOBuilder configuration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        public FlowTransformationDTOBuilder executionOrder(int executionOrder) {
            this.executionOrder = executionOrder;
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

        public FlowTransformationDTOBuilder fieldMappings(List<FieldMappingDTO> fieldMappings) {
            this.fieldMappings = fieldMappings;
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

        public FlowTransformationDTOBuilder errorStrategy(String errorStrategy) {
            this.errorStrategy = errorStrategy;
            return this;
        }

        public FlowTransformationDTO build() {
            return new FlowTransformationDTO(id, flowId, type, configuration, executionOrder, isActive, createdAt, updatedAt, fieldMappings, name, description, errorStrategy);
        }
    }
}
