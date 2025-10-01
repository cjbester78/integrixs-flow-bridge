package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Health history for an adapter.
 */
public class AdapterHealthHistory {
    private String adapterId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<HealthScorePoint> healthScores;
    private List<StatusChangeEvent> statusChanges;
    private List<ErrorEvent> errorEvents;
    private HealthHistoryStatistics statistics;

    // Default constructor
    public AdapterHealthHistory() {
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<HealthScorePoint> getHealthScores() {
        return healthScores;
    }

    public void setHealthScores(List<HealthScorePoint> healthScores) {
        this.healthScores = healthScores;
    }

    public List<StatusChangeEvent> getStatusChanges() {
        return statusChanges;
    }

    public void setStatusChanges(List<StatusChangeEvent> statusChanges) {
        this.statusChanges = statusChanges;
    }

    public List<ErrorEvent> getErrorEvents() {
        return errorEvents;
    }

    public void setErrorEvents(List<ErrorEvent> errorEvents) {
        this.errorEvents = errorEvents;
    }

    public HealthHistoryStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(HealthHistoryStatistics statistics) {
        this.statistics = statistics;
    }
}
