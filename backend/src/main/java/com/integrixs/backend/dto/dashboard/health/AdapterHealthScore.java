package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;
import java.util.Map;

public class AdapterHealthScore {
    private double overallScore;
    private Map<String, Double> componentScores;
    private String rating;
    private String trend;
    private double connectionScore;
    private double performanceScore;
    private double errorScore;
    private double resourceScore;
    private double slaComplianceScore;
    private LocalDateTime calculatedAt;

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

    public Map<String, Double> getComponentScores() {
        return componentScores;
    }

    public void setComponentScores(Map<String, Double> componentScores) {
        this.componentScores = componentScores;
    }

    public double getConnectionScore() {
        return connectionScore;
    }

    public void setConnectionScore(double connectionScore) {
        this.connectionScore = connectionScore;
    }

    public double getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(double performanceScore) {
        this.performanceScore = performanceScore;
    }

    public double getErrorScore() {
        return errorScore;
    }

    public void setErrorScore(double errorScore) {
        this.errorScore = errorScore;
    }

    public double getResourceScore() {
        return resourceScore;
    }

    public void setResourceScore(double resourceScore) {
        this.resourceScore = resourceScore;
    }

    public double getSlaComplianceScore() {
        return slaComplianceScore;
    }

    public void setSlaComplianceScore(double slaComplianceScore) {
        this.slaComplianceScore = slaComplianceScore;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}