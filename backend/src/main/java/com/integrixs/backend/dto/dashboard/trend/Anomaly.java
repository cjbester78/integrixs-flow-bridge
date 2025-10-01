package com.integrixs.backend.dto.dashboard.trend;

import java.time.LocalDateTime;

/**
 * Detected anomaly in time series data.
 */
public class Anomaly {
    private LocalDateTime timestamp;
    private double value;
    private double expectedValue;
    private double deviation; // Number of standard deviations
    private String severity; // LOW, MEDIUM, HIGH

    // Default constructor
    public Anomaly() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(double expectedValue) {
        this.expectedValue = expectedValue;
    }

    public double getDeviation() {
        return deviation;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
