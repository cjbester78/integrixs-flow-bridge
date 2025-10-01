package com.integrixs.backend.plugin.api;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Result of a send operation
 */
public class SendResult {

    /**
     * Whether the send was successful
     */
    private boolean successful;

    /**
     * Message ID assigned by the external system
     */
    private String externalMessageId;

    /**
     * Original message ID
     */
    private String messageId;

    /**
     * Status message
     */
    private String message;

    /**
     * Error details if failed
     */
    private String errorDetails;

    /**
     * Timestamp when sent
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Response from external system
     */
    private Object response;

    /**
     * Response headers/metadata
     */
    private Map<String, String> responseHeaders;

    /**
     * Additional details
     */
    private Map<String, Object> details;

    // Default constructor
    public SendResult() {}

    // All-args constructor
    public SendResult(boolean successful, String externalMessageId, String messageId,
                      String message, String errorDetails, LocalDateTime timestamp,
                      Object response, Map<String, String> responseHeaders,
                      Map<String, Object> details) {
        this.successful = successful;
        this.externalMessageId = externalMessageId;
        this.messageId = messageId;
        this.message = message;
        this.errorDetails = errorDetails;
        this.timestamp = timestamp;
        this.response = response;
        this.responseHeaders = responseHeaders;
        this.details = details;
    }

    /**
     * Static factory for success
     */
    public static SendResult success(String messageId, String externalMessageId) {
        return SendResult.builder()
                .successful(true)
                .messageId(messageId)
                .externalMessageId(externalMessageId)
                .message("Message sent successfully")
                .build();
    }

    /**
     * Static factory for failure
     */
    public static SendResult failure(String messageId, String error) {
        return SendResult.builder()
                .successful(false)
                .messageId(messageId)
                .message("Failed to send message")
                .errorDetails(error)
                .build();
    }

    // Getters and Setters
    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getExternalMessageId() {
        return externalMessageId;
    }

    public void setExternalMessageId(String externalMessageId) {
        this.externalMessageId = externalMessageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    // Convenience method for backward compatibility
    public String getError() {
        return errorDetails;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    // Builder pattern
    public static SendResultBuilder builder() {
        return new SendResultBuilder();
    }

    public static class SendResultBuilder {
        private boolean successful;
        private String externalMessageId;
        private String messageId;
        private String message;
        private String errorDetails;
        private LocalDateTime timestamp = LocalDateTime.now();
        private Object response;
        private Map<String, String> responseHeaders;
        private Map<String, Object> details;

        public SendResultBuilder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public SendResultBuilder externalMessageId(String externalMessageId) {
            this.externalMessageId = externalMessageId;
            return this;
        }

        public SendResultBuilder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public SendResultBuilder message(String message) {
            this.message = message;
            return this;
        }

        public SendResultBuilder errorDetails(String errorDetails) {
            this.errorDetails = errorDetails;
            return this;
        }

        public SendResultBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SendResultBuilder response(Object response) {
            this.response = response;
            return this;
        }

        public SendResultBuilder responseHeaders(Map<String, String> responseHeaders) {
            this.responseHeaders = responseHeaders;
            return this;
        }

        public SendResultBuilder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public SendResult build() {
            return new SendResult(successful, externalMessageId, messageId,
                                  message, errorDetails, timestamp,
                                  response, responseHeaders, details);
        }
    }
}
