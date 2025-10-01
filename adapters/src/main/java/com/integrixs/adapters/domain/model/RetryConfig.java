package com.integrixs.adapters.domain.model;

/**
 * Domain model for retry configuration
 */
public class RetryConfig {
    private boolean enabled = false;
    private int maxAttempts = 3;
    private long initialDelay = 1000; // milliseconds
    private double backoffMultiplier = 2.0;
    private long maxDelay = 60000; // milliseconds
    private boolean retryOnTimeout = true;
    private boolean retryOnConnectionError = true;

    /**
     * Calculate delay for retry attempt
     * @param attempt Current attempt number(starting from 1)
     * @return Delay in milliseconds
     */
    public long calculateDelay(int attempt) {
        if(attempt <= 1) {
            return initialDelay;
        }

        long delay = (long) (initialDelay * Math.pow(backoffMultiplier, attempt - 1));
        return Math.min(delay, maxDelay);
    }
    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public int getMaxAttempts() {
        return maxAttempts;
    }
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    public long getInitialDelay() {
        return initialDelay;
    }
    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }
    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }
    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }
    public long getMaxDelay() {
        return maxDelay;
    }
    public void setMaxDelay(long maxDelay) {
        this.maxDelay = maxDelay;
    }
    public boolean isRetryOnTimeout() {
        return retryOnTimeout;
    }
    public void setRetryOnTimeout(boolean retryOnTimeout) {
        this.retryOnTimeout = retryOnTimeout;
    }
    public boolean isRetryOnConnectionError() {
        return retryOnConnectionError;
    }
    public void setRetryOnConnectionError(boolean retryOnConnectionError) {
        this.retryOnConnectionError = retryOnConnectionError;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean enabled;
        private int maxAttempts;
        private long initialDelay;
        private double backoffMultiplier;
        private long maxDelay;
        private boolean retryOnTimeout;
        private boolean retryOnConnectionError;

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder initialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        public Builder backoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        public Builder maxDelay(long maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        public Builder retryOnTimeout(boolean retryOnTimeout) {
            this.retryOnTimeout = retryOnTimeout;
            return this;
        }

        public Builder retryOnConnectionError(boolean retryOnConnectionError) {
            this.retryOnConnectionError = retryOnConnectionError;
            return this;
        }

        public RetryConfig build() {
            RetryConfig obj = new RetryConfig();
            obj.enabled = this.enabled;
            obj.maxAttempts = this.maxAttempts;
            obj.initialDelay = this.initialDelay;
            obj.backoffMultiplier = this.backoffMultiplier;
            obj.maxDelay = this.maxDelay;
            obj.retryOnTimeout = this.retryOnTimeout;
            obj.retryOnConnectionError = this.retryOnConnectionError;
            return obj;
        }
    }
}
