package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;

public class HeatmapStatistics {
    private long totalExecutions;
    private double overallSuccessRate;
    private double averageDuration;
    private LocalDateTime peakExecutionTime;
    private long peakExecutionCount;
    private Integer peakExecutionHour;

    // Default constructor
    public HeatmapStatistics() {
    }

    public long getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(long totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public double getOverallSuccessRate() {
        return overallSuccessRate;
    }

    public void setOverallSuccessRate(double overallSuccessRate) {
        this.overallSuccessRate = overallSuccessRate;
    }

    public double getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(double averageDuration) {
        this.averageDuration = averageDuration;
    }

    public LocalDateTime getPeakExecutionTime() {
        return peakExecutionTime;
    }

    public void setPeakExecutionTime(LocalDateTime peakExecutionTime) {
        this.peakExecutionTime = peakExecutionTime;
    }

    public long getPeakExecutionCount() {
        return peakExecutionCount;
    }

    public void setPeakExecutionCount(long peakExecutionCount) {
        this.peakExecutionCount = peakExecutionCount;
    }

    public Integer getPeakExecutionHour() {
        return peakExecutionHour;
    }

    public void setPeakExecutionHour(Integer peakExecutionHour) {
        this.peakExecutionHour = peakExecutionHour;
    }
}