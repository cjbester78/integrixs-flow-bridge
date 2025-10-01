package com.integrixs.backend.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Configuration for circuit breakers across different adapter types.
 * Provides customized circuit breaker configurations based on adapter characteristics.
 */
@Configuration
public class CircuitBreakerConfiguration {

    @Value("${resilience4j.circuitbreaker.instances.default.failureRateThreshold:50}")
    private float defaultFailureRateThreshold;

    @Value("${resilience4j.circuitbreaker.instances.default.waitDurationInOpenState:60000}")
    private long defaultWaitDurationInOpenState;

    @Value("${resilience4j.circuitbreaker.instances.default.slidingWindowSize:100}")
    private int defaultSlidingWindowSize;

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        // Create registry with custom configurations for different adapter types
        Map<String, CircuitBreakerConfig> configs = new HashMap<>();

        // Default configuration
        configs.put("default", createDefaultConfig());

        // HTTP/REST adapters-more sensitive to timeouts
        configs.put("http", createHttpConfig());

        // Database adapters-longer wait times, fewer retries
        configs.put("database", createDatabaseConfig());

        // Message queue adapters-high throughput, quick recovery
        configs.put("messaging", createMessagingConfig());

        // File transfer adapters-longer operations, patient recovery
        configs.put("file", createFileTransferConfig());

        // SAP adapters-critical systems, conservative approach
        configs.put("sap", createSapConfig());

        return CircuitBreakerRegistry.of(configs);
    }

    private CircuitBreakerConfig createDefaultConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(defaultFailureRateThreshold)
            .waitDurationInOpenState(Duration.ofMillis(defaultWaitDurationInOpenState))
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(defaultSlidingWindowSize)
            .permittedNumberOfCallsInHalfOpenState(10)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .slowCallRateThreshold(80)
            .slowCallDurationThreshold(Duration.ofSeconds(10))
            .recordExceptions(Exception.class)
            .ignoreExceptions()
            .build();
    }

    private CircuitBreakerConfig createHttpConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(40) // More sensitive to failures
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Faster recovery
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
            .slidingWindowSize(60) // 60 seconds window
            .permittedNumberOfCallsInHalfOpenState(5)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .slowCallRateThreshold(50)
            .slowCallDurationThreshold(Duration.ofSeconds(3)) // HTTP should be fast
            .recordExceptions(
                Exception.class,
                TimeoutException.class,
                java.net.SocketTimeoutException.class,
                java.net.ConnectException.class
           )
            .ignoreExceptions()
            .build();
    }

    private CircuitBreakerConfig createDatabaseConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(60) // More tolerant of failures
            .waitDurationInOpenState(Duration.ofMinutes(2)) // Longer recovery time
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(50)
            .permittedNumberOfCallsInHalfOpenState(3) // Careful testing
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .slowCallRateThreshold(70)
            .slowCallDurationThreshold(Duration.ofSeconds(30)) // DB operations can be slow
            .recordExceptions(
                java.sql.SQLException.class,
                org.springframework.dao.DataAccessException.class,
                org.springframework.transaction.TransactionException.class
           )
            .ignoreExceptions(
                org.springframework.dao.DuplicateKeyException.class // Business logic, not failure
           )
            .build();
    }

    private CircuitBreakerConfig createMessagingConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(30) // Low tolerance for messaging failures
            .waitDurationInOpenState(Duration.ofSeconds(20)) // Quick recovery
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
            .slidingWindowSize(30) // 30 seconds window
            .permittedNumberOfCallsInHalfOpenState(10)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .slowCallRateThreshold(60)
            .slowCallDurationThreshold(Duration.ofSeconds(5))
            .recordExceptions(
                org.springframework.messaging.MessagingException.class,
                java.io.IOException.class
           )
            .ignoreExceptions()
            .build();
    }

    private CircuitBreakerConfig createFileTransferConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(70) // File operations can have transient failures
            .waitDurationInOpenState(Duration.ofMinutes(5)) // Longer wait for file systems
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(20) // Fewer operations
            .permittedNumberOfCallsInHalfOpenState(2)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .slowCallRateThreshold(90)
            .slowCallDurationThreshold(Duration.ofMinutes(2)) // Large files take time
            .recordExceptions(
                java.io.IOException.class,
                java.net.UnknownHostException.class,
                org.apache.commons.net.ftp.FTPConnectionClosedException.class
           )
            .ignoreExceptions(
                java.io.FileNotFoundException.class // Might be expected
           )
            .build();
    }

    private CircuitBreakerConfig createSapConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(80) // Very tolerant-SAP is critical
            .waitDurationInOpenState(Duration.ofMinutes(10)) // Long recovery period
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10) // Small window for critical systems
            .permittedNumberOfCallsInHalfOpenState(1) // Very careful testing
            .automaticTransitionFromOpenToHalfOpenEnabled(false) // Manual intervention preferred
            .slowCallRateThreshold(95)
            .slowCallDurationThreshold(Duration.ofMinutes(5)) // SAP can be very slow
            .recordExceptions(
                RuntimeException.class,
                java.sql.SQLException.class
           )
            .ignoreExceptions()
            .build();
    }

    /**
     * Get circuit breaker for a specific adapter.
     */
    public CircuitBreaker getCircuitBreaker(CircuitBreakerRegistry registry,
                                           String adapterType,
                                           String adapterId) {
        String configName = mapAdapterTypeToConfig(adapterType);
        String breakerName = adapterType + "-" + adapterId;

        return registry.circuitBreaker(breakerName, configName);
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
}
