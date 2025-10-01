package com.integrixs.data.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.integrixs.shared.enums.TransformationType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a flow transformation.
 *
 * <p>Transformations are applied to data as it flows from source to target.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class FlowTransformation {

    /**
     * Unique identifier(UUID) for the entity
     */
        private UUID id;

    /**
     * The integration flow this transformation belongs to
     */
    @NotNull(message = "Flow is required")
    private IntegrationFlow flow;

    /**
     * Type of transformation
     */
    @NotNull(message = "Transformation type is required")
    private TransformationType type;

    /**
     * Configuration in JSON format
     */
    @NotBlank(message = "Configuration is required")
    @Size(max = 10000, message = "Configuration cannot exceed 10000 characters")
    private String configuration;

    /**
     * Order of execution within the flow
     */
    @Min(value = 1, message = "Execution order must be at least 1")
    private int executionOrder = 1;

    /**
     * Whether this transformation is active
     */
    @NotNull(message = "Active status is required")
    private boolean isActive = true;

    /**
     * Transformation name
     */
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    /**
     * Transformation description
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Timestamp of entity creation
     */
        private LocalDateTime createdAt;

    /**
     * Timestamp of last entity update
     */
        private LocalDateTime updatedAt;

    /**
     * User who created this transformation
     */
    @JsonIgnore
    private User createdBy;

    /**
     * User who last updated this transformation
     */
    @JsonIgnore
    private User updatedBy;

    /**
     * Field mappings for this transformation
     * Batch size optimization for lazy loading
     */
    private List<FieldMapping> fieldMappings = new ArrayList<>();

    /**
     * Transformation types
     */
    public enum TransformationType {
        FIELD_MAPPING,
        CUSTOM_FUNCTION,
        FILTER,
        ENRICHMENT,
        VALIDATION,
        AGGREGATION,
        ROUTING
    }

    /**
     * Adds a field mapping to this transformation
     *
     * @param fieldMapping the field mapping to add
     */
    public void addFieldMapping(FieldMapping fieldMapping) {
        fieldMappings.add(fieldMapping);
        fieldMapping.setTransformation(this);
    }

    /**
     * Removes a field mapping from this transformation
     *
     * @param fieldMapping the field mapping to remove
     */
    public void removeFieldMapping(FieldMapping fieldMapping) {
        fieldMappings.remove(fieldMapping);
        fieldMapping.setTransformation(null);
    }

    // Default constructor
    public FlowTransformation() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public IntegrationFlow getFlow() {
        return flow;
    }

    public void setFlow(IntegrationFlow flow) {
        this.flow = flow;
    }

    public TransformationType getType() {
        return type;
    }

    public void setType(TransformationType type) {
        this.type = type;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public int getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(int executionOrder) {
        this.executionOrder = executionOrder;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    public void setFieldMappings(List<FieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    // Builder
    public static FlowTransformationBuilder builder() {
        return new FlowTransformationBuilder();
    }

    public static class FlowTransformationBuilder {
        private UUID id;
        private IntegrationFlow flow;
        private TransformationType type;
        private String configuration;
        private int executionOrder;
        private boolean isActive;
        private String name;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private User createdBy;
        private User updatedBy;
        private List<FieldMapping> fieldMappings;

        public FlowTransformationBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public FlowTransformationBuilder flow(IntegrationFlow flow) {
            this.flow = flow;
            return this;
        }

        public FlowTransformationBuilder type(TransformationType type) {
            this.type = type;
            return this;
        }

        public FlowTransformationBuilder configuration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        public FlowTransformationBuilder executionOrder(int executionOrder) {
            this.executionOrder = executionOrder;
            return this;
        }

        public FlowTransformationBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public FlowTransformationBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FlowTransformationBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FlowTransformationBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FlowTransformationBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public FlowTransformationBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public FlowTransformationBuilder updatedBy(User updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public FlowTransformationBuilder fieldMappings(List<FieldMapping> fieldMappings) {
            this.fieldMappings = fieldMappings;
            return this;
        }

        public FlowTransformation build() {
            FlowTransformation instance = new FlowTransformation();
            instance.setId(this.id);
            instance.setFlow(this.flow);
            instance.setType(this.type);
            instance.setConfiguration(this.configuration);
            instance.setExecutionOrder(this.executionOrder);
            instance.setIsActive(this.isActive);
            instance.setName(this.name);
            instance.setDescription(this.description);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            instance.setCreatedBy(this.createdBy);
            instance.setUpdatedBy(this.updatedBy);
            instance.setFieldMappings(this.fieldMappings);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "FlowTransformation{" +
                "id=" + id + "type=" + type + "executionOrder=" + executionOrder + "isActive=" + isActive + "name=" + name + "..." +
                '}';
    }
}
