package com.integrixs.backend.jobs;

/**
 * Status of a background job
 */
public enum JobStatus {
    PENDING("Pending", "Job is waiting to be processed"),
    RUNNING("Running", "Job is currently being processed"),
    COMPLETED("Completed", "Job completed successfully"),
    FAILED("Failed", "Job failed with error"),
    CANCELLED("Cancelled", "Job was cancelled by user"),
    RETRYING("Retrying", "Job is being retried after failure");

    private final String displayName;
    private final String description;

    JobStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean isActive() {
        return this == RUNNING || this == RETRYING;
    }
}
