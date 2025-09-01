package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Time series data point.
 */
@Data
public class TimeSeriesDataPoint {
    private LocalDateTime timestamp;
    private double value;
    private String metricName;
}