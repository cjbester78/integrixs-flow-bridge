package com.integrixs.data.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity for Alert Rules
 * Defines conditions and actions for system alerts
 */
@Entity
@Table(name = "alert_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AlertRule extends BaseEntity {

    @Column(name = "rule_name", nullable = false, unique = true)
    private String ruleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "alert_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(name = "severity", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // Alert Condition Configuration
    @Column(name = "condition_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConditionType conditionType;

    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Column(name = "threshold_operator")
    @Enumerated(EnumType.STRING)
    private ThresholdOperator thresholdOperator;

    @Column(name = "time_window_minutes")
    private Integer timeWindowMinutes;

    @Column(name = "occurrence_count")
    private Integer occurrenceCount;

    // Target Configuration
    @Column(name = "target_type")
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Column(name = "target_id")
    private String targetId; // Flow ID, Adapter ID, etc.

    // Notification Configuration
    @ElementCollection
    @CollectionTable(name = "alert_rule_channels", joinColumns = @JoinColumn(name = "alert_rule_id"))
    @Column(name = "channel_id")
    @Builder.Default
    private Set<String> notificationChannelIds = new HashSet<>();

    // Alert Suppression
    @Column(name = "suppression_duration_minutes")
    private Integer suppressionDurationMinutes;

    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;

    @Column(name = "trigger_count")
    @Builder.Default
    private Integer triggerCount = 0;

    // Escalation Configuration
    @Column(name = "escalation_enabled")
    @Builder.Default
    private boolean escalationEnabled = false;

    @Column(name = "escalation_after_minutes")
    private Integer escalationAfterMinutes;

    @ElementCollection
    @CollectionTable(name = "alert_rule_escalation_channels", joinColumns = @JoinColumn(name = "alert_rule_id"))
    @Column(name = "channel_id")
    @Builder.Default
    private Set<String> escalationChannelIds = new HashSet<>();

    // Additional Metadata
    @ElementCollection
    @CollectionTable(name = "alert_rule_tags", joinColumns = @JoinColumn(name = "alert_rule_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
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
}
