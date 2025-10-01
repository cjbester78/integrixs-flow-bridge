package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;

/**
 * DTO representing a point-in-time health snapshot
 */
public class HealthSnapshot {
    private LocalDateTime timestamp;
    private Integer healthScore;
    private String status;
    private Long messagesProcessed;
    private Long errorCount;
    private Double errorRate;
    private Long responseTime;
    private Double cpuUsage;
    private Double memoryUsage;
    private Long activeConnections;
    private Boolean isConnected;
    private String notes;

    public HealthSnapshot() {
    }

    public HealthSnapshot(LocalDateTime timestamp, Integer healthScore, String status, Long messagesProcessed,
                         Long errorCount, Double errorRate, Long responseTime, Double cpuUsage,
                         Double memoryUsage, Long activeConnections, Boolean isConnected, String notes) {
        this.timestamp = timestamp;
        this.healthScore = healthScore;
        this.status = status;
        this.messagesProcessed = messagesProcessed;
        this.errorCount = errorCount;
        this.errorRate = errorRate;
        this.responseTime = responseTime;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.activeConnections = activeConnections;
        this.isConnected = isConnected;
        this.notes = notes;
    }

    // Getters and setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getMessagesProcessed() {
        return messagesProcessed;
    }

    public void setMessagesProcessed(Long messagesProcessed) {
        this.messagesProcessed = messagesProcessed;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public Double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Long getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(Long activeConnections) {
        this.activeConnections = activeConnections;
    }

    public Boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}