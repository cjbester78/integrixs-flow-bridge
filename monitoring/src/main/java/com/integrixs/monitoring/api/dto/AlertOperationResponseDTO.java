package com.integrixs.monitoring.api.dto;


/**
 * DTO for alert operation response
 */
public class AlertOperationResponseDTO {
    private boolean success;
    private String alertId;
    private String status;
    private String message;
    private String errorMessage;

    // Constructors
    public AlertOperationResponseDTO() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AlertOperationResponseDTO dto = new AlertOperationResponseDTO();

        public Builder success(boolean success) {
            dto.success = success;
            return this;
        }

        public Builder alertId(String alertId) {
            dto.alertId = alertId;
            return this;
        }

        public Builder status(String status) {
            dto.status = status;
            return this;
        }

        public Builder message(String message) {
            dto.message = message;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            dto.errorMessage = errorMessage;
            return this;
        }

        public AlertOperationResponseDTO build() {
            return dto;
        }
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getAlertId() {
        return alertId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
