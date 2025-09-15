package com.integrixs.data.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing adapter health records
 */
@Entity
@Table(name = "adapter_health_records")
public class AdapterHealthRecord extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adapter_id", nullable = false)
    private CommunicationAdapter adapter;

    @Column(name = "health_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private HealthStatus healthStatus;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "available_connections")
    private Integer availableConnections;

    @Column(name = "active_connections")
    private Integer activeConnections;

    @Column(name = "error_count")
    private Integer errorCount = 0;

    @Column(name = "success_count")
    private Integer successCount = 0;

    @Column(name = "cpu_usage")
    private Double cpuUsage;

    @Column(name = "memory_usage")
    private Double memoryUsage;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "health_details", columnDefinition = "TEXT")
    private String healthDetails;

    @Column(name = "is_available")
    private boolean available = true;

    public enum HealthStatus {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }

    // Getters and setters

    public CommunicationAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(CommunicationAdapter adapter) {
        this.adapter = adapter;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public Integer getAvailableConnections() {
        return availableConnections;
    }

    public void setAvailableConnections(Integer availableConnections) {
        this.availableConnections = availableConnections;
    }

    public Integer getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(Integer activeConnections) {
        this.activeConnections = activeConnections;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    public String getHealthDetails() {
        return healthDetails;
    }

    public void setHealthDetails(String healthDetails) {
        this.healthDetails = healthDetails;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // Additional helper methods
    public void setAdapterId(UUID adapterId) {
        if(this.adapter == null) {
            this.adapter = new CommunicationAdapter();
        }
        this.adapter.setId(adapterId);
    }

    public void setHealthy(boolean healthy) {
        this.healthStatus = healthy ? HealthStatus.HEALTHY : HealthStatus.UNHEALTHY;
    }

    public void setErrorMessage(String errorMessage) {
        this.lastErrorMessage = errorMessage;
    }

    public void setCheckTime(LocalDateTime checkTime) {
        this.checkedAt = checkTime;
    }
}
