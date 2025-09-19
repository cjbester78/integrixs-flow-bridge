package com.integrixs.backend.dto.dashboard.trend;

import java.util.List;
import java.util.Map;

/**
 * Capacity planning insights and projections.
 */
public class CapacityPlanningInsights {
    private Map<String, ResourceTrend> resourceTrends;
    private Map<String, CapacityProjection> capacityProjections;
    private List<GrowthPattern> growthPatterns;
    private List<String> recommendations;

    // Default constructor
    public CapacityPlanningInsights() {
    }

    public List<GrowthPattern> getGrowthPatterns() {
        return growthPatterns;
    }

    public void setGrowthPatterns(List<GrowthPattern> growthPatterns) {
        this.growthPatterns = growthPatterns;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
