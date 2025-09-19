package com.integrixs.monitoring.api.dto;


/**
 * DTO for alert rule operation response
 */
public class AlertRuleOperationResponseDTO {
    private boolean success;
    private String ruleId;
    private String message;
    private String errorMessage;

    // Constructors
    public AlertRuleOperationResponseDTO() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AlertRuleOperationResponseDTO dto = new AlertRuleOperationResponseDTO();

        public Builder success(boolean success) {
            dto.success = success;
            return this;
        }

        public Builder ruleId(String ruleId) {
            dto.ruleId = ruleId;
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

        public AlertRuleOperationResponseDTO build() {
            return dto;
        }
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getRuleId() {
        return ruleId;
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

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
