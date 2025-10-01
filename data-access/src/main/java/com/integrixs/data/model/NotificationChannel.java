package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity for Notification Channels
 * Defines various channels for sending alert notifications
 */
public class NotificationChannel extends BaseEntity {

    private String channelName;

    private String description;

    private ChannelType channelType;

    private boolean enabled = true;

    // Channel Configuration(stored as JSON)
    private Map<String, String> configuration = new HashMap<>();

    // Rate Limiting
    private Integer rateLimitPerHour;

    private LocalDateTime lastNotificationAt;

    private Integer notificationCountCurrentHour = 0;

    // Testing and Validation
    private LocalDateTime lastTestAt;

    private Boolean lastTestSuccess;

    private String lastTestMessage;

    /**
     * Channel Types
     */
    public enum ChannelType {
        EMAIL,
        SMS,
        WEBHOOK,
        SLACK,
        TEAMS,
        PAGERDUTY,
        CUSTOM
    }

    /**
     * Get configuration value
     */
    public String getConfigValue(String key) {
        return configuration.get(key);
    }

    /**
     * Set configuration value
     */
    public void setConfigValue(String key, String value) {
        configuration.put(key, value);
    }

    /**
     * Get required configuration based on channel type
     */
    public Map<String, String> getRequiredConfiguration() {
        Map<String, String> required = new HashMap<>();

        switch(channelType) {
            case EMAIL:
                required.put("smtp_host", "SMTP server host");
                required.put("smtp_port", "SMTP server port");
                required.put("smtp_username", "SMTP username");
                required.put("smtp_password", "SMTP password");
                required.put("from_address", "From email address");
                required.put("to_addresses", "Comma - separated recipient addresses");
                break;

            case SMS:
                required.put("provider", "SMS provider(twilio, aws_sns, etc.)");
                required.put("api_key", "Provider API key");
                required.put("api_secret", "Provider API secret");
                required.put("from_number", "From phone number");
                required.put("to_numbers", "Comma - separated recipient numbers");
                break;

            case WEBHOOK:
                required.put("url", "Webhook URL");
                required.put("method", "HTTP method(POST, PUT)");
                required.put("headers", "JSON object of headers");
                required.put("auth_type", "Authentication type(none, basic, bearer, api_key)");
                break;

            case SLACK:
                required.put("webhook_url", "Slack webhook URL");
                required.put("channel", "Slack channel");
                required.put("username", "Bot username");
                break;

            case TEAMS:
                required.put("webhook_url", "Teams webhook URL");
                break;

            case PAGERDUTY:
                required.put("integration_key", "PagerDuty integration key");
                required.put("api_key", "PagerDuty API key");
                break;

            case CUSTOM:
                required.put("handler_class", "Custom handler class name");
                break;
        }

        return required;
    }

    // Default constructor
    public NotificationChannel() {
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getRateLimitPerHour() {
        return rateLimitPerHour;
    }

    public void setRateLimitPerHour(Integer rateLimitPerHour) {
        this.rateLimitPerHour = rateLimitPerHour;
    }

    public LocalDateTime getLastNotificationAt() {
        return lastNotificationAt;
    }

    public void setLastNotificationAt(LocalDateTime lastNotificationAt) {
        this.lastNotificationAt = lastNotificationAt;
    }

    public Integer getNotificationCountCurrentHour() {
        return notificationCountCurrentHour;
    }

    public void setNotificationCountCurrentHour(Integer notificationCountCurrentHour) {
        this.notificationCountCurrentHour = notificationCountCurrentHour;
    }

    public LocalDateTime getLastTestAt() {
        return lastTestAt;
    }

    public void setLastTestAt(LocalDateTime lastTestAt) {
        this.lastTestAt = lastTestAt;
    }

    public Boolean getLastTestSuccess() {
        return lastTestSuccess;
    }

    public void setLastTestSuccess(Boolean lastTestSuccess) {
        this.lastTestSuccess = lastTestSuccess;
    }

    public String getLastTestMessage() {
        return lastTestMessage;
    }

    public void setLastTestMessage(String lastTestMessage) {
        this.lastTestMessage = lastTestMessage;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    // Alias for updatedAt from BaseEntity
    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.setUpdatedAt(modifiedAt);
    }

    // Builder
    public static NotificationChannelBuilder builder() {
        return new NotificationChannelBuilder();
    }

    public static class NotificationChannelBuilder {
        private String channelName;
        private String description;
        private ChannelType channelType;
        private boolean enabled;
        private Integer rateLimitPerHour;
        private LocalDateTime lastNotificationAt;
        private Integer notificationCountCurrentHour;
        private LocalDateTime lastTestAt;
        private Boolean lastTestSuccess;
        private String lastTestMessage;

        public NotificationChannelBuilder channelName(String channelName) {
            this.channelName = channelName;
            return this;
        }

        public NotificationChannelBuilder description(String description) {
            this.description = description;
            return this;
        }

        public NotificationChannelBuilder channelType(ChannelType channelType) {
            this.channelType = channelType;
            return this;
        }

        public NotificationChannelBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public NotificationChannelBuilder rateLimitPerHour(Integer rateLimitPerHour) {
            this.rateLimitPerHour = rateLimitPerHour;
            return this;
        }

        public NotificationChannelBuilder lastNotificationAt(LocalDateTime lastNotificationAt) {
            this.lastNotificationAt = lastNotificationAt;
            return this;
        }

        public NotificationChannelBuilder notificationCountCurrentHour(Integer notificationCountCurrentHour) {
            this.notificationCountCurrentHour = notificationCountCurrentHour;
            return this;
        }

        public NotificationChannelBuilder lastTestAt(LocalDateTime lastTestAt) {
            this.lastTestAt = lastTestAt;
            return this;
        }

        public NotificationChannelBuilder lastTestSuccess(Boolean lastTestSuccess) {
            this.lastTestSuccess = lastTestSuccess;
            return this;
        }

        public NotificationChannelBuilder lastTestMessage(String lastTestMessage) {
            this.lastTestMessage = lastTestMessage;
            return this;
        }

        public NotificationChannel build() {
            NotificationChannel instance = new NotificationChannel();
            instance.setChannelName(this.channelName);
            instance.setDescription(this.description);
            instance.setChannelType(this.channelType);
            instance.setEnabled(this.enabled);
            instance.setRateLimitPerHour(this.rateLimitPerHour);
            instance.setLastNotificationAt(this.lastNotificationAt);
            instance.setNotificationCountCurrentHour(this.notificationCountCurrentHour);
            instance.setLastTestAt(this.lastTestAt);
            instance.setLastTestSuccess(this.lastTestSuccess);
            instance.setLastTestMessage(this.lastTestMessage);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "NotificationChannel{" +
                "channelName=" + channelName + "description=" + description + "channelType=" + channelType + "enabled=" + enabled + "rateLimitPerHour=" + rateLimitPerHour + "..." +
                '}';
    }
}
