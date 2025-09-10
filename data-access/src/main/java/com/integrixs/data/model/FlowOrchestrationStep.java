package com.integrixs.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing an orchestration step in a flow
 */
@Entity
@Table(name = "flow_orchestration_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"flow"})
public class FlowOrchestrationStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id", nullable = false)
    private IntegrationFlow flow;
    
    @Column(name = "step_type", nullable = false)
    private String stepType;
    
    @Column(name = "step_name")
    private String stepName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "execution_order", nullable = false)
    private Integer executionOrder;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration", columnDefinition = "jsonb")
    private Map<String, Object> configuration;
    
    @Column(name = "condition_expression")
    private String conditionExpression;
    
    @Column(name = "is_conditional")
    @Builder.Default
    private Boolean isConditional = false;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;
    
    @Column(name = "retry_attempts")
    @Builder.Default
    private Integer retryAttempts = 0;
    
    @Column(name = "retry_delay_seconds")
    @Builder.Default
    private Integer retryDelaySeconds = 60;
    
    // For routing steps
    @Column(name = "target_adapter_id")
    private UUID targetAdapterId;
    
    @Column(name = "target_flow_structure_id")
    private UUID targetFlowStructureId;
    
    // For transformation steps
    @Column(name = "transformation_id")
    private UUID transformationId;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Step types enum
     */
    public enum StepType {
        ROUTE,          // Route to another adapter
        TRANSFORM,      // Apply transformation
        CONDITION,      // Conditional branching
        LOOP,           // Loop over collection
        AGGREGATE,      // Aggregate multiple messages
        SPLIT,          // Split message into multiple
        ENRICH,         // Enrich with additional data
        VALIDATE,       // Validate message
        LOG,            // Log message
        CUSTOM          // Custom step type
    }
}