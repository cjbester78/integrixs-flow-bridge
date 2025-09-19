package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;

/**
 * Error event in adapter history.
 */
public class ErrorEvent {
    private LocalDateTime timestamp;
    private String errorType;
    private String message;
    private String impact; // HIGH, MEDIUM, LOW

    // Default constructor
    public ErrorEvent() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }
}
