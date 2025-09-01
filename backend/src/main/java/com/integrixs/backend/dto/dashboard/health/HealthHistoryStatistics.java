package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

/**
 * Statistics calculated from health history.
 */
@Data
public class HealthHistoryStatistics {
    private double averageHealthScore;
    private int minHealthScore;
    private int maxHealthScore;
    private int totalErrors;
    private int totalStatusChanges;
    private double uptimePercentage;
}