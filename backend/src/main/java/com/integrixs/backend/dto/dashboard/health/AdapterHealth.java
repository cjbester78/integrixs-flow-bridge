package com.integrixs.backend.dto.dashboard.health;

import java.time.Instant;
import java.util.UUID;

public class AdapterHealth {
    private UUID adapterId;
    private String status;
    private double uptime;
    private long messagesProcessed;
    private double successRate;
    private double averageResponseTime;
    private Instant lastHealthCheck;
    private String healthCheckMessage;

    // Default constructor
    public AdapterHealth() {
    }

    public UUID getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(UUID adapterId) {
        this.adapterId = adapterId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getUptime() {
        return uptime;
    }

    public void setUptime(double uptime) {
        this.uptime = uptime;
    }

    public long getMessagesProcessed() {
        return messagesProcessed;
    }

    public void setMessagesProcessed(long messagesProcessed) {
        this.messagesProcessed = messagesProcessed;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public Instant getLastHealthCheck() {
        return lastHealthCheck;
    }

    public void setLastHealthCheck(Instant lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }

    public String getHealthCheckMessage() {
        return healthCheckMessage;
    }

    public void setHealthCheckMessage(String healthCheckMessage) {
        this.healthCheckMessage = healthCheckMessage;
    }
}