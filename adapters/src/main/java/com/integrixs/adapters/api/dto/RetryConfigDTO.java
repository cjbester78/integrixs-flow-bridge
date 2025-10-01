package com.integrixs.adapters.api.dto;

/**
 * DTO for retry configuration
 */
public class RetryConfigDTO {
    private boolean enabled = false;
    private int maxAttempts = 3;
    private long initialDelay = 1000;
    private double backoffMultiplier = 2.0;
    private long maxDelay = 60000;
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
}
