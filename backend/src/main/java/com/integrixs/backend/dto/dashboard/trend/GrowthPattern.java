package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

/**
 * Identified growth pattern.
 */
@Data
public class GrowthPattern {
    private String metric;
    private String type; // STABLE, LINEAR, EXPONENTIAL, DECLINING
    private double growthRate;
    private double volatility;
}