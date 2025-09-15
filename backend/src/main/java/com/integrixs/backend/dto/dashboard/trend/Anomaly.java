package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Detected anomaly in time series data.
 */
@Data
public class Anomaly {
    private LocalDateTime timestamp;
    private double value;
    private double expectedValue;
    private double deviation; // Number of standard deviations
    private String severity; // LOW, MEDIUM, HIGH
}
