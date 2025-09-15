package com.integrixs.backend.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Real - time metrics for the performance dashboard.
 */
@Data
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
}
