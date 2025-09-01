package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Trend analysis result.
 */
@Data
public class TrendAnalysis {
    private String metricName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<TimeSeriesDataPoint> dataPoints;
    private Map<String, Double> statistics;
    private String trend; // INCREASING, DECREASING, STABLE, etc.
    private List<Anomaly> anomalies;
    private List<Prediction> predictions;
    private Map<String, Double> correlations; // Correlations with other metrics
}