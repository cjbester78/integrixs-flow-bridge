package com.integrixs.backend.dto.dashboard.heatmap;

import java.util.Map;

/**
 * Performance distribution for a specific component.
 */
public class ComponentPerformanceDistribution {
    private String componentName;
    private long minDuration;
    private long maxDuration;
    private double averageDuration;
    private Map<String, Long> percentiles;

    // Default constructor
    public ComponentPerformanceDistribution() {
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public long getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(long minDuration) {
        this.minDuration = minDuration;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public double getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(double averageDuration) {
        this.averageDuration = averageDuration;
    }
}
