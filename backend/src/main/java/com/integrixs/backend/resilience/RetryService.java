package com.integrixs.backend.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing retry policies across adapters.
 * Provides centralized retry operations with monitoring.
 */
@Service("backendRetryService")
public class RetryService {

    private static final Logger log = LoggerFactory.getLogger(RetryService.class);


    private final RetryRegistry retryRegistry;
    private final RetryPolicyConfiguration configuration;
    private final MeterRegistry meterRegistry;

    // Cache of active retry instances
    private final Map<String, Retry> retryCache = new ConcurrentHashMap<>();

    // Executor for async retries
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    public RetryService(RetryRegistry retryRegistry,
                       RetryPolicyConfiguration configuration,
                       MeterRegistry meterRegistry) {
        this.retryRegistry = retryRegistry;
        this.configuration = configuration;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Register event listeners for all retry instances
        retryRegistry.getEventPublisher()
            .onEntryAdded(event -> {
                Retry retry = event.getAddedEntry();
                retry.getEventPublisher()
                    .onRetry(evt -> {
                        log.debug("Retry attempt {} for {}",
                            evt.getNumberOfRetryAttempts(), evt.getName());
                        recordMetric("retry", evt.getName());
                    })
                    .onSuccess(evt -> recordMetric("success", evt.getName()))
                    .onError(evt -> {
                        log.warn("Retry exhausted for {} after {} attempts",
                            evt.getName(), evt.getNumberOfRetryAttempts());
                        recordMetric("exhausted", evt.getName());
                    })
                    .onIgnoredError(evt -> recordMetric("ignored", evt.getName()));
            });
    }

    /**
     * Execute operation with retry.
     */
    public <T> T executeWithRetry(String adapterType,
                                 String adapterId,
                                 Supplier<T> operation) {
        Retry retry = getOrCreateRetry(adapterType, adapterId);

        return retry.executeSupplier(operation);
    }

    /**
     * Execute operation with retry and fallback.
     */
    public <T> T executeWithFallback(String adapterType,
                                    String adapterId,
                                    Supplier<T> operation,
                                    Supplier<T> fallback) {
        Retry retry = getOrCreateRetry(adapterType, adapterId);

        Supplier<T> decoratedSupplier = Retry.decorateSupplier(retry, operation);

        try {
            return decoratedSupplier.get();
        } catch(Exception e) {
            log.warn("Retry exhausted for {} - {}, executing fallback", adapterType, adapterId, e);
            return fallback.get();
        }
    }

    /**
     * Execute async operation with retry.
     */
    public <T> CompletableFuture<T> executeAsync(String adapterType,
                                                String adapterId,
                                                Supplier<CompletableFuture<T>> operation) {
        Retry retry = getOrCreateRetry(adapterType, adapterId);

        return Retry.decorateCompletionStage(retry, executorService, () -> operation.get())
            .get()
            .toCompletableFuture();
    }

    /**
     * Execute callable with retry.
     */
    public <T> T executeCallable(String adapterType,
                               String adapterId,
                               Callable<T> operation) throws Exception {
        Retry retry = getOrCreateRetry(adapterType, adapterId);

        return retry.executeCallable(operation);
    }

    /**
     * Get retry metrics.
     */
    public RetryMetrics getMetrics(String adapterType, String adapterId) {
        Retry retry = getOrCreateRetry(adapterType, adapterId);
        Retry.Metrics metrics = retry.getMetrics();

        return RetryMetrics.builder()
            .adapterType(adapterType)
            .adapterId(adapterId)
            .numberOfSuccessfulCallsWithoutRetryAttempt(
                metrics.getNumberOfSuccessfulCallsWithoutRetryAttempt())
            .numberOfSuccessfulCallsWithRetryAttempt(
                metrics.getNumberOfSuccessfulCallsWithRetryAttempt())
            .numberOfFailedCallsWithoutRetryAttempt(
                metrics.getNumberOfFailedCallsWithoutRetryAttempt())
            .numberOfFailedCallsWithRetryAttempt(
                metrics.getNumberOfFailedCallsWithRetryAttempt())
            .build();
    }

    /**
     * Get all retry metrics.
     */
    public Map<String, RetryMetrics> getAllMetrics() {
        Map<String, RetryMetrics> allMetrics = new HashMap<>();

        for(Retry retry : retryRegistry.getAllRetries()) {
            String[] parts = retry.getName().split("-", 2);
            if(parts.length == 2) {
                allMetrics.put(retry.getName(), getMetrics(parts[0], parts[1]));
            }
        }

        return allMetrics;
    }

    /**
     * Get retry configuration for adapter type.
     */
    public RetryConfigInfo getRetryConfig(String adapterType) {
        String configName = configuration.mapAdapterTypeToConfig(adapterType);
        io.github.resilience4j.retry.RetryConfig config =
            retryRegistry.getConfiguration(configName)
                .orElse(retryRegistry.getDefaultConfig());

        return RetryConfigInfo.builder()
            .adapterType(adapterType)
            .maxAttempts(config.getMaxAttempts())
            .waitDuration(1000L) // Default 1 second wait duration
            .retryExceptions(List.of("Exception")) // Default retry on all exceptions
            .ignoreExceptions(List.of()) // No ignored exceptions by default
            .build();
    }

    private Retry getOrCreateRetry(String adapterType, String adapterId) {
        String key = adapterType + "-" + adapterId;

        return retryCache.computeIfAbsent(key, k -> {
            Retry retry = configuration.getRetry(retryRegistry, adapterType, adapterId);

            // Register metrics
            Tags tags = Tags.of("adapter.type", adapterType, "adapter.id", adapterId);
            retry.getEventPublisher()
                .onRetry(event ->
                    meterRegistry.counter("retry.attempts", tags).increment())
                .onSuccess(event ->
                    meterRegistry.counter("retry.calls",
                        tags.and("result", "success")).increment())
                .onError(event ->
                    meterRegistry.counter("retry.calls",
                        tags.and("result", "exhausted")).increment());

            return retry;
        });
    }

    private void recordMetric(String type, String retryName) {
        meterRegistry.counter("retry.events",
            "type", type,
            "name", retryName).increment();
    }

    public static class RetryMetrics {
        private String adapterType;
        private String adapterId;
        private long numberOfSuccessfulCallsWithoutRetryAttempt;
        private long numberOfSuccessfulCallsWithRetryAttempt;
        private long numberOfFailedCallsWithoutRetryAttempt;
        private long numberOfFailedCallsWithRetryAttempt;

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

        public long getNumberOfSuccessfulCallsWithoutRetryAttempt() {
            return numberOfSuccessfulCallsWithoutRetryAttempt;
        }

        public void setNumberOfSuccessfulCallsWithoutRetryAttempt(long numberOfSuccessfulCallsWithoutRetryAttempt) {
            this.numberOfSuccessfulCallsWithoutRetryAttempt = numberOfSuccessfulCallsWithoutRetryAttempt;
        }

        public long getNumberOfSuccessfulCallsWithRetryAttempt() {
            return numberOfSuccessfulCallsWithRetryAttempt;
        }

        public void setNumberOfSuccessfulCallsWithRetryAttempt(long numberOfSuccessfulCallsWithRetryAttempt) {
            this.numberOfSuccessfulCallsWithRetryAttempt = numberOfSuccessfulCallsWithRetryAttempt;
        }

        public long getNumberOfFailedCallsWithoutRetryAttempt() {
            return numberOfFailedCallsWithoutRetryAttempt;
        }

        public void setNumberOfFailedCallsWithoutRetryAttempt(long numberOfFailedCallsWithoutRetryAttempt) {
            this.numberOfFailedCallsWithoutRetryAttempt = numberOfFailedCallsWithoutRetryAttempt;
        }

        public long getNumberOfFailedCallsWithRetryAttempt() {
            return numberOfFailedCallsWithRetryAttempt;
        }

        public void setNumberOfFailedCallsWithRetryAttempt(long numberOfFailedCallsWithRetryAttempt) {
            this.numberOfFailedCallsWithRetryAttempt = numberOfFailedCallsWithRetryAttempt;
        }

        // Builder pattern
        public static RetryMetricsBuilder builder() {
            return new RetryMetricsBuilder();
        }

        public static class RetryMetricsBuilder {
            private String adapterType;
            private String adapterId;
            private long numberOfSuccessfulCallsWithoutRetryAttempt;
            private long numberOfSuccessfulCallsWithRetryAttempt;
            private long numberOfFailedCallsWithoutRetryAttempt;
            private long numberOfFailedCallsWithRetryAttempt;

            public RetryMetricsBuilder adapterType(String adapterType) {
                this.adapterType = adapterType;
                return this;
            }

            public RetryMetricsBuilder adapterId(String adapterId) {
                this.adapterId = adapterId;
                return this;
            }

            public RetryMetricsBuilder numberOfSuccessfulCallsWithoutRetryAttempt(long numberOfSuccessfulCallsWithoutRetryAttempt) {
                this.numberOfSuccessfulCallsWithoutRetryAttempt = numberOfSuccessfulCallsWithoutRetryAttempt;
                return this;
            }

            public RetryMetricsBuilder numberOfSuccessfulCallsWithRetryAttempt(long numberOfSuccessfulCallsWithRetryAttempt) {
                this.numberOfSuccessfulCallsWithRetryAttempt = numberOfSuccessfulCallsWithRetryAttempt;
                return this;
            }

            public RetryMetricsBuilder numberOfFailedCallsWithoutRetryAttempt(long numberOfFailedCallsWithoutRetryAttempt) {
                this.numberOfFailedCallsWithoutRetryAttempt = numberOfFailedCallsWithoutRetryAttempt;
                return this;
            }

            public RetryMetricsBuilder numberOfFailedCallsWithRetryAttempt(long numberOfFailedCallsWithRetryAttempt) {
                this.numberOfFailedCallsWithRetryAttempt = numberOfFailedCallsWithRetryAttempt;
                return this;
            }

            public RetryMetrics build() {
                RetryMetrics metrics = new RetryMetrics();
                metrics.adapterType = this.adapterType;
                metrics.adapterId = this.adapterId;
                metrics.numberOfSuccessfulCallsWithoutRetryAttempt = this.numberOfSuccessfulCallsWithoutRetryAttempt;
                metrics.numberOfSuccessfulCallsWithRetryAttempt = this.numberOfSuccessfulCallsWithRetryAttempt;
                metrics.numberOfFailedCallsWithoutRetryAttempt = this.numberOfFailedCallsWithoutRetryAttempt;
                metrics.numberOfFailedCallsWithRetryAttempt = this.numberOfFailedCallsWithRetryAttempt;
                return metrics;
            }
        }
    }

    public static class RetryConfigInfo {
        private String adapterType;
        private int maxAttempts;
        private long waitDuration;
        private List<String> retryExceptions;
        private List<String> ignoreExceptions;

        // Getters and Setters
        public String getAdapterType() {
            return adapterType;
        }

        public void setAdapterType(String adapterType) {
            this.adapterType = adapterType;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getWaitDuration() {
            return waitDuration;
        }

        public void setWaitDuration(long waitDuration) {
            this.waitDuration = waitDuration;
        }

        public List<String> getRetryExceptions() {
            return retryExceptions;
        }

        public void setRetryExceptions(List<String> retryExceptions) {
            this.retryExceptions = retryExceptions;
        }

        public List<String> getIgnoreExceptions() {
            return ignoreExceptions;
        }

        public void setIgnoreExceptions(List<String> ignoreExceptions) {
            this.ignoreExceptions = ignoreExceptions;
        }

        // Builder pattern
        public static RetryConfigInfoBuilder builder() {
            return new RetryConfigInfoBuilder();
        }

        public static class RetryConfigInfoBuilder {
            private String adapterType;
            private int maxAttempts;
            private long waitDuration;
            private List<String> retryExceptions;
            private List<String> ignoreExceptions;

            public RetryConfigInfoBuilder adapterType(String adapterType) {
                this.adapterType = adapterType;
                return this;
            }

            public RetryConfigInfoBuilder maxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
                return this;
            }

            public RetryConfigInfoBuilder waitDuration(long waitDuration) {
                this.waitDuration = waitDuration;
                return this;
            }

            public RetryConfigInfoBuilder retryExceptions(List<String> retryExceptions) {
                this.retryExceptions = retryExceptions;
                return this;
            }

            public RetryConfigInfoBuilder ignoreExceptions(List<String> ignoreExceptions) {
                this.ignoreExceptions = ignoreExceptions;
                return this;
            }

            public RetryConfigInfo build() {
                RetryConfigInfo info = new RetryConfigInfo();
                info.adapterType = this.adapterType;
                info.maxAttempts = this.maxAttempts;
                info.waitDuration = this.waitDuration;
                info.retryExceptions = this.retryExceptions;
                info.ignoreExceptions = this.ignoreExceptions;
                return info;
            }
        }
    }
}
