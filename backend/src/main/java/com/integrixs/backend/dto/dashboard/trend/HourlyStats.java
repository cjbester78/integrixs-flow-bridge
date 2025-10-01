package com.integrixs.backend.dto.dashboard.trend;

/**
 * Statistics for a specific hour of day.
 */
public class HourlyStats {
    private int hour; // 0-23
    private double averageValue;
    private int sampleCount;

    // Default constructor
    public HourlyStats() {
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public double getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(double averageValue) {
        this.averageValue = averageValue;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }
}
