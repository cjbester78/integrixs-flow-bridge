package com.integrixs.backend.dto.dashboard.trend;

/**
 * Capacity projection for a resource.
 */
public class CapacityProjection {
    private String resource;
    private double currentUsage;
    private double projectedUsage;
    private int daysUntilCapacity;
    private double confidence; // 0.0 to 1.0

    // Default constructor
    public CapacityProjection() {
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public double getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(double currentUsage) {
        this.currentUsage = currentUsage;
    }

    public double getProjectedUsage() {
        return projectedUsage;
    }

    public void setProjectedUsage(double projectedUsage) {
        this.projectedUsage = projectedUsage;
    }

    public int getDaysUntilCapacity() {
        return daysUntilCapacity;
    }

    public void setDaysUntilCapacity(int daysUntilCapacity) {
        this.daysUntilCapacity = daysUntilCapacity;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
