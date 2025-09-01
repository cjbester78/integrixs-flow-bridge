package com.integrixs.backend.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Performance snapshot for historical tracking.
 */
@Data
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
}