package com.integrixs.backend.dto.dashboard.trend;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Comparison of performance across components.
 */
public class ComponentComparison {
    private List<String> componentIds;
    private String metric;
    private Duration timeRange;
    private Map<String, List<TimeSeriesDataPoint>> componentData;
    private Map<String, ComponentStats> componentStatistics;
    private List<ComponentRanking> ranking;
    private List<String> outliers;

    // Default constructor
    public ComponentComparison() {
    }

    public List<String> getComponentIds() {
        return componentIds;
    }

    public void setComponentIds(List<String> componentIds) {
        this.componentIds = componentIds;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Duration getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(Duration timeRange) {
        this.timeRange = timeRange;
    }

    public List<ComponentRanking> getRanking() {
        return ranking;
    }

    public void setRanking(List<ComponentRanking> ranking) {
        this.ranking = ranking;
    }

    public List<String> getOutliers() {
        return outliers;
    }

    public void setOutliers(List<String> outliers) {
        this.outliers = outliers;
    }

    public Map<String, List<TimeSeriesDataPoint>> getComponentData() {
        return componentData;
    }

    public void setComponentData(Map<String, List<TimeSeriesDataPoint>> componentData) {
        this.componentData = componentData;
    }

    public Map<String, ComponentStats> getComponentStatistics() {
        return componentStatistics;
    }

    public void setComponentStatistics(Map<String, ComponentStats> componentStatistics) {
        this.componentStatistics = componentStatistics;
    }
}
