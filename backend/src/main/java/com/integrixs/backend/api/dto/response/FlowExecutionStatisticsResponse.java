package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for flow execution statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowExecutionStatisticsResponse {
    
    private int activeExecutions;
    private int runningExecutions;
    private int completedExecutions;
    private int failedExecutions;
    private double averageExecutionTimeMs;
    private int uniqueFlowsMonitored;
}