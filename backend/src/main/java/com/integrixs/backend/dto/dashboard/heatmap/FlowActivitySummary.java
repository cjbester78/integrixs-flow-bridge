package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Summary of flow activity.
 */
@Data
public class FlowActivitySummary {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<FlowActivityMetrics> flowMetrics;
    private List<String> mostActiveFlows;
    private int totalExecutions;
    private int uniqueFlows;
    private double overallSuccessRate;
}
