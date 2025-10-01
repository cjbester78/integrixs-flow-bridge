package com.integrixs.shared.services;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for rate limiting API calls
 */
public interface RateLimiterService {

    /**
     * Execute a callable with rate limiting
     * @param rateLimiterName The name of the rate limiter to use
     * @param callable The callable to execute
     * @return The result of the callable
     * @throws Exception if the callable throws an exception
     */
    <T> T executeWithRateLimit(String rateLimiterName, Callable<T> callable) throws Exception;

    /**
     * Execute a callable asynchronously with rate limiting
     * @param rateLimiterName The name of the rate limiter to use
     * @param callable The callable to execute
     * @return A CompletableFuture with the result
     */
    <T> CompletableFuture<T> executeWithRateLimitAsync(String rateLimiterName, Callable<T> callable);

    /**
     * Check if a rate limiter allows execution
     * @param rateLimiterName The name of the rate limiter
     * @return true if execution is allowed
     */
    boolean tryAcquirePermission(String rateLimiterName);

    /**
     * Check if a rate limiter allows execution with a timeout
     * @param rateLimiterName The name of the rate limiter
     * @param timeoutMs Timeout in milliseconds
     * @return true if execution is allowed
     */
    boolean tryAcquirePermission(String rateLimiterName, long timeoutMs);

    /**
     * Get the current rate limit status
     * @param rateLimiterName The name of the rate limiter
     * @return RateLimitResult with current status
     */
    RateLimitResult getRateLimitStatus(String rateLimiterName);

    /**
     * Acquire a permit from the rate limiter, blocking if necessary
     * @param rateLimiterName The name of the rate limiter
     * @param permits The number of permits to acquire
     */
    void acquire(String rateLimiterName, int permits);

    /**
     * Result class for rate limit status
     */
    class RateLimitResult {
        private final boolean allowed;
        private final long waitTimeMs;
        private final int availablePermits;

        public RateLimitResult(boolean allowed, long waitTimeMs, int availablePermits) {
            this.allowed = allowed;
            this.waitTimeMs = waitTimeMs;
            this.availablePermits = availablePermits;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public long getWaitTimeMs() {
            return waitTimeMs;
        }

        public int getAvailablePermits() {
            return availablePermits;
        }
    }
}
