package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;
import java.util.List;

public class FlowExecutionHeatmap {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String granularity;
    private List<TimeSlot> timeGrid;
    private double[][] executionGrid;
    private double[][] successRateGrid;
    private List<ExecutionHotspot> hotspots;
    private HeatmapStatistics statistics;

    // Default constructor
    public FlowExecutionHeatmap() {
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

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public List<TimeSlot> getTimeGrid() {
        return timeGrid;
    }

    public void setTimeGrid(List<TimeSlot> timeGrid) {
        this.timeGrid = timeGrid;
    }

    public double[][] getExecutionGrid() {
        return executionGrid;
    }

    public void setExecutionGrid(double[][] executionGrid) {
        this.executionGrid = executionGrid;
    }

    // Alias method for compatibility
    public void setPerformanceGrid(double[][] performanceGrid) {
        this.executionGrid = performanceGrid;
    }

    public double[][] getSuccessRateGrid() {
        return successRateGrid;
    }

    public void setSuccessRateGrid(double[][] successRateGrid) {
        this.successRateGrid = successRateGrid;
    }

    public List<ExecutionHotspot> getHotspots() {
        return hotspots;
    }

    public void setHotspots(List<ExecutionHotspot> hotspots) {
        this.hotspots = hotspots;
    }

    public HeatmapStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(HeatmapStatistics statistics) {
        this.statistics = statistics;
    }
}