package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

/**
 * Current health metrics for an adapter.
 */
@Data
public class AdapterHealthMetrics {
    private String adapterId;
    private double requestsPerMinute;
    private double errorRate; // percentage
    private double successRate; // percentage
    private double averageResponseTime; // milliseconds
    private double uptime; // percentage
}