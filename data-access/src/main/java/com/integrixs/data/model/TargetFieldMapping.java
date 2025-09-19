package com.integrixs.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing field mappings specific to an orchestration target.
 * Allows different field mappings for each target in an orchestration flow.
 */
@Entity
@Table(name = "target_field_mappings",
    indexes = {
        @Index(name = "idx_target_mapping_target", columnList = "orchestration_target_id"),
        @Index(name = "idx_target_mapping_source", columnList = "source_field_path"),
        @Index(name = "idx_target_mapping_target_field", columnList = "target_field_path")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_target_field_mapping",
            columnNames = {"orchestration_target_id", "target_field_path"})
    }
)
public class TargetFieldMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The orchestration target this mapping belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orchestration_target_id", nullable = false)
    @NotNull(message = "Orchestration target is required")
    private OrchestrationTarget orchestrationTarget;

    /**
     * Source field path(e.g., "order.customer.name")
     */
    @Column(name = "source_field_path", nullable = false, length = 500)
    @NotBlank(message = "Source field path is required")
    @Size(max = 500, message = "Source field path cannot exceed 500 characters")
    private String sourceFieldPath;

    /**
     * Target field path(e.g., "customerInfo.fullName")
     */
    @Column(name = "target_field_path", nullable = false, length = 500)
    @NotBlank(message = "Target field path is required")
    @Size(max = 500, message = "Target field path cannot exceed 500 characters")
    private String targetFieldPath;

    /**
     * Mapping type(DIRECT, FUNCTION, CONSTANT, CONDITIONAL, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_type", length = 50, nullable = false)
    @NotNull(message = "Mapping type is required")
    private MappingType mappingType = MappingType.DIRECT;

    /**
     * Transformation function or expression
     */
    @Column(name = "transformation_expression", columnDefinition = "TEXT")
    private String transformationExpression;

    /**
     * Constant value(when mapping type is CONSTANT)
     */
    @Column(name = "constant_value", length = 1000)
    @Size(max = 1000, message = "Constant value cannot exceed 1000 characters")
    private String constantValue;

    /**
     * Condition for conditional mappings
     */
    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    /**
     * Default value when source is null or condition fails
     */
    @Column(name = "default_value", length = 1000)
    @Size(max = 1000, message = "Default value cannot exceed 1000 characters")
    private String defaultValue;

    /**
     * Data type of the target field
     */
    @Column(name = "target_data_type", length = 50)
    private String targetDataType;

    /**
     * Whether this mapping is required(target field must have a value)
     */
    @Column(name = "is_required")
    private boolean required = false;

    /**
     * Mapping order(for sequential processing)
     */
    @Column(name = "mapping_order")
    @Min(value = 0, message = "Mapping order must be non - negative")
    private Integer mappingOrder = 0;

    /**
     * Visual flow data(JSON) from the visual flow editor
     */
    @Column(name = "visual_flow_data", columnDefinition = "TEXT")
    private String visualFlowData;

    /**
     * Validation rules(JSON)
     */
    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules;

    /**
     * Description or notes about this mapping
     */
    @Column(length = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Whether this mapping is active
     */
    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * User who created this mapping
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * User who last updated this mapping
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
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
