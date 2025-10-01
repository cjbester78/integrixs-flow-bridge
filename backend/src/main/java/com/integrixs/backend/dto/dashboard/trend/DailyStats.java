package com.integrixs.backend.dto.dashboard.trend;

/**
 * Statistics for a specific day of week.
 */
public class DailyStats {
    private String dayOfWeek;
    private double averageValue;
    private int sampleCount;

    // Default constructor
    public DailyStats() {
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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
