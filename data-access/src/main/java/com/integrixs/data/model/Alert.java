package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity for Alerts
 * Represents actual alert instances triggered by alert rules
 */
public class Alert extends BaseEntity {

    private AlertRule alertRule;

    private String alertId;

    private String title;

    private String message;

    private AlertRule.AlertSeverity severity;

    private AlertStatus status = AlertStatus.TRIGGERED;

    private LocalDateTime triggeredAt;

    private LocalDateTime acknowledgedAt;

    private String acknowledgedBy;

    private LocalDateTime resolvedAt;

    private String resolvedBy;

    private String resolutionNotes;

    // Alert Context
    private SourceType sourceType;

    private String sourceId;

    private String sourceName;

    // Alert Details
    private Map<String, String> details = new HashMap<>();

    // Notification Tracking
    private Set<String> notificationIds = new HashSet<>();

    private Integer notificationCount = 0;

    private LocalDateTime lastNotificationAt;

    // Escalation Tracking
    private boolean escalated = false;

    private LocalDateTime escalatedAt;

    private Set<String> escalationNotificationIds = new HashSet<>();

    // Suppression
    private boolean suppressed = false;

    private LocalDateTime suppressedUntil;

    private String suppressionReason;

    /**
     * Alert Status
     */
    public enum AlertStatus {
        TRIGGERED,
        NOTIFIED,
        ACKNOWLEDGED,
        ESCALATED,
        RESOLVED,
        SUPPRESSED,
        EXPIRED
    }

    /**
     * Source Types
     */
    public enum SourceType {
        FLOW,
        ADAPTER,
        SYSTEM,
        TRANSFORMATION,
        MONITORING
    }

    /**
     * Add detail
     */
    public void addDetail(String key, String value) {
        details.put(key, value);
    }

    /**
     * Add notification ID
     */
    public void addNotificationId(String notificationId) {
        notificationIds.add(notificationId);
        notificationCount++;
        lastNotificationAt = LocalDateTime.now();
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
    public void resolve(String userId, String notes) {
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = userId;
        this.resolutionNotes = notes;
    }

    /**
     * Escalate alert
     */
    public void escalate() {
        this.escalated = true;
        this.escalatedAt = LocalDateTime.now();
        this.status = AlertStatus.ESCALATED;
    }

    /**
     * Suppress alert
     */
    public void suppress(LocalDateTime until, String reason) {
        this.suppressed = true;
        this.suppressedUntil = until;
        this.suppressionReason = reason;
        this.status = AlertStatus.SUPPRESSED;
    }

    // Default constructor
    public Alert() {
    }

    public AlertRule getAlertRule() {
        return alertRule;
    }

    public void setAlertRule(AlertRule alertRule) {
        this.alertRule = alertRule;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AlertRule.AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertRule.AlertSeverity severity) {
        this.severity = severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
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

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public Set<String> getNotificationIds() {
        return notificationIds;
    }

    public void setNotificationIds(Set<String> notificationIds) {
        this.notificationIds = notificationIds;
    }

    public Integer getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(Integer notificationCount) {
        this.notificationCount = notificationCount;
    }

    public LocalDateTime getLastNotificationAt() {
        return lastNotificationAt;
    }

    public void setLastNotificationAt(LocalDateTime lastNotificationAt) {
        this.lastNotificationAt = lastNotificationAt;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }

    public LocalDateTime getEscalatedAt() {
        return escalatedAt;
    }

    public void setEscalatedAt(LocalDateTime escalatedAt) {
        this.escalatedAt = escalatedAt;
    }

    public Set<String> getEscalationNotificationIds() {
        return escalationNotificationIds;
    }

    public void setEscalationNotificationIds(Set<String> escalationNotificationIds) {
        this.escalationNotificationIds = escalationNotificationIds;
    }

    public boolean isSuppressed() {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }

    public LocalDateTime getSuppressedUntil() {
        return suppressedUntil;
    }

    public void setSuppressedUntil(LocalDateTime suppressedUntil) {
        this.suppressedUntil = suppressedUntil;
    }

    public String getSuppressionReason() {
        return suppressionReason;
    }

    public void setSuppressionReason(String suppressionReason) {
        this.suppressionReason = suppressionReason;
    }

    // Builder
    public static AlertBuilder builder() {
        return new AlertBuilder();
    }

    public static class AlertBuilder {
        private AlertRule alertRule;
        private String alertId;
        private String title;
        private String message;
        private AlertRule.AlertSeverity severity;
        private AlertStatus status;
        private LocalDateTime triggeredAt;
        private LocalDateTime acknowledgedAt;
        private String acknowledgedBy;
        private LocalDateTime resolvedAt;
        private String resolvedBy;
        private String resolutionNotes;
        private SourceType sourceType;
        private String sourceId;
        private String sourceName;
        private Set<String> notificationIds;
        private Integer notificationCount;
        private LocalDateTime lastNotificationAt;
        private boolean escalated;
        private LocalDateTime escalatedAt;
        private Set<String> escalationNotificationIds;
        private boolean suppressed;
        private LocalDateTime suppressedUntil;
        private String suppressionReason;
        private Map<String, String> details = new HashMap<>();

        public AlertBuilder alertRule(AlertRule alertRule) {
            this.alertRule = alertRule;
            return this;
        }

        public AlertBuilder alertId(String alertId) {
            this.alertId = alertId;
            return this;
        }

        public AlertBuilder title(String title) {
            this.title = title;
            return this;
        }

        public AlertBuilder message(String message) {
            this.message = message;
            return this;
        }

        public AlertBuilder severity(AlertRule.AlertSeverity severity) {
            this.severity = severity;
            return this;
        }

        public AlertBuilder status(AlertStatus status) {
            this.status = status;
            return this;
        }

        public AlertBuilder triggeredAt(LocalDateTime triggeredAt) {
            this.triggeredAt = triggeredAt;
            return this;
        }

        public AlertBuilder acknowledgedAt(LocalDateTime acknowledgedAt) {
            this.acknowledgedAt = acknowledgedAt;
            return this;
        }

        public AlertBuilder acknowledgedBy(String acknowledgedBy) {
            this.acknowledgedBy = acknowledgedBy;
            return this;
        }

        public AlertBuilder resolvedAt(LocalDateTime resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }

        public AlertBuilder resolvedBy(String resolvedBy) {
            this.resolvedBy = resolvedBy;
            return this;
        }

        public AlertBuilder resolutionNotes(String resolutionNotes) {
            this.resolutionNotes = resolutionNotes;
            return this;
        }

        public AlertBuilder sourceType(SourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public AlertBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public AlertBuilder sourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }

        public AlertBuilder notificationIds(Set<String> notificationIds) {
            this.notificationIds = notificationIds;
            return this;
        }

        public AlertBuilder notificationCount(Integer notificationCount) {
            this.notificationCount = notificationCount;
            return this;
        }

        public AlertBuilder lastNotificationAt(LocalDateTime lastNotificationAt) {
            this.lastNotificationAt = lastNotificationAt;
            return this;
        }

        public AlertBuilder escalated(boolean escalated) {
            this.escalated = escalated;
            return this;
        }

        public AlertBuilder escalatedAt(LocalDateTime escalatedAt) {
            this.escalatedAt = escalatedAt;
            return this;
        }

        public AlertBuilder escalationNotificationIds(Set<String> escalationNotificationIds) {
            this.escalationNotificationIds = escalationNotificationIds;
            return this;
        }

        public AlertBuilder suppressed(boolean suppressed) {
            this.suppressed = suppressed;
            return this;
        }

        public AlertBuilder suppressedUntil(LocalDateTime suppressedUntil) {
            this.suppressedUntil = suppressedUntil;
            return this;
        }

        public AlertBuilder suppressionReason(String suppressionReason) {
            this.suppressionReason = suppressionReason;
            return this;
        }

        public AlertBuilder details(Map<String, String> details) {
            this.details = details;
            return this;
        }

        public Alert build() {
            Alert instance = new Alert();
            instance.setAlertRule(this.alertRule);
            instance.setAlertId(this.alertId);
            instance.setTitle(this.title);
            instance.setMessage(this.message);
            instance.setSeverity(this.severity);
            instance.setStatus(this.status);
            instance.setTriggeredAt(this.triggeredAt);
            instance.setAcknowledgedAt(this.acknowledgedAt);
            instance.setAcknowledgedBy(this.acknowledgedBy);
            instance.setResolvedAt(this.resolvedAt);
            instance.setResolvedBy(this.resolvedBy);
            instance.setResolutionNotes(this.resolutionNotes);
            instance.setSourceType(this.sourceType);
            instance.setSourceId(this.sourceId);
            instance.setSourceName(this.sourceName);
            instance.setNotificationIds(this.notificationIds);
            instance.setNotificationCount(this.notificationCount);
            instance.setLastNotificationAt(this.lastNotificationAt);
            instance.setEscalated(this.escalated);
            instance.setEscalatedAt(this.escalatedAt);
            instance.setEscalationNotificationIds(this.escalationNotificationIds);
            instance.setSuppressed(this.suppressed);
            instance.setSuppressedUntil(this.suppressedUntil);
            instance.setSuppressionReason(this.suppressionReason);
            instance.setDetails(this.details);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "Alert{" +
                "alertRule=" + alertRule + "alertId=" + alertId + "title=" + title + "message=" + message + "severity=" + severity + "..." +
                '}';
    }
}
