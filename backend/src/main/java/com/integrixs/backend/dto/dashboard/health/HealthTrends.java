package com.integrixs.backend.dto.dashboard.health;

/**
 * Health trends across all adapters.
 */
public class HealthTrends {
    private double errorRateTrend; // positive means increasing errors
    private double responseTimeTrend; // positive means slower
    private double availabilityTrend; // positive means improving

    // Default constructor
    public HealthTrends() {
    }

    public double getErrorRateTrend() {
        return errorRateTrend;
    }

    public void setErrorRateTrend(double errorRateTrend) {
        this.errorRateTrend = errorRateTrend;
    }

    public double getResponseTimeTrend() {
        return responseTimeTrend;
    }

    public void setResponseTimeTrend(double responseTimeTrend) {
        this.responseTimeTrend = responseTimeTrend;
    }

    public double getAvailabilityTrend() {
        return availabilityTrend;
    }

    public void setAvailabilityTrend(double availabilityTrend) {
        this.availabilityTrend = availabilityTrend;
    }
}
