package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Resource usage metrics for an adapter.
 */
@Data
public class ResourceUsageMetrics {
    private String adapterId;
    private double cpuUsage; // percentage
    private long memoryUsageMB;
    private int threadCount;
    private int fileHandles;
    private double networkBandwidthKBps;
    private LocalDateTime lastUpdated;
}