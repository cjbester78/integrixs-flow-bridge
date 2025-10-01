package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;

/**
 * Information about a peak period.
 */
public class PeakPeriodInfo {
    private int hour;
    private int dayOffset;
    private String dayOfWeek;
    private int weekOffset;
    private int executionCount;
    private LocalDateTime timestamp;

    // Default constructor
    public PeakPeriodInfo() {
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDayOffset() {
        return dayOffset;
    }

    public void setDayOffset(int dayOffset) {
        this.dayOffset = dayOffset;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getWeekOffset() {
        return weekOffset;
    }

    public void setWeekOffset(int weekOffset) {
        this.weekOffset = weekOffset;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
