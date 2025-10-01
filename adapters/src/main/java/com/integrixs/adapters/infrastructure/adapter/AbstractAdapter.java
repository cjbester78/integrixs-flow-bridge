package com.integrixs.adapters.infrastructure.adapter;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.model.AdapterMetadata;
import com.integrixs.adapters.domain.port.AdapterPort;
import com.integrixs.shared.exceptions.AdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base class for all adapter implementations
 */
public abstract class AbstractAdapter implements AdapterPort {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected AdapterConfiguration configuration;
    protected final AtomicBoolean initialized = new AtomicBoolean(false);
    protected final AtomicBoolean running = new AtomicBoolean(false);
    protected final Map<String, Object> metadata = new HashMap<>();
    protected long lastActivityTimestamp;
    protected int messagesProcessed;
    protected int errorCount;

    // Getters
    public AdapterConfiguration getConfiguration() {
        return configuration;
    }

    public AtomicBoolean getInitialized() {
        return initialized;
    }

    public AtomicBoolean getRunning() {
        return running;
    }

    public Map<String, Object> getMetadataMap() {
        return metadata;
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterName(this.getClass().getSimpleName())
                .adapterType(getAdapterType())
                .adapterMode(getAdapterMode())
                .version("1.0.0")
                .description(getConfigurationSummary())
                .supportsAsync(false)
                .supportsBatch(false)
                .supportsStreaming(false)
                .requiresAuthentication(true)
                .capabilities(metadata)
                .build();
    }

    public long getLastActivityTimestamp() {
        return lastActivityTimestamp;
    }

    public int getMessagesProcessed() {
        return messagesProcessed;
    }

    public int getErrorCount() {
        return errorCount;
    }

    @Override
    public void initialize(AdapterConfiguration config) {
        log.info("Initializing adapter: {} type: {} mode: {}",
                config.getAdapterId(), config.getAdapterType(), config.getAdapterMode());

        try {
            this.configuration = config;

            // Perform adapter - specific initialization
            AdapterOperationResult result = performInitialization();

            if(result.isSuccess()) {
                initialized.set(true);
                metadata.put("initialized", true);
                metadata.put("initTimestamp", System.currentTimeMillis());
            }

            if(!result.isSuccess()) {
                throw new RuntimeException("Failed to initialize adapter: " + result.getMessage());
            }
        } catch(Exception e) {
            log.error("Error initializing adapter: {}", e.getMessage(), e);
            initialized.set(false);
            throw new RuntimeException("Initialization failed: " + e.getMessage(), e);
        }
    }

    public AdapterOperationResult start() {
        if(!initialized.get()) {
            return AdapterOperationResult.failure("Adapter not initialized");
        }

        if(running.get()) {
            return AdapterOperationResult.success("Adapter already running");
        }

        try {
            AdapterOperationResult result = performStart();

            if(result.isSuccess()) {
                running.set(true);
                metadata.put("running", true);
                metadata.put("startTimestamp", System.currentTimeMillis());
            }

            return result;
        } catch(Exception e) {
            log.error("Error starting adapter: {}", e.getMessage(), e);
            return AdapterOperationResult.failure("Start failed: " + e.getMessage());
        }
    }

    public AdapterOperationResult stop() {
        if(!running.get()) {
            return AdapterOperationResult.success("Adapter not running");
        }

        try {
            AdapterOperationResult result = performStop();

            running.set(false);
            metadata.put("running", false);
            metadata.put("stopTimestamp", System.currentTimeMillis());

            return result;
        } catch(Exception e) {
            log.error("Error stopping adapter: {}", e.getMessage(), e);
            return AdapterOperationResult.failure("Stop failed: " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        log.info("Shutting down adapter: {}", configuration != null ? configuration.getAdapterId() : "unknown");

        try {
            // First stop if running
            if(running.get()) {
                stop();
            }

            // Perform cleanup
            AdapterOperationResult result = performShutdown();

            initialized.set(false);
            metadata.clear();

            if(!result.isSuccess()) {
                log.error("Shutdown failed: {}", result.getMessage());
            }
        } catch(Exception e) {
            log.error("Error shutting down adapter: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isReady() {
        return initialized.get() && running.get();
    }

    @Override
    public AdapterOperationResult getHealthStatus() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("initialized", initialized.get());
        healthData.put("running", running.get());
        healthData.put("lastActivityTimestamp", lastActivityTimestamp);
        healthData.put("messagesProcessed", messagesProcessed);
        healthData.put("errorCount", errorCount);
        healthData.put("uptime", System.currentTimeMillis() - (Long) metadata.getOrDefault("startTimestamp", System.currentTimeMillis()));

        if(!initialized.get()) {
            return AdapterOperationResult.failure("Not initialized");
        }

        if(!running.get()) {
            return AdapterOperationResult.failure("Not running");
        }

        return AdapterOperationResult.success(healthData);
    }

    @Override
    public AdapterOperationResult validateConfiguration(AdapterConfiguration configuration) {
        if(configuration == null) {
            return AdapterOperationResult.failure("Configuration is null");
        }

        if(configuration.getAdapterType() == null) {
            return AdapterOperationResult.failure("Adapter type is required");
        }

        if(configuration.getAdapterMode() == null) {
            return AdapterOperationResult.failure("Adapter mode is required");
        }

        // Delegate to concrete implementation for specific validation
        return performConfigurationValidation(configuration);
    }

    @Override
    public AdapterOperationResult testConnection(AdapterConfiguration configuration) {
        // Temporarily initialize with the provided configuration
        AdapterConfiguration originalConfig = this.configuration;
        this.configuration = configuration;

        try {
            return performConnectionTest();
        } catch(Exception e) {
            return AdapterOperationResult.failure("Connection test failed: " + e.getMessage());
        } finally {
            // Restore original configuration
            this.configuration = originalConfig;
        }
    }

    public AdapterOperationResult getStatus() {
        Map<String, Object> statusMetadata = new HashMap<>(metadata);
        statusMetadata.put("initialized", initialized.get());
        statusMetadata.put("running", running.get());
        statusMetadata.put("lastActivityTimestamp", lastActivityTimestamp);
        statusMetadata.put("messagesProcessed", messagesProcessed);
        statusMetadata.put("errorCount", errorCount);

        if(!initialized.get()) {
            return AdapterOperationResult.failure("Not initialized")
                    .withMetadata(statusMetadata);
        }

        if(!running.get()) {
            return AdapterOperationResult.failure("Not running")
                    .withMetadata(statusMetadata);
        }

        return AdapterOperationResult.success("Running")
                .withMetadata(statusMetadata);
    }


    /**
     * Update activity timestamp
     */
    protected void updateActivity() {
        this.lastActivityTimestamp = System.currentTimeMillis();
    }

    /**
     * Increment message counter
     */
    protected void incrementMessageCount() {
        this.messagesProcessed++;
        updateActivity();
    }

    /**
     * Increment error counter
     */
    protected void incrementErrorCount() {
        this.errorCount++;
    }

    // Abstract methods to be implemented by concrete adapters

    /**
     * Perform adapter - specific initialization
     * @return Operation result
     */
    protected abstract AdapterOperationResult performInitialization();

    /**
     * Perform adapter - specific start operations
     * @return Operation result
     */
    protected abstract AdapterOperationResult performStart();

    /**
     * Perform adapter - specific stop operations
     * @return Operation result
     */
    protected abstract AdapterOperationResult performStop();

    /**
     * Perform adapter - specific shutdown operations
     * @return Operation result
     */
    protected abstract AdapterOperationResult performShutdown();

    /**
     * Perform adapter - specific connection test
     * @return Operation result
     */
    protected abstract AdapterOperationResult performConnectionTest();

    /**
     * Perform adapter - specific configuration validation
     * @param configuration The configuration to validate
     * @return Operation result
     */
    protected AdapterOperationResult performConfigurationValidation(AdapterConfiguration configuration) {
        // Default implementation - can be overridden by subclasses
        return AdapterOperationResult.success("Configuration is valid");
    }

    /**
     * Get adapter type from configuration or concrete implementation
     * @return The adapter type
     */
    protected abstract AdapterConfiguration.AdapterTypeEnum getAdapterType();

    /**
     * Get adapter mode from configuration or concrete implementation
     * @return The adapter mode
     */
    protected abstract AdapterConfiguration.AdapterModeEnum getAdapterMode();

    /**
     * Get configuration summary for logging/debugging
     * @return Configuration summary string
     */
    public abstract String getConfigurationSummary();
}
