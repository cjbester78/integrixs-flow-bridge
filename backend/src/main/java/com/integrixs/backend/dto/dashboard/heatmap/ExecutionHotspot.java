package com.integrixs.backend.dto.dashboard.heatmap;

public class ExecutionHotspot {
    private int flowIndex;
    private int timeSlotIndex;
    private double intensity;
    private TimeSlot timeSlot;
    private String description;

    // Default constructor
    public ExecutionHotspot() {
    }

    public int getFlowIndex() {
        return flowIndex;
    }

    public void setFlowIndex(int flowIndex) {
        this.flowIndex = flowIndex;
    }

    public int getTimeSlotIndex() {
        return timeSlotIndex;
    }

    public void setTimeSlotIndex(int timeSlotIndex) {
        this.timeSlotIndex = timeSlotIndex;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}