package com.integrixs.backend.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Detailed performance metrics for a specific component.
 */
@Data
public class ComponentPerformance {
    
    private String componentId;
    private LocalDateTime timestamp;
    
    // Operation-level metrics
    private List<OperationMetrics> operationMetrics = new ArrayList<>();
    
    // Resource usage
    private Map<String, Object> resourceUsage;
    
    // Error statistics
    private Map<String, Object> errorStatistics;
    
    /**
     * Metrics for a specific operation.
     */
    @Data
    public static class OperationMetrics {
        private String operationName;
        private long count;
        private double meanDuration;
        private double maxDuration;
        private Map<String, Double> percentiles;
    }
}