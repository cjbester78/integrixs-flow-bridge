package com.integrixs.monitoring.infrastructure.service;

import com.integrixs.monitoring.domain.model.MetricSnapshot;
import com.integrixs.monitoring.domain.repository.MetricRepository;
import com.integrixs.monitoring.domain.service.MetricsCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure implementation of metrics collector service
 */
@Service
public class MetricsCollectorServiceImpl implements MetricsCollectorService {

    private static final Logger log = LoggerFactory.getLogger(MetricsCollectorServiceImpl.class);
    private final MetricRepository metricRepository;

    public MetricsCollectorServiceImpl(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    @Override
    public void recordMetric(MetricSnapshot metric) {
        try {
            if(metric.getMetricId() == null) {
                metric.setMetricId(UUID.randomUUID().toString());
            }
            if(metric.getTimestamp() == null) {
                metric.setTimestamp(LocalDateTime.now());
            }

            metricRepository.save(metric);
            log.debug("Metric recorded: {} = {}", metric.getMetricName(), metric.getValue());
        } catch(Exception e) {
            log.error("Error recording metric: {}", e.getMessage(), e);
        }
    }

    @Override
    public void recordMetricsBatch(List<MetricSnapshot> metrics) {
        try {
            // Set IDs and timestamps if not provided
            metrics.forEach(metric -> {
                if(metric.getMetricId() == null) {
                    metric.setMetricId(UUID.randomUUID().toString());
                }
                if(metric.getTimestamp() == null) {
                    metric.setTimestamp(LocalDateTime.now());
                }
            });

            metricRepository.saveAll(metrics);
            log.debug("Batch recorded {} metrics", metrics.size());
        } catch(Exception e) {
            log.error("Error batch recording metrics: {}", e.getMessage(), e);
        }
    }

    @Override
    public void incrementCounter(String metricName, Map<String, String> tags) {
        incrementCounter(metricName, 1.0, tags);
    }

    @Override
    public void incrementCounter(String metricName, double amount, Map<String, String> tags) {
        MetricSnapshot metric = MetricSnapshot.builder()
                .metricName(metricName)
                .metricType(MetricSnapshot.MetricType.COUNTER)
                .value(amount)
                .unit("count")
                .source("system")
                .tags(tags != null ? tags : new HashMap<>())
                .build();

        recordMetric(metric);
    }

    @Override
    public void setGauge(String metricName, double value, Map<String, String> tags) {
        MetricSnapshot metric = MetricSnapshot.builder()
                .metricName(metricName)
                .metricType(MetricSnapshot.MetricType.GAUGE)
                .value(value)
                .unit("value")
                .source("system")
                .tags(tags != null ? tags : new HashMap<>())
                .build();

        recordMetric(metric);
    }

    @Override
    public void recordTimer(String metricName, long durationMillis, Map<String, String> tags) {
        MetricSnapshot metric = MetricSnapshot.builder()
                .metricName(metricName)
                .metricType(MetricSnapshot.MetricType.TIMER)
                .value((double) durationMillis)
                .unit("milliseconds")
                .source("system")
                .tags(tags != null ? tags : new HashMap<>())
                .build();

        recordMetric(metric);
    }

    @Override
    public void recordHistogram(String metricName, double value, Map<String, String> tags) {
        MetricSnapshot metric = MetricSnapshot.builder()
                .metricName(metricName)
                .metricType(MetricSnapshot.MetricType.HISTOGRAM)
                .value(value)
                .unit("value")
                .source("system")
                .tags(tags != null ? tags : new HashMap<>())
                .build();

        recordMetric(metric);
    }

    @Override
    public MetricSnapshot getCurrentMetric(String metricName, Map<String, String> tags) {
        return metricRepository.findLatest(metricName, tags).orElse(null);
    }

    @Override
    public List<MetricSnapshot> queryMetrics(MetricQueryCriteria criteria) {
        return metricRepository.query(criteria);
    }

    @Override
    public double calculateAggregation(String metricName, AggregationType aggregationType,
                                     long startTime, long endTime, Map<String, String> tags) {
        return metricRepository.calculateAggregation(metricName, aggregationType, startTime, endTime, tags);
    }
}
