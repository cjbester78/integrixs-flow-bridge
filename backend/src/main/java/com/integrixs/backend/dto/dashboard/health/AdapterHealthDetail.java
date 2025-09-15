package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed health information for a specific adapter.
 */
@Data
public class AdapterHealthDetail {
    private String adapterId;
    private String adapterName;
    private String adapterType;
    private String status;
    private LocalDateTime lastChecked;
    private int healthScore; // 0-100
    private AdapterHealthMetrics currentMetrics;
    private ConnectionPoolMetrics connectionPoolMetrics;
    private ResourceUsageMetrics resourceUsageMetrics;
    private List<RecentError> recentErrors;
    private PerformanceTrend performanceTrends;
}
