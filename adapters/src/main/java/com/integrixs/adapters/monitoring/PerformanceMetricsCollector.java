package com.integrixs.adapters.monitoring;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Performance metrics collector for adapter operations using Micrometer.
 * Provides detailed performance tracking, SLA monitoring, and custom metrics.
 */
@Component
public class PerformanceMetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsCollector.class);

    private final MeterRegistry meterRegistry;

    // SLA thresholds in milliseconds
    private final Map<String, Long> slaThresholds = new ConcurrentHashMap<>();

    // Operation timers
    private final Map<String, Timer> operationTimers = new ConcurrentHashMap<>();

    // Throughput counters
    private final Map<String, Counter> throughputCounters = new ConcurrentHashMap<>();

    // Error rate counters
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();

    // Active operation gauges
    private final Map<String, AtomicInteger> activeOperations = new ConcurrentHashMap<>();

    @Autowired
    public PerformanceMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry != null ? meterRegistry : new SimpleMeterRegistry();
        initializeDefaultSLAs();
    }

    /**
     * Initialize default SLA thresholds for common operations.
     */
    private void initializeDefaultSLAs() {
        // Default SLA thresholds in milliseconds
        slaThresholds.put("testConnection", 5000L);      // 5 seconds
        slaThresholds.put("send", 10000L);               // 10 seconds
        slaThresholds.put("fetch", 10000L);              // 10 seconds
        slaThresholds.put("poll", 5000L);                // 5 seconds
        slaThresholds.put("transform", 1000L);           // 1 second
        slaThresholds.put("authenticate", 3000L);        // 3 seconds
    }

    /**
     * Set custom SLA threshold for an operation.
     */
    public void setSLAThreshold(String operationName, long thresholdMs) {
        slaThresholds.put(operationName, thresholdMs);
        logger.info("Set SLA threshold for operation ' {}' to {}ms", operationName, thresholdMs);
    }

    /**
     * Record the start of an operation.
     */
    public Timer.Sample startOperation(String adapterType, String adapterMode, String operationName) {
        String metricKey = createMetricKey(adapterType, adapterMode, operationName);

        // Increment active operations counter
        activeOperations.computeIfAbsent(metricKey, k -> {
            AtomicInteger gauge = new AtomicInteger(0);
            Gauge.builder("adapter.operations.active", gauge, AtomicInteger::get)
                .tags("adapter.type", adapterType, "adapter.mode", adapterMode, "operation", operationName)
                .description("Number of active adapter operations")
                .register(meterRegistry);
            return gauge;
        }).incrementAndGet();

        return Timer.start(meterRegistry);
    }

    /**
     * Record the completion of an operation.
     */
    public void recordOperation(String adapterType, String adapterMode, String operationName,
                              Timer.Sample sample, boolean success, String errorType) {
        String metricKey = createMetricKey(adapterType, adapterMode, operationName);

        // Get or create timer
        Timer timer = operationTimers.computeIfAbsent(metricKey, k ->
            Timer.builder("adapter.operation.duration")
                .tags("adapter.type", adapterType, "adapter.mode", adapterMode,
                      "operation", operationName, "success", String.valueOf(success),
                      "error.type", errorType != null ? errorType : "none")
                .description("Adapter operation duration")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .publishPercentileHistogram()
                .sla(getSLADurations(operationName))
                .register(meterRegistry)
       );

        // Stop the timer
        long duration = sample.stop(timer);

        // Decrement active operations counter
        AtomicInteger activeCounter = activeOperations.get(metricKey);
        if(activeCounter != null) {
            activeCounter.decrementAndGet();
        }

        // Record throughput
        throughputCounters.computeIfAbsent(metricKey, k ->
            Counter.builder("adapter.operations.total")
                .tags("adapter.type", adapterType, "adapter.mode", adapterMode, "operation", operationName)
                .description("Total number of adapter operations")
                .register(meterRegistry)
       ).increment();

        // Record errors
        if(!success) {
            errorCounters.computeIfAbsent(metricKey + "-" + errorType, k ->
                Counter.builder("adapter.operations.errors")
                    .tags("adapter.type", adapterType, "adapter.mode", adapterMode,
                          "operation", operationName, "error.type", errorType)
                    .description("Number of adapter operation errors")
                    .register(meterRegistry)
           ).increment();
        }

        // Check SLA violation
        Long slaThreshold = slaThresholds.get(operationName);
        if(slaThreshold != null && duration > slaThreshold * 1_000_000) { // Convert to nanos
            recordSLAViolation(adapterType, adapterMode, operationName, duration, slaThreshold);
        }
    }

    /**
     * Record custom metric value.
     */
    public void recordCustomMetric(String adapterType, String adapterMode, String metricName,
                                 double value, String... additionalTags) {
        String fullMetricName = "adapter.custom." + metricName;

        Tags tags = Tags.of("adapter.type", adapterType, "adapter.mode", adapterMode);
        if(additionalTags.length > 0 && additionalTags.length % 2 == 0) {
            for(int i = 0; i < additionalTags.length; i += 2) {
                tags = tags.and(additionalTags[i], additionalTags[i + 1]);
            }
        }

        meterRegistry.gauge(fullMetricName, tags, value);
    }

    /**
     * Record data volume metrics.
     */
    public void recordDataVolume(String adapterType, String adapterMode, String direction,
                               long bytes) {
        Counter.builder("adapter.data.volume")
            .tags("adapter.type", adapterType, "adapter.mode", adapterMode, "direction", direction)
            .description("Data volume processed by adapters")
            .baseUnit("bytes")
            .register(meterRegistry)
            .increment(bytes);
    }

    /**
     * Record message count metrics.
     */
    public void recordMessageCount(String adapterType, String adapterMode, String status,
                                 long count) {
        Counter.builder("adapter.messages.processed")
            .tags("adapter.type", adapterType, "adapter.mode", adapterMode, "status", status)
            .description("Number of messages processed by adapters")
            .register(meterRegistry)
            .increment(count);
    }

    /**
     * Record connection pool metrics.
     */
    public void recordConnectionPoolMetrics(String adapterType, String poolName,
                                          int activeConnections, int idleConnections,
                                          int totalConnections) {
        Tags tags = Tags.of("adapter.type", adapterType, "pool.name", poolName);

        meterRegistry.gauge("adapter.connections.active", tags, activeConnections);
        meterRegistry.gauge("adapter.connections.idle", tags, idleConnections);
        meterRegistry.gauge("adapter.connections.total", tags, totalConnections);
    }

    /**
     * Record queue metrics for message - based adapters.
     */
    public void recordQueueMetrics(String adapterType, String queueName,
                                 long queueSize, long messagesInFlight) {
        Tags tags = Tags.of("adapter.type", adapterType, "queue.name", queueName);

        meterRegistry.gauge("adapter.queue.size", tags, queueSize);
        meterRegistry.gauge("adapter.queue.inflight", tags, messagesInFlight);
    }

    /**
     * Get current throughput rate.
     */
    public double getThroughputRate(String adapterType, String adapterMode, String operationName) {
        String metricKey = createMetricKey(adapterType, adapterMode, operationName);
        Counter counter = throughputCounters.get(metricKey);

        if(counter != null) {
            return counter.count();
        }

        return 0.0;
    }

    /**
     * Get error rate.
     */
    public double getErrorRate(String adapterType, String adapterMode, String operationName) {
        String metricKey = createMetricKey(adapterType, adapterMode, operationName);

        Counter throughput = throughputCounters.get(metricKey);
        double totalOps = throughput != null ? throughput.count() : 0.0;

        if(totalOps == 0) {
            return 0.0;
        }

        double totalErrors = errorCounters.entrySet().stream()
            .filter(e -> e.getKey().startsWith(metricKey + "-"))
            .mapToDouble(e -> e.getValue().count())
            .sum();

        return(totalErrors / totalOps) * 100.0;
    }

    /**
     * Get average operation duration.
     */
    public double getAverageDuration(String adapterType, String adapterMode, String operationName) {
        String metricKey = createMetricKey(adapterType, adapterMode, operationName);
        Timer timer = operationTimers.get(metricKey);

        if(timer != null) {
            return timer.mean(TimeUnit.MILLISECONDS);
        }

        return 0.0;
    }

    /**
     * Record SLA violation.
     */
    private void recordSLAViolation(String adapterType, String adapterMode, String operationName,
                                  long actualDurationNanos, long slaThresholdMs) {
        Counter.builder("adapter.sla.violations")
            .tags("adapter.type", adapterType, "adapter.mode", adapterMode, "operation", operationName)
            .description("Number of SLA violations")
            .register(meterRegistry)
            .increment();

        long actualMs = TimeUnit.NANOSECONDS.toMillis(actualDurationNanos);
        logger.warn("SLA violation for {} - {} - {}: {}ms(threshold: {}ms)",
                   adapterType, adapterMode, operationName, actualMs, slaThresholdMs);
    }

    /**
     * Get SLA durations for histogram buckets.
     */
    private Duration[] getSLADurations(String operationName) {
        Long slaMs = slaThresholds.get(operationName);
        if(slaMs != null) {
            return new Duration[] {
                Duration.ofMillis(slaMs / 4),     // 25% of SLA
                Duration.ofMillis(slaMs / 2),     // 50% of SLA
                Duration.ofMillis(slaMs * 3 / 4), // 75% of SLA
                Duration.ofMillis(slaMs),         // 100% of SLA
                Duration.ofMillis(slaMs * 2)       // 200% of SLA
            };
        }

        // Default buckets
        return new Duration[] {
            Duration.ofMillis(100),
            Duration.ofMillis(500),
            Duration.ofMillis(1000),
            Duration.ofMillis(5000),
            Duration.ofMillis(10000)
        };
    }

    /**
     * Create a metric key from adapter information.
     */
    private String createMetricKey(String adapterType, String adapterMode, String operationName) {
        return String.format("%s-%s-%s", adapterType, adapterMode, operationName);
    }

    /**
     * Export metrics summary.
     */
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new ConcurrentHashMap<>();

        // Add throughput rates
        throughputCounters.forEach((key, counter) -> {
            summary.put("throughput." + key, counter.count());
        });

        // Add error counts
        errorCounters.forEach((key, counter) -> {
            summary.put("errors." + key, counter.count());
        });

        // Add active operations
        activeOperations.forEach((key, gauge) -> {
            summary.put("active." + key, gauge.get());
        });

        // Add timer statistics
        operationTimers.forEach((key, timer) -> {
            summary.put("duration.mean." + key, timer.mean(TimeUnit.MILLISECONDS));
            summary.put("duration.max." + key, timer.max(TimeUnit.MILLISECONDS));
            summary.put("duration.count." + key, timer.count());
        });

        return summary;
    }

    // Static inner class for thread - safe atomic integer
    private static class AtomicInteger extends java.util.concurrent.atomic.AtomicInteger {
        public AtomicInteger(int initialValue) {
            super(initialValue);
        }
    }
}
