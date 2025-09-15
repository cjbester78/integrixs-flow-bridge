package com.integrixs.data.model;

import jakarta.persistence.*;

/**
 * Entity representing a retry policy for error handling
 */
@Entity
@Table(name = "retry_policies")
public class RetryPolicy extends BaseEntity {


    @Column(name = "policy_name", unique = true, nullable = false)
    private String policyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id")
    private IntegrationFlow flow;

    @Column(name = "error_type")
    @Enumerated(EnumType.STRING)
    private ErrorRecord.ErrorType errorType;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 3;

    @Column(name = "initial_interval_ms", nullable = false)
    private Long initialIntervalMs = 1000L;

    @Column(name = "multiplier", nullable = false)
    private Double multiplier = 2.0;

    @Column(name = "max_interval_ms", nullable = false)
    private Long maxIntervalMs = 60000L;

    @Column(name = "retry_strategy", nullable = false)
    @Enumerated(EnumType.STRING)
    private RetryStrategy retryStrategy = RetryStrategy.EXPONENTIAL_BACKOFF;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "retry_on_errors", columnDefinition = "TEXT")
    private String retryOnErrors;

    @Column(name = "skip_on_errors", columnDefinition = "TEXT")
    private String skipOnErrors;

    @Column(columnDefinition = "TEXT")
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
