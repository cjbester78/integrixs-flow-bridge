package com.integrixs.backend.dto.dashboard.trend;

/**
 * Statistical summary for a component.
 */
public class ComponentStats {
    private double mean;
    private double max;
    private double min;
    private double stdDev;
    private String trend;

    // Default constructor
    public ComponentStats() {
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getStdDev() {
        return stdDev;
    }

    public void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }
}
