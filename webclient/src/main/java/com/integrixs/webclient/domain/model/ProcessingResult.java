package com.integrixs.webclient.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing the result of processing an inbound message
 */
@Data
@Builder
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
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

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
}
