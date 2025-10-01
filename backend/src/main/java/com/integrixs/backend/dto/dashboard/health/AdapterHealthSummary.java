package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO representing adapter health summary
 */
public class AdapterHealthSummary {
    private String adapterId;
    private String adapterName;
    private String adapterType;
    private String status;
    private Integer healthScore;
    private LocalDateTime lastActivity;
    private LocalDateTime lastHealthCheck;
    private Long totalMessagesProcessed;
    private Long messagesProcessedToday;
    private Long errorCountToday;
    private Double errorRate;
    private Long averageResponseTime;
    private Boolean isConnected;
    private List<String> recentErrors;
    private Map<String, Object> metrics;
    private List<CriticalIssue> criticalIssues;
    private String healthStatus;
    private double uptime;
    private List<String> activeIssues;

    public AdapterHealthSummary() {
    }

    public AdapterHealthSummary(String adapterId, String adapterName, String adapterType, String status,
                               Integer healthScore, LocalDateTime lastActivity, LocalDateTime lastHealthCheck,
                               Long totalMessagesProcessed, Long messagesProcessedToday, Long errorCountToday,
                               Double errorRate, Long averageResponseTime, Boolean isConnected,
                               List<String> recentErrors, Map<String, Object> metrics,
                               List<CriticalIssue> criticalIssues) {
        this.adapterId = adapterId;
        this.adapterName = adapterName;
        this.adapterType = adapterType;
        this.status = status;
        this.healthScore = healthScore;
        this.lastActivity = lastActivity;
        this.lastHealthCheck = lastHealthCheck;
        this.totalMessagesProcessed = totalMessagesProcessed;
        this.messagesProcessedToday = messagesProcessedToday;
        this.errorCountToday = errorCountToday;
        this.errorRate = errorRate;
        this.averageResponseTime = averageResponseTime;
        this.isConnected = isConnected;
        this.recentErrors = recentErrors;
        this.metrics = metrics;
        this.criticalIssues = criticalIssues;
    }

    // Getters and setters
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

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public LocalDateTime getLastHealthCheck() {
        return lastHealthCheck;
    }

    public void setLastHealthCheck(LocalDateTime lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }

    public Long getTotalMessagesProcessed() {
        return totalMessagesProcessed;
    }

    public void setTotalMessagesProcessed(Long totalMessagesProcessed) {
        this.totalMessagesProcessed = totalMessagesProcessed;
    }

    public Long getMessagesProcessedToday() {
        return messagesProcessedToday;
    }

    public void setMessagesProcessedToday(Long messagesProcessedToday) {
        this.messagesProcessedToday = messagesProcessedToday;
    }

    public Long getErrorCountToday() {
        return errorCountToday;
    }

    public void setErrorCountToday(Long errorCountToday) {
        this.errorCountToday = errorCountToday;
    }

    public Double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }

    public Long getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(Long averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public Boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public List<String> getRecentErrors() {
        return recentErrors;
    }

    public void setRecentErrors(List<String> recentErrors) {
        this.recentErrors = recentErrors;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public List<CriticalIssue> getCriticalIssues() {
        return criticalIssues;
    }

    public void setCriticalIssues(List<CriticalIssue> criticalIssues) {
        this.criticalIssues = criticalIssues;
    }

    // Alias for getStatus
    public String getHealthStatus() {
        return healthStatus != null ? healthStatus : status;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public double getUptime() {
        return uptime;
    }

    public void setUptime(double uptime) {
        this.uptime = uptime;
    }

    public List<String> getActiveIssues() {
        return activeIssues;
    }

    public void setActiveIssues(List<String> activeIssues) {
        this.activeIssues = activeIssues;
    }
}