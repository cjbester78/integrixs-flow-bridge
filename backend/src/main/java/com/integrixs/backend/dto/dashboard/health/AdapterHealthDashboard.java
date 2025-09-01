package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main dashboard containing health information for all adapters.
 */
@Data
public class AdapterHealthDashboard {
    private LocalDateTime timestamp;
    private int totalAdapters;
    private int healthyAdapters;
    private int unhealthyAdapters;
    private int warningAdapters;
    private int inactiveAdapters;
    private int overallHealthScore; // 0-100
    private List<AdapterHealthDetail> adapterHealthDetails;
    private HealthTrends healthTrends;
    private List<CriticalAlert> criticalAlerts;
}