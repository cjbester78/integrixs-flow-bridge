package com.integrixs.webserver.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing a response from external services
 */
public class OutboundResponse {
    private String requestId;
    private boolean success;
    private int statusCode;
    private String statusMessage;
    private Object responseBody;
    private String contentType;
    private Map<String, String> headers = new HashMap<>();
    private LocalDateTime timestamp;
    private long responseTimeMillis;
    private String errorMessage;
    private String errorCode;
    private RetryInfo retryInfo;
    private int attemptCount;
    private boolean wasRetried;
    private String lastRetryReason;
    private LocalDateTime lastRetryTime;

    /**
     * Retry information
     */
    public static class RetryInfo {
        private int attemptCount;
        private boolean wasRetried;
        private String lastRetryReason;
        private LocalDateTime lastRetryTime;

        // Default constructor
        public RetryInfo() {
        }

        // All args constructor
        public RetryInfo(int attemptCount, boolean wasRetried, String lastRetryReason, LocalDateTime lastRetryTime) {
            this.attemptCount = attemptCount;
            this.wasRetried = wasRetried;
            this.lastRetryReason = lastRetryReason;
            this.lastRetryTime = lastRetryTime;
        }

        // Getters
        public int getAttemptCount() {
            return attemptCount;
        }

        public boolean isWasRetried() {
            return wasRetried;
        }

        public String getLastRetryReason() {
            return lastRetryReason;
        }

        public LocalDateTime getLastRetryTime() {
            return lastRetryTime;
        }

        // Setters
        public void setAttemptCount(int attemptCount) {
            this.attemptCount = attemptCount;
        }

        public void setWasRetried(boolean wasRetried) {
            this.wasRetried = wasRetried;
        }

        public void setLastRetryReason(String lastRetryReason) {
            this.lastRetryReason = lastRetryReason;
        }

        public void setLastRetryTime(LocalDateTime lastRetryTime) {
            this.lastRetryTime = lastRetryTime;
        }

        // Builder
        public static RetryInfoBuilder builder() {
            return new RetryInfoBuilder();
        }

        public static class RetryInfoBuilder {
            private int attemptCount;
            private boolean wasRetried;
            private String lastRetryReason;
            private LocalDateTime lastRetryTime;

            public RetryInfoBuilder attemptCount(int attemptCount) {
                this.attemptCount = attemptCount;
                return this;
            }

            public RetryInfoBuilder wasRetried(boolean wasRetried) {
                this.wasRetried = wasRetried;
                return this;
            }

            public RetryInfoBuilder lastRetryReason(String lastRetryReason) {
                this.lastRetryReason = lastRetryReason;
                return this;
            }

            public RetryInfoBuilder lastRetryTime(LocalDateTime lastRetryTime) {
                this.lastRetryTime = lastRetryTime;
                return this;
            }

            public RetryInfo build() {
                return new RetryInfo(attemptCount, wasRetried, lastRetryReason, lastRetryTime);
            }
        }
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
     * Check if response is successful(2xx status)
     * @return true if successful
     */
    public boolean isSuccessful() {
        return success && statusCode >= 200 && statusCode < 300;
    }

    // Default constructor
    public OutboundResponse() {
    }

    // All args constructor
    public OutboundResponse(String requestId, boolean success, int statusCode, String statusMessage, Object responseBody, String contentType, LocalDateTime timestamp, long responseTimeMillis, String errorMessage, String errorCode, RetryInfo retryInfo, int attemptCount, boolean wasRetried, String lastRetryReason, LocalDateTime lastRetryTime) {
        this.requestId = requestId;
        this.success = success;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.responseBody = responseBody;
        this.contentType = contentType;
        this.timestamp = timestamp;
        this.responseTimeMillis = responseTimeMillis;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.retryInfo = retryInfo;
        this.attemptCount = attemptCount;
        this.wasRetried = wasRetried;
        this.lastRetryReason = lastRetryReason;
        this.lastRetryTime = lastRetryTime;
    }

    // Getters
    public String getRequestId() {
        return requestId;
    }
    public boolean isSuccess() {
        return success;
    }
    public int getStatusCode() {
        return statusCode;
    }
    public String getStatusMessage() {
        return statusMessage;
    }
    public Object getResponseBody() {
        return responseBody;
    }
    public String getContentType() {
        return contentType;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public long getResponseTimeMillis() {
        return responseTimeMillis;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public String getErrorCode() {
        return errorCode;
    }
    public RetryInfo getRetryInfo() {
        return retryInfo;
    }
    public int getAttemptCount() {
        return attemptCount;
    }
    public boolean isWasRetried() {
        return wasRetried;
    }
    public String getLastRetryReason() {
        return lastRetryReason;
    }
    public LocalDateTime getLastRetryTime() {
        return lastRetryTime;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    // Setters
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    public void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public void setResponseTimeMillis(long responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    public void setRetryInfo(RetryInfo retryInfo) {
        this.retryInfo = retryInfo;
    }
    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }
    public void setWasRetried(boolean wasRetried) {
        this.wasRetried = wasRetried;
    }
    public void setLastRetryReason(String lastRetryReason) {
        this.lastRetryReason = lastRetryReason;
    }
    public void setLastRetryTime(LocalDateTime lastRetryTime) {
        this.lastRetryTime = lastRetryTime;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    // Builder
    public static OutboundResponseBuilder builder() {
        return new OutboundResponseBuilder();
    }

    public static class OutboundResponseBuilder {
        private String requestId;
        private boolean success;
        private int statusCode;
        private String statusMessage;
        private Object responseBody;
        private String contentType;
        private LocalDateTime timestamp;
        private long responseTimeMillis;
        private String errorMessage;
        private String errorCode;
        private RetryInfo retryInfo;
        private int attemptCount;
        private boolean wasRetried;
        private String lastRetryReason;
        private LocalDateTime lastRetryTime;

        public OutboundResponseBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public OutboundResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public OutboundResponseBuilder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public OutboundResponseBuilder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public OutboundResponseBuilder responseBody(Object responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public OutboundResponseBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public OutboundResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public OutboundResponseBuilder responseTimeMillis(long responseTimeMillis) {
            this.responseTimeMillis = responseTimeMillis;
            return this;
        }

        public OutboundResponseBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public OutboundResponseBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public OutboundResponseBuilder retryInfo(RetryInfo retryInfo) {
            this.retryInfo = retryInfo;
            return this;
        }

        public OutboundResponseBuilder attemptCount(int attemptCount) {
            this.attemptCount = attemptCount;
            return this;
        }

        public OutboundResponseBuilder wasRetried(boolean wasRetried) {
            this.wasRetried = wasRetried;
            return this;
        }

        public OutboundResponseBuilder lastRetryReason(String lastRetryReason) {
            this.lastRetryReason = lastRetryReason;
            return this;
        }

        public OutboundResponseBuilder lastRetryTime(LocalDateTime lastRetryTime) {
            this.lastRetryTime = lastRetryTime;
            return this;
        }

        public OutboundResponse build() {
            return new OutboundResponse(requestId, success, statusCode, statusMessage, responseBody, contentType, timestamp, responseTimeMillis, errorMessage, errorCode, retryInfo, attemptCount, wasRetried, lastRetryReason, lastRetryTime);
        }
    }}
