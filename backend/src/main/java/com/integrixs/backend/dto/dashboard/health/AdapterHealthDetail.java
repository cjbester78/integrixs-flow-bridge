package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed health information for a specific adapter.
 */
public class AdapterHealthDetail {
    private String adapterId;
    private String adapterName;
    private String adapterType;
    private String status;
    private LocalDateTime lastChecked;
    private int healthScore; // 0-100
    private AdapterHealthMetrics currentMetrics;
    private ConnectionPoolMetrics connectionPoolMetrics;
    private ResourceUsageMetrics resourceUsageMetrics;
    private List<RecentError> recentErrors;
    private PerformanceTrend performanceTrends;

    // Default constructor
    public AdapterHealthDetail() {
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public AdapterHealthMetrics getCurrentMetrics() {
        return currentMetrics;
    }

    public void setCurrentMetrics(AdapterHealthMetrics currentMetrics) {
        this.currentMetrics = currentMetrics;
    }

    public ConnectionPoolMetrics getConnectionPoolMetrics() {
        return connectionPoolMetrics;
    }

    public void setConnectionPoolMetrics(ConnectionPoolMetrics connectionPoolMetrics) {
        this.connectionPoolMetrics = connectionPoolMetrics;
    }

    public ResourceUsageMetrics getResourceUsageMetrics() {
        return resourceUsageMetrics;
    }

    public void setResourceUsageMetrics(ResourceUsageMetrics resourceUsageMetrics) {
        this.resourceUsageMetrics = resourceUsageMetrics;
    }

    public List<RecentError> getRecentErrors() {
        return recentErrors;
    }

    public void setRecentErrors(List<RecentError> recentErrors) {
        this.recentErrors = recentErrors;
    }

    public PerformanceTrend getPerformanceTrends() {
        return performanceTrends;
    }

    public void setPerformanceTrends(PerformanceTrend performanceTrends) {
        this.performanceTrends = performanceTrends;
    }
}
