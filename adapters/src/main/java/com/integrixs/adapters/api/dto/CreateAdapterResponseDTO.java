package com.integrixs.adapters.api.dto;

/**
 * DTO for create adapter response
 */
public class CreateAdapterResponseDTO {
    public CreateAdapterResponseDTO() {
    }


    private String adapterId;
    private boolean success;
    private String message;
    private String errorMessage;
    // Getters and Setters
    public String getAdapterId() {
        return adapterId;
    }
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String adapterId;
        private boolean success;
        private String message;
        private String errorMessage;

        public Builder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public CreateAdapterResponseDTO build() {
            CreateAdapterResponseDTO obj = new CreateAdapterResponseDTO();
            obj.adapterId = this.adapterId;
            obj.success = this.success;
            obj.message = this.message;
            obj.errorMessage = this.errorMessage;
            return obj;
        }
    }
}
