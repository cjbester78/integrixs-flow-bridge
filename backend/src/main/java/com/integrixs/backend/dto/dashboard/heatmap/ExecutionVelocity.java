package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;

public class ExecutionVelocity {
    private LocalDateTime timestamp;
    private double velocity;
    private double acceleration;
    private String trend;
    private double currentRate;
    private double averageRate;
    private double changePercentage;

    // Default constructor
    public ExecutionVelocity() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public double getCurrentRate() {
        return currentRate;
    }

    public void setCurrentRate(double currentRate) {
        this.currentRate = currentRate;
    }

    public double getAverageRate() {
        return averageRate;
    }

    public void setAverageRate(double averageRate) {
        this.averageRate = averageRate;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    public void setChangePercentage(double changePercentage) {
        this.changePercentage = changePercentage;
    }

    // Method for numeric trend
    public double getTrendValue() {
        // Return trend as numeric value
        if ("increasing".equals(trend)) return 1.0;
        if ("decreasing".equals(trend)) return -1.0;
        return 0.0;
    }
}
