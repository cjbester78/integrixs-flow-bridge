package com.integrixs.soapbindings.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model for SOAP operation response
 */
public class SoapOperationResponse {

    private String operationId;
    private boolean success;
    private Object response;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Default constructor
    public SoapOperationResponse() {
    }

    // All args constructor
    public SoapOperationResponse(String operationId, boolean success, Object response, String errorMessage, LocalDateTime startTime, LocalDateTime endTime) {
        this.operationId = operationId;
        this.success = success;
        this.response = response;
        this.errorMessage = errorMessage;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public String getOperationId() { return operationId; }
    public boolean isSuccess() { return success; }
    public Object getResponse() { return response; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    // Setters
    public void setOperationId(String operationId) { this.operationId = operationId; }
    public void setSuccess(boolean success) { this.success = success; }
    public void setResponse(Object response) { this.response = response; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    // Builder
    public static SoapOperationResponseBuilder builder() {
        return new SoapOperationResponseBuilder();
    }

    public static class SoapOperationResponseBuilder {
        private String operationId;
        private boolean success;
        private Object response;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public SoapOperationResponseBuilder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public SoapOperationResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public SoapOperationResponseBuilder response(Object response) {
            this.response = response;
            return this;
        }

        public SoapOperationResponseBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public SoapOperationResponseBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public SoapOperationResponseBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public SoapOperationResponse build() {
            return new SoapOperationResponse(operationId, success, response, errorMessage, startTime, endTime);
        }
    }
}
