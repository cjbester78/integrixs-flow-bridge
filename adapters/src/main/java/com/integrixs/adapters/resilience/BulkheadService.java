package com.integrixs.adapters.resilience;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Service for managing bulkheads for adapters to limit concurrent executions
 */
@Service
public class BulkheadService {
    private static final Logger log = LoggerFactory.getLogger(BulkheadService.class);


    private final BulkheadRegistry bulkheadRegistry;
    private final ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;
    private final ConcurrentHashMap<String, Bulkhead> bulkheads = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ThreadPoolBulkhead> threadPoolBulkheads = new ConcurrentHashMap<>();

    public BulkheadService() {
        // Semaphore bulkhead configuration
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(25)
            .maxWaitDuration(Duration.ofMillis(500))
            .build();

        this.bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);

        // Thread pool bulkhead configuration
        ThreadPoolBulkheadConfig threadPoolConfig = ThreadPoolBulkheadConfig.custom()
            .maxThreadPoolSize(10)
            .coreThreadPoolSize(5)
            .queueCapacity(100)
            .keepAliveDuration(Duration.ofMillis(1000))
            .build();

        this.threadPoolBulkheadRegistry = ThreadPoolBulkheadRegistry.of(threadPoolConfig);
    }

    /**
     * Get or create a semaphore bulkhead
     */
    public Bulkhead getBulkhead(String name) {
        return bulkheads.computeIfAbsent(name, key ->
            bulkheadRegistry.bulkhead(name)
       );
    }

    /**
     * Get or create a thread pool bulkhead
     */
    public ThreadPoolBulkhead getThreadPoolBulkhead(String name) {
        return threadPoolBulkheads.computeIfAbsent(name, key ->
            threadPoolBulkheadRegistry.bulkhead(name)
       );
    }

    /**
     * Execute with semaphore bulkhead
     */
    public <T> T executeWithBulkhead(String bulkheadName, Callable<T> callable) throws Exception {
        Bulkhead bulkhead = getBulkhead(bulkheadName);
        return bulkhead.executeCallable(callable);
    }

    /**
     * Execute with semaphore bulkhead using supplier
     */
    public <T> T executeWithBulkhead(String bulkheadName, Supplier<T> supplier) {
        Bulkhead bulkhead = getBulkhead(bulkheadName);
        return bulkhead.executeSupplier(supplier);
    }

    /**
     * Execute asynchronously with thread pool bulkhead
     */
    public <T> CompletableFuture<T> executeAsyncWithBulkhead(String bulkheadName, Supplier<T> supplier) {
        ThreadPoolBulkhead bulkhead = getThreadPoolBulkhead(bulkheadName);
        return bulkhead.executeSupplier(() ->
            CompletableFuture.completedFuture(supplier.get())
        ).toCompletableFuture().thenCompose(future -> future);
    }

    /**
     * Get available concurrent calls
     */
    public int getAvailableConcurrentCalls(String bulkheadName) {
        Bulkhead bulkhead = getBulkhead(bulkheadName);
        return bulkhead.getMetrics().getAvailableConcurrentCalls();
    }

    /**
     * Execute with fallback
     */
    public <T> T executeWithFallback(String bulkheadName, String instanceId,
                                     Supplier<T> supplier, Supplier<T> fallbackSupplier) {
        try {
            return executeWithBulkhead(bulkheadName, supplier);
        } catch (io.github.resilience4j.bulkhead.BulkheadFullException e) {
            log.warn("Bulkhead {} is full for instance {}, executing fallback", bulkheadName, instanceId);
            return fallbackSupplier.get();
        } catch (Exception e) {
            log.error("Error executing bulkhead operation for {} instance {}", bulkheadName, instanceId, e);
            throw e;
        }
    }
}
