package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;

/**
 * Critical alert requiring immediate attention.
 */
public class CriticalAlert {
    private String adapterId;
    private String adapterName;
    private String severity; // CRITICAL, HIGH, MEDIUM
    private String message;
    private LocalDateTime timestamp;
    private String actionRequired;

    // Default constructor
    public CriticalAlert() {
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(String actionRequired) {
        this.actionRequired = actionRequired;
    }
}
