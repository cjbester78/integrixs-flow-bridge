package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Temporal patterns analysis (hourly, daily, weekly).
 */
@Data
public class TemporalPatterns {
    private String metric;
    private int analysisPeriodDays;
    private Map<Integer, HourlyStats> hourlyPatterns;
    private Map<String, DailyStats> dailyPatterns;
    private List<PeakPeriod> peakPeriods;
    private double seasonalityScore; // 0.0 to 1.0
}