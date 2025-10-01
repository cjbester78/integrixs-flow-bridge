package com.integrixs.shared.dto;

public class GlobalRetrySettingsDTO {

    private boolean enabled;
    private int maxRetries;
    private int retryInterval;
    private String retryIntervalUnit;

    // Default constructor
    public GlobalRetrySettingsDTO() {
    }

    // All args constructor
    public GlobalRetrySettingsDTO(boolean enabled, int maxRetries, int retryInterval, String retryIntervalUnit) {
        this.enabled = enabled;
        this.maxRetries = maxRetries;
        this.retryInterval = retryInterval;
        this.retryIntervalUnit = retryIntervalUnit;
    }

    // Getters
    public boolean isEnabled() { return enabled; }
    public int getMaxRetries() { return maxRetries; }
    public int getRetryInterval() { return retryInterval; }
    public String getRetryIntervalUnit() { return retryIntervalUnit; }

    // Setters
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public void setRetryInterval(int retryInterval) { this.retryInterval = retryInterval; }
    public void setRetryIntervalUnit(String retryIntervalUnit) { this.retryIntervalUnit = retryIntervalUnit; }

    // Builder
    public static GlobalRetrySettingsDTOBuilder builder() {
        return new GlobalRetrySettingsDTOBuilder();
    }

    public static class GlobalRetrySettingsDTOBuilder {
        private boolean enabled;
        private int maxRetries;
        private int retryInterval;
        private String retryIntervalUnit;

        public GlobalRetrySettingsDTOBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public GlobalRetrySettingsDTOBuilder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public GlobalRetrySettingsDTOBuilder retryInterval(int retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }

        public GlobalRetrySettingsDTOBuilder retryIntervalUnit(String retryIntervalUnit) {
            this.retryIntervalUnit = retryIntervalUnit;
            return this;
        }

        public GlobalRetrySettingsDTO build() {
            return new GlobalRetrySettingsDTO(enabled, maxRetries, retryInterval, retryIntervalUnit);
        }
    }
}
