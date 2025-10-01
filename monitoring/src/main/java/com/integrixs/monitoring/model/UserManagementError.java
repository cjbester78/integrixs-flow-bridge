package com.integrixs.monitoring.model;

import com.integrixs.data.model.SystemLog;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing user management errors tracked in the system.
 * Links to SystemLog entries for detailed error tracking.
 */
public class UserManagementError {

    private UUID id;
    private String action;
    private String description;
    private String payload;
    private SystemLog log;
    private LocalDateTime createdAt;

    // Constructor
    public UserManagementError() {
        this.id = UUID.randomUUID();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public SystemLog getLog() {
        return log;
    }

    public void setLog(SystemLog log) {
        this.log = log;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}