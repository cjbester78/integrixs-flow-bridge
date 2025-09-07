package com.integrixs.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a target adapter in an orchestration flow.
 * Supports multiple target adapters per integration flow with routing conditions.
 */
@Entity
@Table(name = "orchestration_targets",
    indexes = {
        @Index(name = "idx_orch_target_flow", columnList = "flow_id"),
        @Index(name = "idx_orch_target_adapter", columnList = "target_adapter_id"),
        @Index(name = "idx_orch_target_order", columnList = "flow_id,execution_order")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrchestrationTarget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    
    /**
     * The orchestration flow this target belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id", nullable = false)
    @NotNull(message = "Flow is required")
    private IntegrationFlow flow;
    
    /**
     * The target adapter
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_adapter_id", nullable = false)
    @NotNull(message = "Target adapter is required")
    private CommunicationAdapter targetAdapter;
    
    /**
     * Execution order (for sequential processing)
     * Lower numbers execute first
     */
    @Column(name = "execution_order", nullable = false)
    @Min(value = 0, message = "Execution order must be non-negative")
    @Builder.Default
    private Integer executionOrder = 0;
    
    /**
     * Whether this target executes in parallel with others at the same order level
     */
    @Column(name = "is_parallel")
    @Builder.Default
    private boolean parallel = false;
    
    /**
     * Routing condition (e.g., "payload.type == 'ORDER'")
     * If null, always routes to this target
     */
    @Column(name = "routing_condition", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Routing condition cannot exceed 1000 characters")
    private String routingCondition;
    
    /**
     * Condition type (EXPRESSION, HEADER_MATCH, CONTENT_TYPE, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", length = 50)
    @Builder.Default
    private ConditionType conditionType = ConditionType.ALWAYS;
    
    /**
     * Target data structure ID for this specific target
     */
    @Column(name = "structure_id")
    private UUID structureId;
    
    /**
     * Response structure ID for synchronous calls to this target
     */
    @Column(name = "response_structure_id")
    private UUID responseStructureId;
    
    /**
     * Whether to wait for response from this target
     */
    @Column(name = "await_response")
    @Builder.Default
    private boolean awaitResponse = false;
    
    /**
     * Timeout in milliseconds for this target
     */
    @Column(name = "timeout_ms")
    @Min(value = 0, message = "Timeout must be non-negative")
    @Max(value = 3600000, message = "Timeout cannot exceed 1 hour")
    @Builder.Default
    private Long timeoutMs = 30000L; // 30 seconds default
    
    /**
     * Retry policy for this target
     */
    @Embedded
    private RetryPolicy retryPolicy;
    
    /**
     * Error handling strategy
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "error_strategy", length = 50)
    @Builder.Default
    private ErrorStrategy errorStrategy = ErrorStrategy.FAIL_FLOW;
    
    /**
     * Whether this target is active
     */
    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;
    
    /**
     * Configuration specific to this target (JSON)
     */
    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;
    
    /**
     * Description of this target's purpose
     */
    @Column(length = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    /**
     * User who created this target
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    /**
     * User who last updated this target
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    /**
     * Condition types for routing
     */
    public enum ConditionType {
        ALWAYS,           // Always route to this target
        EXPRESSION,       // SpEL or JavaScript expression
        HEADER_MATCH,     // Match on message headers
        CONTENT_TYPE,     // Match on content type
        XPATH,           // XPath expression for XML
        JSONPATH,        // JSONPath expression for JSON
        REGEX,           // Regular expression match
        CUSTOM           // Custom condition evaluator
    }
    
    /**
     * Error handling strategies
     */
    public enum ErrorStrategy {
        FAIL_FLOW,       // Fail the entire flow
        SKIP_TARGET,     // Skip this target and continue
        RETRY,           // Retry based on retry policy
        DEAD_LETTER,     // Send to dead letter queue
        COMPENSATE,      // Trigger compensation flow
        CUSTOM           // Custom error handler
    }
    
    /**
     * Embedded retry policy
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RetryPolicy {
        
        @Column(name = "max_attempts")
        @Min(0)
        @Max(10)
        @Builder.Default
        private Integer maxAttempts = 3;
        
        @Column(name = "retry_delay_ms")
        @Min(0)
        @Max(300000) // 5 minutes max
        @Builder.Default
        private Long retryDelayMs = 1000L;
        
        @Column(name = "backoff_multiplier")
        @Min(1)
        @Max(10)
        @Builder.Default
        private Double backoffMultiplier = 2.0;
        
        @Column(name = "max_retry_delay_ms")
        @Min(0)
        @Max(3600000) // 1 hour max
        @Builder.Default
        private Long maxRetryDelayMs = 60000L; // 1 minute
        
        @Column(name = "retry_on_errors", length = 500)
        private String retryOnErrors; // Comma-separated error types
    }
}