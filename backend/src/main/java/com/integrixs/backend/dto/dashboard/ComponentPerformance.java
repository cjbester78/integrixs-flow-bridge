package com.integrixs.backend.dto.dashboard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Detailed performance metrics for a specific component.
 */
public class ComponentPerformance {

    private String componentId;
    private LocalDateTime timestamp;

    // Operation - level metrics
    private List<OperationMetrics> operationMetrics = new ArrayList<>();

    // Resource usage
    private Map<String, Object> resourceUsage;

    // Error statistics
    private Map<String, Object> errorStatistics;

    /**
     * Metrics for a specific operation.
     */
    public static class OperationMetrics {
        private String operationName;
        private long count;
        private double meanDuration;
        private double maxDuration;
        private Map<String, Double> percentiles;

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public double getMeanDuration() {
            return meanDuration;
        }

        public void setMeanDuration(double meanDuration) {
            this.meanDuration = meanDuration;
        }

        public double getMaxDuration() {
            return maxDuration;
        }

        public void setMaxDuration(double maxDuration) {
            this.maxDuration = maxDuration;
        }

        public Map<String, Double> getPercentiles() {
            return percentiles;
        }

        public void setPercentiles(Map<String, Double> percentiles) {
            this.percentiles = percentiles;
        }
    }

    // Default constructor
    public ComponentPerformance() {
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<OperationMetrics> getOperationMetrics() {
        return operationMetrics;
    }

    public void setOperationMetrics(List<OperationMetrics> operationMetrics) {
        this.operationMetrics = operationMetrics;
    }

    public Map<String, Object> getResourceUsage() {
        return resourceUsage;
    }

    public void setResourceUsage(Map<String, Object> resourceUsage) {
        this.resourceUsage = resourceUsage;
    }

    public Map<String, Object> getErrorStatistics() {
        return errorStatistics;
    }

    public void setErrorStatistics(Map<String, Object> errorStatistics) {
        this.errorStatistics = errorStatistics;
    }
}
