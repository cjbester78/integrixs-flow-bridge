package com.integrixs.backend.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model for trace event
 */
public class TraceEvent {
    private String eventType;
    private String message;
    private LocalDateTime timestamp;

    // Default constructor
    public TraceEvent() {
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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
}
