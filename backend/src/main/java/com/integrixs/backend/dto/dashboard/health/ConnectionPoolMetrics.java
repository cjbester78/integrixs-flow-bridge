package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;

/**
 * Connection pool metrics for an adapter.
 */
public class ConnectionPoolMetrics {
    private String adapterId;
    private int maxConnections;
    private int activeConnections;
    private int idleConnections;
    private int waitingThreads;
    private double connectionUtilization; // percentage
    private LocalDateTime lastUpdated;

    // Default constructor
    public ConnectionPoolMetrics() {
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(int activeConnections) {
        this.activeConnections = activeConnections;
    }

    public int getIdleConnections() {
        return idleConnections;
    }

    public void setIdleConnections(int idleConnections) {
        this.idleConnections = idleConnections;
    }

    public int getWaitingThreads() {
        return waitingThreads;
    }

    public void setWaitingThreads(int waitingThreads) {
        this.waitingThreads = waitingThreads;
    }

    public double getConnectionUtilization() {
        return connectionUtilization;
    }

    public void setConnectionUtilization(double connectionUtilization) {
        this.connectionUtilization = connectionUtilization;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
