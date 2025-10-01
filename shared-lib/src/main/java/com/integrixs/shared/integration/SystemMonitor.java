package com.integrixs.shared.integration;

import java.util.Map;

/**
 * System - wide monitoring interface
 * All modules use this to report metrics and events
 */
public interface SystemMonitor {

    /**
     * Record a system activity
     * @param module Module name
     * @param operation Operation being performed
     * @param context Additional context data
     */
    void recordActivity(String module, String operation, Map<String, Object> context);

    /**
     * Record an error
     * @param module Module name
     * @param error Exception that occurred
     * @param context Additional context data
     */
    void recordError(String module, Exception error, Map<String, Object> context);

    /**
     * Record performance metric
     * @param module Module name
     * @param operation Operation being measured
     * @param durationMs Duration in milliseconds
     */
    void recordPerformance(String module, String operation, long durationMs);

    /**
     * Record a custom metric
     * @param metricName Metric name
     * @param value Metric value
     * @param tags Metric tags for grouping
     */
    void recordMetric(String metricName, double value, Map<String, String> tags);

    /**
     * Check if system is healthy
     * @return Health check result
     */
    HealthCheckResult checkHealth();

    /**
     * Get health status for a module
     * @param module The module name
     * @return Health status map with status, timestamp, and details
     */
    Map<String, Object> getHealthStatus(String module);
}
