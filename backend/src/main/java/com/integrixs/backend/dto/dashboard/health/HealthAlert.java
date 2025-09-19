package com.integrixs.backend.dto.dashboard.health;

import java.time.Instant;
import java.util.UUID;

public class HealthAlert {
    private UUID adapterId;
    private String severity;
    private String message;
    private String type;
    private Instant timestamp;
    private String category;
    private String recommendation;
    private String alertId;
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum AlertType {
        PERFORMANCE_DEGRADATION,
        CONNECTION_FAILURE,
        ERROR_RATE_HIGH,
        RESPONSE_TIME_HIGH,
        CONFIGURATION_ERROR,
        RESOURCE_LIMIT,
        TIMEOUT
    }

    // Default constructor
    public HealthAlert() {
    }

    public UUID getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(UUID adapterId) {
        this.adapterId = adapterId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }
}