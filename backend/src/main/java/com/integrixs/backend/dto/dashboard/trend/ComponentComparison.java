package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Comparison of performance across components.
 */
@Data
public class ComponentComparison {
    private List<String> componentIds;
    private String metric;
    private Duration timeRange;
    private Map<String, List<TimeSeriesDataPoint>> componentData;
    private Map<String, ComponentStats> componentStatistics;
    private List<ComponentRanking> ranking;
    private List<String> outliers;
}