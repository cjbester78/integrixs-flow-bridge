package com.integrixs.backend.api.dto.request;

import com.integrixs.data.model.OrchestrationTarget;
import jakarta.validation.constraints.*;
import java.util.Map;

/**
 * Request DTO for updating an orchestration target
 */
public class UpdateOrchestrationTargetRequest {

    @Min(value = 0, message = "Execution order must be non - negative")
    private Integer executionOrder;

    private Boolean parallel;

    private String routingCondition;

    private OrchestrationTarget.ConditionType conditionType;

    private String structureId;

    private String responseStructureId;

    private Boolean awaitResponse;

    @Min(value = 0, message = "Timeout must be non - negative")
    @Max(value = 3600000, message = "Timeout cannot exceed 1 hour")
    private Long timeoutMs;

    private RetryPolicyDto retryPolicy;

    private OrchestrationTarget.ErrorStrategy errorStrategy;

    private Map<String, Object> configuration;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Retry policy DTO
     */
    public static class RetryPolicyDto {

        @Min(0)
        @Max(10)
        private Integer maxAttempts;

        @Min(0)
        @Max(300000)
        private Long retryDelayMs;

        @Min(1)
        @Max(10)
        private Double backoffMultiplier;

        @Min(0)
        @Max(3600000)
        private Long maxRetryDelayMs;

        private String retryOnErrors;

        // Default constructor
        public RetryPolicyDto() {
        }

        // Getters and Setters
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
    }

    // Default constructor
    public UpdateOrchestrationTargetRequest() {
    }

    public Integer getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(Integer executionOrder) {
        this.executionOrder = executionOrder;
    }

    public Boolean getParallel() {
        return parallel;
    }

    public void setParallel(Boolean parallel) {
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

    public Boolean getAwaitResponse() {
        return awaitResponse;
    }

    public void setAwaitResponse(Boolean awaitResponse) {
        this.awaitResponse = awaitResponse;
    }

    public Long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public RetryPolicyDto getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicyDto retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public OrchestrationTarget.ErrorStrategy getErrorStrategy() {
        return errorStrategy;
    }

    public void setErrorStrategy(OrchestrationTarget.ErrorStrategy errorStrategy) {
        this.errorStrategy = errorStrategy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
}
