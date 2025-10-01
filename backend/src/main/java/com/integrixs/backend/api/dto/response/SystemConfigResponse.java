package com.integrixs.backend.api.dto.response;

import java.util.Map;

/**
 * Response DTO for system configuration
 */
public class SystemConfigResponse {

    private String timezone;
    private String dateFormat;
    private String timeFormat;
    private String dateTimeFormat;
    private Map<String, String> allConfigurations;

    // Default constructor
    public SystemConfigResponse() {
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public Map<String, String> getAllConfigurations() {
        return allConfigurations;
    }

    public void setAllConfigurations(Map<String, String> allConfigurations) {
        this.allConfigurations = allConfigurations;
    }

    // Builder
    public static SystemConfigResponseBuilder builder() {
        return new SystemConfigResponseBuilder();
    }

    public static class SystemConfigResponseBuilder {
        private String timezone;
        private String dateFormat;
        private String timeFormat;
        private String dateTimeFormat;
        private Map<String, String> allConfigurations;

        public SystemConfigResponseBuilder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public SystemConfigResponseBuilder dateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public SystemConfigResponseBuilder timeFormat(String timeFormat) {
            this.timeFormat = timeFormat;
            return this;
        }

        public SystemConfigResponseBuilder dateTimeFormat(String dateTimeFormat) {
            this.dateTimeFormat = dateTimeFormat;
            return this;
        }

        public SystemConfigResponseBuilder allConfigurations(Map<String, String> allConfigurations) {
            this.allConfigurations = allConfigurations;
            return this;
        }

        public SystemConfigResponse build() {
            SystemConfigResponse result = new SystemConfigResponse();
            result.setTimezone(this.timezone);
            result.setDateFormat(this.dateFormat);
            result.setTimeFormat(this.timeFormat);
            result.setDateTimeFormat(this.dateTimeFormat);
            result.setAllConfigurations(this.allConfigurations);
            return result;
        }
    }
}
