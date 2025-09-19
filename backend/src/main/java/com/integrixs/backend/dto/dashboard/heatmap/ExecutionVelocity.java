package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;

public class ExecutionVelocity {
    private LocalDateTime timestamp;
    private double velocity;
    private double acceleration;
    private String trend;

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
}
