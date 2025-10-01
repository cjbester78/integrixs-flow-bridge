package com.integrixs.backend.dto;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * DTO representing execution statistics
 */
public class ExecutionStats {
    private String componentId;
    private String componentName;
    private String componentType;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // Execution counts
    private Long totalExecutions;
    private Long successfulExecutions;
    private Long failedExecutions;
    private Long pendingExecutions;
    private Long cancelledExecutions;

    // Performance metrics
    private Duration averageExecutionTime;
    private Duration minExecutionTime;
    private Duration maxExecutionTime;
    private Duration totalExecutionTime;
    private Double executionsPerMinute;

    // Success metrics
    private Double successRate;
    private Double failureRate;
    private Long consecutiveFailures;
    private LocalDateTime lastSuccess;
    private LocalDateTime lastFailure;

    // Resource usage
    private Double averageCpuUsage;
    private Double averageMemoryUsage;
    private Long averageThreadCount;

    // Data metrics
    private Long messagesProcessed;
    private Long bytesProcessed;
    private Double averageMessageSize;
    private Double throughput; // messages per second

    // Default constructor
    public ExecutionStats() {
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Long getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(Long totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public Long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public void setSuccessfulExecutions(Long successfulExecutions) {
        this.successfulExecutions = successfulExecutions;
    }

    public Long getFailedExecutions() {
        return failedExecutions;
    }

    public void setFailedExecutions(Long failedExecutions) {
        this.failedExecutions = failedExecutions;
    }

    public Long getPendingExecutions() {
        return pendingExecutions;
    }

    public void setPendingExecutions(Long pendingExecutions) {
        this.pendingExecutions = pendingExecutions;
    }

    public Long getCancelledExecutions() {
        return cancelledExecutions;
    }

    public void setCancelledExecutions(Long cancelledExecutions) {
        this.cancelledExecutions = cancelledExecutions;
    }

    public Duration getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public void setAverageExecutionTime(Duration averageExecutionTime) {
        this.averageExecutionTime = averageExecutionTime;
    }

    public Duration getMinExecutionTime() {
        return minExecutionTime;
    }

    public void setMinExecutionTime(Duration minExecutionTime) {
        this.minExecutionTime = minExecutionTime;
    }

    public Duration getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public void setMaxExecutionTime(Duration maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }

    public Duration getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public void setTotalExecutionTime(Duration totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }

    public Double getExecutionsPerMinute() {
        return executionsPerMinute;
    }

    public void setExecutionsPerMinute(Double executionsPerMinute) {
        this.executionsPerMinute = executionsPerMinute;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(Double failureRate) {
        this.failureRate = failureRate;
    }

    public Long getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(Long consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }

    public LocalDateTime getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(LocalDateTime lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    public LocalDateTime getLastFailure() {
        return lastFailure;
    }

    public void setLastFailure(LocalDateTime lastFailure) {
        this.lastFailure = lastFailure;
    }

    public Double getAverageCpuUsage() {
        return averageCpuUsage;
    }

    public void setAverageCpuUsage(Double averageCpuUsage) {
        this.averageCpuUsage = averageCpuUsage;
    }

    public Double getAverageMemoryUsage() {
        return averageMemoryUsage;
    }

    public void setAverageMemoryUsage(Double averageMemoryUsage) {
        this.averageMemoryUsage = averageMemoryUsage;
    }

    public Long getAverageThreadCount() {
        return averageThreadCount;
    }

    public void setAverageThreadCount(Long averageThreadCount) {
        this.averageThreadCount = averageThreadCount;
    }

    public Long getMessagesProcessed() {
        return messagesProcessed;
    }

    public void setMessagesProcessed(Long messagesProcessed) {
        this.messagesProcessed = messagesProcessed;
    }

    public Long getBytesProcessed() {
        return bytesProcessed;
    }

    public void setBytesProcessed(Long bytesProcessed) {
        this.bytesProcessed = bytesProcessed;
    }

    public Double getAverageMessageSize() {
        return averageMessageSize;
    }

    public void setAverageMessageSize(Double averageMessageSize) {
        this.averageMessageSize = averageMessageSize;
    }

    public Double getThroughput() {
        return throughput;
    }

    public void setThroughput(Double throughput) {
        this.throughput = throughput;
    }

    // Alias method for compatibility
    public Long getCount() {
        return totalExecutions;
    }

    // Additional alias methods for FlowExecutionHeatmapService compatibility
    public void setCount(int count) {
        this.totalExecutions = Long.valueOf(count);
    }

    public void setAverageDuration(double duration) {
        this.averageExecutionTime = Duration.ofMillis((long) duration);
    }
}