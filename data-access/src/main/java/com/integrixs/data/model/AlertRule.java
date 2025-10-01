package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity for Alert Rules
 * Defines conditions and actions for system alerts
 */
public class AlertRule extends BaseEntity {

    private String ruleName;

    private String description;

    private AlertType alertType;

    private AlertSeverity severity;

    private boolean enabled = true;

    // Alert Condition Configuration
    private ConditionType conditionType;

    private String conditionExpression;

    private Double thresholdValue;

    private ThresholdOperator thresholdOperator;

    private Integer timeWindowMinutes;

    private Integer occurrenceCount;

    // Target Configuration
    private TargetType targetType;

    private String targetId; // Flow ID, Adapter ID, etc.

    // Notification Configuration
    private Set<String> notificationChannelIds = new HashSet<>();

    // Alert Suppression
    private Integer suppressionDurationMinutes;

    private LocalDateTime lastTriggeredAt;

    private Integer triggerCount = 0;

    // Escalation Configuration
    private boolean escalationEnabled = false;

    private Integer escalationAfterMinutes;

    private Set<String> escalationChannelIds = new HashSet<>();

    // Additional Metadata
    private Set<String> tags = new HashSet<>();

    private String createdByUser;

    private String modifiedBy;

    /**
     * Alert Types
     */
    public enum AlertType {
        FLOW_FAILURE,
        FLOW_SLA_BREACH,
        ADAPTER_CONNECTION_FAILURE,
        ADAPTER_HEALTH_DEGRADED,
        PERFORMANCE_THRESHOLD,
        ERROR_RATE_THRESHOLD,
        SYSTEM_RESOURCE,
        CUSTOM
    }

    /**
     * Alert Severity Levels
     */
    public enum AlertSeverity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW,
        INFO
    }

    /**
     * Condition Types
     */
    public enum ConditionType {
        SIMPLE_THRESHOLD,
        RATE_THRESHOLD,
        PATTERN_MATCH,
        CUSTOM_EXPRESSION,
        ABSENCE_DETECTION
    }

    /**
     * Threshold Operators
     */
    public enum ThresholdOperator {
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
        EQUAL,
        NOT_EQUAL
    }

    /**
     * Target Types
     */
    public enum TargetType {
        ALL,
        FLOW,
        ADAPTER,
        SYSTEM,
        BUSINESS_COMPONENT
    }

    // Default constructor
    public AlertRule() {
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public Double getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(Double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public ThresholdOperator getThresholdOperator() {
        return thresholdOperator;
    }

    public void setThresholdOperator(ThresholdOperator thresholdOperator) {
        this.thresholdOperator = thresholdOperator;
    }

    public Integer getTimeWindowMinutes() {
        return timeWindowMinutes;
    }

    public void setTimeWindowMinutes(Integer timeWindowMinutes) {
        this.timeWindowMinutes = timeWindowMinutes;
    }

    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(Integer occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Set<String> getNotificationChannelIds() {
        return notificationChannelIds;
    }

    public void setNotificationChannelIds(Set<String> notificationChannelIds) {
        this.notificationChannelIds = notificationChannelIds;
    }

    public Integer getSuppressionDurationMinutes() {
        return suppressionDurationMinutes;
    }

    public void setSuppressionDurationMinutes(Integer suppressionDurationMinutes) {
        this.suppressionDurationMinutes = suppressionDurationMinutes;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public Integer getTriggerCount() {
        return triggerCount;
    }

    public void setTriggerCount(Integer triggerCount) {
        this.triggerCount = triggerCount;
    }

    public boolean isEscalationEnabled() {
        return escalationEnabled;
    }

    public void setEscalationEnabled(boolean escalationEnabled) {
        this.escalationEnabled = escalationEnabled;
    }

    public Integer getEscalationAfterMinutes() {
        return escalationAfterMinutes;
    }

    public void setEscalationAfterMinutes(Integer escalationAfterMinutes) {
        this.escalationAfterMinutes = escalationAfterMinutes;
    }

    public Set<String> getEscalationChannelIds() {
        return escalationChannelIds;
    }

    public void setEscalationChannelIds(Set<String> escalationChannelIds) {
        this.escalationChannelIds = escalationChannelIds;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    // Builder
    public static AlertRuleBuilder builder() {
        return new AlertRuleBuilder();
    }

    public static class AlertRuleBuilder {
        private String ruleName;
        private String description;
        private AlertType alertType;
        private AlertSeverity severity;
        private boolean enabled;
        private ConditionType conditionType;
        private String conditionExpression;
        private Double thresholdValue;
        private ThresholdOperator thresholdOperator;
        private Integer timeWindowMinutes;
        private Integer occurrenceCount;
        private TargetType targetType;
        private String targetId;
        private Set<String> notificationChannelIds;
        private Integer suppressionDurationMinutes;
        private LocalDateTime lastTriggeredAt;
        private Integer triggerCount;
        private boolean escalationEnabled;
        private Integer escalationAfterMinutes;
        private Set<String> escalationChannelIds;
        private Set<String> tags;
        private String createdByUser;
        private String modifiedBy;

        public AlertRuleBuilder ruleName(String ruleName) {
            this.ruleName = ruleName;
            return this;
        }

        public AlertRuleBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AlertRuleBuilder alertType(AlertType alertType) {
            this.alertType = alertType;
            return this;
        }

        public AlertRuleBuilder severity(AlertSeverity severity) {
            this.severity = severity;
            return this;
        }

        public AlertRuleBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public AlertRuleBuilder conditionType(ConditionType conditionType) {
            this.conditionType = conditionType;
            return this;
        }

        public AlertRuleBuilder conditionExpression(String conditionExpression) {
            this.conditionExpression = conditionExpression;
            return this;
        }

        public AlertRuleBuilder thresholdValue(Double thresholdValue) {
            this.thresholdValue = thresholdValue;
            return this;
        }

        public AlertRuleBuilder thresholdOperator(ThresholdOperator thresholdOperator) {
            this.thresholdOperator = thresholdOperator;
            return this;
        }

        public AlertRuleBuilder timeWindowMinutes(Integer timeWindowMinutes) {
            this.timeWindowMinutes = timeWindowMinutes;
            return this;
        }

        public AlertRuleBuilder occurrenceCount(Integer occurrenceCount) {
            this.occurrenceCount = occurrenceCount;
            return this;
        }

        public AlertRuleBuilder targetType(TargetType targetType) {
            this.targetType = targetType;
            return this;
        }

        public AlertRuleBuilder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public AlertRuleBuilder notificationChannelIds(Set<String> notificationChannelIds) {
            this.notificationChannelIds = notificationChannelIds;
            return this;
        }

        public AlertRuleBuilder suppressionDurationMinutes(Integer suppressionDurationMinutes) {
            this.suppressionDurationMinutes = suppressionDurationMinutes;
            return this;
        }

        public AlertRuleBuilder lastTriggeredAt(LocalDateTime lastTriggeredAt) {
            this.lastTriggeredAt = lastTriggeredAt;
            return this;
        }

        public AlertRuleBuilder triggerCount(Integer triggerCount) {
            this.triggerCount = triggerCount;
            return this;
        }

        public AlertRuleBuilder escalationEnabled(boolean escalationEnabled) {
            this.escalationEnabled = escalationEnabled;
            return this;
        }

        public AlertRuleBuilder escalationAfterMinutes(Integer escalationAfterMinutes) {
            this.escalationAfterMinutes = escalationAfterMinutes;
            return this;
        }

        public AlertRuleBuilder escalationChannelIds(Set<String> escalationChannelIds) {
            this.escalationChannelIds = escalationChannelIds;
            return this;
        }

        public AlertRuleBuilder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public AlertRuleBuilder createdByUser(String createdByUser) {
            this.createdByUser = createdByUser;
            return this;
        }

        public AlertRuleBuilder modifiedBy(String modifiedBy) {
            this.modifiedBy = modifiedBy;
            return this;
        }

        public AlertRule build() {
            AlertRule instance = new AlertRule();
            instance.setRuleName(this.ruleName);
            instance.setDescription(this.description);
            instance.setAlertType(this.alertType);
            instance.setSeverity(this.severity);
            instance.setEnabled(this.enabled);
            instance.setConditionType(this.conditionType);
            instance.setConditionExpression(this.conditionExpression);
            instance.setThresholdValue(this.thresholdValue);
            instance.setThresholdOperator(this.thresholdOperator);
            instance.setTimeWindowMinutes(this.timeWindowMinutes);
            instance.setOccurrenceCount(this.occurrenceCount);
            instance.setTargetType(this.targetType);
            instance.setTargetId(this.targetId);
            instance.setNotificationChannelIds(this.notificationChannelIds);
            instance.setSuppressionDurationMinutes(this.suppressionDurationMinutes);
            instance.setLastTriggeredAt(this.lastTriggeredAt);
            instance.setTriggerCount(this.triggerCount);
            instance.setEscalationEnabled(this.escalationEnabled);
            instance.setEscalationAfterMinutes(this.escalationAfterMinutes);
            instance.setEscalationChannelIds(this.escalationChannelIds);
            instance.setTags(this.tags);
            instance.setCreatedByUser(this.createdByUser);
            instance.setModifiedBy(this.modifiedBy);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "AlertRule{" +
                "ruleName=" + ruleName + "description=" + description + "alertType=" + alertType + "severity=" + severity + "enabled=" + enabled + "..." +
                '}';
    }
}
