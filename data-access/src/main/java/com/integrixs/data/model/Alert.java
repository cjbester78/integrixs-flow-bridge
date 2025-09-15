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
import java.util.HashSet;
import java.util.Set;

/**
 * Entity for Alerts
 * Represents actual alert instances triggered by alert rules
 */
@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Alert extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_rule_id", nullable = false)
    private AlertRule alertRule;

    @Column(name = "alert_id", nullable = false, unique = true)
    private String alertId;

    @Column(name = "alert_title", nullable = false)
    private String title;

    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "severity", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertRule.AlertSeverity severity;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AlertStatus status = AlertStatus.TRIGGERED;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledged_by")
    private String acknowledgedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    // Alert Context
    @Column(name = "source_type")
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(name = "source_id")
    private String sourceId;

    @Column(name = "source_name")
    private String sourceName;

    // Alert Details
    @ElementCollection
    @CollectionTable(name = "alert_details", joinColumns = @JoinColumn(name = "alert_id"))
    @MapKeyColumn(name = "detail_key")
    @Column(name = "detail_value", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, String> details = new HashMap<>();

    // Notification Tracking
    @ElementCollection
    @CollectionTable(name = "alert_notifications", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "notification_id")
    @Builder.Default
    private Set<String> notificationIds = new HashSet<>();

    @Column(name = "notification_count")
    @Builder.Default
    private Integer notificationCount = 0;

    @Column(name = "last_notification_at")
    private LocalDateTime lastNotificationAt;

    // Escalation Tracking
    @Column(name = "is_escalated")
    @Builder.Default
    private boolean escalated = false;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @ElementCollection
    @CollectionTable(name = "alert_escalation_notifications", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "notification_id")
    @Builder.Default
    private Set<String> escalationNotificationIds = new HashSet<>();

    // Suppression
    @Column(name = "is_suppressed")
    @Builder.Default
    private boolean suppressed = false;

    @Column(name = "suppressed_until")
    private LocalDateTime suppressedUntil;

    @Column(name = "suppression_reason")
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
}
