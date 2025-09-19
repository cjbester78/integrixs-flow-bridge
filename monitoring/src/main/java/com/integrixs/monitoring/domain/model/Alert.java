package com.integrixs.monitoring.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model representing an alert
 */
public class Alert {
    private String alertId;
    private String alertName;
    private AlertType alertType;
    private AlertSeverity severity;
    private AlertStatus status;
    private String source;
    private String message;
    private String condition;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
    private String domainType;
    private String domainReferenceId;
    private Map<String, Object> metadata = new HashMap<>();
    private AlertAction action;

    // Constructors
    public Alert() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Alert alert = new Alert();

        public Builder alertId(String alertId) {
            alert.alertId = alertId;
            return this;
        }

        public Builder alertName(String alertName) {
            alert.alertName = alertName;
            return this;
        }

        public Builder alertType(AlertType alertType) {
            alert.alertType = alertType;
            return this;
        }

        public Builder severity(AlertSeverity severity) {
            alert.severity = severity;
            return this;
        }

        public Builder status(AlertStatus status) {
            alert.status = status;
            return this;
        }

        public Builder source(String source) {
            alert.source = source;
            return this;
        }

        public Builder message(String message) {
            alert.message = message;
            return this;
        }

        public Builder condition(String condition) {
            alert.condition = condition;
            return this;
        }

        public Builder triggeredAt(LocalDateTime triggeredAt) {
            alert.triggeredAt = triggeredAt;
            return this;
        }

        public Builder resolvedAt(LocalDateTime resolvedAt) {
            alert.resolvedAt = resolvedAt;
            return this;
        }

        public Builder acknowledgedAt(LocalDateTime acknowledgedAt) {
            alert.acknowledgedAt = acknowledgedAt;
            return this;
        }

        public Builder acknowledgedBy(String acknowledgedBy) {
            alert.acknowledgedBy = acknowledgedBy;
            return this;
        }

        public Builder domainType(String domainType) {
            alert.domainType = domainType;
            return this;
        }

        public Builder domainReferenceId(String domainReferenceId) {
            alert.domainReferenceId = domainReferenceId;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            alert.metadata = metadata != null ? metadata : new HashMap<>();
            return this;
        }

        public Builder action(AlertAction action) {
            alert.action = action;
            return this;
        }

        public Alert build() {
            return alert;
        }
    }

    // Getters and Setters
    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
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

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public String getDomainType() {
        return domainType;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public String getDomainReferenceId() {
        return domainReferenceId;
    }

    public void setDomainReferenceId(String domainReferenceId) {
        this.domainReferenceId = domainReferenceId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public AlertAction getAction() {
        return action;
    }

    public void setAction(AlertAction action) {
        this.action = action;
    }

    /**
     * Alert types
     */
    public enum AlertType {
        THRESHOLD,         // Metric exceeded threshold
        ANOMALY,           // Unusual pattern detected
        ERROR_RATE,        // High error rate
        AVAILABILITY,      // Service availability issue
        PERFORMANCE,       // Performance degradation
        SECURITY,          // Security event
        CUSTOM              // Custom alert rule
    }

    /**
     * Alert severities
     */
    public enum AlertSeverity {
        INFO,
        WARNING,
        MINOR,
        MAJOR,
        CRITICAL
    }

    /**
     * Alert status
     */
    public enum AlertStatus {
        TRIGGERED,
        ACKNOWLEDGED,
        RESOLVED,
        SUPPRESSED
    }

    /**
     * Alert action
     */
    public static class AlertAction {
        private ActionType type;
        private Map<String, String> parameters;

        public AlertAction() {
        }

        // Builder
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private AlertAction alertAction = new AlertAction();

            public Builder type(ActionType type) {
                alertAction.type = type;
                return this;
            }

            public Builder parameters(Map<String, String> parameters) {
                alertAction.parameters = parameters;
                return this;
            }

            public AlertAction build() {
                return alertAction;
            }
        }

        // Getters and Setters
        public ActionType getType() {
            return type;
        }

        public void setType(ActionType type) {
            this.type = type;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        public enum ActionType {
            EMAIL,
            SMS,
            WEBHOOK,
            TICKET,
            AUTO_SCALE,
            RESTART_SERVICE
        }
    }


    /**
     * Acknowledge alert
     */
    public void acknowledge(String userId) {
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = LocalDateTime.now();
        this.acknowledgedBy = userId;
    }

    /**
     * Resolve alert
     */
    public void resolve() {
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Check if alert is active
     */
    public boolean isActive() {
        return status == AlertStatus.TRIGGERED || status == AlertStatus.ACKNOWLEDGED;
    }

    /**
     * Check if alert requires immediate action
     */
    public boolean requiresImmediateAction() {
        return severity == AlertSeverity.CRITICAL && status == AlertStatus.TRIGGERED;
    }

    /**
     * Add metadata
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}
