package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Information about a peak period.
 */
@Data
public class PeakPeriodInfo {
    private int hour;
    private int dayOffset;
    private String dayOfWeek;
    private int weekOffset;
    private int executionCount;
    private LocalDateTime timestamp;
}