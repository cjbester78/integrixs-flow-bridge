package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;

/**
 * Response DTO for flow performance metrics
 */
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

    // Default constructor
    public FlowPerformanceMetricsResponse() {
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public int getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(int totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public int getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public void setSuccessfulExecutions(int successfulExecutions) {
        this.successfulExecutions = successfulExecutions;
    }

    public int getFailedExecutions() {
        return failedExecutions;
    }

    public void setFailedExecutions(int failedExecutions) {
        this.failedExecutions = failedExecutions;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public void setAverageExecutionTimeMs(double averageExecutionTimeMs) {
        this.averageExecutionTimeMs = averageExecutionTimeMs;
    }

    public long getMinExecutionTimeMs() {
        return minExecutionTimeMs;
    }

    public void setMinExecutionTimeMs(long minExecutionTimeMs) {
        this.minExecutionTimeMs = minExecutionTimeMs;
    }

    public long getMaxExecutionTimeMs() {
        return maxExecutionTimeMs;
    }

    public void setMaxExecutionTimeMs(long maxExecutionTimeMs) {
        this.maxExecutionTimeMs = maxExecutionTimeMs;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public static FlowPerformanceMetricsResponseBuilder builder() {
        return new FlowPerformanceMetricsResponseBuilder();
    }

    public static class FlowPerformanceMetricsResponseBuilder {
        private FlowPerformanceMetricsResponse response = new FlowPerformanceMetricsResponse();

        public FlowPerformanceMetricsResponseBuilder flowId(String flowId) {
            response.setFlowId(flowId);
            return this;
        }

        public FlowPerformanceMetricsResponseBuilder flowName(String flowName) {
            response.setFlowName(flowName);
            return this;
        }

        public FlowPerformanceMetricsResponseBuilder totalExecutions(int totalExecutions) {
            response.setTotalExecutions(totalExecutions);
            return this;
        }

        public FlowPerformanceMetricsResponseBuilder successfulExecutions(int successfulExecutions) {
            response.setSuccessfulExecutions(successfulExecutions);
            return this;
        }

        public FlowPerformanceMetricsResponseBuilder failedExecutions(int failedExecutions) {
            response.setFailedExecutions(failedExecutions);
            return this;
        }

        public FlowPerformanceMetricsResponseBuilder successRate(double successRate) {
            response.setSuccessRate(successRate);
            return this;
        }

        public FlowPerformanceMetricsResponseBuilder averageExecutionTimeMs(double averageExecutionTimeMs) {
            response.setAverageExecutionTimeMs(averageExecutionTimeMs);
            return this;
        }

        public FlowPerformanceMetricsResponseBuilder minExecutionTimeMs(long minExecutionTimeMs) {
            response.setMinExecutionTimeMs(minExecutionTimeMs);
            return this;
        }

        public FlowPerformanceMetricsResponseBuilder maxExecutionTimeMs(long maxExecutionTimeMs) {
            response.setMaxExecutionTimeMs(maxExecutionTimeMs);
            return this;
        }

        public FlowPerformanceMetricsResponseBuilder lastUpdate(LocalDateTime lastUpdate) {
            response.setLastUpdate(lastUpdate);
            return this;
        }

        public FlowPerformanceMetricsResponse build() {
            return response;
        }
    }
}
