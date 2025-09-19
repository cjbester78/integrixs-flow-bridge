package com.integrixs.backend.api.dto.response;

import com.integrixs.data.model.OrchestrationTarget;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for orchestration target
 */
public class OrchestrationTargetResponse {

    private String id;
    private String flowId;
    private AdapterSummary targetAdapter;
    private Integer executionOrder;
    private boolean parallel;
    private String routingCondition;
    private OrchestrationTarget.ConditionType conditionType;
    private String structureId;
    private String responseStructureId;
    private boolean awaitResponse;
    private Long timeoutMs;
    private RetryPolicyResponse retryPolicy;
    private OrchestrationTarget.ErrorStrategy errorStrategy;
    private boolean active;
    private Map<String, Object> configuration;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long mappingCount;

    /**
     * Adapter summary
     */
                    public static class AdapterSummary {
        private String id;
        private String name;
        private String type;
        private String mode;
        private boolean active;

        // Default constructor
        public AdapterSummary() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

    // Builder
    public static AdapterSummaryBuilder builder() {
        return new AdapterSummaryBuilder();
    }

    public static class AdapterSummaryBuilder {
        private String id;
        private String name;
        private String type;
        private String mode;
        private boolean active;

        public AdapterSummaryBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AdapterSummaryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AdapterSummaryBuilder type(String type) {
            this.type = type;
            return this;
        }

        public AdapterSummaryBuilder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public AdapterSummaryBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public AdapterSummary build() {
            AdapterSummary result = new AdapterSummary();
            result.id = this.id;
            result.name = this.name;
            result.type = this.type;
            result.mode = this.mode;
            result.active = this.active;
            return result;
        }
    }
    }

    /**
     * Retry policy response
     */
                    public static class RetryPolicyResponse {
        private Integer maxAttempts;
        private Long retryDelayMs;
        private Double backoffMultiplier;
        private Long maxRetryDelayMs;
        private String retryOnErrors;

        // Default constructor
        public RetryPolicyResponse() {
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
    public static RetryPolicyResponseBuilder builder() {
        return new RetryPolicyResponseBuilder();
    }

    public static class RetryPolicyResponseBuilder {
        private Integer maxAttempts;
        private Long retryDelayMs;
        private Double backoffMultiplier;
        private Long maxRetryDelayMs;
        private String retryOnErrors;

        public RetryPolicyResponseBuilder maxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public RetryPolicyResponseBuilder retryDelayMs(Long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
            return this;
        }

        public RetryPolicyResponseBuilder backoffMultiplier(Double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        public RetryPolicyResponseBuilder maxRetryDelayMs(Long maxRetryDelayMs) {
            this.maxRetryDelayMs = maxRetryDelayMs;
            return this;
        }

        public RetryPolicyResponseBuilder retryOnErrors(String retryOnErrors) {
            this.retryOnErrors = retryOnErrors;
            return this;
        }

        public RetryPolicyResponse build() {
            RetryPolicyResponse result = new RetryPolicyResponse();
            result.maxAttempts = this.maxAttempts;
            result.retryDelayMs = this.retryDelayMs;
            result.backoffMultiplier = this.backoffMultiplier;
            result.maxRetryDelayMs = this.maxRetryDelayMs;
            result.retryOnErrors = this.retryOnErrors;
            return result;
        }
    }
    }

    // Default constructor
    public OrchestrationTargetResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public AdapterSummary getTargetAdapter() {
        return targetAdapter;
    }

    public void setTargetAdapter(AdapterSummary targetAdapter) {
        this.targetAdapter = targetAdapter;
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

    public OrchestrationTarget.ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(OrchestrationTarget.ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public String getStructureId() {
        return structureId;
    }

    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }

    public String getResponseStructureId() {
        return responseStructureId;
    }

    public void setResponseStructureId(String responseStructureId) {
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

    public RetryPolicyResponse getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicyResponse retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public OrchestrationTarget.ErrorStrategy getErrorStrategy() {
        return errorStrategy;
    }

    public void setErrorStrategy(OrchestrationTarget.ErrorStrategy errorStrategy) {
        this.errorStrategy = errorStrategy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public Long getMappingCount() {
        return mappingCount;
    }

    public void setMappingCount(Long mappingCount) {
        this.mappingCount = mappingCount;
    }

    // Builder
    public static OrchestrationTargetResponseBuilder builder() {
        return new OrchestrationTargetResponseBuilder();
    }

    public static class OrchestrationTargetResponseBuilder {
        private String id;
        private String flowId;
        private AdapterSummary targetAdapter;
        private Integer executionOrder;
        private boolean parallel;
        private String routingCondition;
        private OrchestrationTarget.ConditionType conditionType;
        private String structureId;
        private String responseStructureId;
        private boolean awaitResponse;
        private Long timeoutMs;
        private RetryPolicyResponse retryPolicy;
        private OrchestrationTarget.ErrorStrategy errorStrategy;
        private boolean active;
        private Map<String, Object> configuration;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long mappingCount;

        public OrchestrationTargetResponseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public OrchestrationTargetResponseBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public OrchestrationTargetResponseBuilder targetAdapter(AdapterSummary targetAdapter) {
            this.targetAdapter = targetAdapter;
            return this;
        }

        public OrchestrationTargetResponseBuilder executionOrder(Integer executionOrder) {
            this.executionOrder = executionOrder;
            return this;
        }

        public OrchestrationTargetResponseBuilder parallel(boolean parallel) {
            this.parallel = parallel;
            return this;
        }

        public OrchestrationTargetResponseBuilder routingCondition(String routingCondition) {
            this.routingCondition = routingCondition;
            return this;
        }

        public OrchestrationTargetResponseBuilder conditionType(OrchestrationTarget.ConditionType conditionType) {
            this.conditionType = conditionType;
            return this;
        }

        public OrchestrationTargetResponseBuilder structureId(String structureId) {
            this.structureId = structureId;
            return this;
        }

        public OrchestrationTargetResponseBuilder responseStructureId(String responseStructureId) {
            this.responseStructureId = responseStructureId;
            return this;
        }

        public OrchestrationTargetResponseBuilder awaitResponse(boolean awaitResponse) {
            this.awaitResponse = awaitResponse;
            return this;
        }

        public OrchestrationTargetResponseBuilder timeoutMs(Long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public OrchestrationTargetResponseBuilder retryPolicy(RetryPolicyResponse retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public OrchestrationTargetResponseBuilder errorStrategy(OrchestrationTarget.ErrorStrategy errorStrategy) {
            this.errorStrategy = errorStrategy;
            return this;
        }

        public OrchestrationTargetResponseBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public OrchestrationTargetResponseBuilder configuration(Map<String, Object> configuration) {
            this.configuration = configuration;
            return this;
        }

        public OrchestrationTargetResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public OrchestrationTargetResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OrchestrationTargetResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public OrchestrationTargetResponseBuilder mappingCount(Long mappingCount) {
            this.mappingCount = mappingCount;
            return this;
        }

        public OrchestrationTargetResponse build() {
            OrchestrationTargetResponse result = new OrchestrationTargetResponse();
            result.id = this.id;
            result.flowId = this.flowId;
            result.targetAdapter = this.targetAdapter;
            result.executionOrder = this.executionOrder;
            result.parallel = this.parallel;
            result.routingCondition = this.routingCondition;
            result.conditionType = this.conditionType;
            result.structureId = this.structureId;
            result.responseStructureId = this.responseStructureId;
            result.awaitResponse = this.awaitResponse;
            result.timeoutMs = this.timeoutMs;
            result.retryPolicy = this.retryPolicy;
            result.errorStrategy = this.errorStrategy;
            result.active = this.active;
            result.configuration = this.configuration;
            result.description = this.description;
            result.createdAt = this.createdAt;
            result.updatedAt = this.updatedAt;
            result.mappingCount = this.mappingCount;
            return result;
        }
    }

}