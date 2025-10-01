package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;
import java.util.List;

public class ExecutionCluster {
    private int clusterId;
    private double centerX;
    private double centerY;
    private List<FlowExecutionData> members;
    private double density;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long executionCount;
    private double intensity;
    private List<FlowExecutionData> executions;
    private int size;
    private long duration;

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

    public long getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(long executionCount) {
        this.executionCount = executionCount;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public List<FlowExecutionData> getExecutions() {
        return executions;
    }

    public void setExecutions(List<FlowExecutionData> executions) {
        this.executions = executions;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
