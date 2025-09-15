package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Connection pool metrics for an adapter.
 */
@Data
public class ConnectionPoolMetrics {
    private String adapterId;
    private int maxConnections;
    private int activeConnections;
    private int idleConnections;
    private int waitingThreads;
    private double connectionUtilization; // percentage
    private LocalDateTime lastUpdated;
}
