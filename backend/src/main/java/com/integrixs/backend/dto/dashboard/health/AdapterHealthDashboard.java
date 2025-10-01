package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main dashboard containing health information for all adapters.
 */
public class AdapterHealthDashboard {
    private LocalDateTime timestamp;
    private int totalAdapters;
    private int healthyAdapters;
    private int unhealthyAdapters;
    private int warningAdapters;
    private int inactiveAdapters;
    private int overallHealthScore; // 0-100
    private List<AdapterHealthDetail> adapterHealthDetails;
    private List<AdapterHealthSummary> adapterHealthSummaries;
    private HealthTrends healthTrends;
    private List<CriticalAlert> criticalAlerts;
    private int criticalAdapters;
    private List<CriticalIssue> criticalIssues;
    private List<String> recommendations;

    // Default constructor
    public AdapterHealthDashboard() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotalAdapters() {
        return totalAdapters;
    }

    public void setTotalAdapters(int totalAdapters) {
        this.totalAdapters = totalAdapters;
    }

    public int getHealthyAdapters() {
        return healthyAdapters;
    }

    public void setHealthyAdapters(int healthyAdapters) {
        this.healthyAdapters = healthyAdapters;
    }

    public int getUnhealthyAdapters() {
        return unhealthyAdapters;
    }

    public void setUnhealthyAdapters(int unhealthyAdapters) {
        this.unhealthyAdapters = unhealthyAdapters;
    }

    public int getWarningAdapters() {
        return warningAdapters;
    }

    public void setWarningAdapters(int warningAdapters) {
        this.warningAdapters = warningAdapters;
    }

    public int getInactiveAdapters() {
        return inactiveAdapters;
    }

    public void setInactiveAdapters(int inactiveAdapters) {
        this.inactiveAdapters = inactiveAdapters;
    }

    public int getOverallHealthScore() {
        return overallHealthScore;
    }

    public void setOverallHealthScore(int overallHealthScore) {
        this.overallHealthScore = overallHealthScore;
    }

    public List<AdapterHealthDetail> getAdapterHealthDetails() {
        return adapterHealthDetails;
    }

    public void setAdapterHealthDetails(List<AdapterHealthDetail> adapterHealthDetails) {
        this.adapterHealthDetails = adapterHealthDetails;
    }

    public HealthTrends getHealthTrends() {
        return healthTrends;
    }

    public void setHealthTrends(HealthTrends healthTrends) {
        this.healthTrends = healthTrends;
    }

    public List<CriticalAlert> getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(List<CriticalAlert> criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public List<AdapterHealthSummary> getAdapterHealthSummaries() {
        return adapterHealthSummaries;
    }

    public void setAdapterHealthSummaries(List<AdapterHealthSummary> adapterHealthSummaries) {
        this.adapterHealthSummaries = adapterHealthSummaries;
    }

    public int getCriticalAdapters() {
        return criticalAdapters;
    }

    public void setCriticalAdapters(int criticalAdapters) {
        this.criticalAdapters = criticalAdapters;
    }

    public List<CriticalIssue> getCriticalIssues() {
        return criticalIssues;
    }

    public void setCriticalIssues(List<CriticalIssue> criticalIssues) {
        this.criticalIssues = criticalIssues;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
