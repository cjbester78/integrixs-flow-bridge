package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Result of a health check operation.
 */
@Data
public class HealthCheckResult {
    private String adapterId;
    private LocalDateTime checkTime;
    private String overallStatus; // HEALTHY, WARNING, UNHEALTHY
    private int healthScore;
    private List<HealthCheckItem> checkItems;
    private List<String> recommendations;
}
