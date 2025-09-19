package com.integrixs.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
public class OrchestrationTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Name of this orchestration target
     */
    @Column(name = "name", nullable = false, length = 255)
    @NotBlank(message = "Target name is required")
    @Size(max = 255, message = "Target name cannot exceed 255 characters")
    private String name;

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
    @JoinColumn(name = "target_adapter_id")
    private CommunicationAdapter targetAdapter;

    /**
     * The target flow (alternative to target adapter)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_flow_id")
    private IntegrationFlow targetFlow;

    /**
     * Execution order(for sequential processing)
     * Lower numbers execute first
     */
    @Column(name = "execution_order", nullable = false)
    @Min(value = 0, message = "Execution order must be non - negative")
    private Integer executionOrder = 0;

    /**
     * Whether this target executes in parallel with others at the same order level
     */
    @Column(name = "is_parallel")
    private boolean parallel = false;

    /**
     * Routing condition(e.g., "payload.type == 'ORDER'")
     * If null, always routes to this target
     */
    @Column(name = "routing_condition", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Routing condition cannot exceed 1000 characters")
    private String routingCondition;

    /**
     * Condition type(EXPRESSION, HEADER_MATCH, CONTENT_TYPE, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", length = 50)
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
    private boolean awaitResponse = false;

    /**
     * Timeout in milliseconds for this target
     */
    @Column(name = "timeout_ms")
    @Min(value = 0, message = "Timeout must be non - negative")
    @Max(value = 3600000, message = "Timeout cannot exceed 1 hour")
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
    private ErrorStrategy errorStrategy = ErrorStrategy.FAIL_FLOW;

    /**
     * Whether this target is active
     */
    @Column(name = "is_active")
    private boolean active = true;

    /**
     * Status of this target
     */
    @Column(name = "status", length = 50)
    private String status = "ACTIVE";

    /**
     * Priority for execution ordering (higher values execute first)
     */
    @Column(name = "priority")
    private Integer priority = 0;

    /**
     * Configuration specific to this target(JSON)
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
        ALWAYS,          // Always route to this target
        EXPRESSION,      // SpEL or JavaScript expression
        HEADER_MATCH,    // Match on message headers
        CONTENT_TYPE,    // Match on content type
        XPATH,          // XPath expression for XML
        JSONPATH,       // JSONPath expression for JSON
        REGEX,          // Regular expression match
        CUSTOM           // Custom condition evaluator
    }

    /**
     * Error handling strategies
     */
    public enum ErrorStrategy {
        FAIL_FLOW,      // Fail the entire flow
        SKIP_TARGET,    // Skip this target and continue
        RETRY,          // Retry based on retry policy
        DEAD_LETTER,    // Send to dead letter queue
        COMPENSATE,     // Trigger compensation flow
        CUSTOM           // Custom error handler
    }

    /**
     * Embedded retry policy
     */
    @Embeddable
    public static class RetryPolicy {

        @Column(name = "max_attempts")
        @Min(0)
        @Max(10)
        private Integer maxAttempts = 3;

        @Column(name = "retry_delay_ms")
        @Min(0)
        @Max(300000) // 5 minutes max
        private Long retryDelayMs = 1000L;

        @Column(name = "backoff_multiplier")
        @Min(1)
        @Max(10)
        private Double backoffMultiplier = 2.0;

        @Column(name = "max_retry_delay_ms")
        @Min(0)
        @Max(3600000) // 1 hour max
        private Long maxRetryDelayMs = 60000L; // 1 minute

        @Column(name = "retry_on_errors", length = 500)
        private String retryOnErrors; // Comma - separated error types

        // Default constructor
        public RetryPolicy() {
        }

        public Integer getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Long getRetryDelayMs() {
            return retryDelayMs;
        }

        public void setRetryDelayMs(Long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
        }

        public Double getBackoffMultiplier() {
            return backoffMultiplier;
        }

        public void setBackoffMultiplier(Double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
        }

        public Long getMaxRetryDelayMs() {
            return maxRetryDelayMs;
        }

        public void setMaxRetryDelayMs(Long maxRetryDelayMs) {
            this.maxRetryDelayMs = maxRetryDelayMs;
        }

        public String getRetryOnErrors() {
            return retryOnErrors;
        }

        public void setRetryOnErrors(String retryOnErrors) {
            this.retryOnErrors = retryOnErrors;
        }

        // Builder
        public static RetryPolicyBuilder builder() {
            return new RetryPolicyBuilder();
        }

        public static class RetryPolicyBuilder {
            private Integer maxAttempts = 3;
            private Long retryDelayMs = 1000L;
            private Double backoffMultiplier = 2.0;
            private Long maxRetryDelayMs = 60000L;
            private String retryOnErrors;

            public RetryPolicyBuilder maxAttempts(Integer maxAttempts) {
                this.maxAttempts = maxAttempts;
                return this;
            }

            public RetryPolicyBuilder retryDelayMs(Long retryDelayMs) {
                this.retryDelayMs = retryDelayMs;
                return this;
            }

            public RetryPolicyBuilder backoffMultiplier(Double backoffMultiplier) {
                this.backoffMultiplier = backoffMultiplier;
                return this;
            }

            public RetryPolicyBuilder maxRetryDelayMs(Long maxRetryDelayMs) {
                this.maxRetryDelayMs = maxRetryDelayMs;
                return this;
            }

            public RetryPolicyBuilder retryOnErrors(String retryOnErrors) {
                this.retryOnErrors = retryOnErrors;
                return this;
            }

            public RetryPolicy build() {
                RetryPolicy policy = new RetryPolicy();
                policy.maxAttempts = this.maxAttempts;
                policy.retryDelayMs = this.retryDelayMs;
                policy.backoffMultiplier = this.backoffMultiplier;
                policy.maxRetryDelayMs = this.maxRetryDelayMs;
                policy.retryOnErrors = this.retryOnErrors;
                return policy;
            }
        }
    }

    // Default constructor
    public OrchestrationTarget() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public CommunicationAdapter getTargetAdapter() {
        return targetAdapter;
    }

    public void setTargetAdapter(CommunicationAdapter targetAdapter) {
        this.targetAdapter = targetAdapter;
    }

    public IntegrationFlow getTargetFlow() {
        return targetFlow;
    }

    public void setTargetFlow(IntegrationFlow targetFlow) {
        this.targetFlow = targetFlow;
    }

    public Integer getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(Integer executionOrder) {
        this.executionOrder = executionOrder;
    }

    public boolean isParallel() {
        return parallel;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public String getRoutingCondition() {
        return routingCondition;
    }

    public void setRoutingCondition(String routingCondition) {
        this.routingCondition = routingCondition;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public UUID getStructureId() {
        return structureId;
    }

    public void setStructureId(UUID structureId) {
        this.structureId = structureId;
    }

    public UUID getResponseStructureId() {
        return responseStructureId;
    }

    public void setResponseStructureId(UUID responseStructureId) {
        this.responseStructureId = responseStructureId;
    }

    public boolean isAwaitResponse() {
        return awaitResponse;
    }

    public void setAwaitResponse(boolean awaitResponse) {
        this.awaitResponse = awaitResponse;
    }

    public Long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public ErrorStrategy getErrorStrategy() {
        return errorStrategy;
    }

    public void setErrorStrategy(ErrorStrategy errorStrategy) {
        this.errorStrategy = errorStrategy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
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

    // Builder
    public static OrchestrationTargetBuilder builder() {
        return new OrchestrationTargetBuilder();
    }

    public static class OrchestrationTargetBuilder {
        private UUID id;
        private String name;
        private IntegrationFlow flow;
        private CommunicationAdapter targetAdapter;
        private IntegrationFlow targetFlow;
        private Integer executionOrder;
        private boolean parallel;
        private String routingCondition;
        private ConditionType conditionType;
        private UUID structureId;
        private UUID responseStructureId;
        private boolean awaitResponse;
        private Long timeoutMs;
        private RetryPolicy retryPolicy;
        private ErrorStrategy errorStrategy;
        private boolean active;
        private String status;
        private Integer priority;
        private String configuration;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private User createdBy;
        private User updatedBy;
        private Integer maxAttempts;
        private Long retryDelayMs;
        private Double backoffMultiplier;
        private Long maxRetryDelayMs;
        private String retryOnErrors;

        public OrchestrationTargetBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public OrchestrationTargetBuilder name(String name) {
            this.name = name;
            return this;
        }

        public OrchestrationTargetBuilder flow(IntegrationFlow flow) {
            this.flow = flow;
            return this;
        }

        public OrchestrationTargetBuilder targetAdapter(CommunicationAdapter targetAdapter) {
            this.targetAdapter = targetAdapter;
            return this;
        }

        public OrchestrationTargetBuilder targetFlow(IntegrationFlow targetFlow) {
            this.targetFlow = targetFlow;
            return this;
        }

        public OrchestrationTargetBuilder executionOrder(Integer executionOrder) {
            this.executionOrder = executionOrder;
            return this;
        }

        public OrchestrationTargetBuilder parallel(boolean parallel) {
            this.parallel = parallel;
            return this;
        }

        public OrchestrationTargetBuilder routingCondition(String routingCondition) {
            this.routingCondition = routingCondition;
            return this;
        }

        public OrchestrationTargetBuilder conditionType(ConditionType conditionType) {
            this.conditionType = conditionType;
            return this;
        }

        public OrchestrationTargetBuilder structureId(UUID structureId) {
            this.structureId = structureId;
            return this;
        }

        public OrchestrationTargetBuilder responseStructureId(UUID responseStructureId) {
            this.responseStructureId = responseStructureId;
            return this;
        }

        public OrchestrationTargetBuilder awaitResponse(boolean awaitResponse) {
            this.awaitResponse = awaitResponse;
            return this;
        }

        public OrchestrationTargetBuilder timeoutMs(Long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public OrchestrationTargetBuilder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public OrchestrationTargetBuilder errorStrategy(ErrorStrategy errorStrategy) {
            this.errorStrategy = errorStrategy;
            return this;
        }

        public OrchestrationTargetBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public OrchestrationTargetBuilder status(String status) {
            this.status = status;
            return this;
        }

        public OrchestrationTargetBuilder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public OrchestrationTargetBuilder configuration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        public OrchestrationTargetBuilder description(String description) {
            this.description = description;
            return this;
        }

        public OrchestrationTargetBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OrchestrationTargetBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public OrchestrationTargetBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public OrchestrationTargetBuilder updatedBy(User updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public OrchestrationTargetBuilder maxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public OrchestrationTargetBuilder retryDelayMs(Long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
            return this;
        }

        public OrchestrationTargetBuilder backoffMultiplier(Double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        public OrchestrationTargetBuilder maxRetryDelayMs(Long maxRetryDelayMs) {
            this.maxRetryDelayMs = maxRetryDelayMs;
            return this;
        }

        public OrchestrationTargetBuilder retryOnErrors(String retryOnErrors) {
            this.retryOnErrors = retryOnErrors;
            return this;
        }

        public OrchestrationTarget build() {
            OrchestrationTarget instance = new OrchestrationTarget();
            instance.setId(this.id);
            instance.setName(this.name);
            instance.setFlow(this.flow);
            instance.setTargetAdapter(this.targetAdapter);
            instance.setTargetFlow(this.targetFlow);
            instance.setExecutionOrder(this.executionOrder);
            instance.setParallel(this.parallel);
            instance.setRoutingCondition(this.routingCondition);
            instance.setConditionType(this.conditionType);
            instance.setStructureId(this.structureId);
            instance.setResponseStructureId(this.responseStructureId);
            instance.setAwaitResponse(this.awaitResponse);
            instance.setTimeoutMs(this.timeoutMs);
            instance.setRetryPolicy(this.retryPolicy);
            instance.setErrorStrategy(this.errorStrategy);
            instance.setActive(this.active);
            instance.setStatus(this.status);
            instance.setPriority(this.priority);
            instance.setConfiguration(this.configuration);
            instance.setDescription(this.description);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            instance.setCreatedBy(this.createdBy);
            instance.setUpdatedBy(this.updatedBy);
            // Retry fields are part of RetryPolicy, not OrchestrationTarget
            return instance;
        }
    }
}
