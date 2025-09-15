package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Future prediction based on historical data.
 */
@Data
public class Prediction {
    private LocalDateTime timestamp;
    private double predictedValue;
    private double confidenceLevel; // 0.0 to 1.0
}
