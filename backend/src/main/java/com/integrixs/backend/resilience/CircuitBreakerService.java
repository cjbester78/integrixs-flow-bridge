package com.integrixs.backend.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing circuit breakers across adapters.
 * Provides centralized circuit breaker operations with monitoring.
 */
@Service("backendCircuitBreakerService")
public class CircuitBreakerService {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerService.class);


    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final CircuitBreakerConfiguration configuration;
    private final MeterRegistry meterRegistry;

    // Cache of active circuit breakers
    private final Map<String, CircuitBreaker> circuitBreakerCache = new ConcurrentHashMap<>();

    public CircuitBreakerService(CircuitBreakerRegistry circuitBreakerRegistry,
                                CircuitBreakerConfiguration configuration,
                                MeterRegistry meterRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.configuration = configuration;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Register event listeners for all circuit breakers
        circuitBreakerRegistry.getEventPublisher()
            .onEntryAdded(event -> {
                CircuitBreaker cb = event.getAddedEntry();
                cb.getEventPublisher()
                    .onStateTransition(this::handleStateTransition)
                    .onSuccess(evt -> recordMetric("success", evt.getCircuitBreakerName()))
                    .onError(evt -> recordMetric("error", evt.getCircuitBreakerName()))
                    .onIgnoredError(evt -> recordMetric("ignored", evt.getCircuitBreakerName()))
                    .onSlowCallRateExceeded(evt ->
                        log.warn("Circuit breaker {} slow call rate exceeded", evt.getCircuitBreakerName()));
            });
    }

    /**
     * Execute operation with circuit breaker protection.
     */
    public <T> T executeWithCircuitBreaker(String adapterType,
                                          String adapterId,
                                          Supplier<T> operation) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(adapterType, adapterId);

        return circuitBreaker.executeSupplier(operation);
    }

    /**
     * Execute operation with circuit breaker and fallback.
     */
    public <T> T executeWithFallback(String adapterType,
                                    String adapterId,
                                    Supplier<T> operation,
                                    Supplier<T> fallback) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(adapterType, adapterId);

        Supplier<T> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, operation);

        try {
            return decoratedSupplier.get();
        } catch(Exception e) {
            log.warn("Circuit breaker {} triggered fallback: {}",
                circuitBreaker.getName(), e.getMessage());
            return fallback.get();
        }
    }

    /**
     * Check if circuit breaker is open.
     */
    public boolean isCircuitBreakerOpen(String adapterType, String adapterId) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(adapterType, adapterId);
        return circuitBreaker.getState() == CircuitBreaker.State.OPEN;
    }

    /**
     * Get circuit breaker health status.
     */
    public CircuitBreakerHealthStatus getHealthStatus(String adapterType, String adapterId) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(adapterType, adapterId);
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        return CircuitBreakerHealthStatus.builder()
            .adapterType(adapterType)
            .adapterId(adapterId)
            .state(circuitBreaker.getState().toString())
            .failureRate(metrics.getFailureRate())
            .slowCallRate(metrics.getSlowCallRate())
            .numberOfBufferedCalls(metrics.getNumberOfBufferedCalls())
            .numberOfFailedCalls(metrics.getNumberOfFailedCalls())
            .numberOfSlowCalls(metrics.getNumberOfSlowCalls())
            .numberOfSuccessfulCalls(metrics.getNumberOfSuccessfulCalls())
            .build();
    }

    /**
     * Reset circuit breaker.
     */
    public void resetCircuitBreaker(String adapterType, String adapterId) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(adapterType, adapterId);
        circuitBreaker.reset();
        log.info("Circuit breaker reset for {} - {}", adapterType, adapterId);
    }

    /**
     * Force circuit breaker to open state.
     */
    public void forceOpen(String adapterType, String adapterId) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(adapterType, adapterId);
        circuitBreaker.transitionToOpenState();
        log.warn("Circuit breaker forced open for {} - {}", adapterType, adapterId);
    }

    /**
     * Get all circuit breaker statuses.
     */
    public Map<String, CircuitBreakerHealthStatus> getAllStatuses() {
        Map<String, CircuitBreakerHealthStatus> statuses = new HashMap<>();

        for(CircuitBreaker cb : circuitBreakerRegistry.getAllCircuitBreakers()) {
            String[] parts = cb.getName().split("-", 2);
            if(parts.length == 2) {
                statuses.put(cb.getName(), getHealthStatus(parts[0], parts[1]));
            }
        }

        return statuses;
    }

    private CircuitBreaker getOrCreateCircuitBreaker(String adapterType, String adapterId) {
        String key = adapterType + "-" + adapterId;

        return circuitBreakerCache.computeIfAbsent(key, k -> {
            CircuitBreaker cb = configuration.getCircuitBreaker(
                circuitBreakerRegistry, adapterType, adapterId);

            // Register metrics
            Tags tags = Tags.of("adapter.type", adapterType, "adapter.id", adapterId);
            cb.getEventPublisher()
                .onStateTransition(event ->
                    meterRegistry.counter("circuit.breaker.state.transition", tags).increment())
                .onSuccess(event ->
                    meterRegistry.counter("circuit.breaker.calls",
                        tags.and("result", "success")).increment())
                .onError(event ->
                    meterRegistry.counter("circuit.breaker.calls",
                        tags.and("result", "error")).increment());

            return cb;
        });
    }

    private void handleStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        log.warn("Circuit breaker {} transitioned from {} to {}",
            event.getCircuitBreakerName(),
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState());

        // Could trigger alerts here
        if(event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
            // Send alert about circuit breaker opening
            String[] parts = event.getCircuitBreakerName().split("-", 2);
            if(parts.length == 2) {
                log.error("ALERT: Circuit breaker opened for adapter {} - {}", parts[0], parts[1]);
            }
        }
    }

    private void recordMetric(String type, String circuitBreakerName) {
        meterRegistry.counter("circuit.breaker.events",
            "type", type,
            "name", circuitBreakerName).increment();
    }

    public static class CircuitBreakerHealthStatus {
        private String adapterType;
        private String adapterId;
        private String state;
        private float failureRate;
        private float slowCallRate;
        private int numberOfBufferedCalls;
        private int numberOfFailedCalls;
        private int numberOfSlowCalls;
        private int numberOfSuccessfulCalls;

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

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public float getFailureRate() {
            return failureRate;
        }

        public void setFailureRate(float failureRate) {
            this.failureRate = failureRate;
        }

        public float getSlowCallRate() {
            return slowCallRate;
        }

        public void setSlowCallRate(float slowCallRate) {
            this.slowCallRate = slowCallRate;
        }

        public int getNumberOfBufferedCalls() {
            return numberOfBufferedCalls;
        }

        public void setNumberOfBufferedCalls(int numberOfBufferedCalls) {
            this.numberOfBufferedCalls = numberOfBufferedCalls;
        }

        public int getNumberOfFailedCalls() {
            return numberOfFailedCalls;
        }

        public void setNumberOfFailedCalls(int numberOfFailedCalls) {
            this.numberOfFailedCalls = numberOfFailedCalls;
        }

        public int getNumberOfSlowCalls() {
            return numberOfSlowCalls;
        }

        public void setNumberOfSlowCalls(int numberOfSlowCalls) {
            this.numberOfSlowCalls = numberOfSlowCalls;
        }

        public int getNumberOfSuccessfulCalls() {
            return numberOfSuccessfulCalls;
        }

        public void setNumberOfSuccessfulCalls(int numberOfSuccessfulCalls) {
            this.numberOfSuccessfulCalls = numberOfSuccessfulCalls;
        }

        // Builder pattern
        public static CircuitBreakerHealthStatusBuilder builder() {
            return new CircuitBreakerHealthStatusBuilder();
        }

        public static class CircuitBreakerHealthStatusBuilder {
            private String adapterType;
            private String adapterId;
            private String state;
            private float failureRate;
            private float slowCallRate;
            private int numberOfBufferedCalls;
            private int numberOfFailedCalls;
            private int numberOfSlowCalls;
            private int numberOfSuccessfulCalls;

            public CircuitBreakerHealthStatusBuilder adapterType(String adapterType) {
                this.adapterType = adapterType;
                return this;
            }

            public CircuitBreakerHealthStatusBuilder adapterId(String adapterId) {
                this.adapterId = adapterId;
                return this;
            }

            public CircuitBreakerHealthStatusBuilder state(String state) {
                this.state = state;
                return this;
            }

            public CircuitBreakerHealthStatusBuilder failureRate(float failureRate) {
                this.failureRate = failureRate;
                return this;
            }

            public CircuitBreakerHealthStatusBuilder slowCallRate(float slowCallRate) {
                this.slowCallRate = slowCallRate;
                return this;
            }

            public CircuitBreakerHealthStatusBuilder numberOfBufferedCalls(int numberOfBufferedCalls) {
                this.numberOfBufferedCalls = numberOfBufferedCalls;
                return this;
            }

            public CircuitBreakerHealthStatusBuilder numberOfFailedCalls(int numberOfFailedCalls) {
                this.numberOfFailedCalls = numberOfFailedCalls;
                return this;
            }

            public CircuitBreakerHealthStatusBuilder numberOfSlowCalls(int numberOfSlowCalls) {
                this.numberOfSlowCalls = numberOfSlowCalls;
                return this;
            }

            public CircuitBreakerHealthStatusBuilder numberOfSuccessfulCalls(int numberOfSuccessfulCalls) {
                this.numberOfSuccessfulCalls = numberOfSuccessfulCalls;
                return this;
            }

            public CircuitBreakerHealthStatus build() {
                CircuitBreakerHealthStatus status = new CircuitBreakerHealthStatus();
                status.adapterType = this.adapterType;
                status.adapterId = this.adapterId;
                status.state = this.state;
                status.failureRate = this.failureRate;
                status.slowCallRate = this.slowCallRate;
                status.numberOfBufferedCalls = this.numberOfBufferedCalls;
                status.numberOfFailedCalls = this.numberOfFailedCalls;
                status.numberOfSlowCalls = this.numberOfSlowCalls;
                status.numberOfSuccessfulCalls = this.numberOfSuccessfulCalls;
                return status;
            }
        }
    }
}
