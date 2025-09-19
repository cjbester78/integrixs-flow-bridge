package com.integrixs.backend.dto.dashboard.heatmap;

import java.util.List;
import java.util.UUID;

public class CriticalPath {
    private List<UUID> flowIds;
    private double totalDuration;
    private double criticality;
    private List<String> bottlenecks;

    // Default constructor
    public CriticalPath() {
    }

    public List<UUID> getFlowIds() {
        return flowIds;
    }

    public void setFlowIds(List<UUID> flowIds) {
        this.flowIds = flowIds;
    }

    public double getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(double totalDuration) {
        this.totalDuration = totalDuration;
    }

    public double getCriticality() {
        return criticality;
    }

    public void setCriticality(double criticality) {
        this.criticality = criticality;
    }

    public List<String> getBottlenecks() {
        return bottlenecks;
    }

    public void setBottlenecks(List<String> bottlenecks) {
        this.bottlenecks = bottlenecks;
    }
}
