package com.integrixs.backend.api.dto.response;

/**
 * Response DTO for flow execution statistics
 */
public class FlowExecutionStatisticsResponse {

    private int activeExecutions;
    private int runningExecutions;
    private int completedExecutions;
    private int failedExecutions;
    private double averageExecutionTimeMs;
    private int uniqueFlowsMonitored;

    // Default constructor
    public FlowExecutionStatisticsResponse() {
    }

    public int getActiveExecutions() {
        return activeExecutions;
    }

    public void setActiveExecutions(int activeExecutions) {
        this.activeExecutions = activeExecutions;
    }

    public int getRunningExecutions() {
        return runningExecutions;
    }

    public void setRunningExecutions(int runningExecutions) {
        this.runningExecutions = runningExecutions;
    }

    public int getCompletedExecutions() {
        return completedExecutions;
    }

    public void setCompletedExecutions(int completedExecutions) {
        this.completedExecutions = completedExecutions;
    }

    public int getFailedExecutions() {
        return failedExecutions;
    }

    public void setFailedExecutions(int failedExecutions) {
        this.failedExecutions = failedExecutions;
    }

    public double getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public void setAverageExecutionTimeMs(double averageExecutionTimeMs) {
        this.averageExecutionTimeMs = averageExecutionTimeMs;
    }

    public int getUniqueFlowsMonitored() {
        return uniqueFlowsMonitored;
    }

    public void setUniqueFlowsMonitored(int uniqueFlowsMonitored) {
        this.uniqueFlowsMonitored = uniqueFlowsMonitored;
    }

    public static FlowExecutionStatisticsResponseBuilder builder() {
        return new FlowExecutionStatisticsResponseBuilder();
    }

    public static class FlowExecutionStatisticsResponseBuilder {
        private FlowExecutionStatisticsResponse response = new FlowExecutionStatisticsResponse();

        public FlowExecutionStatisticsResponseBuilder activeExecutions(int activeExecutions) {
            response.setActiveExecutions(activeExecutions);
            return this;
        }

        public FlowExecutionStatisticsResponseBuilder runningExecutions(int runningExecutions) {
            response.setRunningExecutions(runningExecutions);
            return this;
        }

        public FlowExecutionStatisticsResponseBuilder completedExecutions(int completedExecutions) {
            response.setCompletedExecutions(completedExecutions);
            return this;
        }

        public FlowExecutionStatisticsResponseBuilder failedExecutions(int failedExecutions) {
            response.setFailedExecutions(failedExecutions);
            return this;
        }

        public FlowExecutionStatisticsResponseBuilder averageExecutionTimeMs(double averageExecutionTimeMs) {
            response.setAverageExecutionTimeMs(averageExecutionTimeMs);
            return this;
        }

        public FlowExecutionStatisticsResponseBuilder uniqueFlowsMonitored(int uniqueFlowsMonitored) {
            response.setUniqueFlowsMonitored(uniqueFlowsMonitored);
            return this;
        }

        public FlowExecutionStatisticsResponse build() {
            return response;
        }
    }
}
