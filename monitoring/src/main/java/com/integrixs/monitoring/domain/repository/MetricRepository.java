package com.integrixs.monitoring.domain.repository;

import com.integrixs.monitoring.domain.model.MetricSnapshot;
import com.integrixs.monitoring.domain.service.MetricsCollectorService.MetricQueryCriteria;
import com.integrixs.monitoring.domain.service.MetricsCollectorService.AggregationType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Domain repository interface for metrics
 */
public interface MetricRepository {

    /**
     * Save a metric snapshot
     * @param metric Metric to save
     * @return Saved metric
     */
    MetricSnapshot save(MetricSnapshot metric);

    /**
     * Save multiple metrics in batch
     * @param metrics Metrics to save
     * @return Saved metrics
     */
    List<MetricSnapshot> saveAll(List<MetricSnapshot> metrics);

    /**
     * Find metric by ID
     * @param metricId Metric ID
     * @return Metric if found
     */
    Optional<MetricSnapshot> findById(String metricId);

    /**
     * Get latest metric value
     * @param metricName Metric name
     * @param tags Metric tags
     * @return Latest metric snapshot
     */
    Optional<MetricSnapshot> findLatest(String metricName, Map<String, String> tags);

    /**
     * Query metrics based on criteria
     * @param criteria Query criteria
     * @return List of matching metrics
     */
    List<MetricSnapshot> query(MetricQueryCriteria criteria);

    /**
     * Calculate aggregation
     * @param metricName Metric name
     * @param aggregationType Aggregation type
     * @param startTime Start time
     * @param endTime End time
     * @param tags Tags to filter by
     * @return Aggregated value
     */
    double calculateAggregation(String metricName, AggregationType aggregationType,
                               long startTime, long endTime, Map<String, String> tags);

    /**
     * Get time series data
     * @param metricName Metric name
     * @param startTime Start time
     * @param endTime End time
     * @param interval Interval in seconds
     * @param tags Tags to filter by
     * @return Time series data points
     */
    List<TimeSeriesDataPoint> getTimeSeries(String metricName, long startTime, long endTime,
                                           int interval, Map<String, String> tags);

    /**
     * Delete old metrics
     * @param retentionDays Number of days to retain
     * @return Number of deleted metrics
     */
    long deleteOlderThan(int retentionDays);

    /**
     * Get metric names
     * @return List of unique metric names
     */
    List<String> getMetricNames();

    /**
     * Get tags for a metric
     * @param metricName Metric name
     * @return List of tag key - value pairs
     */
    List<Map<String, String>> getMetricTags(String metricName);

    /**
     * Time series data point
     */
    class TimeSeriesDataPoint {
        private long timestamp;
        private double value;
        private long count;

        public TimeSeriesDataPoint(long timestamp, double value, long count) {
            this.timestamp = timestamp;
            this.value = value;
            this.count = count;
        }

        // Getters and setters
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
}
