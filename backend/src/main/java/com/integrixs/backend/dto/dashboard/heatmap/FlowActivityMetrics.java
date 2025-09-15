package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

/**
 * Activity metrics for a specific flow.
 */
@Data
public class FlowActivityMetrics {
    private String flowId;
    private int executionCount;
    private double successRate;
    private double averageDuration;
    private double activityScore; // Executions per hour
}
