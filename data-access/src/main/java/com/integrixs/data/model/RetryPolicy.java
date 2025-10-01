package com.integrixs.data.model;

/**
 * Entity representing a retry policy for error handling
 */
public class RetryPolicy extends BaseEntity {

    private String policyName;

    private IntegrationFlow flow;

    private ErrorRecord.ErrorType errorType;

    private Integer maxAttempts = 3;

    private Long initialIntervalMs = 1000L;

    private Double multiplier = 2.0;

    private Long maxIntervalMs = 60000L;

    private RetryStrategy retryStrategy = RetryStrategy.EXPONENTIAL_BACKOFF;

    private boolean active = true;

    private String retryOnErrors;

    private String skipOnErrors;

    private String description;

    public enum RetryStrategy {
        FIXED_DELAY,
        EXPONENTIAL_BACKOFF,
        LINEAR_BACKOFF,
        RANDOM_JITTER
    }

    // Getters and setters

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public IntegrationFlow getFlow() {
        return flow;
    }

    public void setFlow(IntegrationFlow flow) {
        this.flow = flow;
    }

    public ErrorRecord.ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorRecord.ErrorType errorType) {
        this.errorType = errorType;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Long getInitialIntervalMs() {
        return initialIntervalMs;
    }

    public void setInitialIntervalMs(Long initialIntervalMs) {
        this.initialIntervalMs = initialIntervalMs;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public Long getMaxIntervalMs() {
        return maxIntervalMs;
    }

    public void setMaxIntervalMs(Long maxIntervalMs) {
        this.maxIntervalMs = maxIntervalMs;
    }

    public RetryStrategy getRetryStrategy() {
        return retryStrategy;
    }

    public void setRetryStrategy(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRetryOnErrors() {
        return retryOnErrors;
    }

    public void setRetryOnErrors(String retryOnErrors) {
        this.retryOnErrors = retryOnErrors;
    }

    public String getSkipOnErrors() {
        return skipOnErrors;
    }

    public void setSkipOnErrors(String skipOnErrors) {
        this.skipOnErrors = skipOnErrors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Additional helper methods for ErrorHandlingService
    public int getMaxRetries() {
        return maxAttempts != null ? maxAttempts : 3;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxAttempts = maxRetries;
    }

    public long getRetryDelayMs() {
        return initialIntervalMs != null ? initialIntervalMs : 1000L;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.initialIntervalMs = retryDelayMs;
    }
}
