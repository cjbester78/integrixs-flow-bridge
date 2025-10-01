package com.integrixs.backend.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for bulkheads to isolate adapter resources.
 * Provides both semaphore and thread pool bulkheads based on adapter characteristics.
 */
@Configuration
public class BulkheadConfiguration {

    @Value("${resilience4j.bulkhead.instances.default.maxConcurrentCalls:25}")
    private int defaultMaxConcurrentCalls;

    @Value("${resilience4j.bulkhead.instances.default.maxWaitDuration:5000}")
    private long defaultMaxWaitDuration;

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        Map<String, BulkheadConfig> configs = new HashMap<>();

        // Default configuration
        configs.put("default", createDefaultBulkheadConfig());

        // HTTP/REST adapters-higher concurrency
        configs.put("http", createHttpBulkheadConfig());

        // Database adapters-limited connections
        configs.put("database", createDatabaseBulkheadConfig());

        // Message queue adapters-controlled throughput
        configs.put("messaging", createMessagingBulkheadConfig());

        // File transfer adapters-limited parallel operations
        configs.put("file", createFileBulkheadConfig());

        // SAP adapters-very limited concurrent calls
        configs.put("sap", createSapBulkheadConfig());

        return BulkheadRegistry.of(configs);
    }

    @Bean
    public ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry() {
        Map<String, ThreadPoolBulkheadConfig> configs = new HashMap<>();

        // Thread pool configurations for async operations
        configs.put("default", createDefaultThreadPoolConfig());
        configs.put("http-async", createHttpThreadPoolConfig());
        configs.put("messaging-async", createMessagingThreadPoolConfig());
        configs.put("file-async", createFileThreadPoolConfig());

        return ThreadPoolBulkheadRegistry.of(configs);
    }

    private BulkheadConfig createDefaultBulkheadConfig() {
        return BulkheadConfig.custom()
            .maxConcurrentCalls(defaultMaxConcurrentCalls)
            .maxWaitDuration(Duration.ofMillis(defaultMaxWaitDuration))
            .writableStackTraceEnabled(false)
            .build();
    }

    private BulkheadConfig createHttpBulkheadConfig() {
        return BulkheadConfig.custom()
            .maxConcurrentCalls(50) // Higher concurrency for HTTP
            .maxWaitDuration(Duration.ofSeconds(2)) // Short wait
            .writableStackTraceEnabled(false)
            .build();
    }

    private BulkheadConfig createDatabaseBulkheadConfig() {
        return BulkheadConfig.custom()
            .maxConcurrentCalls(10) // Limited by connection pool
            .maxWaitDuration(Duration.ofSeconds(10)) // Longer wait acceptable
            .writableStackTraceEnabled(false)
            .build();
    }

    private BulkheadConfig createMessagingBulkheadConfig() {
        return BulkheadConfig.custom()
            .maxConcurrentCalls(20) // Moderate concurrency
            .maxWaitDuration(Duration.ofSeconds(5))
            .writableStackTraceEnabled(false)
            .build();
    }

    private BulkheadConfig createFileBulkheadConfig() {
        return BulkheadConfig.custom()
            .maxConcurrentCalls(5) // Limited file operations
            .maxWaitDuration(Duration.ofSeconds(30)) // Files can take time
            .writableStackTraceEnabled(false)
            .build();
    }

    private BulkheadConfig createSapBulkheadConfig() {
        return BulkheadConfig.custom()
            .maxConcurrentCalls(3) // Very limited for SAP
            .maxWaitDuration(Duration.ofMinutes(1)) // Long wait for critical system
            .writableStackTraceEnabled(false)
            .build();
    }

    // Thread pool configurations for async operations

    private ThreadPoolBulkheadConfig createDefaultThreadPoolConfig() {
        return ThreadPoolBulkheadConfig.custom()
            .maxThreadPoolSize(10)
            .coreThreadPoolSize(5)
            .queueCapacity(100)
            .keepAliveDuration(Duration.ofMillis(1000))
            .build();
    }

    private ThreadPoolBulkheadConfig createHttpThreadPoolConfig() {
        return ThreadPoolBulkheadConfig.custom()
            .maxThreadPoolSize(20)
            .coreThreadPoolSize(10)
            .queueCapacity(200)
            .keepAliveDuration(Duration.ofMillis(500))
            .build();
    }

    private ThreadPoolBulkheadConfig createMessagingThreadPoolConfig() {
        return ThreadPoolBulkheadConfig.custom()
            .maxThreadPoolSize(15)
            .coreThreadPoolSize(8)
            .queueCapacity(500) // Large queue for messages
            .keepAliveDuration(Duration.ofMillis(2000))
            .build();
    }

    private ThreadPoolBulkheadConfig createFileThreadPoolConfig() {
        return ThreadPoolBulkheadConfig.custom()
            .maxThreadPoolSize(5)
            .coreThreadPoolSize(2)
            .queueCapacity(50) // Small queue for file operations
            .keepAliveDuration(Duration.ofMillis(5000))
            .build();
    }

    /**
     * Get bulkhead for a specific adapter.
     */
    public Bulkhead getBulkhead(BulkheadRegistry registry, String adapterType, String adapterId) {
        String configName = mapAdapterTypeToConfig(adapterType);
        String bulkheadName = adapterType + "-" + adapterId;

        return registry.bulkhead(bulkheadName, configName);
    }

    /**
     * Get thread pool bulkhead for async operations.
     */
    public ThreadPoolBulkhead getThreadPoolBulkhead(ThreadPoolBulkheadRegistry registry,
                                                   String adapterType,
                                                   String adapterId) {
        String configName = mapAdapterTypeToThreadPoolConfig(adapterType);
        String bulkheadName = adapterType + "-" + adapterId + "-async";

        return registry.bulkhead(bulkheadName, configName);
    }

    private String mapAdapterTypeToConfig(String adapterType) {
        switch(adapterType.toUpperCase()) {
            case "HTTP":
            case "REST":
            case "SOAP":
            case "ODATA":
                return "http";

            case "JDBC":
                return "database";

            case "JMS":
            case "KAFKA":
                return "messaging";

            case "FTP":
            case "SFTP":
                return "file";

            case "RFC":
            case "IDOC":
                return "sap";

            default:
                return "default";
        }
    }

    private String mapAdapterTypeToThreadPoolConfig(String adapterType) {
        switch(adapterType.toUpperCase()) {
            case "HTTP":
            case "REST":
            case "SOAP":
            case "ODATA":
                return "http-async";

            case "JMS":
            case "KAFKA":
                return "messaging-async";

            case "FTP":
            case "SFTP":
                return "file-async";

            default:
                return "default";
        }
    }
}
