package com.integrixs.webserver.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing a response from external services
 */
@Data
@Builder
public class OutboundResponse {
    private String requestId;
    private boolean success;
    private int statusCode;
    private String statusMessage;
    private Object responseBody;
    private String contentType;
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    private LocalDateTime timestamp;
    private long responseTimeMillis;
    private String errorMessage;
    private String errorCode;
    private RetryInfo retryInfo;
    
    /**
     * Retry information
     */
    @Data
    @Builder
    public static class RetryInfo {
        private int attemptCount;
        private boolean wasRetried;
        private String lastRetryReason;
        private LocalDateTime lastRetryTime;
    }
    
    /**
     * Create success response
     * @param requestId Request ID
     * @param statusCode HTTP status code
     * @param responseBody Response body
     * @return Success response
     */
    public static OutboundResponse success(String requestId, int statusCode, Object responseBody) {
        return OutboundResponse.builder()
                .requestId(requestId)
                .success(true)
                .statusCode(statusCode)
                .statusMessage("Success")
                .responseBody(responseBody)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Create failure response
     * @param requestId Request ID
     * @param statusCode HTTP status code
     * @param errorMessage Error message
     * @return Failure response
     */
    public static OutboundResponse failure(String requestId, int statusCode, String errorMessage) {
        return OutboundResponse.builder()
                .requestId(requestId)
                .success(false)
                .statusCode(statusCode)
                .statusMessage("Failed")
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Create timeout response
     * @param requestId Request ID
     * @param errorMessage Error message
     * @return Timeout response
     */
    public static OutboundResponse timeout(String requestId, String errorMessage) {
        return OutboundResponse.builder()
                .requestId(requestId)
                .success(false)
                .statusCode(-1)
                .statusMessage("Timeout")
                .errorMessage(errorMessage)
                .errorCode("TIMEOUT")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Add response time
     * @param startTime Start time in milliseconds
     * @return This response
     */
    public OutboundResponse withResponseTime(long startTime) {
        this.responseTimeMillis = System.currentTimeMillis() - startTime;
        return this;
    }
    
    /**
     * Add retry info
     * @param retryInfo Retry information
     * @return This response
     */
    public OutboundResponse withRetryInfo(RetryInfo retryInfo) {
        this.retryInfo = retryInfo;
        return this;
    }
    
    /**
     * Check if response is successful (2xx status)
     * @return true if successful
     */
    public boolean isSuccessful() {
        return success && statusCode >= 200 && statusCode < 300;
    }
}