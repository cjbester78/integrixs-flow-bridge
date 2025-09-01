package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

import java.util.Map;

/**
 * Performance distribution for a specific component.
 */
@Data
public class ComponentPerformanceDistribution {
    private String componentName;
    private long minDuration;
    private long maxDuration;
    private double averageDuration;
    private Map<String, Long> percentiles;
}