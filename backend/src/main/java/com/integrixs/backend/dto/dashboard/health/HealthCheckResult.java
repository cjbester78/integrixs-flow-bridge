package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Result of a health check operation.
 */
public class HealthCheckResult {
    private String adapterId;
    private LocalDateTime checkTime;
    private String overallStatus; // HEALTHY, WARNING, UNHEALTHY
    private int healthScore;
    private List<HealthCheckItem> checkItems;
    private List<String> recommendations;

    // Default constructor
    public HealthCheckResult() {
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public List<HealthCheckItem> getCheckItems() {
        return checkItems;
    }

    public void setCheckItems(List<HealthCheckItem> checkItems) {
        this.checkItems = checkItems;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
