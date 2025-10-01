package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;

/**
 * Resource usage metrics for an adapter.
 */
public class ResourceUsageMetrics {
    private String adapterId;
    private double cpuUsage; // percentage
    private long memoryUsageMB;
    private int threadCount;
    private int fileHandles;
    private double networkBandwidthKBps;
    private LocalDateTime lastUpdated;

    // Default constructor
    public ResourceUsageMetrics() {
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public long getMemoryUsageMB() {
        return memoryUsageMB;
    }

    public void setMemoryUsageMB(long memoryUsageMB) {
        this.memoryUsageMB = memoryUsageMB;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getFileHandles() {
        return fileHandles;
    }

    public void setFileHandles(int fileHandles) {
        this.fileHandles = fileHandles;
    }

    public double getNetworkBandwidthKBps() {
        return networkBandwidthKBps;
    }

    public void setNetworkBandwidthKBps(double networkBandwidthKBps) {
        this.networkBandwidthKBps = networkBandwidthKBps;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
