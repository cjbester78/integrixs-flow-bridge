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

    public Map<String, ResourceTrend> getResourceTrends() {
        return resourceTrends;
    }

    public void setResourceTrends(Map<String, ResourceTrend> resourceTrends) {
        this.resourceTrends = resourceTrends;
    }

    public Map<String, CapacityProjection> getCapacityProjections() {
        return capacityProjections;
    }

    public void setCapacityProjections(Map<String, CapacityProjection> capacityProjections) {
        this.capacityProjections = capacityProjections;
    }
}
