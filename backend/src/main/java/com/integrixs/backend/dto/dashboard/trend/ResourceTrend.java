package com.integrixs.backend.dto.dashboard.trend;

/**
 * Resource utilization trend.
 */
public class ResourceTrend {
    private String metric;
    private double currentValue;
    private double averageValue;
    private double peakValue;
    private String trend;
    private double growthRate; // Percentage

    // Default constructor
    public ResourceTrend() {
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(double averageValue) {
        this.averageValue = averageValue;
    }

    public double getPeakValue() {
        return peakValue;
    }

    public void setPeakValue(double peakValue) {
        this.peakValue = peakValue;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }
}
