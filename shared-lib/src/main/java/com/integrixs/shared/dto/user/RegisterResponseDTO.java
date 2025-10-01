package com.integrixs.shared.dto.user;

/**
 * DTO for RegisterResponseDTO.
 * Encapsulates data for transport between layers.
 */
public class RegisterResponseDTO {
    private boolean success;
    private String message;
    private String userId;

    public RegisterResponseDTO() {}

    public RegisterResponseDTO(boolean success, String message, String userId) {
        this.success = success;
        this.message = message;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public static RegisterResponseDTOBuilder builder() {
        return new RegisterResponseDTOBuilder();
    }

    public static class RegisterResponseDTOBuilder {
        private boolean success;
        private String message;
        private String userId;

        public RegisterResponseDTOBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public RegisterResponseDTOBuilder message(String message) {
            this.message = message;
            return this;
        }

        public RegisterResponseDTOBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public RegisterResponseDTO build() {
            return new RegisterResponseDTO(success, message, userId);
        }
    }
}
