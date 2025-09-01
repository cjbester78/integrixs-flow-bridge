package com.integrixs.backend.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Service for managing bulkheads across adapters.
 * Provides resource isolation and concurrency control.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BulkheadService {
    
    private final BulkheadRegistry bulkheadRegistry;
    private final ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;
    private final BulkheadConfiguration configuration;
    private final MeterRegistry meterRegistry;
    
    // Cache of active bulkheads
    private final Map<String, Bulkhead> bulkheadCache = new ConcurrentHashMap<>();
    private final Map<String, ThreadPoolBulkhead> threadPoolBulkheadCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        // Register event listeners for all bulkheads
        bulkheadRegistry.getEventPublisher()
            .onCallPermitted(event -> recordMetric("permitted", event.getBulkheadName()))
            .onCallRejected(event -> {
                log.warn("Bulkhead {} rejected call - max concurrent calls reached", 
                    event.getBulkheadName());
                recordMetric("rejected", event.getBulkheadName());
            })
            .onCallFinished(event -> recordMetric("finished", event.getBulkheadName()));
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
        } catch (io.github.resilience4j.bulkhead.BulkheadFullException e) {
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
        
        return bulkhead.executeCallable(operation);
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
            .coreThreadPoolSize(metrics.getCoreThreadPoolSize())
            .threadPoolSize(metrics.getThreadPoolSize())
            .maxThreadPoolSize(metrics.getMaxThreadPoolSize())
            .queueDepth(metrics.getQueueDepth())
            .remainingQueueCapacity(metrics.getRemainingQueueCapacity())
            .build();
    }
    
    /**
     * Get all bulkhead statuses.
     */
    public Map<String, BulkheadMetrics> getAllMetrics() {
        Map<String, BulkheadMetrics> allMetrics = new HashMap<>();
        
        for (Bulkhead bulkhead : bulkheadRegistry.getAllBulkheads()) {
            String[] parts = bulkhead.getName().split("-", 2);
            if (parts.length == 2) {
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
        return (double) used / metrics.getMaxAllowedConcurrentCalls() * 100;
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
        String key = adapterType + "-" + adapterId + "-async";
        
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
    
    @lombok.Builder
    @lombok.Data
    public static class BulkheadMetrics {
        private String adapterType;
        private String adapterId;
        private int availableConcurrentCalls;
        private int maxAllowedConcurrentCalls;
    }
    
    @lombok.Builder
    @lombok.Data
    public static class ThreadPoolBulkheadMetrics {
        private String adapterType;
        private String adapterId;
        private int coreThreadPoolSize;
        private int threadPoolSize;
        private int maxThreadPoolSize;
        private int queueDepth;
        private int remainingQueueCapacity;
    }
}