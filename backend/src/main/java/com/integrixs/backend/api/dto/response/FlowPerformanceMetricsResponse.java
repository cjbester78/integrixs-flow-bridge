package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for flow performance metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowPerformanceMetricsResponse {

    private String flowId;
    private String flowName;
    private int totalExecutions;
    private int successfulExecutions;
    private int failedExecutions;
    private double successRate;
    private double averageExecutionTimeMs;
    private long minExecutionTimeMs;
    private long maxExecutionTimeMs;
    private LocalDateTime lastUpdate;
}
