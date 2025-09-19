package com.integrixs.backend.dto.dashboard.health;

import java.util.Map;

public class AdapterHealthScore {
    private double overallScore;
    private Map<String, Double> componentScores;
    private String rating;
    private String trend;
    
    public enum Rating {
        EXCELLENT, GOOD, FAIR, POOR, CRITICAL
    }

    // Default constructor
    public AdapterHealthScore() {
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }
}