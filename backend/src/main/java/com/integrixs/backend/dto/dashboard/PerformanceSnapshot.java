package com.integrixs.backend.dto.dashboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Performance snapshot for historical tracking.
 */
public class PerformanceSnapshot {

    private LocalDateTime timestamp;

    // System metrics
    private double cpuUsage;
    private double memoryUsage;
    private int threadCount;

    // Overall metrics
    private double totalThroughput;
    private double errorRate;

    // Component summaries
    private List<Map<String, Object>> componentSummaries;

    // Default constructor
    public PerformanceSnapshot() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public double getTotalThroughput() {
        return totalThroughput;
    }

    public void setTotalThroughput(double totalThroughput) {
        this.totalThroughput = totalThroughput;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public List<Map<String, Object>> getComponentSummaries() {
        return componentSummaries;
    }

    public void setComponentSummaries(List<Map<String, Object>> componentSummaries) {
        this.componentSummaries = componentSummaries;
    }
}
