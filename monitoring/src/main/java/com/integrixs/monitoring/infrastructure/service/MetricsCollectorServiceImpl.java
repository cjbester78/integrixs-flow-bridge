package com.integrixs.monitoring.infrastructure.service;

import com.integrixs.monitoring.domain.model.MetricSnapshot;
import com.integrixs.monitoring.domain.repository.MetricRepository;
import com.integrixs.monitoring.domain.service.MetricsCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure implementation of metrics collector service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsCollectorServiceImpl implements MetricsCollectorService {

    private final MetricRepository metricRepository;

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
    public void incrementCounter(String metricName, Map<String, String> tags) {
        incrementCounter(metricName, 1.0, tags);
    }

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional(readOnly = true)
    public MetricSnapshot getCurrentMetric(String metricName, Map<String, String> tags) {
        return metricRepository.findLatest(metricName, tags).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetricSnapshot> queryMetrics(MetricQueryCriteria criteria) {
        return metricRepository.query(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateAggregation(String metricName, AggregationType aggregationType,
                                     long startTime, long endTime, Map<String, String> tags) {
        return metricRepository.calculateAggregation(metricName, aggregationType, startTime, endTime, tags);
    }
}
