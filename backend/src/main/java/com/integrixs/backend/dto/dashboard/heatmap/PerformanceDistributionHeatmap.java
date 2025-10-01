package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Performance distribution heatmap.
 */
public class PerformanceDistributionHeatmap {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String flowId;
    private Map<String, Integer> durationBuckets; // Bucket -> Count
    private Map<String, Long> percentiles;
    private List<Long> outliers;
    private Map<String, ComponentPerformanceDistribution> componentDistributions;

    // Default constructor
    public PerformanceDistributionHeatmap() {
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public List<Long> getOutliers() {
        return outliers;
    }

    public void setOutliers(List<Long> outliers) {
        this.outliers = outliers;
    }
}
