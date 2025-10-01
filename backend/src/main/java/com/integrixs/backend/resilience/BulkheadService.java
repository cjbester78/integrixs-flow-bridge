package com.integrixs.backend.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing bulkheads across adapters.
 * Provides resource isolation and concurrency control.
 */
@Service("backendBulkheadService")
public class BulkheadService {

    private static final Logger log = LoggerFactory.getLogger(BulkheadService.class);


    private final BulkheadRegistry bulkheadRegistry;
    private final ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;
    private final BulkheadConfiguration configuration;
    private final MeterRegistry meterRegistry;

    // Cache of active bulkheads
    private final Map<String, Bulkhead> bulkheadCache = new ConcurrentHashMap<>();
    private final Map<String, ThreadPoolBulkhead> threadPoolBulkheadCache = new ConcurrentHashMap<>();

    // Configuration values
    @Value("${resilience.bulkhead.thread-pool.core-size:10}")
    private int defaultCoreThreadPoolSize;

    @Value("${resilience.bulkhead.thread-pool.size:20}")
    private int defaultThreadPoolSize;

    @Value("${resilience.bulkhead.thread-pool.max-size:30}")
    private int defaultMaxThreadPoolSize;

    @Value("${resilience.bulkhead.utilization.threshold:100}")
    private double utilizationThreshold;

    public BulkheadService(BulkheadRegistry bulkheadRegistry,
                          ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry,
                          BulkheadConfiguration configuration,
                          MeterRegistry meterRegistry) {
        this.bulkheadRegistry = bulkheadRegistry;
        this.threadPoolBulkheadRegistry = threadPoolBulkheadRegistry;
        this.configuration = configuration;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Register event listeners for bulkhead creation
        bulkheadRegistry.getEventPublisher()
            .onEntryAdded(event -> {
                Bulkhead bulkhead = event.getAddedEntry();
                bulkhead.getEventPublisher()
                    .onCallPermitted(e -> recordMetric("permitted", bulkhead.getName()))
                    .onCallRejected(e -> {
                        log.warn("Bulkhead {} rejected call - max concurrent calls reached",
                            bulkhead.getName());
                        recordMetric("rejected", bulkhead.getName());
                    })
                    .onCallFinished(e -> recordMetric("finished", bulkhead.getName()));
            });
    }

    /**
     * Execute operation with bulkhead protection.
     */
    public <T> T executeWithBulkhead(String adapterType,
                                    String adapterId,
                                    Supplier<T> operation) {
        Bulkhead bulkhead = getOrCreateBulkhead(adapterType, adapterId);

        return bulkhead.executeSupplier(operation);
    }

    /**
     * Execute operation with bulkhead and fallback.
     */
    public <T> T executeWithFallback(String adapterType,
                                    String adapterId,
                                    Supplier<T> operation,
                                    Supplier<T> fallback) {
        Bulkhead bulkhead = getOrCreateBulkhead(adapterType, adapterId);

        Supplier<T> decoratedSupplier = Bulkhead.decorateSupplier(bulkhead, operation);

        try {
            return decoratedSupplier.get();
        } catch(io.github.resilience4j.bulkhead.BulkheadFullException e) {
            log.warn("Bulkhead {} full, executing fallback", bulkhead.getName());
            return fallback.get();
        }
    }

    /**
     * Execute async operation with thread pool bulkhead.
     */
    public <T> CompletableFuture<T> executeAsync(String adapterType,
                                                String adapterId,
                                                Callable<T> operation) {
        ThreadPoolBulkhead bulkhead = getOrCreateThreadPoolBulkhead(adapterType, adapterId);

        return bulkhead.executeCallable(operation).toCompletableFuture();
    }

    /**
     * Get bulkhead metrics.
     */
    public BulkheadMetrics getMetrics(String adapterType, String adapterId) {
        Bulkhead bulkhead = getOrCreateBulkhead(adapterType, adapterId);
        Bulkhead.Metrics metrics = bulkhead.getMetrics();

        return BulkheadMetrics.builder()
            .adapterType(adapterType)
            .adapterId(adapterId)
            .availableConcurrentCalls(metrics.getAvailableConcurrentCalls())
            .maxAllowedConcurrentCalls(metrics.getMaxAllowedConcurrentCalls())
            .build();
    }

    /**
     * Get thread pool bulkhead metrics.
     */
    public ThreadPoolBulkheadMetrics getThreadPoolMetrics(String adapterType, String adapterId) {
        ThreadPoolBulkhead bulkhead = getOrCreateThreadPoolBulkhead(adapterType, adapterId);
        ThreadPoolBulkhead.Metrics metrics = bulkhead.getMetrics();

        return ThreadPoolBulkheadMetrics.builder()
            .adapterType(adapterType)
            .adapterId(adapterId)
            .coreThreadPoolSize(defaultCoreThreadPoolSize)
            .threadPoolSize(defaultThreadPoolSize)
            .maxThreadPoolSize(defaultMaxThreadPoolSize)
            .queueDepth(metrics.getQueueDepth())
            .remainingQueueCapacity(metrics.getRemainingQueueCapacity())
            .build();
    }

    /**
     * Get all bulkhead statuses.
     */
    public Map<String, BulkheadMetrics> getAllMetrics() {
        Map<String, BulkheadMetrics> allMetrics = new HashMap<>();

        for(Bulkhead bulkhead : bulkheadRegistry.getAllBulkheads()) {
            String[] parts = bulkhead.getName().split("-", 2);
            if(parts.length == 2) {
                allMetrics.put(bulkhead.getName(), getMetrics(parts[0], parts[1]));
            }
        }

        return allMetrics;
    }

    /**
     * Check if adapter resources are available.
     */
    public boolean hasAvailableCapacity(String adapterType, String adapterId) {
        Bulkhead bulkhead = getOrCreateBulkhead(adapterType, adapterId);
        return bulkhead.getMetrics().getAvailableConcurrentCalls() > 0;
    }

    /**
     * Get current utilization percentage.
     */
    public double getUtilizationPercentage(String adapterType, String adapterId) {
        Bulkhead bulkhead = getOrCreateBulkhead(adapterType, adapterId);
        Bulkhead.Metrics metrics = bulkhead.getMetrics();

        int used = metrics.getMaxAllowedConcurrentCalls() - metrics.getAvailableConcurrentCalls();
        return (double) used / metrics.getMaxAllowedConcurrentCalls() * utilizationThreshold;
    }

    private Bulkhead getOrCreateBulkhead(String adapterType, String adapterId) {
        String key = adapterType + "-" + adapterId;

        return bulkheadCache.computeIfAbsent(key, k -> {
            Bulkhead bulkhead = configuration.getBulkhead(
                bulkheadRegistry, adapterType, adapterId);

            // Register metrics
            Tags tags = Tags.of("adapter.type", adapterType, "adapter.id", adapterId);
            bulkhead.getEventPublisher()
                .onCallPermitted(event ->
                    meterRegistry.counter("bulkhead.calls",
                        tags.and("result", "permitted")).increment())
                .onCallRejected(event ->
                    meterRegistry.counter("bulkhead.calls",
                        tags.and("result", "rejected")).increment())
                .onCallFinished(event ->
                    meterRegistry.counter("bulkhead.calls",
                        tags.and("result", "finished")).increment());

            return bulkhead;
        });
    }

    private ThreadPoolBulkhead getOrCreateThreadPoolBulkhead(String adapterType, String adapterId) {
        String key = adapterType + "-" + adapterId + " - async";

        return threadPoolBulkheadCache.computeIfAbsent(key, k -> {
            ThreadPoolBulkhead bulkhead = configuration.getThreadPoolBulkhead(
                threadPoolBulkheadRegistry, adapterType, adapterId);

            // Register metrics
            Tags tags = Tags.of("adapter.type", adapterType, "adapter.id", adapterId);
            bulkhead.getEventPublisher()
                .onCallPermitted(event ->
                    meterRegistry.counter("thread.pool.bulkhead.calls",
                        tags.and("result", "permitted")).increment())
                .onCallRejected(event ->
                    meterRegistry.counter("thread.pool.bulkhead.calls",
                        tags.and("result", "rejected")).increment());

            return bulkhead;
        });
    }

    private void recordMetric(String type, String bulkheadName) {
        meterRegistry.counter("bulkhead.events",
            "type", type,
            "name", bulkheadName).increment();
    }

    public static class BulkheadMetrics {
        private String adapterType;
        private String adapterId;
        private int availableConcurrentCalls;
        private int maxAllowedConcurrentCalls;

        // Getters and Setters
        public String getAdapterType() {
            return adapterType;
        }

        public void setAdapterType(String adapterType) {
            this.adapterType = adapterType;
        }

        public String getAdapterId() {
            return adapterId;
        }

        public void setAdapterId(String adapterId) {
            this.adapterId = adapterId;
        }

        public int getAvailableConcurrentCalls() {
            return availableConcurrentCalls;
        }

        public void setAvailableConcurrentCalls(int availableConcurrentCalls) {
            this.availableConcurrentCalls = availableConcurrentCalls;
        }

        public int getMaxAllowedConcurrentCalls() {
            return maxAllowedConcurrentCalls;
        }

        public void setMaxAllowedConcurrentCalls(int maxAllowedConcurrentCalls) {
            this.maxAllowedConcurrentCalls = maxAllowedConcurrentCalls;
        }

        // Builder pattern
        public static BulkheadMetricsBuilder builder() {
            return new BulkheadMetricsBuilder();
        }

        public static class BulkheadMetricsBuilder {
            private String adapterType;
            private String adapterId;
            private int availableConcurrentCalls;
            private int maxAllowedConcurrentCalls;

            public BulkheadMetricsBuilder adapterType(String adapterType) {
                this.adapterType = adapterType;
                return this;
            }

            public BulkheadMetricsBuilder adapterId(String adapterId) {
                this.adapterId = adapterId;
                return this;
            }

            public BulkheadMetricsBuilder availableConcurrentCalls(int availableConcurrentCalls) {
                this.availableConcurrentCalls = availableConcurrentCalls;
                return this;
            }

            public BulkheadMetricsBuilder maxAllowedConcurrentCalls(int maxAllowedConcurrentCalls) {
                this.maxAllowedConcurrentCalls = maxAllowedConcurrentCalls;
                return this;
            }

            public BulkheadMetrics build() {
                BulkheadMetrics metrics = new BulkheadMetrics();
                metrics.adapterType = this.adapterType;
                metrics.adapterId = this.adapterId;
                metrics.availableConcurrentCalls = this.availableConcurrentCalls;
                metrics.maxAllowedConcurrentCalls = this.maxAllowedConcurrentCalls;
                return metrics;
            }
        }
    }

    public static class ThreadPoolBulkheadMetrics {
        private String adapterType;
        private String adapterId;
        private int coreThreadPoolSize;
        private int threadPoolSize;
        private int maxThreadPoolSize;
        private int queueDepth;
        private int remainingQueueCapacity;

        // Getters and Setters
        public String getAdapterType() {
            return adapterType;
        }

        public void setAdapterType(String adapterType) {
            this.adapterType = adapterType;
        }

        public String getAdapterId() {
            return adapterId;
        }

        public void setAdapterId(String adapterId) {
            this.adapterId = adapterId;
        }

        public int getCoreThreadPoolSize() {
            return coreThreadPoolSize;
        }

        public void setCoreThreadPoolSize(int coreThreadPoolSize) {
            this.coreThreadPoolSize = coreThreadPoolSize;
        }

        public int getThreadPoolSize() {
            return threadPoolSize;
        }

        public void setThreadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
        }

        public int getMaxThreadPoolSize() {
            return maxThreadPoolSize;
        }

        public void setMaxThreadPoolSize(int maxThreadPoolSize) {
            this.maxThreadPoolSize = maxThreadPoolSize;
        }

        public int getQueueDepth() {
            return queueDepth;
        }

        public void setQueueDepth(int queueDepth) {
            this.queueDepth = queueDepth;
        }

        public int getRemainingQueueCapacity() {
            return remainingQueueCapacity;
        }

        public void setRemainingQueueCapacity(int remainingQueueCapacity) {
            this.remainingQueueCapacity = remainingQueueCapacity;
        }

        // Builder pattern
        public static ThreadPoolBulkheadMetricsBuilder builder() {
            return new ThreadPoolBulkheadMetricsBuilder();
        }

        public static class ThreadPoolBulkheadMetricsBuilder {
            private String adapterType;
            private String adapterId;
            private int coreThreadPoolSize;
            private int threadPoolSize;
            private int maxThreadPoolSize;
            private int queueDepth;
            private int remainingQueueCapacity;

            public ThreadPoolBulkheadMetricsBuilder adapterType(String adapterType) {
                this.adapterType = adapterType;
                return this;
            }

            public ThreadPoolBulkheadMetricsBuilder adapterId(String adapterId) {
                this.adapterId = adapterId;
                return this;
            }

            public ThreadPoolBulkheadMetricsBuilder coreThreadPoolSize(int coreThreadPoolSize) {
                this.coreThreadPoolSize = coreThreadPoolSize;
                return this;
            }

            public ThreadPoolBulkheadMetricsBuilder threadPoolSize(int threadPoolSize) {
                this.threadPoolSize = threadPoolSize;
                return this;
            }

            public ThreadPoolBulkheadMetricsBuilder maxThreadPoolSize(int maxThreadPoolSize) {
                this.maxThreadPoolSize = maxThreadPoolSize;
                return this;
            }

            public ThreadPoolBulkheadMetricsBuilder queueDepth(int queueDepth) {
                this.queueDepth = queueDepth;
                return this;
            }

            public ThreadPoolBulkheadMetricsBuilder remainingQueueCapacity(int remainingQueueCapacity) {
                this.remainingQueueCapacity = remainingQueueCapacity;
                return this;
            }

            public ThreadPoolBulkheadMetrics build() {
                ThreadPoolBulkheadMetrics metrics = new ThreadPoolBulkheadMetrics();
                metrics.adapterType = this.adapterType;
                metrics.adapterId = this.adapterId;
                metrics.coreThreadPoolSize = this.coreThreadPoolSize;
                metrics.threadPoolSize = this.threadPoolSize;
                metrics.maxThreadPoolSize = this.maxThreadPoolSize;
                metrics.queueDepth = this.queueDepth;
                metrics.remainingQueueCapacity = this.remainingQueueCapacity;
                return metrics;
            }
        }
    }
}
