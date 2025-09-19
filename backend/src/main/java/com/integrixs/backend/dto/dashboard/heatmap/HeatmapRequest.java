package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class HeatmapRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<UUID> flowIds;
    private String granularity;
    private String metric;
    private String groupBy;

    // Default constructor
    public HeatmapRequest() {
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

    public List<UUID> getFlowIds() {
        return flowIds;
    }

    public void setFlowIds(List<UUID> flowIds) {
        this.flowIds = flowIds;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }
}