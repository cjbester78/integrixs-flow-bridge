package com.integrixs.soapbindings.api.dto;

import java.time.LocalDateTime;

/**
 * DTO for SOAP operation response
 */
public class SoapOperationResponseDTO {

    private String operationId;
    private boolean success;
    private Object response;
    private String errorMessage;
    private LocalDateTime timestamp;

    // Default constructor
    public SoapOperationResponseDTO() {
    }

    // All args constructor
    public SoapOperationResponseDTO(String operationId, boolean success, Object response, String errorMessage, LocalDateTime timestamp) {
        this.operationId = operationId;
        this.success = success;
        this.response = response;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
    }

    // Getters
    public String getOperationId() { return operationId; }
    public boolean isSuccess() { return success; }
    public Object getResponse() { return response; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Setters
    public void setOperationId(String operationId) { this.operationId = operationId; }
    public void setSuccess(boolean success) { this.success = success; }
    public void setResponse(Object response) { this.response = response; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // Builder
    public static SoapOperationResponseDTOBuilder builder() {
        return new SoapOperationResponseDTOBuilder();
    }

    public static class SoapOperationResponseDTOBuilder {
        private String operationId;
        private boolean success;
        private Object response;
        private String errorMessage;
        private LocalDateTime timestamp;

        public SoapOperationResponseDTOBuilder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public SoapOperationResponseDTOBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public SoapOperationResponseDTOBuilder response(Object response) {
            this.response = response;
            return this;
        }

        public SoapOperationResponseDTOBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public SoapOperationResponseDTOBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SoapOperationResponseDTO build() {
            return new SoapOperationResponseDTO(operationId, success, response, errorMessage, timestamp);
        }
    }
}
