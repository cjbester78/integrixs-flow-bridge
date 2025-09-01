package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Identified peak usage period.
 */
@Data
public class PeakPeriod {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;
    private double peakValue;
}