package com.integrixs.backend.dto.dashboard.health;

/**
 * Current health metrics for an adapter.
 */
public class AdapterHealthMetrics {
    private String adapterId;
    private double requestsPerMinute;
    private double errorRate; // percentage
    private double successRate; // percentage
    private double averageResponseTime; // milliseconds
    private double uptime; // percentage

    // Default constructor
    public AdapterHealthMetrics() {
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public double getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public void setRequestsPerMinute(double requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public double getUptime() {
        return uptime;
    }

    public void setUptime(double uptime) {
        this.uptime = uptime;
    }
}
