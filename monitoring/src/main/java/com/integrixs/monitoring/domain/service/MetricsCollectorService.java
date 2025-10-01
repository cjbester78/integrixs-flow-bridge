package com.integrixs.monitoring.domain.service;

import com.integrixs.monitoring.domain.model.MetricSnapshot;

import java.util.List;
import java.util.Map;

/**
 * Domain service interface for metrics collection
 */
public interface MetricsCollectorService {

    /**
     * Record a metric value
     * @param metric The metric to record
     */
    void recordMetric(MetricSnapshot metric);

    /**
     * Record multiple metrics in batch
     * @param metrics List of metrics to record
     */
    void recordMetricsBatch(List<MetricSnapshot> metrics);

    /**
     * Increment a counter metric
     * @param metricName Name of the counter
     * @param tags Tags for the metric
     */
    void incrementCounter(String metricName, Map<String, String> tags);

    /**
     * Increment a counter by specific amount
     * @param metricName Name of the counter
     * @param amount Amount to increment by
     * @param tags Tags for the metric
     */
    void incrementCounter(String metricName, double amount, Map<String, String> tags);

    /**
     * Set a gauge value
     * @param metricName Name of the gauge
     * @param value Current value
     * @param tags Tags for the metric
     */
    void setGauge(String metricName, double value, Map<String, String> tags);

    /**
     * Record a timing/duration
     * @param metricName Name of the timer
     * @param durationMillis Duration in milliseconds
     * @param tags Tags for the metric
     */
    void recordTimer(String metricName, long durationMillis, Map<String, String> tags);

    /**
     * Record a histogram value
     * @param metricName Name of the histogram
     * @param value Value to record
     * @param tags Tags for the metric
     */
    void recordHistogram(String metricName, double value, Map<String, String> tags);

    /**
     * Get current value of a metric
     * @param metricName Metric name
     * @param tags Metric tags
     * @return Current metric value
     */
    MetricSnapshot getCurrentMetric(String metricName, Map<String, String> tags);

    /**
     * Query metrics
     * @param criteria Query criteria
     * @return List of matching metrics
     */
    List<MetricSnapshot> queryMetrics(MetricQueryCriteria criteria);

    /**
     * Calculate aggregated metrics
     * @param metricName Metric name
     * @param aggregationType Type of aggregation
     * @param startTime Start time
     * @param endTime End time
     * @param tags Tags to filter by
     * @return Aggregated value
     */
    double calculateAggregation(String metricName, AggregationType aggregationType,
                               long startTime, long endTime, Map<String, String> tags);

    /**
     * Aggregation types
     */
    enum AggregationType {
        SUM,
        AVG,
        MIN,
        MAX,
        COUNT,
        P50,   // 50th percentile
        P90,   // 90th percentile
        P95,   // 95th percentile
        P99     // 99th percentile
    }

    /**
     * Metric query criteria
     */
    class MetricQueryCriteria {
        private String metricName;
        private MetricSnapshot.MetricType metricType;
        private Map<String, String> tags;
        private Long startTime;
        private Long endTime;
        private Integer limit;
        private String orderBy;

        // Getters and setters
        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }

        public MetricSnapshot.MetricType getMetricType() { return metricType; }
        public void setMetricType(MetricSnapshot.MetricType metricType) { this.metricType = metricType; }

        public Map<String, String> getTags() { return tags; }
        public void setTags(Map<String, String> tags) { this.tags = tags; }

        public Long getStartTime() { return startTime; }
        public void setStartTime(Long startTime) { this.startTime = startTime; }

        public Long getEndTime() { return endTime; }
        public void setEndTime(Long endTime) { this.endTime = endTime; }

        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }

        public String getOrderBy() { return orderBy; }
        public void setOrderBy(String orderBy) { this.orderBy = orderBy; }
    }
}
