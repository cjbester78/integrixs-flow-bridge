package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Performance distribution heatmap.
 */
@Data
public class PerformanceDistributionHeatmap {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String flowId;
    private Map<String, Integer> durationBuckets; // Bucket -> Count
    private Map<String, Long> percentiles;
    private List<Long> outliers;
    private Map<String, ComponentPerformanceDistribution> componentDistributions;
}
