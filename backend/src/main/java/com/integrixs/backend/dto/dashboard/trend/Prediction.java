package com.integrixs.backend.dto.dashboard.trend;

import java.time.LocalDateTime;

/**
 * Future prediction based on historical data.
 */
public class Prediction {
    private LocalDateTime timestamp;
    private double predictedValue;
    private double confidenceLevel; // 0.0 to 1.0

    // Default constructor
    public Prediction() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getPredictedValue() {
        return predictedValue;
    }

    public void setPredictedValue(double predictedValue) {
        this.predictedValue = predictedValue;
    }

    public double getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }
}
