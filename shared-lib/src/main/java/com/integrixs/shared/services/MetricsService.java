package com.integrixs.shared.services;

import java.util.Map;

/**
 * Service for collecting and reporting metrics
 */
public interface MetricsService {

    /**
     * Record a counter metric
     * @param name The metric name
     * @param value The counter value to add
     * @param tags Additional tags for the metric
     */
    void recordCounter(String name, long value, Map<String, String> tags);

    /**
     * Record a gauge metric
     * @param name The metric name
     * @param value The gauge value
     * @param tags Additional tags for the metric
     */
    void recordGauge(String name, double value, Map<String, String> tags);

    /**
     * Record a timing metric
     * @param name The metric name
     * @param duration The duration in milliseconds
     * @param tags Additional tags for the metric
     */
    void recordTiming(String name, long duration, Map<String, String> tags);

    /**
     * Start a timer for measuring duration
     * @param name The metric name
     * @return A timer handle
     */
    Timer startTimer(String name);

    /**
     * Get current metric values
     * @param name The metric name
     * @return The metric values
     */
    Map<String, Object> getMetricValues(String name);

    /**
     * Timer interface for measuring durations
     */
    interface Timer {
        /**
         * Stop the timer and record the duration
         */
        void stop();

        /**
         * Stop the timer and record the duration with tags
         * @param tags Additional tags for the metric
         */
        void stop(Map<String, String> tags);
    }
}
