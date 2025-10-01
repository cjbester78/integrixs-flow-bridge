package com.integrixs.adapters.application.service;

import com.integrixs.adapters.api.dto.*;
import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.domain.service.AdapterManagementService;
import com.integrixs.adapters.domain.service.AdapterRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Application service for adapter operations
 * Orchestrates domain services and handles use cases
 */
@Service
public class AdapterApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AdapterApplicationService.class);

    private final AdapterManagementService adapterManagementService;
    private final AdapterRegistryService adapterRegistryService;

    public AdapterApplicationService(AdapterManagementService adapterManagementService,
                                   AdapterRegistryService adapterRegistryService) {
        this.adapterManagementService = adapterManagementService;
        this.adapterRegistryService = adapterRegistryService;
    }

    /**
     * Create a new adapter
     * @param request Create adapter request
     * @return Create adapter response
     */
    public CreateAdapterResponseDTO createAdapter(CreateAdapterRequestDTO request) {
        log.info("Creating adapter of type: {} mode: {}", request.getAdapterType(), request.getAdapterMode());

        try {
            // Convert DTO to domain model
            AdapterConfiguration configuration = convertToConfiguration(request);

            // Validate configuration
            AdapterOperationResult validationResult = adapterManagementService.validateConfiguration(configuration);
            if(!validationResult.isSuccess()) {
                return CreateAdapterResponseDTO.builder()
                        .success(false)
                        .errorMessage(validationResult.getMessage())
                        .build();
            }

            // Create adapter
            String adapterId = adapterManagementService.createAdapter(configuration);

            return CreateAdapterResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(true)
                    .message("Adapter created successfully")
                    .build();

        } catch(Exception e) {
            log.error("Error creating adapter: {}", e.getMessage(), e);
            return CreateAdapterResponseDTO.builder()
                    .success(false)
                    .errorMessage("Failed to create adapter: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Update adapter configuration
     * @param adapterId Adapter ID
     * @param request Update request
     * @return Update response
     */
    public AdapterOperationResponseDTO updateAdapter(String adapterId, UpdateAdapterRequestDTO request) {
        log.info("Updating adapter: {}", adapterId);

        try {
            AdapterConfiguration configuration = convertToConfiguration(request, adapterId);
            adapterManagementService.updateAdapterConfiguration(adapterId, configuration);

            return AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(true)
                    .message("Adapter updated successfully")
                    .build();

        } catch(Exception e) {
            log.error("Error updating adapter {}: {}", adapterId, e.getMessage(), e);
            return AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(false)
                    .errorMessage("Failed to update adapter: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Delete an adapter
     * @param adapterId Adapter ID
     * @return Delete response
     */
    public AdapterOperationResponseDTO deleteAdapter(String adapterId) {
        log.info("Deleting adapter: {}", adapterId);

        try {
            adapterManagementService.deleteAdapter(adapterId);

            return AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(true)
                    .message("Adapter deleted successfully")
                    .build();

        } catch(Exception e) {
            log.error("Error deleting adapter {}: {}", adapterId, e.getMessage(), e);
            return AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(false)
                    .errorMessage("Failed to delete adapter: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Test adapter connection
     * @param adapterId Adapter ID
     * @return Test result
     */
    public AdapterOperationResponseDTO testConnection(String adapterId) {
        log.info("Testing connection for adapter: {}", adapterId);

        try {
            AdapterOperationResult result = adapterManagementService.testAdapterConnection(adapterId);

            return AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(result.isSuccess())
                    .message(result.getMessage())
                    .errorMessage(result.getErrorCode())
                    .metadata(result.getMetadata())
                    .build();

        } catch(Exception e) {
            log.error("Error testing adapter connection {}: {}", adapterId, e.getMessage(), e);
            return AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(false)
                    .errorMessage("Connection test failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Fetch data using inbound adapter
     * @param adapterId Adapter ID
     * @param request Fetch request
     * @return Fetch response
     */
    public AdapterOperationResponseDTO fetchData(String adapterId, FetchDataRequestDTO request) {
        log.info("Fetching data from adapter: {}", adapterId);

        try {
            // Get adapter
            var adapter = adapterRegistryService.getAdapter(adapterId)
                    .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));

            if(!(adapter instanceof InboundAdapterPort)) {
                throw new IllegalArgumentException("Adapter is not a inbound adapter");
            }

            InboundAdapterPort senderAdapter = (InboundAdapterPort) adapter;

            // Convert to domain model
            FetchRequest fetchRequest = FetchRequest.builder()
                    .requestId(UUID.randomUUID().toString())
                    .adapterId(adapterId)
                    .parameters(request.getParameters())
                    .headers(request.getHeaders())
                    .query(request.getQuery())
                    .limit(request.getLimit())
                    .timeout(request.getTimeout())
                    .build();

            // Execute fetch
            AdapterOperationResult result = senderAdapter.fetch(fetchRequest);

            return AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(result.isSuccess())
                    .data(result.getData())
                    .message(result.getMessage())
                    .metadata(result.getMetadata())
                    .recordsProcessed(result.getRecordsProcessed())
                    .build();

        } catch(Exception e) {
            log.error("Error fetching data from adapter {}: {}", adapterId, e.getMessage(), e);
            return AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(false)
                    .errorMessage("Fetch failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Send data using outbound adapter
     * @param adapterId Adapter ID
     * @param request Send request
     * @return Send response
     */
    public AdapterOperationResponseDTO sendData(String adapterId, SendDataRequestDTO request) {
        log.info("Sending data to adapter: {}", adapterId);

        try {
            // Get adapter
            var adapter = adapterRegistryService.getAdapter(adapterId)
                    .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));

            if(!(adapter instanceof OutboundAdapterPort)) {
                throw new IllegalArgumentException("Adapter is not a outbound adapter");
            }

            OutboundAdapterPort receiverAdapter = (OutboundAdapterPort) adapter;

            // Convert to domain model
            SendRequest sendRequest = SendRequest.builder()
                    .requestId(UUID.randomUUID().toString())
                    .adapterId(adapterId)
                    .payload(request.getPayload())
                    .parameters(request.getParameters())
                    .headers(request.getHeaders())
                    .destination(request.getDestination())
                    .timeout(request.getTimeout())
                    .build();

            // Execute send
            AdapterOperationResult result = receiverAdapter.send(sendRequest);

            return AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(result.isSuccess())
                    .message(result.getMessage())
                    .metadata(result.getMetadata())
                    .recordsProcessed(result.getRecordsProcessed())
                    .build();

        } catch(Exception e) {
            log.error("Error sending data to adapter {}: {}", adapterId, e.getMessage(), e);
            return AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(false)
                    .errorMessage("Send failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Get adapter status
     * @param adapterId Adapter ID
     * @return Status response
     */
    public AdapterStatusResponseDTO getAdapterStatus(String adapterId) {
        try {
            AdapterOperationResult status = adapterManagementService.getAdapterStatus(adapterId);
            AdapterConfiguration config = adapterManagementService.getAdapterConfiguration(adapterId);

            return AdapterStatusResponseDTO.builder()
                    .adapterId(adapterId)
                    .adapterName(config.getName())
                    .adapterType(config.getAdapterType().name())
                    .adapterMode(config.getAdapterMode().name())
                    .isActive(status.isSuccess())
                    .status(status.getMessage())
                    .metadata(status.getMetadata())
                    .build();

        } catch(Exception e) {
            log.error("Error getting adapter status {}: {}", adapterId, e.getMessage(), e);
            return AdapterStatusResponseDTO.builder()
                    .adapterId(adapterId)
                    .isActive(false)
                    .status("Error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * List all adapters
     * @return List of adapter info
     */
    public List<AdapterInfoDTO> listAdapters() {
        return adapterManagementService.listAdapters().stream()
                .map(this::convertToAdapterInfo)
                .collect(Collectors.toList());
    }

    /**
     * Get adapter metadata
     * @param adapterType Adapter type
     * @param adapterMode Adapter mode
     * @return Metadata response
     */
    public AdapterMetadataDTO getAdapterMetadata(String adapterType, String adapterMode) {
        try {
            AdapterConfiguration.AdapterTypeEnum type = AdapterConfiguration.AdapterTypeEnum.valueOf(adapterType);
            AdapterConfiguration.AdapterModeEnum mode = AdapterConfiguration.AdapterModeEnum.valueOf(adapterMode);

            AdapterMetadata metadata = adapterManagementService.getAdapterMetadata(type, mode);

            return convertToMetadataDTO(metadata);

        } catch(Exception e) {
            log.error("Error getting adapter metadata: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Start adapter
     * @param adapterId Adapter ID
     */
    public void startAdapter(String adapterId) {
        log.info("Starting adapter: {}", adapterId);
        adapterManagementService.startAdapter(adapterId);
    }

    /**
     * Stop adapter
     * @param adapterId Adapter ID
     */
    public void stopAdapter(String adapterId) {
        log.info("Stopping adapter: {}", adapterId);
        adapterManagementService.stopAdapter(adapterId);
    }

    /**
     * Reset adapter
     * @param adapterId Adapter ID
     */
    public void resetAdapter(String adapterId) {
        log.info("Resetting adapter: {}", adapterId);
        adapterManagementService.resetAdapter(adapterId);
    }

    // Conversion methods

    private AdapterConfiguration convertToConfiguration(UpdateAdapterRequestDTO request, String adapterId) {
        // Get existing configuration to preserve adapter type and mode
        AdapterConfiguration existing = adapterManagementService.getAdapterConfiguration(adapterId);

        return AdapterConfiguration.builder()
                .adapterId(adapterId)
                .adapterType(existing.getAdapterType())
                .adapterMode(existing.getAdapterMode())
                .name(request.getName() != null ? request.getName() : existing.getName())
                .description(request.getDescription() != null ? request.getDescription() : existing.getDescription())
                .enableLogging(existing.isEnableLogging()) // Keep existing logging state
                .enableMonitoring(existing.isEnableMonitoring()) // Keep existing monitoring state
                .connectionProperties(mergeConfiguration(existing.getConnectionProperties(), request.getConnectionProperties()))
                .retryConfig(request.getRetryConfig() != null ? convertRetryConfig(request.getRetryConfig()) : existing.getRetryConfig())
                .authentication(request.getAuthentication() != null ? convertAuthConfig(request.getAuthentication()) : existing.getAuthentication())
                .build();
    }

    private Map<String, Object> mergeConfiguration(Map<String, Object> existing, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) {
            return existing;
        }
        Map<String, Object> merged = new HashMap<>(existing);
        merged.putAll(updates);
        return merged;
    }

    private AdapterConfiguration convertToConfiguration(CreateAdapterRequestDTO request) {
        return AdapterConfiguration.builder()
                .adapterId(UUID.randomUUID().toString())
                .adapterType(AdapterConfiguration.AdapterTypeEnum.valueOf(request.getAdapterType()))
                .adapterMode(AdapterConfiguration.AdapterModeEnum.valueOf(request.getAdapterMode()))
                .name(request.getName())
                .description(request.getDescription())
                .connectionProperties(request.getConnectionProperties())
                .operationProperties(request.getOperationProperties())
                .authentication(convertAuthConfig(request.getAuthentication()))
                .retryConfig(convertRetryConfig(request.getRetryConfig()))
                .timeout(request.getTimeout() != null ? request.getTimeout().intValue() : null)
                .build();
    }


    private AuthenticationConfig convertAuthConfig(AuthenticationConfigDTO dto) {
        if(dto == null) {
            return null;
        }

        return AuthenticationConfig.builder()
                .type(AuthenticationConfig.AuthenticationType.valueOf(dto.getType()))
                .username(dto.getUsername())
                .password(dto.getPassword())
                .apiKey(dto.getApiKey())
                .token(dto.getToken())
                .certificatePath(dto.getCertificatePath())
                .certificatePassword(dto.getCertificatePassword())
                .customHeaders(dto.getCustomHeaders())
                .build();
    }

    private RetryConfig convertRetryConfig(RetryConfigDTO dto) {
        if(dto == null) {
            return RetryConfig.builder().build();
        }

        return RetryConfig.builder()
                .enabled(dto.isEnabled())
                .maxAttempts(dto.getMaxAttempts())
                .initialDelay(dto.getInitialDelay())
                .backoffMultiplier(dto.getBackoffMultiplier())
                .maxDelay(dto.getMaxDelay())
                .build();
    }

    private AdapterInfoDTO convertToAdapterInfo(AdapterConfiguration config) {
        AdapterOperationResult status = adapterManagementService.getAdapterStatus(config.getAdapterId());

        return AdapterInfoDTO.builder()
                .adapterId(config.getAdapterId())
                .name(config.getName())
                .description(config.getDescription())
                .adapterType(config.getAdapterType().name())
                .adapterMode(config.getAdapterMode().name())
                .isActive(status.isSuccess())
                .status(status.getMessage())
                .build();
    }

    private AdapterMetadataDTO convertToMetadataDTO(AdapterMetadata metadata) {
        Map<String, String> capabilitiesStr = new HashMap<>();
        if(metadata.getCapabilities() != null) {
            metadata.getCapabilities().forEach((key, value) ->
                capabilitiesStr.put(key, value != null ? value.toString() : null));
        }

        return AdapterMetadataDTO.builder()
                .adapterName(metadata.getAdapterName())
                .adapterType(metadata.getAdapterType().name())
                .adapterMode(metadata.getAdapterMode().name())
                .version(metadata.getVersion())
                .description(metadata.getDescription())
                .supportedOperations(metadata.getSupportedOperations())
                .requiredProperties(metadata.getRequiredProperties())
                .optionalProperties(metadata.getOptionalProperties())
                .capabilities(capabilitiesStr)
                .supportsAsync(metadata.isSupportsAsync())
                .supportsBatch(metadata.isSupportsBatch())
                .supportsStreaming(metadata.isSupportsStreaming())
                .build();
    }
}
