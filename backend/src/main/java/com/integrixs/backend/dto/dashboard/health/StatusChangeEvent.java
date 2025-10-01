package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;

/**
 * Adapter status change event.
 */
public class StatusChangeEvent {
    private LocalDateTime timestamp;
    private String fromStatus;
    private String toStatus;
    private String reason;

    // Default constructor
    public StatusChangeEvent() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
