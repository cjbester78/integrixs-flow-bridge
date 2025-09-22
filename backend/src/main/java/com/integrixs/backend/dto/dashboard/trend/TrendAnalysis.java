package com.integrixs.backend.dto.dashboard.trend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Trend analysis result.
 */
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

    // Default constructor
    public TrendAnalysis() {
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<TimeSeriesDataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<TimeSeriesDataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public List<Anomaly> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<Anomaly> anomalies) {
        this.anomalies = anomalies;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }

    public Map<String, Double> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Double> statistics) {
        this.statistics = statistics;
    }

    public Map<String, Double> getCorrelations() {
        return correlations;
    }

    public void setCorrelations(Map<String, Double> correlations) {
        this.correlations = correlations;
    }
}
