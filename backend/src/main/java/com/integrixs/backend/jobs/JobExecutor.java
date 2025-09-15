package com.integrixs.backend.jobs;

import java.util.Map;

/**
 * Interface for job executors
 */
public interface JobExecutor {

    /**
     * Get the job type this executor handles
     */
    String getJobType();

    /**
     * Execute the job
     * @param job The job to execute
     * @param progressCallback Callback to report progress
     * @return Results of the job execution
     */
    Map<String, String> execute(BackgroundJob job, ProgressCallback progressCallback) throws Exception;

    /**
     * Validate job parameters before execution
     */
    default void validateParameters(Map<String, String> parameters) throws IllegalArgumentException {
        // Default implementation - no validation
    }

    /**
     * Get estimated duration in milliseconds
     */
    default Long getEstimatedDuration() {
        return null;
    }

    /**
     * Check if job can be retried after failure
     */
    default boolean isRetryable() {
        return true;
    }

    /**
     * Get retry delay in milliseconds
     */
    default long getRetryDelay(int attemptNumber) {
        // Exponential backoff: 1s, 2s, 4s, 8s...
        return Math.min(1000L * (1L << attemptNumber), 60000L); // Max 1 minute
    }

    /**
     * Callback interface for progress updates
     */
    @FunctionalInterface
    interface ProgressCallback {
        void updateProgress(int progress, String currentStep);
    }
}
