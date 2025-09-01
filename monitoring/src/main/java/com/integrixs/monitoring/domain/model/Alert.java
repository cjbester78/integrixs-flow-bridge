package com.integrixs.monitoring.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model representing an alert
 */
@Data
@Builder
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
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private AlertAction action;
    
    /**
     * Alert types
     */
    public enum AlertType {
        THRESHOLD,          // Metric exceeded threshold
        ANOMALY,            // Unusual pattern detected
        ERROR_RATE,         // High error rate
        AVAILABILITY,       // Service availability issue
        PERFORMANCE,        // Performance degradation
        SECURITY,           // Security event
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
    @Data
    @Builder
    public static class AlertAction {
        private ActionType type;
        private Map<String, String> parameters;
        
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