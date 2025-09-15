package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Capacity planning insights and projections.
 */
@Data
public class CapacityPlanningInsights {
    private Map<String, ResourceTrend> resourceTrends;
    private Map<String, CapacityProjection> capacityProjections;
    private List<GrowthPattern> growthPatterns;
    private List<String> recommendations;
}
