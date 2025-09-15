package com.integrixs.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity for Notification Channels
 * Defines various channels for sending alert notifications
 */
@Entity
@Table(name = "notification_channels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotificationChannel extends BaseEntity {

    @Column(name = "channel_name", nullable = false, unique = true)
    private String channelName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "channel_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChannelType channelType;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // Channel Configuration(stored as JSON)
    @ElementCollection
    @CollectionTable(name = "notification_channel_config", joinColumns = @JoinColumn(name = "channel_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, String> configuration = new HashMap<>();

    // Rate Limiting
    @Column(name = "rate_limit_per_hour")
    private Integer rateLimitPerHour;

    @Column(name = "last_notification_at")
    private LocalDateTime lastNotificationAt;

    @Column(name = "notification_count_current_hour")
    @Builder.Default
    private Integer notificationCountCurrentHour = 0;

    // Testing and Validation
    @Column(name = "last_test_at")
    private LocalDateTime lastTestAt;

    @Column(name = "last_test_success")
    private Boolean lastTestSuccess;

    @Column(name = "last_test_message")
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
}
