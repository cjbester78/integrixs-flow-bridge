package com.integrixs.backend.dto.dashboard.trend;

/**
 * Identified growth pattern.
 */
public class GrowthPattern {
    private String metric;
    private String type; // STABLE, LINEAR, EXPONENTIAL, DECLINING
    private double growthRate;
    private double volatility;

    // Default constructor
    public GrowthPattern() {
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }
}
