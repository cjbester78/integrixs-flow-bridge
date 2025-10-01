package com.integrixs.backend.dto.dashboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Real - time metrics for the performance dashboard.
 */
public class RealTimeMetrics {

    private LocalDateTime timestamp;

    // System - wide metrics
    private Map<String, Object> systemMetrics;

    // Component performance
    private List<Map<String, Object>> componentPerformance;

    // Active operations
    private List<Map<String, Object>> activeOperations;

    // Recent errors
    private List<Map<String, Object>> recentErrors;

    // SLA compliance
    private Map<String, Object> slaCompliance;

    // Throughput rates
    private Map<String, Double> throughputRates;

    // Default constructor
    public RealTimeMetrics() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getSystemMetrics() {
        return systemMetrics;
    }

    public void setSystemMetrics(Map<String, Object> systemMetrics) {
        this.systemMetrics = systemMetrics;
    }

    public List<Map<String, Object>> getComponentPerformance() {
        return componentPerformance;
    }

    public void setComponentPerformance(List<Map<String, Object>> componentPerformance) {
        this.componentPerformance = componentPerformance;
    }

    public List<Map<String, Object>> getActiveOperations() {
        return activeOperations;
    }

    public void setActiveOperations(List<Map<String, Object>> activeOperations) {
        this.activeOperations = activeOperations;
    }

    public List<Map<String, Object>> getRecentErrors() {
        return recentErrors;
    }

    public void setRecentErrors(List<Map<String, Object>> recentErrors) {
        this.recentErrors = recentErrors;
    }

    public Map<String, Object> getSlaCompliance() {
        return slaCompliance;
    }

    public void setSlaCompliance(Map<String, Object> slaCompliance) {
        this.slaCompliance = slaCompliance;
    }

    public Map<String, Double> getThroughputRates() {
        return throughputRates;
    }

    public void setThroughputRates(Map<String, Double> throughputRates) {
        this.throughputRates = throughputRates;
    }
}
