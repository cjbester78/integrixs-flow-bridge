package com.integrixs.backend.dto.dashboard.health;

/**
 * Performance trend information.
 */
public class PerformanceTrend {
    private double hourlyTrend;
    private double dailyTrend;
    private double weeklyTrend;

    // Default constructor
    public PerformanceTrend() {
    }

    public double getHourlyTrend() {
        return hourlyTrend;
    }

    public void setHourlyTrend(double hourlyTrend) {
        this.hourlyTrend = hourlyTrend;
    }

    public double getDailyTrend() {
        return dailyTrend;
    }

    public void setDailyTrend(double dailyTrend) {
        this.dailyTrend = dailyTrend;
    }

    public double getWeeklyTrend() {
        return weeklyTrend;
    }

    public void setWeeklyTrend(double weeklyTrend) {
        this.weeklyTrend = weeklyTrend;
    }
}
