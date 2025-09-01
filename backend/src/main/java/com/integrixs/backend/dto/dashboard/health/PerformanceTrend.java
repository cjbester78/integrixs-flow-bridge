package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

/**
 * Performance trend information.
 */
@Data
public class PerformanceTrend {
    private double hourlyTrend;
    private double dailyTrend;
    private double weeklyTrend;
}