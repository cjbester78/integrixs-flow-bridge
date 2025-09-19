package com.integrixs.backend.plugin.api;

import java.util.List;
import java.util.Map;

/**
 * Result of a batch send operation
 */
public class BatchSendResult {

    /**
     * Total messages in batch
     */
    private int totalMessages;

    /**
     * Number of successful sends
     */
    private int successCount;

    /**
     * Number of failed sends
     */
    private int failureCount;

    /**
     * Individual results per message
     */
    private List<SendResult> results;

    /**
     * Batch ID if assigned by external system
     */
    private String batchId;

    /**
     * Overall status message
     */
    private String message;

    /**
     * Additional batch metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if all messages were sent successfully
     */
    public boolean isCompleteSuccess() {
        return successCount == totalMessages && failureCount == 0;
    }

    /**
     * Check if all messages failed
     */
    public boolean isCompleteFailure() {
        return failureCount == totalMessages && successCount == 0;
    }

    /**
     * Check if batch had partial success
     */
    public boolean isPartialSuccess() {
        return successCount > 0 && failureCount > 0;
    }
}
