package com.integrixs.adapters.resilience;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Service for managing circuit breakers for adapters
 */
@Service
public class CircuitBreakerService {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerService.class);


    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public CircuitBreakerService() {
        // Default configuration
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slidingWindowSize(10)
            .build();

        this.circuitBreakerRegistry = CircuitBreakerRegistry.of(config);
    }

    /**
     * Get or create a circuit breaker for the given name
     */
    public CircuitBreaker getCircuitBreaker(String name) {
        return circuitBreakers.computeIfAbsent(name, key ->
            circuitBreakerRegistry.circuitBreaker(name)
       );
    }

    /**
     * Execute a callable with circuit breaker protection
     */
    public <T> T executeWithCircuitBreaker(String circuitBreakerName, Callable<T> callable) throws Exception {
        CircuitBreaker circuitBreaker = getCircuitBreaker(circuitBreakerName);
        return circuitBreaker.executeCallable(callable);
    }

    /**
     * Execute a supplier with circuit breaker protection
     */
    public <T> T executeWithCircuitBreaker(String circuitBreakerName, Supplier<T> supplier) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(circuitBreakerName);
        return circuitBreaker.executeSupplier(supplier);
    }

    /**
     * Check if circuit breaker is open
     */
    public boolean isOpen(String circuitBreakerName) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(circuitBreakerName);
        return circuitBreaker.getState() == CircuitBreaker.State.OPEN;
    }

    /**
     * Reset circuit breaker
     */
    public void reset(String circuitBreakerName) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(circuitBreakerName);
        circuitBreaker.reset();
    }

    /**
     * Execute with fallback
     */
    public <T> T executeWithFallback(String circuitBreakerName, String instanceId,
                                     Supplier<T> supplier, Supplier<T> fallbackSupplier) {
        try {
            return executeWithCircuitBreaker(circuitBreakerName, supplier);
        } catch (io.github.resilience4j.circuitbreaker.CallNotPermittedException e) {
            log.warn("Circuit breaker {} is open for instance {}, executing fallback", circuitBreakerName, instanceId);
            return fallbackSupplier.get();
        } catch (Exception e) {
            log.error("Error executing circuit breaker operation for {} instance {}", circuitBreakerName, instanceId, e);
            throw e;
        }
    }
}
