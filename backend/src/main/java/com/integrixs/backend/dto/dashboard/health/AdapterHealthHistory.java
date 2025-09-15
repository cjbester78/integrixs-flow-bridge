package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Health history for an adapter.
 */
@Data
public class AdapterHealthHistory {
    private String adapterId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<HealthScorePoint> healthScores;
    private List<StatusChangeEvent> statusChanges;
    private List<ErrorEvent> errorEvents;
    private HealthHistoryStatistics statistics;
}
