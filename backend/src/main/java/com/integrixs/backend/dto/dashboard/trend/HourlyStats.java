package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

/**
 * Statistics for a specific hour of day.
 */
@Data
public class HourlyStats {
    private int hour; // 0-23
    private double averageValue;
    private int sampleCount;
}
