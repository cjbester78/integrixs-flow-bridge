package com.integrixs.adapters.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * Domain model for retry configuration
 */
@Data
@Builder
public class RetryConfig {
    @Builder.Default
    private boolean enabled = false;
    @Builder.Default
    private int maxAttempts = 3;
    @Builder.Default
    private long initialDelay = 1000; // milliseconds
    @Builder.Default
    private double backoffMultiplier = 2.0;
    @Builder.Default
    private long maxDelay = 60000; // milliseconds
    @Builder.Default
    private boolean retryOnTimeout = true;
    @Builder.Default
    private boolean retryOnConnectionError = true;
    
    /**
     * Calculate delay for retry attempt
     * @param attempt Current attempt number (starting from 1)
     * @return Delay in milliseconds
     */
    public long calculateDelay(int attempt) {
        if (attempt <= 1) {
            return initialDelay;
        }
        
        long delay = (long) (initialDelay * Math.pow(backoffMultiplier, attempt - 1));
        return Math.min(delay, maxDelay);
    }
}