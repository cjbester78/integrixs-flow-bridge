package com.integrixs.backend.dto.dashboard.health;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class HealthAlert {
    private UUID adapterId;
    private String adapterIdString; // For string-based adapter IDs
    private String adapterName;
    private String severity;
    private String message;
    private String type;
    private String alertType;
    private Instant timestamp;
    private LocalDateTime timestampLocal;
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

    public String getAdapterIdString() {
        return adapterIdString;
    }

    public void setAdapterIdString(String adapterIdString) {
        this.adapterIdString = adapterIdString;
    }

    // Convenience method for setting adapterId as string
    public void setAdapterId(String adapterId) {
        this.adapterIdString = adapterId;
        try {
            this.adapterId = UUID.fromString(adapterId);
        } catch (Exception e) {
            // Ignore if not a valid UUID
        }
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public LocalDateTime getTimestampLocal() {
        return timestampLocal;
    }

    public void setTimestampLocal(LocalDateTime timestampLocal) {
        this.timestampLocal = timestampLocal;
    }

    // Convenience method for getting timestamp as LocalDateTime
    public LocalDateTime getTimestampAsLocalDateTime() {
        if (timestampLocal != null) {
            return timestampLocal;
        }
        if (timestamp != null) {
            return LocalDateTime.ofInstant(timestamp, java.time.ZoneId.systemDefault());
        }
        return null;
    }

    // Overloaded setter for LocalDateTime
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestampLocal = timestamp;
        if (timestamp != null) {
            this.timestamp = timestamp.atZone(java.time.ZoneId.systemDefault()).toInstant();
        }
    }
}