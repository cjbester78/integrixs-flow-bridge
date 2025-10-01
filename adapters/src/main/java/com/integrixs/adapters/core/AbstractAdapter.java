package com.integrixs.adapters.core;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.monitoring.PerformanceMetricsCollector;
import com.integrixs.adapters.monitoring.CustomMetricsRegistry;
import com.integrixs.adapters.resilience.CircuitBreakerService;
import com.integrixs.adapters.resilience.BulkheadService;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import com.integrixs.shared.exceptions.AdapterException;

/**
 * Abstract base implementation providing common adapter functionality.
 */
public abstract class AbstractAdapter implements BaseAdapter {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AdapterConfiguration.AdapterTypeEnum adapterType;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private Instant lastActivityTime;
    private String adapterId;

    @Autowired(required = false)
    private PerformanceMetricsCollector metricsCollector;

    @Autowired(required = false)
    private CustomMetricsRegistry customMetricsRegistry;

    @Autowired(required = false)
    private CircuitBreakerService circuitBreakerService;

    @Autowired(required = false)
    private BulkheadService bulkheadService;

    @Autowired(required = false)
    @Qualifier("coreAdapterMonitoringService")
    private AdapterMonitoringService adapterMonitoringService;

    @Autowired(required = false)
    private AdapterExceptionHandler adapterExceptionHandler;

    protected AbstractAdapter(AdapterConfiguration.AdapterTypeEnum adapterType) {
        this.adapterType = adapterType;
        this.lastActivityTime = Instant.now();
        this.adapterId = generateAdapterId();
    }

    /**
     * Register with monitoring service after dependency injection.
     */
    @PostConstruct
    private void registerWithMonitoring() {
        if (adapterMonitoringService != null) {
            adapterMonitoringService.registerAdapter(this);
        }
    }

    /**
     * Unregister from monitoring service before destruction.
     */
    @PreDestroy
    private void unregisterFromMonitoring() {
        if (adapterMonitoringService != null) {
            adapterMonitoringService.unregisterAdapter(this);
        }
    }

    /**
     * Generate a unique adapter identifier.
     */
    private String generateAdapterId() {
        return adapterType + "-" + System.currentTimeMillis() + "-" +
               Integer.toHexString(this.hashCode());
    }

    /**
     * Get the unique adapter identifier.
     */
    protected String getAdapterId() {
        return adapterId;
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return adapterType;
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public void initialize() throws AdapterException {
        if(initialized.get()) {
            logger.warn("Adapter {} already initialized", getAdapterType());
            return;
        }

        try {
            logger.info("Initializing {} adapter in {} mode", getAdapterType(), getAdapterMode());
            doInitialize();
            initialized.set(true);
            active.set(true);
            updateLastActivity();
            logger.info("Successfully initialized {} adapter", getAdapterType());
        } catch(Exception e) {
            logger.error("Failed to initialize {} adapter", getAdapterType(), e);
            throw new AdapterException("Initialization failed: " + e.getMessage(), e, getAdapterType().name(), "INIT_ERROR");
        }
    }

    @Override
    public void destroy() throws AdapterException {
        if(!initialized.get()) {
            logger.debug("Adapter {} not initialized, nothing to destroy", getAdapterType());
            return;
        }

        try {
            logger.info("Destroying {} adapter", getAdapterType());
            active.set(false);
            doDestroy();
            initialized.set(false);

            // Unregister from monitoring service
            if (adapterMonitoringService != null) {
                adapterMonitoringService.unregisterAdapter(this);
            }

            logger.info("Successfully destroyed {} adapter", getAdapterType());
        } catch(Exception e) {
            logger.error("Failed to destroy {} adapter", getAdapterType(), e);
            throw new AdapterException("Destruction failed: " + e.getMessage(), e, getAdapterType().name(), "DESTROY_ERROR");
        }
    }

    @Override
    public AdapterResult testConnection() {
        if(!initialized.get()) {
            return AdapterResult.failure("Adapter not initialized");
        }

        long startTime = System.currentTimeMillis();
        Timer.Sample sample = null;

        if(metricsCollector != null) {
            sample = metricsCollector.startOperation(
                getAdapterType().name(), getAdapterMode().name(), "testConnection");
        }

        try {
            logger.debug("Testing connection for {} adapter", getAdapterType());
            AdapterResult result = doTestConnection();
            long duration = System.currentTimeMillis() - startTime;
            result.setDurationMs(duration);
            updateLastActivity();

            if(result.isSuccess()) {
                logger.debug("Connection test successful for {} adapter in {}ms", getAdapterType(), duration);
                // Record success metrics
                if (adapterMonitoringService != null) {
                    adapterMonitoringService.recordSuccess(
                        getAdapterType(), getAdapterMode(), getAdapterId(), "testConnection", duration);
                }

                if(metricsCollector != null && sample != null) {
                    metricsCollector.recordOperation(
                        getAdapterType().name(), getAdapterMode().name(), "testConnection",
                        sample, true, null);
                }
            } else {
                logger.warn("Connection test failed for {} adapter: {}", getAdapterType(), result.getMessage());
                // Record failure metrics
                Exception error = result.getError() != null ?
                    (result.getError() instanceof Exception ? (Exception)result.getError() : new Exception(result.getError())) :
                    new Exception(result.getMessage());
                if (adapterMonitoringService != null) {
                    adapterMonitoringService.recordFailure(
                        getAdapterType(), getAdapterMode(), getAdapterId(), "testConnection", error, duration);
                }

                if(metricsCollector != null && sample != null) {
                    metricsCollector.recordOperation(
                        getAdapterType().name(), getAdapterMode().name(), "testConnection",
                        sample, false, error.getClass().getSimpleName());
                }
            }

            return result;
        } catch(Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Connection test error for {} adapter", getAdapterType(), e);
            AdapterResult result = AdapterResult.failure("Connection test failed: " + e.getMessage(), e);
            result.setDurationMs(duration);

            // Record failure metrics
            if (adapterMonitoringService != null) {
                adapterMonitoringService.recordFailure(
                    getAdapterType(), getAdapterMode(), getAdapterId(), "testConnection", e, duration);
            }

            if(metricsCollector != null && sample != null) {
                metricsCollector.recordOperation(
                    getAdapterType().name(), getAdapterMode().name(), "testConnection",
                    sample, false, e.getClass().getSimpleName());
            }

            return result;
        }
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("Adapter {type = %s, mode = %s, active = %s, initialized = %s, lastActivity = %s}",
                getAdapterType(), getAdapterMode(), isActive(), initialized.get(), lastActivityTime);
    }

    /**
     * Update the last activity timestamp.
     */
    protected void updateLastActivity() {
        this.lastActivityTime = Instant.now();
    }

    /**
     * Get the last activity timestamp.
     */
    protected Instant getLastActivityTime() {
        return lastActivityTime;
    }

    /**
     * Check if adapter is initialized.
     */
    protected boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Validate that the adapter is ready for operation.
     *
     * @throws AdapterException if adapter is not ready
     */
    protected void validateReady() throws AdapterException {
        if(!initialized.get()) {
            throw new AdapterException("Adapter not initialized", getAdapterType().name(), "NOT_INITIALIZED");
        }
        if(!active.get()) {
            throw new AdapterException("Adapter not active", getAdapterType().name(), "NOT_ACTIVE");
        }
    }

    /**
     * Create a timed operation wrapper that tracks duration and updates activity.
     */
    protected AdapterResult executeTimedOperation(String operationName, TimedOperation operation) {
        String adapterId = getAdapterId();
        Map<String, Object> context = RetryExecutor.createRetryContext(operationName, null);
        long startTime = System.currentTimeMillis();
        Timer.Sample sample = null;

        if(metricsCollector != null) {
            sample = metricsCollector.startOperation(
                getAdapterType().name(), getAdapterMode().name(), operationName);
        }

        try {
            logger.debug("Executing {} for {} adapter", operationName, getAdapterType());

            // Core operation wrapped with retry
            Supplier<AdapterResult> baseOperation = () -> {
                try {
                    if (adapterExceptionHandler != null) {
                        return RetryExecutor.executeWithRetry(
                                getAdapterType(),
                                getAdapterMode(),
                                adapterId,
                                () -> {
                                    try {
                                        return operation.execute();
                                    } catch(Exception e) {
                                        if(e instanceof RuntimeException) {
                                            throw(RuntimeException) e;
                                        }
                                        throw new RuntimeException(e);
                                    }
                                },
                                context,
                                adapterExceptionHandler
                       );
                    } else {
                        // Fallback without retry if exception handler not available
                        return operation.execute();
                    }
                } catch(Exception e) {
                    if(e instanceof RuntimeException) {
                        throw(RuntimeException) e;
                    }
                    throw new RuntimeException("Retry execution failed", e);
                }
            };

            // Wrap with bulkhead if available
            Supplier<AdapterResult> bulkheadWrappedOperation = baseOperation;
            if(bulkheadService != null) {
                bulkheadWrappedOperation = () -> bulkheadService.executeWithFallback(
                    getAdapterType().name(),
                    adapterId,
                    baseOperation,
                    () -> AdapterResult.failure("Adapter resources exhausted - too many concurrent calls")
               );
            }

            // Wrap with circuit breaker if available
            AdapterResult result;
            if(circuitBreakerService != null) {
                result = circuitBreakerService.executeWithFallback(
                    getAdapterType().name(),
                    adapterId,
                    bulkheadWrappedOperation,
                    () -> AdapterResult.failure("Circuit breaker open - adapter temporarily unavailable")
               );
            } else {
                result = bulkheadWrappedOperation.get();
            }

            long duration = System.currentTimeMillis() - startTime;
            result.setDurationMs(duration);
            result.addMetadata("operation", operationName);
            updateLastActivity();

            if(result.isSuccess()) {
                logger.debug(" {} completed successfully for {} adapter in {}ms",
                        operationName, getAdapterType(), duration);
                // Record success metrics
                if (adapterMonitoringService != null) {
                    adapterMonitoringService.recordSuccess(
                        getAdapterType(), getAdapterMode(), adapterId, operationName, duration);
                }

                if(metricsCollector != null && sample != null) {
                    metricsCollector.recordOperation(
                        getAdapterType().name(), getAdapterMode().name(), operationName,
                        sample, true, null);
                }
            } else {
                logger.warn(" {} failed for {} adapter: {}",
                        operationName, getAdapterType(), result.getMessage());
                // Record failure metrics
                Exception error = result.getError() != null ?
                    (result.getError() instanceof Exception ? (Exception)result.getError() : new Exception(result.getError())) :
                    new Exception(result.getMessage());
                if (adapterMonitoringService != null) {
                    adapterMonitoringService.recordFailure(
                        getAdapterType(), getAdapterMode(), adapterId, operationName, error, duration);
                }

                if(metricsCollector != null && sample != null) {
                    metricsCollector.recordOperation(
                        getAdapterType().name(), getAdapterMode().name(), operationName,
                        sample, false, error.getClass().getSimpleName());
                }
            }

            return result;

        } catch(Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error(" {} error for {} adapter", operationName, getAdapterType(), e);
            AdapterResult result = AdapterResult.failure(operationName + " failed: " + e.getMessage(), e);
            result.setDurationMs(duration);
            result.addMetadata("operation", operationName);

            // Record failure metrics
            if (adapterMonitoringService != null) {
                adapterMonitoringService.recordFailure(
                    getAdapterType(), getAdapterMode(), adapterId, operationName, e, duration);
            }

            if(metricsCollector != null && sample != null) {
                metricsCollector.recordOperation(
                    getAdapterType().name(), getAdapterMode().name(), operationName,
                    sample, false, e.getClass().getSimpleName());
            }

            return result;
        }
    }

    /**
     * Functional interface for timed operations.
     */
    @FunctionalInterface
    protected interface TimedOperation {
        AdapterResult execute() throws Exception;
    }

    /**
     * Record data volume metrics.
     */
    protected void recordDataVolume(String direction, long bytes) {
        if(metricsCollector != null) {
            metricsCollector.recordDataVolume(
                getAdapterType().name(), getAdapterMode().name(), direction, bytes);
        }
    }

    /**
     * Record message count metrics.
     */
    protected void recordMessageCount(String status, long count) {
        if(metricsCollector != null) {
            metricsCollector.recordMessageCount(
                getAdapterType().name(), getAdapterMode().name(), status, count);
        }
    }

    /**
     * Record custom metric.
     */
    protected void recordCustomMetric(String metricName, double value, String... tags) {
        if(metricsCollector != null) {
            metricsCollector.recordCustomMetric(
                getAdapterType().name(), getAdapterMode().name(), metricName, value, tags);
        }
    }

    /**
     * Set performance metrics collector(for non - Spring environments).
     */
    public void setMetricsCollector(PerformanceMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    /**
     * Set custom metrics registry(for non - Spring environments).
     */
    public void setCustomMetricsRegistry(CustomMetricsRegistry customMetricsRegistry) {
        this.customMetricsRegistry = customMetricsRegistry;
    }

    /**
     * Register a custom gauge metric.
     */
    protected void registerCustomGauge(String metricName, Supplier<Number> supplier, String... tags) {
        if(customMetricsRegistry != null) {
            customMetricsRegistry.registerGauge(
                getAdapterType().name(), getAdapterMode().name(), metricName, supplier, tags);
        }
    }

    /**
     * Update a custom gauge value.
     */
    protected void updateCustomGauge(String metricName, double value, String... tags) {
        if(customMetricsRegistry != null) {
            customMetricsRegistry.updateGauge(
                getAdapterType().name(), getAdapterMode().name(), metricName, value, tags);
        }
    }

    /**
     * Increment a custom counter.
     */
    protected void incrementCustomCounter(String metricName, double amount, String... tags) {
        if(customMetricsRegistry != null) {
            customMetricsRegistry.incrementCounter(
                getAdapterType().name(), getAdapterMode().name(), metricName, amount, tags);
        }
    }

    /**
     * Record a custom distribution value.
     */
    protected void recordCustomDistribution(String metricName, double value, String... tags) {
        if(customMetricsRegistry != null) {
            customMetricsRegistry.recordDistribution(
                getAdapterType().name(), getAdapterMode().name(), metricName, value, tags);
        }
    }

    // Abstract methods that subclasses must implement

    /**
     * Perform adapter - specific initialization.
     */
    protected abstract void doInitialize() throws Exception;

    /**
     * Perform adapter - specific cleanup.
     */
    protected abstract void doDestroy() throws Exception;

    /**
     * Perform adapter - specific connection testing.
     */
    protected abstract AdapterResult doTestConnection() throws Exception;
}
