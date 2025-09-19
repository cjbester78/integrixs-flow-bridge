package com.integrixs.backend.dto.dashboard.heatmap;

import java.util.List;

public class ExecutionCluster {
    private int clusterId;
    private double centerX;
    private double centerY;
    private List<FlowExecutionData> members;
    private double density;
    private String description;

    // Default constructor
    public ExecutionCluster() {
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public List<FlowExecutionData> getMembers() {
        return members;
    }

    public void setMembers(List<FlowExecutionData> members) {
        this.members = members;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
