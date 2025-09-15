package com.integrixs.shared.enums;

/**
 * Enum representing the status of a message in the integration flow
 */
public enum MessageStatus {
    /**
     * Message created but not yet processed
     */
    NEW,

    /**
     * Message is being processed
     */
    PROCESSING,

    /**
     * Message processed successfully
     */
    SUCCESS,

    /**
     * Message processing failed
     */
    FAILED,

    /**
     * Message processing resulted in a warning but continued
     */
    WARNING,

    /**
     * Message is queued for retry
     */
    RETRY,

    /**
     * Message processing was cancelled
     */
    CANCELLED,

    /**
     * Message processing timed out
     */
    TIMEOUT,

    /**
     * Message was skipped due to filtering or conditions
     */
    SKIPPED,

    /**
     * Message is parked for manual intervention
     */
    PARKED;

    /**
     * Check if the status indicates completion(success or failure)
     */
    public boolean isComplete() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == TIMEOUT;
    }

    /**
     * Check if the status indicates an error state
     */
    public boolean isError() {
        return this == FAILED || this == TIMEOUT;
    }

    /**
     * Check if the status indicates the message can be retried
     */
    public boolean canRetry() {
        return this == FAILED || this == TIMEOUT;
    }
}
