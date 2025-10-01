package com.integrixs.backend.plugin.dto;

/**
 * DTO for plugin initialization results
 */
public class InitializationResultDto {
    private boolean successful;
    private String pluginId;
    private String message;
    private String error;

    public static InitializationResultDto success(String pluginId) {
        return InitializationResultDto.builder()
                .successful(true)
                .pluginId(pluginId)
                .message("Plugin initialized successfully")
                .build();
    }

    public static InitializationResultDto failure(String pluginId, String error) {
        return InitializationResultDto.builder()
                .successful(false)
                .pluginId(pluginId)
                .error(error)
                .build();
    }

    // Default constructor
    public InitializationResultDto() {
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    // Builder pattern
    public static InitializationResultDtoBuilder builder() {
        return new InitializationResultDtoBuilder();
    }

    public static class InitializationResultDtoBuilder {
        private boolean successful;
        private String pluginId;
        private String message;
        private String error;

        public InitializationResultDtoBuilder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public InitializationResultDtoBuilder pluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        public InitializationResultDtoBuilder message(String message) {
            this.message = message;
            return this;
        }

        public InitializationResultDtoBuilder error(String error) {
            this.error = error;
            return this;
        }

        public InitializationResultDto build() {
            InitializationResultDto dto = new InitializationResultDto();
            dto.successful = this.successful;
            dto.pluginId = this.pluginId;
            dto.message = this.message;
            dto.error = this.error;
            return dto;
        }
    }
}
