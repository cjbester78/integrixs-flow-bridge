package com.integrixs.backend.dto.dashboard.trend;

import java.time.LocalDateTime;

/**
 * Time series data point.
 */
public class TimeSeriesDataPoint {
    private LocalDateTime timestamp;
    private double value;
    private String metricName;

    // Default constructor
    public TimeSeriesDataPoint() {
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

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
}
