package com.integrixs.adapters.infrastructure.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.AdapterPort;
import com.integrixs.adapters.domain.repository.AdapterRepository;
import com.integrixs.adapters.domain.service.AdapterManagementService;
import com.integrixs.adapters.domain.service.AdapterRegistryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

/**
 * Infrastructure implementation of adapter management service
 */
@Service
public class AdapterManagementServiceImpl implements AdapterManagementService {
    private static final Logger log = LoggerFactory.getLogger(AdapterManagementServiceImpl.class);


    private final AdapterRepository adapterRepository;
    private final AdapterRegistryService adapterRegistryService;
    private final Map<String, AdapterOperationResult> adapterStatuses = new ConcurrentHashMap<>();

    public AdapterManagementServiceImpl(AdapterRepository adapterRepository,
                                       AdapterRegistryService adapterRegistryService) {
        this.adapterRepository = adapterRepository;
        this.adapterRegistryService = adapterRegistryService;
    }

    @Override
    public String createAdapter(AdapterConfiguration configuration) {
        log.info("Creating adapter: {} type: {} mode: {}",
                configuration.getName(), configuration.getAdapterType(), configuration.getAdapterMode());

        // Generate adapter ID if not provided
        if(configuration.getAdapterId() == null) {
            configuration.setAdapterId(UUID.randomUUID().toString());
        }

        // Validate configuration
        AdapterOperationResult validationResult = validateConfiguration(configuration);
        if(!validationResult.isSuccess()) {
            throw new IllegalArgumentException("Invalid configuration: " + validationResult.getMessage());
        }

        // Save configuration
        adapterRepository.save(configuration);

        // Create and register adapter instance
        try {
            AdapterPort adapter = adapterRegistryService.createAdapter(configuration);
            adapterRegistryService.registerAdapter(configuration.getAdapterId(), adapter);

            // Initialize adapter
            adapter.initialize(configuration);
            AdapterOperationResult initResult = AdapterOperationResult.success("Adapter initialized successfully");
            adapterStatuses.put(configuration.getAdapterId(), initResult);

            return configuration.getAdapterId();

        } catch(Exception e) {
            log.error("Error creating adapter: {}", e.getMessage(), e);
            adapterRepository.delete(configuration.getAdapterId());
            throw new RuntimeException("Failed to create adapter: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateAdapterConfiguration(String adapterId, AdapterConfiguration configuration) {
        log.info("Updating adapter configuration: {}", adapterId);

        // Get existing configuration
        AdapterConfiguration existing = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));

        // Ensure adapter ID and type/mode don't change
        configuration.setAdapterId(adapterId);
        configuration.setAdapterType(existing.getAdapterType());
        configuration.setAdapterMode(existing.getAdapterMode());

        // Validate new configuration
        AdapterOperationResult validationResult = validateConfiguration(configuration);
        if(!validationResult.isSuccess()) {
            throw new IllegalArgumentException("Invalid configuration: " + validationResult.getMessage());
        }

        // Stop existing adapter if running
        try {
            stopAdapter(adapterId);
        } catch(Exception e) {
            log.warn("Error stopping adapter before update: {}", e.getMessage());
        }

        // Update configuration
        adapterRepository.save(configuration);

        // Reinitialize adapter with new configuration
        adapterRegistryService.getAdapter(adapterId).ifPresent(adapter -> {
            adapter.initialize(configuration);
            AdapterOperationResult initResult = adapter.getHealthStatus();
            adapterStatuses.put(adapterId, initResult);
        });
    }

    @Override
    public void deleteAdapter(String adapterId) {
        log.info("Deleting adapter: {}", adapterId);

        // Stop and unregister adapter
        try {
            stopAdapter(adapterId);
            adapterRegistryService.unregisterAdapter(adapterId);
        } catch(Exception e) {
            log.warn("Error stopping adapter before deletion: {}", e.getMessage());
        }

        // Delete configuration
        adapterRepository.delete(adapterId);
        adapterStatuses.remove(adapterId);
    }

    @Override
    public AdapterOperationResult testAdapterConnection(String adapterId) {
        log.info("Testing adapter connection: {}", adapterId);

        return adapterRegistryService.getAdapter(adapterId)
                .map(adapter -> {
                    // Get adapter configuration from repository
                    Optional<AdapterConfiguration> configOpt = adapterRepository.findById(adapterId);
                    if(!configOpt.isPresent()) {
                        return AdapterOperationResult.failure("Adapter configuration not found");
                    }
                    AdapterConfiguration config = configOpt.get();
                    AdapterOperationResult result = adapter.testConnection(config);
                    adapterStatuses.put(adapterId, result);
                    return result;
                })
                .orElse(AdapterOperationResult.failure("Adapter not found"));
    }

    @Override
    public AdapterOperationResult validateConfiguration(AdapterConfiguration configuration) {
        // Basic validation
        if(configuration.getAdapterType() == null) {
            return AdapterOperationResult.failure("Adapter type is required");
        }

        if(configuration.getAdapterMode() == null) {
            return AdapterOperationResult.failure("Adapter mode is required");
        }

        if(configuration.getName() == null || configuration.getName().trim().isEmpty()) {
            return AdapterOperationResult.failure("Adapter name is required");
        }

        // Get metadata for validation
        AdapterMetadata metadata = getAdapterMetadata(
                configuration.getAdapterType(),
                configuration.getAdapterMode()
       );

        // Validate required properties
        for(String requiredProp : metadata.getRequiredProperties()) {
            Object value = configuration.getConnectionProperties().get(requiredProp);
            if(value == null || value.toString().trim().isEmpty()) {
                return AdapterOperationResult.failure("Required property missing: " + requiredProp);
            }
        }

        // Type - specific validation
        return validateTypeSpecificConfiguration(configuration);
    }

    @Override
    public void startAdapter(String adapterId) {
        log.info("Starting adapter: {}", adapterId);

        adapterRegistryService.getAdapter(adapterId).ifPresentOrElse(
                adapter -> {
                    Optional<AdapterConfiguration> configOpt = adapterRepository.findById(adapterId);
                    if(!configOpt.isPresent()) {
                        throw new RuntimeException("Adapter configuration not found");
                    }
                    adapter.initialize(configOpt.get());
                    AdapterOperationResult result = adapter.getHealthStatus();
                    adapterStatuses.put(adapterId, result);
                    if(!result.isSuccess()) {
                        throw new RuntimeException("Failed to start adapter: " + result.getMessage());
                    }
                },
                () -> {
                    throw new IllegalArgumentException("Adapter not found: " + adapterId);
                }
       );
    }

    @Override
    public void stopAdapter(String adapterId) {
        log.info("Stopping adapter: {}", adapterId);

        adapterRegistryService.getAdapter(adapterId).ifPresent(adapter -> {
            // The AdapterPort interface only has shutdown(), not stop()
            adapter.shutdown();
            adapterStatuses.put(adapterId,
                    AdapterOperationResult.success("Adapter stopped"));
        });
    }

    @Override
    public AdapterOperationResult getAdapterStatus(String adapterId) {
        // Check cached status
        AdapterOperationResult cachedStatus = adapterStatuses.get(adapterId);
        if(cachedStatus != null) {
            return cachedStatus;
        }

        // Get live status from adapter
        return adapterRegistryService.getAdapter(adapterId)
                .map(adapter -> {
                    AdapterOperationResult status = adapter.getHealthStatus();
                    adapterStatuses.put(adapterId, status);
                    return status;
                })
                .orElse(AdapterOperationResult.failure("Adapter not found"));
    }

    @Override
    public AdapterConfiguration getAdapterConfiguration(String adapterId) {
        return adapterRepository.findById(adapterId)
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));
    }

    @Override
    public List<AdapterConfiguration> listAdapters() {
        return adapterRepository.findAll();
    }

    @Override
    public AdapterMetadata getAdapterMetadata(
            AdapterConfiguration.AdapterTypeEnum adapterType,
            AdapterConfiguration.AdapterModeEnum adapterMode) {
        return adapterRegistryService.getAdapterMetadata(adapterType, adapterMode);
    }

    @Override
    public AdapterOperationResult checkAdapterHealth(String adapterId) {
        log.debug("Checking adapter health: {}", adapterId);

        return adapterRegistryService.getAdapter(adapterId)
                .map(adapter -> {
                    try {
                        // Check adapter health
                        return adapter.getHealthStatus();

                    } catch(Exception e) {
                        log.error("Error checking adapter health: {}", e.getMessage(), e);
                        return AdapterOperationResult.failure("Health check failed: " + e.getMessage());
                    }
                })
                .orElse(AdapterOperationResult.failure("Adapter not found"));
    }

    @Override
    public void resetAdapter(String adapterId) {
        log.info("Resetting adapter: {}", adapterId);

        adapterRegistryService.getAdapter(adapterId).ifPresent(adapter -> {
            // Shutdown adapter
            adapter.shutdown();

            // Clear any cached state
            adapterStatuses.remove(adapterId);

            // Re - initialize
            AdapterConfiguration config = getAdapterConfiguration(adapterId);
            adapter.initialize(config);
            AdapterOperationResult healthStatus = adapter.getHealthStatus();
            adapterStatuses.put(adapterId, healthStatus);
        });
    }


    /**
     * Validate type - specific configuration
     * @param configuration Configuration to validate
     * @return Validation result
     */
    private AdapterOperationResult validateTypeSpecificConfiguration(AdapterConfiguration configuration) {
        switch(configuration.getAdapterType()) {
            case HTTP:
                return validateHttpConfiguration(configuration);
            case JDBC:
                return validateJdbcConfiguration(configuration);
            case FTP:
                return validateFtpConfiguration(configuration);
            case SOAP:
                return validateSoapConfiguration(configuration);
            default:
                return AdapterOperationResult.success("Configuration valid");
        }
    }

    private AdapterOperationResult validateHttpConfiguration(AdapterConfiguration config) {
        Map<String, Object> props = config.getConnectionProperties();

        String url = (String) props.get("url");
        if(url == null || !url.matches("https?://.*")) {
            return AdapterOperationResult.failure("Invalid HTTP URL");
        }

        return AdapterOperationResult.success("HTTP configuration valid");
    }

    private AdapterOperationResult validateJdbcConfiguration(AdapterConfiguration config) {
        Map<String, Object> props = config.getConnectionProperties();

        String url = (String) props.get("jdbcUrl");
        if(url == null || !url.startsWith("jdbc:")) {
            return AdapterOperationResult.failure("Invalid JDBC URL");
        }

        return AdapterOperationResult.success("JDBC configuration valid");
    }

    private AdapterOperationResult validateFtpConfiguration(AdapterConfiguration config) {
        Map<String, Object> props = config.getConnectionProperties();

        String host = (String) props.get("host");
        if(host == null || host.trim().isEmpty()) {
            return AdapterOperationResult.failure("FTP host is required");
        }

        Integer port = (Integer) props.get("port");
        if(port == null || port < 1 || port > 65535) {
            return AdapterOperationResult.failure("Invalid FTP port");
        }

        return AdapterOperationResult.success("FTP configuration valid");
    }

    private AdapterOperationResult validateSoapConfiguration(AdapterConfiguration config) {
        Map<String, Object> props = config.getConnectionProperties();

        String wsdlUrl = (String) props.get("wsdlUrl");
        if(wsdlUrl == null || !wsdlUrl.matches("https?://.*\\.wsdl")) {
            return AdapterOperationResult.failure("Invalid WSDL URL");
        }

        return AdapterOperationResult.success("SOAP configuration valid");
    }
}
