package com.integrixs.monitoring.infrastructure.persistence;

import com.integrixs.monitoring.domain.model.MetricSnapshot;
import com.integrixs.monitoring.domain.repository.MetricRepository;
import com.integrixs.monitoring.domain.service.MetricsCollectorService.AggregationType;
import com.integrixs.monitoring.domain.service.MetricsCollectorService.MetricQueryCriteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In - memory implementation of metric repository
 */
@Repository
public class InMemoryMetricRepository implements MetricRepository {

    private final Map<String, MetricSnapshot> storage = new ConcurrentHashMap<>();

    @Override
    public MetricSnapshot save(MetricSnapshot metric) {
        if(metric.getMetricId() == null) {
            metric.setMetricId(UUID.randomUUID().toString());
        }
        storage.put(metric.getMetricId(), metric);
        return metric;
    }

    @Override
    public List<MetricSnapshot> saveAll(List<MetricSnapshot> metrics) {
        metrics.forEach(metric -> {
            if(metric.getMetricId() == null) {
                metric.setMetricId(UUID.randomUUID().toString());
            }
            storage.put(metric.getMetricId(), metric);
        });
        return metrics;
    }

    @Override
    public Optional<MetricSnapshot> findById(String metricId) {
        return Optional.ofNullable(storage.get(metricId));
    }

    @Override
    public Optional<MetricSnapshot> findLatest(String metricName, Map<String, String> tags) {
        return storage.values().stream()
                .filter(metric -> metric.getMetricName().equals(metricName))
                .filter(metric -> tagsMatch(metric.getTags(), tags))
                .max(Comparator.comparing(MetricSnapshot::getTimestamp));
    }

    @Override
    public List<MetricSnapshot> query(MetricQueryCriteria criteria) {
        return storage.values().stream()
                .filter(metric -> matchesCriteria(metric, criteria))
                .sorted(getComparator(criteria.getOrderBy()))
                .limit(criteria.getLimit() != null ? criteria.getLimit() : 1000)
                .collect(Collectors.toList());
    }

    @Override
    public double calculateAggregation(String metricName, AggregationType aggregationType,
                                     long startTime, long endTime, Map<String, String> tags) {
        List<MetricSnapshot> metrics = getMetricsInRange(metricName, startTime, endTime, tags);

        if(metrics.isEmpty()) {
            return 0.0;
        }

        switch(aggregationType) {
            case SUM:
                return metrics.stream().mapToDouble(MetricSnapshot::getValue).sum();
            case AVG:
                return metrics.stream().mapToDouble(MetricSnapshot::getValue).average().orElse(0.0);
            case MIN:
                return metrics.stream().mapToDouble(MetricSnapshot::getValue).min().orElse(0.0);
            case MAX:
                return metrics.stream().mapToDouble(MetricSnapshot::getValue).max().orElse(0.0);
            case COUNT:
                return metrics.size();
            case P50:
                return calculatePercentile(metrics, 50);
            case P90:
                return calculatePercentile(metrics, 90);
            case P95:
                return calculatePercentile(metrics, 95);
            case P99:
                return calculatePercentile(metrics, 99);
            default:
                return 0.0;
        }
    }

    @Override
    public List<TimeSeriesDataPoint> getTimeSeries(String metricName, long startTime, long endTime,
                                                  int interval, Map<String, String> tags) {
        List<MetricSnapshot> metrics = getMetricsInRange(metricName, startTime, endTime, tags);
        Map<Long, List<MetricSnapshot>> buckets = new TreeMap<>();

        // Group metrics into time buckets
        for(MetricSnapshot metric : metrics) {
            long timestamp = metric.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long bucket = (timestamp / (interval * 1000)) * (interval * 1000);
            buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(metric);
        }

        // Calculate average for each bucket
        return buckets.entrySet().stream()
                .map(entry -> {
                    double avgValue = entry.getValue().stream()
                            .mapToDouble(MetricSnapshot::getValue)
                            .average()
                            .orElse(0.0);
                    return new TimeSeriesDataPoint(entry.getKey(), avgValue, entry.getValue().size());
                })
                .collect(Collectors.toList());
    }

    @Override
    public long deleteOlderThan(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<String> toDelete = storage.entrySet().stream()
                .filter(entry -> entry.getValue().getTimestamp().isBefore(cutoff))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        toDelete.forEach(storage::remove);
        return toDelete.size();
    }

    @Override
    public List<String> getMetricNames() {
        return storage.values().stream()
                .map(MetricSnapshot::getMetricName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> getMetricTags(String metricName) {
        return storage.values().stream()
                .filter(metric -> metric.getMetricName().equals(metricName))
                .map(MetricSnapshot::getTags)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean matchesCriteria(MetricSnapshot metric, MetricQueryCriteria criteria) {
        if(criteria.getMetricName() != null && !metric.getMetricName().equals(criteria.getMetricName())) {
            return false;
        }

        if(criteria.getMetricType() != null && metric.getMetricType() != criteria.getMetricType()) {
            return false;
        }

        if(criteria.getTags() != null && !tagsMatch(metric.getTags(), criteria.getTags())) {
            return false;
        }

        if(criteria.getStartTime() != null) {
            LocalDateTime start = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(criteria.getStartTime()), ZoneId.systemDefault());
            if(metric.getTimestamp().isBefore(start)) {
                return false;
            }
        }

        if(criteria.getEndTime() != null) {
            LocalDateTime end = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(criteria.getEndTime()), ZoneId.systemDefault());
            if(metric.getTimestamp().isAfter(end)) {
                return false;
            }
        }

        return true;
    }

    private boolean tagsMatch(Map<String, String> metricTags, Map<String, String> searchTags) {
        if(searchTags == null || searchTags.isEmpty()) {
            return true;
        }

        for(Map.Entry<String, String> entry : searchTags.entrySet()) {
            if(!entry.getValue().equals(metricTags.get(entry.getKey()))) {
                return false;
            }
        }

        return true;
    }

    private Comparator<MetricSnapshot> getComparator(String orderBy) {
        if(orderBy == null) {
            return Comparator.comparing(MetricSnapshot::getTimestamp).reversed();
        }

        switch(orderBy) {
            case "timestamp_asc":
                return Comparator.comparing(MetricSnapshot::getTimestamp);
            case "value_asc":
                return Comparator.comparing(MetricSnapshot::getValue);
            case "value_desc":
                return Comparator.comparing(MetricSnapshot::getValue).reversed();
            default:
                return Comparator.comparing(MetricSnapshot::getTimestamp).reversed();
        }
    }

    private List<MetricSnapshot> getMetricsInRange(String metricName, long startTime, long endTime,
                                                   Map<String, String> tags) {
        LocalDateTime start = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

        return storage.values().stream()
                .filter(metric -> metric.getMetricName().equals(metricName))
                .filter(metric -> tagsMatch(metric.getTags(), tags))
                .filter(metric -> !metric.getTimestamp().isBefore(start) &&
                               !metric.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    private double calculatePercentile(List<MetricSnapshot> metrics, int percentile) {
        if(metrics.isEmpty()) {
            return 0.0;
        }

        List<Double> values = metrics.stream()
                .map(MetricSnapshot::getValue)
                .sorted()
                .collect(Collectors.toList());

        int index = (int) Math.ceil(percentile / 100.0 * values.size()) - 1;
        return values.get(Math.max(0, Math.min(index, values.size() - 1)));
    }
}
