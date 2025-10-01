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

    // Getters and Setters
    public int getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<SendResult> getResults() {
        return results;
    }

    public void setResults(List<SendResult> results) {
        this.results = results;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder pattern
    public static BatchSendResultBuilder builder() {
        return new BatchSendResultBuilder();
    }

    public static class BatchSendResultBuilder {
        private int totalMessages;
        private int successCount;
        private int failureCount;
        private List<SendResult> results;
        private String batchId;
        private String message;
        private Map<String, Object> metadata;

        public BatchSendResultBuilder totalMessages(int totalMessages) {
            this.totalMessages = totalMessages;
            return this;
        }

        public BatchSendResultBuilder successCount(int successCount) {
            this.successCount = successCount;
            return this;
        }

        public BatchSendResultBuilder failureCount(int failureCount) {
            this.failureCount = failureCount;
            return this;
        }

        public BatchSendResultBuilder results(List<SendResult> results) {
            this.results = results;
            return this;
        }

        public BatchSendResultBuilder batchId(String batchId) {
            this.batchId = batchId;
            return this;
        }

        public BatchSendResultBuilder message(String message) {
            this.message = message;
            return this;
        }

        public BatchSendResultBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public BatchSendResult build() {
            BatchSendResult result = new BatchSendResult();
            result.totalMessages = this.totalMessages;
            result.successCount = this.successCount;
            result.failureCount = this.failureCount;
            result.results = this.results;
            result.batchId = this.batchId;
            result.message = this.message;
            result.metadata = this.metadata;
            return result;
        }
    }
}
