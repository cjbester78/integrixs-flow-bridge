package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Summary of flow activity.
 */
public class FlowActivitySummary {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<FlowActivityMetrics> flowMetrics;
    private List<String> mostActiveFlows;
    private int totalExecutions;
    private int uniqueFlows;
    private double overallSuccessRate;

    // Default constructor
    public FlowActivitySummary() {
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<FlowActivityMetrics> getFlowMetrics() {
        return flowMetrics;
    }

    public void setFlowMetrics(List<FlowActivityMetrics> flowMetrics) {
        this.flowMetrics = flowMetrics;
    }

    public List<String> getMostActiveFlows() {
        return mostActiveFlows;
    }

    public void setMostActiveFlows(List<String> mostActiveFlows) {
        this.mostActiveFlows = mostActiveFlows;
    }

    public int getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(int totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public int getUniqueFlows() {
        return uniqueFlows;
    }

    public void setUniqueFlows(int uniqueFlows) {
        this.uniqueFlows = uniqueFlows;
    }

    public double getOverallSuccessRate() {
        return overallSuccessRate;
    }

    public void setOverallSuccessRate(double overallSuccessRate) {
        this.overallSuccessRate = overallSuccessRate;
    }
}
