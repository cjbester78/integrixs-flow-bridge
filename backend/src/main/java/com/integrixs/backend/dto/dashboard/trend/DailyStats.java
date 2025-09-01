package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

/**
 * Statistics for a specific day of week.
 */
@Data
public class DailyStats {
    private String dayOfWeek;
    private double averageValue;
    private int sampleCount;
}