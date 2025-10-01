package com.integrixs.backend.dto.dashboard.heatmap;

import java.util.List;
import java.util.UUID;

public class CriticalPath {
    private List<UUID> flowIds;
    private double totalDuration;
    private double criticality;
    private List<String> bottlenecks;
    private String fromComponent;
    private String toComponent;
    private int trafficVolume;
    private double averageLatency;
    private double errorRate;

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

    public String getFromComponent() {
        return fromComponent;
    }

    public void setFromComponent(String fromComponent) {
        this.fromComponent = fromComponent;
    }

    public String getToComponent() {
        return toComponent;
    }

    public void setToComponent(String toComponent) {
        this.toComponent = toComponent;
    }

    public int getTrafficVolume() {
        return trafficVolume;
    }

    public void setTrafficVolume(int trafficVolume) {
        this.trafficVolume = trafficVolume;
    }

    public double getAverageLatency() {
        return averageLatency;
    }

    public void setAverageLatency(double averageLatency) {
        this.averageLatency = averageLatency;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }
}
