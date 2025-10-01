package com.integrixs.webclient.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing the result of processing an inbound message
 */
public class ProcessingResult {
    private String messageId;
    private boolean success;
    private String flowId;
    private String executionId;
    private Object responseData;
    private String errorMessage;
    private String errorCode;
    private LocalDateTime processedAt;
    private long processingTimeMillis;
    private Map<String, Object> metadata = new HashMap<>();

    // Empty constructor
    public ProcessingResult() {
    }

    /**
     * Create success result
     * @param messageId Message ID
     * @param flowId Flow ID
     * @param executionId Execution ID
     * @return Success result
     */
    public static ProcessingResult success(String messageId, String flowId, String executionId) {
        return ProcessingResult.builder()
                .messageId(messageId)
                .success(true)
                .flowId(flowId)
                .executionId(executionId)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create success result with response data
     * @param messageId Message ID
     * @param flowId Flow ID
     * @param executionId Execution ID
     * @param responseData Response data
     * @return Success result
     */
    public static ProcessingResult successWithData(String messageId, String flowId,
                                                  String executionId, Object responseData) {
        return ProcessingResult.builder()
                .messageId(messageId)
                .success(true)
                .flowId(flowId)
                .executionId(executionId)
                .responseData(responseData)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create failure result
     * @param messageId Message ID
     * @param errorMessage Error message
     * @return Failure result
     */
    public static ProcessingResult failure(String messageId, String errorMessage) {
        return ProcessingResult.builder()
                .messageId(messageId)
                .success(false)
                .errorMessage(errorMessage)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create failure result with error code
     * @param messageId Message ID
     * @param errorCode Error code
     * @param errorMessage Error message
     * @return Failure result
     */
    public static ProcessingResult failureWithCode(String messageId, String errorCode, String errorMessage) {
        return ProcessingResult.builder()
                .messageId(messageId)
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Add metadata
     * @param key Metadata key
     * @param value Metadata value
     * @return This result
     */
    public ProcessingResult withMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Set processing time
     * @param startTime Start time
     * @return This result
     */
    public ProcessingResult withProcessingTime(long startTime) {
        this.processingTimeMillis = System.currentTimeMillis() - startTime;
        return this;
    }

    // Getters
    public String getMessageId() {
        return messageId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFlowId() {
        return flowId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public Object getResponseData() {
        return responseData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public long getProcessingTimeMillis() {
        return processingTimeMillis;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    // Setters
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public void setProcessingTimeMillis(long processingTimeMillis) {
        this.processingTimeMillis = processingTimeMillis;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static ProcessingResultBuilder builder() {
        return new ProcessingResultBuilder();
    }

    public static class ProcessingResultBuilder {
        private String messageId;
        private boolean success;
        private String flowId;
        private String executionId;
        private Object responseData;
        private String errorMessage;
        private String errorCode;
        private LocalDateTime processedAt;
        private long processingTimeMillis;
        private Map<String, Object> metadata = new HashMap<>();

        public ProcessingResultBuilder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public ProcessingResultBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public ProcessingResultBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public ProcessingResultBuilder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public ProcessingResultBuilder responseData(Object responseData) {
            this.responseData = responseData;
            return this;
        }

        public ProcessingResultBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public ProcessingResultBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ProcessingResultBuilder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public ProcessingResultBuilder processingTimeMillis(long processingTimeMillis) {
            this.processingTimeMillis = processingTimeMillis;
            return this;
        }

        public ProcessingResultBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : new HashMap<>();
            return this;
        }

        public ProcessingResult build() {
            ProcessingResult result = new ProcessingResult();
            result.setMessageId(messageId);
            result.setSuccess(success);
            result.setFlowId(flowId);
            result.setExecutionId(executionId);
            result.setResponseData(responseData);
            result.setErrorMessage(errorMessage);
            result.setErrorCode(errorCode);
            result.setProcessedAt(processedAt);
            result.setProcessingTimeMillis(processingTimeMillis);
            result.setMetadata(metadata);
            return result;
        }
    }
}
