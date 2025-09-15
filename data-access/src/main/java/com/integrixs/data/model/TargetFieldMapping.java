package com.integrixs.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TargetFieldMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
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
    @Builder.Default
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
    @Builder.Default
    private boolean required = false;

    /**
     * Mapping order(for sequential processing)
     */
    @Column(name = "mapping_order")
    @Min(value = 0, message = "Mapping order must be non - negative")
    @Builder.Default
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
    @Builder.Default
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
}
