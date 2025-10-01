package com.integrixs.webserver.api.dto;

/**
 * DTO for retry configuration
 */
public class RetryConfigDTO {
    private int maxRetries = 3;
    private long retryDelayMillis = 1000;
    private boolean exponentialBackoff = true;
    private double backoffMultiplier = 2.0;

    // Default constructor
    public RetryConfigDTO() {
    }

    // All args constructor
    public RetryConfigDTO(int maxRetries, long retryDelayMillis, boolean exponentialBackoff, double backoffMultiplier) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
        this.exponentialBackoff = exponentialBackoff;
        this.backoffMultiplier = backoffMultiplier;
    }

    // Getters
    public int getMaxRetries() {
        return maxRetries;
    }
    public long getRetryDelayMillis() {
        return retryDelayMillis;
    }
    public boolean isExponentialBackoff() {
        return exponentialBackoff;
    }
    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    // Setters
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    public void setRetryDelayMillis(long retryDelayMillis) {
        this.retryDelayMillis = retryDelayMillis;
    }
    public void setExponentialBackoff(boolean exponentialBackoff) {
        this.exponentialBackoff = exponentialBackoff;
    }
    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    // Builder
    public static RetryConfigDTOBuilder builder() {
        return new RetryConfigDTOBuilder();
    }

    public static class RetryConfigDTOBuilder {
        private int maxRetries;
        private long retryDelayMillis;
        private boolean exponentialBackoff;
        private double backoffMultiplier;

        public RetryConfigDTOBuilder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public RetryConfigDTOBuilder retryDelayMillis(long retryDelayMillis) {
            this.retryDelayMillis = retryDelayMillis;
            return this;
        }

        public RetryConfigDTOBuilder exponentialBackoff(boolean exponentialBackoff) {
            this.exponentialBackoff = exponentialBackoff;
            return this;
        }

        public RetryConfigDTOBuilder backoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        public RetryConfigDTO build() {
            return new RetryConfigDTO(maxRetries, retryDelayMillis, exponentialBackoff, backoffMultiplier);
        }
    }}
