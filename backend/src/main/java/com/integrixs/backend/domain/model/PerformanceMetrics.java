package com.integrixs.backend.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model for flow performance metrics
 */
public class PerformanceMetrics {

    private String flowId;
    private int totalExecutions;
    private int successfulExecutions;
    private int failedExecutions;
    private double averageExecutionTimeMs;
    private long minExecutionTimeMs;
    private long maxExecutionTimeMs;
    private LocalDateTime lastUpdate;

    /**
     * Calculates the success rate percentage
     */
    public double getSuccessRate() {
        if(totalExecutions == 0) {
            return 0.0;
        }
        return(double) successfulExecutions / totalExecutions * 100;
    }

    // Default constructor
    public PerformanceMetrics() {
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public int getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(int totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public int getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public void setSuccessfulExecutions(int successfulExecutions) {
        this.successfulExecutions = successfulExecutions;
    }

    public int getFailedExecutions() {
        return failedExecutions;
    }

    public void setFailedExecutions(int failedExecutions) {
        this.failedExecutions = failedExecutions;
    }

    public double getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public void setAverageExecutionTimeMs(double averageExecutionTimeMs) {
        this.averageExecutionTimeMs = averageExecutionTimeMs;
    }

    public long getMinExecutionTimeMs() {
        return minExecutionTimeMs;
    }

    public void setMinExecutionTimeMs(long minExecutionTimeMs) {
        this.minExecutionTimeMs = minExecutionTimeMs;
    }

    public long getMaxExecutionTimeMs() {
        return maxExecutionTimeMs;
    }

    public void setMaxExecutionTimeMs(long maxExecutionTimeMs) {
        this.maxExecutionTimeMs = maxExecutionTimeMs;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
