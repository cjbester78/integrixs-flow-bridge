package com.integrixs.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response structure for REST API errors.
 *
 * <p>This class provides a consistent error response format across all API endpoints,
 * making it easier for clients to handle errors uniformly.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Error category or type
     */
    private String error;

    /**
     * Specific error code for programmatic handling
     */
    private String errorCode;

    /**
     * Human - readable error message
     */
    private String message;

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * Additional context information about the error
     */
    private Map<String, Object> context;

    /**
     * Unique trace ID for error tracking(optional)
     */
    private String traceId;

    // Default constructor
    public ErrorResponse() {
    }

    // Getters and setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    // Builder
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public static class ErrorResponseBuilder {
        private LocalDateTime timestamp = LocalDateTime.now();
        private int status;
        private String error;
        private String errorCode;
        private String message;
        private String path;
        private Map<String, Object> context;
        private String traceId;

        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        public ErrorResponseBuilder error(String error) {
            this.error = error;
            return this;
        }

        public ErrorResponseBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorResponseBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ErrorResponseBuilder context(Map<String, Object> context) {
            this.context = context;
            return this;
        }

        public ErrorResponseBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public ErrorResponse build() {
            ErrorResponse response = new ErrorResponse();
            response.timestamp = this.timestamp;
            response.status = this.status;
            response.error = this.error;
            response.errorCode = this.errorCode;
            response.message = this.message;
            response.path = this.path;
            response.context = this.context;
            response.traceId = this.traceId;
            return response;
        }
    }
}
