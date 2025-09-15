package com.integrixs.backend.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Service for managing retry policies across adapters.
 * Provides centralized retry operations with monitoring.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryService {

    private final RetryRegistry retryRegistry;
    private final RetryPolicyConfiguration configuration;
    private final MeterRegistry meterRegistry;

    // Cache of active retry instances
    private final Map<String, Retry> retryCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Register event listeners for all retry instances
        retryRegistry.getEventPublisher()
            .onRetry(event -> {
                log.debug("Retry attempt {} for {}",
                    event.getNumberOfRetryAttempts(), event.getName());
                recordMetric("retry", event.getName());
            })
            .onSuccess(event -> recordMetric("success", event.getName()))
            .onError(event -> {
                log.warn("Retry exhausted for {} after {} attempts",
                    event.getName(), event.getNumberOfRetryAttempts());
                recordMetric("exhausted", event.getName());
            })
            .onIgnoredError(event -> recordMetric("ignored", event.getName()));
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

        return Retry.decorateCompletionStage(retry, () -> operation.get())
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
            .waitDuration(config.getWaitDuration().toMillis())
            .retryExceptions(config.getRetryExceptions().stream()
                .map(Class::getSimpleName)
                .toList())
            .ignoreExceptions(config.getIgnoreExceptions().stream()
                .map(Class::getSimpleName)
                .toList())
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

    @lombok.Builder
    @lombok.Data
    public static class RetryMetrics {
        private String adapterType;
        private String adapterId;
        private long numberOfSuccessfulCallsWithoutRetryAttempt;
        private long numberOfSuccessfulCallsWithRetryAttempt;
        private long numberOfFailedCallsWithoutRetryAttempt;
        private long numberOfFailedCallsWithRetryAttempt;
    }

    @lombok.Builder
    @lombok.Data
    public static class RetryConfigInfo {
        private String adapterType;
        private int maxAttempts;
        private long waitDuration;
        private List<String> retryExceptions;
        private List<String> ignoreExceptions;
    }
}
