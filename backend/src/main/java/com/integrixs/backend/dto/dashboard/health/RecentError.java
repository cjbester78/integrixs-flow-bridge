package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;

/**
 * Recent error occurrence.
 */
public class RecentError {
    private LocalDateTime timestamp;
    private String message;
    private int count;

    // Default constructor
    public RecentError() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
