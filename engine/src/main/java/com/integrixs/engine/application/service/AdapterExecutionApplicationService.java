package com.integrixs.engine.application.service;

import com.integrixs.engine.api.dto.AdapterExecutionRequestDTO;
import com.integrixs.engine.api.dto.AdapterExecutionResponseDTO;
import com.integrixs.engine.domain.model.AdapterExecutionContext;
import com.integrixs.engine.domain.model.AdapterExecutionResult;
import com.integrixs.engine.domain.service.FlowAdapterExecutor;
import com.integrixs.engine.infrastructure.adapter.AdapterRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for orchestrating adapter executions
 */
@Service
public class AdapterExecutionApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AdapterExecutionApplicationService.class);


    private final FlowAdapterExecutor adapterExecutionService;
    private final AdapterRegistry adapterRegistry;

    /**
     * Execute adapter to fetch data
     * @param request Execution request
     * @return Execution response
     */
    public AdapterExecutionApplicationService(FlowAdapterExecutor adapterExecutionService, AdapterRegistry adapterRegistry) {
        this.adapterExecutionService = adapterExecutionService;
        this.adapterRegistry = adapterRegistry;
    }

    public AdapterExecutionResponseDTO fetchData(AdapterExecutionRequestDTO request) {
        log.info("Fetching data from adapter: {}", request.getAdapterId());

        try {
            // Validate adapter
            validateAdapter(request.getAdapterId());

            // Build context
            AdapterExecutionContext context = buildContext(request);

            // Execute
            AdapterExecutionResult result = adapterExecutionService.fetchData(
                request.getAdapterId(),
                context
           );

            // Convert to DTO
            return convertToResponseDTO(result);

        } catch(Exception e) {
            log.error("Error fetching data from adapter: {}", request.getAdapterId(), e);
            return createErrorResponse(e);
        }
    }

    /**
     * Execute adapter to send data
     * @param request Execution request
     * @return Execution response
     */
    public AdapterExecutionResponseDTO sendData(AdapterExecutionRequestDTO request) {
        log.info("Sending data to adapter: {}", request.getAdapterId());

        try {
            // Validate adapter
            validateAdapter(request.getAdapterId());

            // Build context
            AdapterExecutionContext context = buildContext(request);

            // Execute
            AdapterExecutionResult result = adapterExecutionService.sendData(
                request.getAdapterId(),
                request.getData(),
                context
           );

            // Convert to DTO
            return convertToResponseDTO(result);

        } catch(Exception e) {
            log.error("Error sending data to adapter: {}", request.getAdapterId(), e);
            return createErrorResponse(e);
        }
    }

    /**
     * Execute adapter asynchronously to fetch data
     * @param request Execution request
     * @return Future with execution response
     */
    public CompletableFuture<AdapterExecutionResponseDTO> fetchDataAsync(AdapterExecutionRequestDTO request) {
        return CompletableFuture.supplyAsync(() -> fetchData(request));
    }

    /**
     * Execute adapter asynchronously to send data
     * @param request Execution request
     * @return Future with execution response
     */
    public CompletableFuture<AdapterExecutionResponseDTO> sendDataAsync(AdapterExecutionRequestDTO request) {
        return CompletableFuture.supplyAsync(() -> sendData(request));
    }

    /**
     * Get adapter capabilities
     * @param adapterId Adapter ID
     * @return Map of capabilities
     */
    public Map<String, Object> getAdapterCapabilities(String adapterId) {
        return adapterExecutionService.getAdapterCapabilities(adapterId);
    }

    /**
     * Check adapter health
     * @param adapterId Adapter ID
     * @return true if healthy
     */
    public boolean isAdapterHealthy(String adapterId) {
        return adapterExecutionService.isAdapterReady(adapterId);
    }

    private void validateAdapter(String adapterId) {
        if(!adapterRegistry.isAdapterRegistered(adapterId)) {
            throw new IllegalArgumentException("Adapter not found: " + adapterId);
        }
    }

    private AdapterExecutionContext buildContext(AdapterExecutionRequestDTO request) {
        return AdapterExecutionContext.builder()
                .executionId(UUID.randomUUID().toString())
                .flowId(request.getFlowId())
                .stepId(request.getStepId())
                .parameters(request.getParameters())
                .headers(request.getHeaders())
                .metadata(request.getMetadata())
                .async(request.isAsync())
                .timeout(request.getTimeout())
                .correlationId(request.getCorrelationId())
                .build();
    }

    private AdapterExecutionResponseDTO convertToResponseDTO(AdapterExecutionResult result) {
        AdapterExecutionResponseDTO dto = new AdapterExecutionResponseDTO();
        dto.setExecutionId(result.getExecutionId());
        dto.setSuccess(result.isSuccess());
        dto.setData(result.getData());
        dto.setErrorMessage(result.getErrorMessage());
        dto.setErrorCode(result.getErrorCode());
        dto.setTimestamp(result.getTimestamp());
        dto.setExecutionTimeMs(result.getExecutionTimeMs());
        dto.setMetadata(result.getMetadata());
        dto.setWarnings(result.getWarnings());
        dto.setAdapterType(result.getAdapterType());
        dto.setAdapterId(result.getAdapterId());
        return dto;
    }

    private AdapterExecutionResponseDTO createErrorResponse(Exception e) {
        AdapterExecutionResponseDTO dto = new AdapterExecutionResponseDTO();
        dto.setSuccess(false);
        dto.setErrorMessage(e.getMessage());
        dto.setErrorCode("ADAPTER_EXECUTION_ERROR");
        return dto;
    }

    // Builder
    public static AdapterExecutionApplicationServiceBuilder builder() {
        return new AdapterExecutionApplicationServiceBuilder();
    }

    public static class AdapterExecutionApplicationServiceBuilder {
        private FlowAdapterExecutor adapterExecutionService;
        private AdapterRegistry adapterRegistry;

        public AdapterExecutionApplicationServiceBuilder adapterExecutionService(FlowAdapterExecutor adapterExecutionService) {
            this.adapterExecutionService = adapterExecutionService;
            return this;
        }

        public AdapterExecutionApplicationServiceBuilder adapterRegistry(AdapterRegistry adapterRegistry) {
            this.adapterRegistry = adapterRegistry;
            return this;
        }

        public AdapterExecutionApplicationService build() {
            return new AdapterExecutionApplicationService(
                this.adapterExecutionService,
                this.adapterRegistry
            );
        }
    }
}
