package com.integrixs.backend.dto.dashboard.health;

/**
 * Statistics calculated from health history.
 */
public class HealthHistoryStatistics {
    private double averageHealthScore;
    private int minHealthScore;
    private int maxHealthScore;
    private int totalErrors;
    private int totalStatusChanges;
    private double uptimePercentage;

    // Default constructor
    public HealthHistoryStatistics() {
    }

    public double getAverageHealthScore() {
        return averageHealthScore;
    }

    public void setAverageHealthScore(double averageHealthScore) {
        this.averageHealthScore = averageHealthScore;
    }

    public int getMinHealthScore() {
        return minHealthScore;
    }

    public void setMinHealthScore(int minHealthScore) {
        this.minHealthScore = minHealthScore;
    }

    public int getMaxHealthScore() {
        return maxHealthScore;
    }

    public void setMaxHealthScore(int maxHealthScore) {
        this.maxHealthScore = maxHealthScore;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(int totalErrors) {
        this.totalErrors = totalErrors;
    }

    public int getTotalStatusChanges() {
        return totalStatusChanges;
    }

    public void setTotalStatusChanges(int totalStatusChanges) {
        this.totalStatusChanges = totalStatusChanges;
    }

    public double getUptimePercentage() {
        return uptimePercentage;
    }

    public void setUptimePercentage(double uptimePercentage) {
        this.uptimePercentage = uptimePercentage;
    }
}
