package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

/**
 * Resource utilization trend.
 */
@Data
public class ResourceTrend {
    private String metric;
    private double currentValue;
    private double averageValue;
    private double peakValue;
    private String trend;
    private double growthRate; // Percentage
}
