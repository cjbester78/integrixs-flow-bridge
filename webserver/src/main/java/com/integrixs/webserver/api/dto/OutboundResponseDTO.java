package com.integrixs.webserver.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for outbound response
 */
public class OutboundResponseDTO {

    private String requestId;
    private boolean success;
    private int statusCode;
    private String statusMessage;
    private Object responseBody;
    private String contentType;
    private Map<String, String> headers;
    private LocalDateTime timestamp;
    private Long responseTimeMillis;
    private String errorMessage;
    private String errorCode;
    private RetryInfoDTO retryInfo;

    // Default constructor
    public OutboundResponseDTO() {
    }

    // All args constructor
    public OutboundResponseDTO(String requestId, boolean success, int statusCode, String statusMessage, Object responseBody, String contentType, LocalDateTime timestamp, Long responseTimeMillis, String errorMessage, String errorCode, RetryInfoDTO retryInfo) {
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
    public Long getResponseTimeMillis() {
        return responseTimeMillis;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public String getErrorCode() {
        return errorCode;
    }
    public RetryInfoDTO getRetryInfo() {
        return retryInfo;
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
    public void setResponseTimeMillis(Long responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    public void setRetryInfo(RetryInfoDTO retryInfo) {
        this.retryInfo = retryInfo;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    // Builder
    public static OutboundResponseDTOBuilder builder() {
        return new OutboundResponseDTOBuilder();
    }

    public static class OutboundResponseDTOBuilder {
        private String requestId;
        private boolean success;
        private int statusCode;
        private String statusMessage;
        private Object responseBody;
        private String contentType;
        private Map<String, String> headers;
        private LocalDateTime timestamp;
        private Long responseTimeMillis;
        private String errorMessage;
        private String errorCode;
        private RetryInfoDTO retryInfo;

        public OutboundResponseDTOBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public OutboundResponseDTOBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public OutboundResponseDTOBuilder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public OutboundResponseDTOBuilder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public OutboundResponseDTOBuilder responseBody(Object responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public OutboundResponseDTOBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public OutboundResponseDTOBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public OutboundResponseDTOBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public OutboundResponseDTOBuilder responseTimeMillis(Long responseTimeMillis) {
            this.responseTimeMillis = responseTimeMillis;
            return this;
        }

        public OutboundResponseDTOBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public OutboundResponseDTOBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public OutboundResponseDTOBuilder retryInfo(RetryInfoDTO retryInfo) {
            this.retryInfo = retryInfo;
            return this;
        }

        public OutboundResponseDTO build() {
            return new OutboundResponseDTO(requestId, success, statusCode, statusMessage, responseBody, contentType, timestamp, responseTimeMillis, errorMessage, errorCode, retryInfo);
        }
    }}
