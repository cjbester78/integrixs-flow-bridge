package com.integrixs.backend.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Domain model for flow performance metrics
 */
@Data
public class PerformanceMetrics {

    private String flowId;
    private int totalExecutions;
    private int successfulExecutions;
    private int failedExecutions;
    private double averageExecutionTimeMs;
    private long minExecutionTimeMs;
    private long maxExecutionTimeMs;
    private LocalDateTime lastUpdate;

    /**
     * Calculates the success rate percentage
     */
    public double getSuccessRate() {
        if(totalExecutions == 0) {
            return 0.0;
        }
        return(double) successfulExecutions / totalExecutions * 100;
    }
}
