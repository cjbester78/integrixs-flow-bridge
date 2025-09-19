package com.integrixs.backend.dto.dashboard.trend;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Identified peak usage period.
 */
public class PeakPeriod {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;
    private double peakValue;

    // Default constructor
    public PeakPeriod() {
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

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public double getPeakValue() {
        return peakValue;
    }

    public void setPeakValue(double peakValue) {
        this.peakValue = peakValue;
    }
}
