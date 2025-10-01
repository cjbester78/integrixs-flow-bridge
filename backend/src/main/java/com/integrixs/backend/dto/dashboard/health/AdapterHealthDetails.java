package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO representing detailed adapter health information
 */
public class AdapterHealthDetails {
    private String adapterId;
    private String adapterName;
    private String adapterType;
    private String businessComponent;
    private String status;
    private Integer healthScore;
    private ConnectionStatus connectionStatus;
    private LocalDateTime lastActivity;
    private LocalDateTime lastHealthCheck;
    private LocalDateTime nextHealthCheck;

    // Performance metrics
    private Long totalMessagesProcessed;
    private Long messagesProcessedToday;
    private Long messagesProcessedThisHour;
    private Long errorCountToday;
    private Long errorCountThisHour;
    private Double errorRate;
    private Long averageResponseTime;
    private Long minResponseTime;
    private Long maxResponseTime;

    // Resource usage
    private Double cpuUsage;
    private Double memoryUsage;
    private Long threadCount;
    private Long connectionPoolSize;
    private Long activeConnections;

    // History
    private List<HealthSnapshot> healthHistory;
    private List<String> recentErrors;
    private List<CriticalIssue> criticalIssues;

    // Configuration
    private Map<String, Object> configuration;
    private Map<String, Object> metrics;
    private Map<String, String> metadata;

    // Additional fields for the service
    private LocalDateTime timestamp;
    private Map<String, Object> performanceMetrics;
    private Map<String, Object> resourceMetrics;
    private Map<String, Object> errorMetrics;
    private List<DiagnosticResult> diagnostics;

    public AdapterHealthDetails() {
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

    public String getBusinessComponent() {
        return businessComponent;
    }

    public void setBusinessComponent(String businessComponent) {
        this.businessComponent = businessComponent;
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

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
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

    public LocalDateTime getNextHealthCheck() {
        return nextHealthCheck;
    }

    public void setNextHealthCheck(LocalDateTime nextHealthCheck) {
        this.nextHealthCheck = nextHealthCheck;
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

    public Long getMessagesProcessedThisHour() {
        return messagesProcessedThisHour;
    }

    public void setMessagesProcessedThisHour(Long messagesProcessedThisHour) {
        this.messagesProcessedThisHour = messagesProcessedThisHour;
    }

    public Long getErrorCountToday() {
        return errorCountToday;
    }

    public void setErrorCountToday(Long errorCountToday) {
        this.errorCountToday = errorCountToday;
    }

    public Long getErrorCountThisHour() {
        return errorCountThisHour;
    }

    public void setErrorCountThisHour(Long errorCountThisHour) {
        this.errorCountThisHour = errorCountThisHour;
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

    public Long getMinResponseTime() {
        return minResponseTime;
    }

    public void setMinResponseTime(Long minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    public Long getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(Long maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
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

    public Long getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Long threadCount) {
        this.threadCount = threadCount;
    }

    public Long getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public void setConnectionPoolSize(Long connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public Long getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(Long activeConnections) {
        this.activeConnections = activeConnections;
    }

    public List<HealthSnapshot> getHealthHistory() {
        return healthHistory;
    }

    public void setHealthHistory(List<HealthSnapshot> healthHistory) {
        this.healthHistory = healthHistory;
    }

    public List<String> getRecentErrors() {
        return recentErrors;
    }

    public void setRecentErrors(List<String> recentErrors) {
        this.recentErrors = recentErrors;
    }

    public List<CriticalIssue> getCriticalIssues() {
        return criticalIssues;
    }

    public void setCriticalIssues(List<CriticalIssue> criticalIssues) {
        this.criticalIssues = criticalIssues;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getPerformanceMetrics() {
        return performanceMetrics;
    }

    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }

    public Map<String, Object> getResourceMetrics() {
        return resourceMetrics;
    }

    public void setResourceMetrics(Map<String, Object> resourceMetrics) {
        this.resourceMetrics = resourceMetrics;
    }

    public Map<String, Object> getErrorMetrics() {
        return errorMetrics;
    }

    public void setErrorMetrics(Map<String, Object> errorMetrics) {
        this.errorMetrics = errorMetrics;
    }

    public List<DiagnosticResult> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<DiagnosticResult> diagnostics) {
        this.diagnostics = diagnostics;
    }
}