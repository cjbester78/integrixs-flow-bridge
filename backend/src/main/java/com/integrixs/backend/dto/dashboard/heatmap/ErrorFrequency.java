package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

import java.util.Map;

/**
 * Error frequency analysis.
 */
@Data
public class ErrorFrequency {
    private String errorType;
    private int totalCount;
    private Map<Integer, Long> hourlyDistribution; // Hour -> Count
}