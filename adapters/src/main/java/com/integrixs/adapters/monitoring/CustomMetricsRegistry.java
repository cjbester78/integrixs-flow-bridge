package com.integrixs.adapters.monitoring;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Registry for custom adapter - specific metrics.
 * Allows adapters to register and update custom metrics dynamically.
 */
@Component
public class CustomMetricsRegistry {

    private static final Logger logger = LoggerFactory.getLogger(CustomMetricsRegistry.class);

    private final MeterRegistry meterRegistry;

    // Custom metric definitions
    private final Map<String, CustomMetricDefinition> metricDefinitions = new ConcurrentHashMap<>();

    // Gauge suppliers
    private final Map<String, Supplier<Number>> gaugeSuppliers = new ConcurrentHashMap<>();

    // Distribution summaries for custom measurements
    private final Map<String, DistributionSummary> distributionSummaries = new ConcurrentHashMap<>();

    @Autowired
    public CustomMetricsRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        registerDefaultMetrics();
    }

    /**
     * Register default custom metrics.
     */
    private void registerDefaultMetrics() {
        // Queue - based adapter metrics
        defineMetric("queue.depth", CustomMetricType.GAUGE, "Current queue depth", "messages");
        defineMetric("queue.lag", CustomMetricType.GAUGE, "Queue processing lag", "messages");
        defineMetric("queue.throughput", CustomMetricType.GAUGE, "Queue throughput rate", "messages/sec");

        // File - based adapter metrics
        defineMetric("file.size", CustomMetricType.DISTRIBUTION, "File size distribution", "bytes");
        defineMetric("file.processing.time", CustomMetricType.DISTRIBUTION, "File processing time", "milliseconds");
        defineMetric("directory.file.count", CustomMetricType.GAUGE, "Number of files in directory", "files");

        // Database adapter metrics
        defineMetric("db.connection.pool.active", CustomMetricType.GAUGE, "Active DB connections", "connections");
        defineMetric("db.connection.pool.idle", CustomMetricType.GAUGE, "Idle DB connections", "connections");
        defineMetric("db.query.rows", CustomMetricType.DISTRIBUTION, "Rows returned by queries", "rows");

        // HTTP/REST adapter metrics
        defineMetric("http.request.size", CustomMetricType.DISTRIBUTION, "HTTP request size", "bytes");
        defineMetric("http.response.size", CustomMetricType.DISTRIBUTION, "HTTP response size", "bytes");
        defineMetric("http.retry.count", CustomMetricType.COUNTER, "HTTP retry attempts", "retries");

        // Transformation metrics
        defineMetric("transform.input.size", CustomMetricType.DISTRIBUTION, "Transform input size", "bytes");
        defineMetric("transform.output.size", CustomMetricType.DISTRIBUTION, "Transform output size", "bytes");
        defineMetric("transform.compression.ratio", CustomMetricType.GAUGE, "Data compression ratio", "ratio");

        // Authentication metrics
        defineMetric("auth.token.expiry", CustomMetricType.GAUGE, "Time until token expiry", "seconds");
        defineMetric("auth.refresh.count", CustomMetricType.COUNTER, "Token refresh count", "refreshes");
        defineMetric("auth.failure.count", CustomMetricType.COUNTER, "Authentication failures", "failures");
    }

    /**
     * Define a custom metric.
     */
    public void defineMetric(String metricName, CustomMetricType type, String description, String unit) {
        CustomMetricDefinition definition = new CustomMetricDefinition(metricName, type, description, unit);
        metricDefinitions.put(metricName, definition);
        logger.debug("Defined custom metric: {}", definition);
    }

    /**
     * Register a gauge metric with a supplier.
     */
    public void registerGauge(String adapterType, String adapterMode, String metricName,
                            Supplier<Number> supplier, String... additionalTags) {
        CustomMetricDefinition definition = metricDefinitions.get(metricName);
        if(definition == null || definition.getType() != CustomMetricType.GAUGE) {
            logger.warn("Metric {} not defined as gauge", metricName);
            return;
        }

        String key = createKey(adapterType, adapterMode, metricName);
        gaugeSuppliers.put(key, supplier);

        Tags tags = createTags(adapterType, adapterMode, additionalTags);

        Gauge.builder("adapter.custom." + metricName, supplier)
            .tags(tags)
            .description(definition.getDescription())
            .baseUnit(definition.getUnit())
            .register(meterRegistry);

        logger.debug("Registered gauge metric: {} for {} - {}", metricName, adapterType, adapterMode);
    }

    /**
     * Update a gauge metric value.
     */
    public void updateGauge(String adapterType, String adapterMode, String metricName,
                          double value, String... additionalTags) {
        String fullMetricName = "adapter.custom." + metricName;
        Tags tags = createTags(adapterType, adapterMode, additionalTags);

        meterRegistry.gauge(fullMetricName, tags, value);
    }

    /**
     * Increment a counter metric.
     */
    public void incrementCounter(String adapterType, String adapterMode, String metricName,
                               double amount, String... additionalTags) {
        CustomMetricDefinition definition = metricDefinitions.get(metricName);
        if(definition == null || definition.getType() != CustomMetricType.COUNTER) {
            logger.warn("Metric {} not defined as counter", metricName);
            return;
        }

        Tags tags = createTags(adapterType, adapterMode, additionalTags);

        Counter.builder("adapter.custom." + metricName)
            .tags(tags)
            .description(definition.getDescription())
            .baseUnit(definition.getUnit())
            .register(meterRegistry)
            .increment(amount);
    }

    /**
     * Record a distribution value.
     */
    public void recordDistribution(String adapterType, String adapterMode, String metricName,
                                 double value, String... additionalTags) {
        CustomMetricDefinition definition = metricDefinitions.get(metricName);
        if(definition == null || definition.getType() != CustomMetricType.DISTRIBUTION) {
            logger.warn("Metric {} not defined as distribution", metricName);
            return;
        }

        String key = createKey(adapterType, adapterMode, metricName);
        Tags tags = createTags(adapterType, adapterMode, additionalTags);

        DistributionSummary summary = distributionSummaries.computeIfAbsent(key, k ->
            DistributionSummary.builder("adapter.custom." + metricName)
                .tags(tags)
                .description(definition.getDescription())
                .baseUnit(definition.getUnit())
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .register(meterRegistry)
       );

        summary.record(value);
    }

    /**
     * Record a timed operation.
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop a timer and record the duration.
     */
    public void recordTimer(String adapterType, String adapterMode, String metricName,
                          Timer.Sample sample, String... additionalTags) {
        Tags tags = createTags(adapterType, adapterMode, additionalTags);

        Timer timer = Timer.builder("adapter.custom." + metricName)
            .tags(tags)
            .description("Custom timed operation")
            .register(meterRegistry);

        sample.stop(timer);
    }

    /**
     * Create tags for metrics.
     */
    private Tags createTags(String adapterType, String adapterMode, String... additionalTags) {
        Tags tags = Tags.of("adapter.type", adapterType, "adapter.mode", adapterMode);

        if(additionalTags.length > 0 && additionalTags.length % 2 == 0) {
            for(int i = 0; i < additionalTags.length; i += 2) {
                tags = tags.and(additionalTags[i], additionalTags[i + 1]);
            }
        }

        return tags;
    }

    /**
     * Create a key for metric storage.
     */
    private String createKey(String adapterType, String adapterMode, String metricName) {
        return String.format("%s-%s-%s", adapterType, adapterMode, metricName);
    }

    /**
     * Get all defined metrics.
     */
    public Map<String, CustomMetricDefinition> getDefinedMetrics() {
        return new ConcurrentHashMap<>(metricDefinitions);
    }

    /**
     * Custom metric types.
     */
    public enum CustomMetricType {
        GAUGE,       // Point - in - time values
        COUNTER,     // Monotonically increasing values
        DISTRIBUTION, // Statistical distribution of values
        TIMER         // Duration measurements
    }

    /**
     * Custom metric definition.
     */
    public static class CustomMetricDefinition {
        private final String name;
        private final CustomMetricType type;
        private final String description;
        private final String unit;

        public CustomMetricDefinition(String name, CustomMetricType type,
                                    String description, String unit) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.unit = unit;
        }

        // Getters
        public String getName() { return name; }
        public CustomMetricType getType() { return type; }
        public String getDescription() { return description; }
        public String getUnit() { return unit; }

        @Override
        public String toString() {
            return String.format("CustomMetric[%s: type = %s, unit = %s, desc = %s]",
                name, type, unit, description);
        }
    }

    /**
     * Builder for custom metrics.
     */
    public static class MetricBuilder {
        private final String adapterType;
        private final String adapterMode;
        private final CustomMetricsRegistry registry;

        public MetricBuilder(String adapterType, String adapterMode, CustomMetricsRegistry registry) {
            this.adapterType = adapterType;
            this.adapterMode = adapterMode;
            this.registry = registry;
        }

        public MetricBuilder gauge(String metricName, Supplier<Number> supplier, String... tags) {
            registry.registerGauge(adapterType, adapterMode, metricName, supplier, tags);
            return this;
        }

        public MetricBuilder counter(String metricName, double amount, String... tags) {
            registry.incrementCounter(adapterType, adapterMode, metricName, amount, tags);
            return this;
        }

        public MetricBuilder distribution(String metricName, double value, String... tags) {
            registry.recordDistribution(adapterType, adapterMode, metricName, value, tags);
            return this;
        }
    }

    /**
     * Create a metric builder for fluent API.
     */
    public MetricBuilder forAdapter(String adapterType, String adapterMode) {
        return new MetricBuilder(adapterType, adapterMode, this);
    }
}
