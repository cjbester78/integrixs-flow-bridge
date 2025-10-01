package com.integrixs.data.model;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing field mappings specific to an orchestration target.
 * Allows different field mappings for each target in an orchestration flow.
 */
public class TargetFieldMapping {

    private UUID id;

    /**
     * The orchestration target this mapping belongs to
     */
    @NotNull(message = "Orchestration target is required")
    private OrchestrationTarget orchestrationTarget;

    /**
     * Source field path(e.g., "order.customer.name")
     */
    @NotBlank(message = "Source field path is required")
    @Size(max = 500, message = "Source field path cannot exceed 500 characters")
    private String sourceFieldPath;

    /**
     * Target field path(e.g., "customerInfo.fullName")
     */
    @NotBlank(message = "Target field path is required")
    @Size(max = 500, message = "Target field path cannot exceed 500 characters")
    private String targetFieldPath;

    /**
     * Mapping type(DIRECT, FUNCTION, CONSTANT, CONDITIONAL, etc.)
     */
    @NotNull(message = "Mapping type is required")
    private MappingType mappingType = MappingType.DIRECT;

    /**
     * Transformation function or expression
     */
    private String transformationExpression;

    /**
     * Constant value(when mapping type is CONSTANT)
     */
    @Size(max = 1000, message = "Constant value cannot exceed 1000 characters")
    private String constantValue;

    /**
     * Condition for conditional mappings
     */
    private String conditionExpression;

    /**
     * Default value when source is null or condition fails
     */
    @Size(max = 1000, message = "Default value cannot exceed 1000 characters")
    private String defaultValue;

    /**
     * Data type of the target field
     */
    private String targetDataType;

    /**
     * Whether this mapping is required(target field must have a value)
     */
    private boolean required = false;

    /**
     * Mapping order(for sequential processing)
     */
    @Min(value = 0, message = "Mapping order must be non - negative")
    private Integer mappingOrder = 0;

    /**
     * Visual flow data(JSON) from the visual flow editor
     */
    private String visualFlowData;

    /**
     * Validation rules(JSON)
     */
    private String validationRules;

    /**
     * Description or notes about this mapping
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Whether this mapping is active
     */
    private boolean active = true;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

    /**
     * User who created this mapping
     */
    private User createdBy;

    /**
     * User who last updated this mapping
     */
    private User updatedBy;

    /**
     * Types of field mappings
     */
    public enum MappingType {
        DIRECT,         // Direct field to field mapping
        FUNCTION,       // Apply transformation function
        CONSTANT,       // Map to constant value
        CONDITIONAL,    // Conditional mapping based on expression
        CONCATENATION, // Concatenate multiple fields
        SPLIT,         // Split field into multiple targets
        LOOKUP,        // Lookup value from external source
        CALCULATION,   // Mathematical calculation
        DATE_FORMAT,   // Date/time formatting
        CUSTOM          // Custom transformation logic
    }

    // Default constructor
    public TargetFieldMapping() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OrchestrationTarget getOrchestrationTarget() {
        return orchestrationTarget;
    }

    public void setOrchestrationTarget(OrchestrationTarget orchestrationTarget) {
        this.orchestrationTarget = orchestrationTarget;
    }

    public String getSourceFieldPath() {
        return sourceFieldPath;
    }

    public void setSourceFieldPath(String sourceFieldPath) {
        this.sourceFieldPath = sourceFieldPath;
    }

    public String getTargetFieldPath() {
        return targetFieldPath;
    }

    public void setTargetFieldPath(String targetFieldPath) {
        this.targetFieldPath = targetFieldPath;
    }

    public MappingType getMappingType() {
        return mappingType;
    }

    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }

    public String getTransformationExpression() {
        return transformationExpression;
    }

    public void setTransformationExpression(String transformationExpression) {
        this.transformationExpression = transformationExpression;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public void setConstantValue(String constantValue) {
        this.constantValue = constantValue;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTargetDataType() {
        return targetDataType;
    }

    public void setTargetDataType(String targetDataType) {
        this.targetDataType = targetDataType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Integer getMappingOrder() {
        return mappingOrder;
    }

    public void setMappingOrder(Integer mappingOrder) {
        this.mappingOrder = mappingOrder;
    }

    public String getVisualFlowData() {
        return visualFlowData;
    }

    public void setVisualFlowData(String visualFlowData) {
        this.visualFlowData = visualFlowData;
    }

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    // Builder
    public static TargetFieldMappingBuilder builder() {
        return new TargetFieldMappingBuilder();
    }

    public static class TargetFieldMappingBuilder {
        private UUID id;
        private OrchestrationTarget orchestrationTarget;
        private String sourceFieldPath;
        private String targetFieldPath;
        private MappingType mappingType;
        private String transformationExpression;
        private String constantValue;
        private String conditionExpression;
        private String defaultValue;
        private String targetDataType;
        private boolean required;
        private Integer mappingOrder;
        private String visualFlowData;
        private String validationRules;
        private String description;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private User createdBy;
        private User updatedBy;

        public TargetFieldMappingBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TargetFieldMappingBuilder orchestrationTarget(OrchestrationTarget orchestrationTarget) {
            this.orchestrationTarget = orchestrationTarget;
            return this;
        }

        public TargetFieldMappingBuilder sourceFieldPath(String sourceFieldPath) {
            this.sourceFieldPath = sourceFieldPath;
            return this;
        }

        public TargetFieldMappingBuilder targetFieldPath(String targetFieldPath) {
            this.targetFieldPath = targetFieldPath;
            return this;
        }

        public TargetFieldMappingBuilder mappingType(MappingType mappingType) {
            this.mappingType = mappingType;
            return this;
        }

        public TargetFieldMappingBuilder transformationExpression(String transformationExpression) {
            this.transformationExpression = transformationExpression;
            return this;
        }

        public TargetFieldMappingBuilder constantValue(String constantValue) {
            this.constantValue = constantValue;
            return this;
        }

        public TargetFieldMappingBuilder conditionExpression(String conditionExpression) {
            this.conditionExpression = conditionExpression;
            return this;
        }

        public TargetFieldMappingBuilder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public TargetFieldMappingBuilder targetDataType(String targetDataType) {
            this.targetDataType = targetDataType;
            return this;
        }

        public TargetFieldMappingBuilder required(boolean required) {
            this.required = required;
            return this;
        }

        public TargetFieldMappingBuilder mappingOrder(Integer mappingOrder) {
            this.mappingOrder = mappingOrder;
            return this;
        }

        public TargetFieldMappingBuilder visualFlowData(String visualFlowData) {
            this.visualFlowData = visualFlowData;
            return this;
        }

        public TargetFieldMappingBuilder validationRules(String validationRules) {
            this.validationRules = validationRules;
            return this;
        }

        public TargetFieldMappingBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TargetFieldMappingBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public TargetFieldMappingBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TargetFieldMappingBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TargetFieldMappingBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public TargetFieldMappingBuilder updatedBy(User updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public TargetFieldMapping build() {
            TargetFieldMapping instance = new TargetFieldMapping();
            instance.setId(this.id);
            instance.setOrchestrationTarget(this.orchestrationTarget);
            instance.setSourceFieldPath(this.sourceFieldPath);
            instance.setTargetFieldPath(this.targetFieldPath);
            instance.setMappingType(this.mappingType);
            instance.setTransformationExpression(this.transformationExpression);
            instance.setConstantValue(this.constantValue);
            instance.setConditionExpression(this.conditionExpression);
            instance.setDefaultValue(this.defaultValue);
            instance.setTargetDataType(this.targetDataType);
            instance.setRequired(this.required);
            instance.setMappingOrder(this.mappingOrder);
            instance.setVisualFlowData(this.visualFlowData);
            instance.setValidationRules(this.validationRules);
            instance.setDescription(this.description);
            instance.setActive(this.active);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            instance.setCreatedBy(this.createdBy);
            instance.setUpdatedBy(this.updatedBy);
            return instance;
        }
    }
}
