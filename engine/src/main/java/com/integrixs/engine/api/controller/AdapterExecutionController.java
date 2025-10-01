package com.integrixs.engine.api.controller;

import com.integrixs.engine.api.dto.AdapterExecutionRequestDTO;
import com.integrixs.engine.api.dto.AdapterExecutionResponseDTO;
import com.integrixs.engine.application.service.AdapterExecutionApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for adapter execution operations
 */
@RestController
@RequestMapping("/api/adapter-execution")
@Tag(name = "Adapter Execution", description = "Operations for executing adapters")
public class AdapterExecutionController {

    private static final Logger log = LoggerFactory.getLogger(AdapterExecutionController.class);


    private final AdapterExecutionApplicationService adapterExecutionApplicationService;

    public AdapterExecutionController(AdapterExecutionApplicationService adapterExecutionApplicationService) {
        this.adapterExecutionApplicationService = adapterExecutionApplicationService;
    }

    /**
     * Fetch data from an adapter
     * @param adapterId Adapter ID
     * @param request Execution request
     * @return Execution response
     */
    @PostMapping("/fetch/{adapterId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Fetch data from an adapter")
    public ResponseEntity<AdapterExecutionResponseDTO> fetchData(
            @Parameter(description = "Adapter ID") @PathVariable String adapterId,
            @Valid @RequestBody AdapterExecutionRequestDTO request) {

        log.info("Fetching data from adapter: {}", adapterId);
        request.setAdapterId(adapterId);

        AdapterExecutionResponseDTO response = adapterExecutionApplicationService.fetchData(request);

        return response.isSuccess() ?
            ResponseEntity.ok(response) :
            ResponseEntity.internalServerError().body(response);
    }

    /**
     * Send data to an adapter
     * @param adapterId Adapter ID
     * @param request Execution request
     * @return Execution response
     */
    @PostMapping("/send/ {adapterId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Send data to an adapter")
    public ResponseEntity<AdapterExecutionResponseDTO> sendData(
            @Parameter(description = "Adapter ID") @PathVariable String adapterId,
            @Valid @RequestBody AdapterExecutionRequestDTO request) {

        log.info("Sending data to adapter: {}", adapterId);
        request.setAdapterId(adapterId);

        AdapterExecutionResponseDTO response = adapterExecutionApplicationService.sendData(request);

        return response.isSuccess() ?
            ResponseEntity.ok(response) :
            ResponseEntity.internalServerError().body(response);
    }

    /**
     * Fetch data from an adapter asynchronously
     * @param adapterId Adapter ID
     * @param request Execution request
     * @return Future with execution response
     */
    @PostMapping("/fetch -async/ {adapterId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Fetch data from an adapter asynchronously")
    public CompletableFuture<ResponseEntity<AdapterExecutionResponseDTO>> fetchDataAsync(
            @Parameter(description = "Adapter ID") @PathVariable String adapterId,
            @Valid @RequestBody AdapterExecutionRequestDTO request) {

        log.info("Fetching data asynchronously from adapter: {}", adapterId);
        request.setAdapterId(adapterId);
        request.setAsync(true);

        return adapterExecutionApplicationService.fetchDataAsync(request)
                .thenApply(response ->
                    response.isSuccess() ?
                        ResponseEntity.ok(response) :
                        ResponseEntity.internalServerError().body(response)
               );
    }

    /**
     * Send data to an adapter asynchronously
     * @param adapterId Adapter ID
     * @param request Execution request
     * @return Future with execution response
     */
    @PostMapping("/send -async/ {adapterId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Send data to an adapter asynchronously")
    public CompletableFuture<ResponseEntity<AdapterExecutionResponseDTO>> sendDataAsync(
            @Parameter(description = "Adapter ID") @PathVariable String adapterId,
            @Valid @RequestBody AdapterExecutionRequestDTO request) {

        log.info("Sending data asynchronously to adapter: {}", adapterId);
        request.setAdapterId(adapterId);
        request.setAsync(true);

        return adapterExecutionApplicationService.sendDataAsync(request)
                .thenApply(response ->
                    response.isSuccess() ?
                        ResponseEntity.ok(response) :
                        ResponseEntity.internalServerError().body(response)
               );
    }

    /**
     * Get adapter capabilities
     * @param adapterId Adapter ID
     * @return Map of capabilities
     */
    @GetMapping("/{adapterId}/capabilities")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Operation(summary = "Get adapter capabilities")
    public ResponseEntity<Map<String, Object>> getAdapterCapabilities(
            @Parameter(description = "Adapter ID") @PathVariable String adapterId) {

        log.info("Getting capabilities for adapter: {}", adapterId);
        Map<String, Object> capabilities = adapterExecutionApplicationService.getAdapterCapabilities(adapterId);

        return ResponseEntity.ok(capabilities);
    }

    /**
     * Check adapter health
     * @param adapterId Adapter ID
     * @return Health status
     */
    @GetMapping("/{adapterId}/health")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Operation(summary = "Check adapter health")
    public ResponseEntity<Map<String, Object>> checkAdapterHealth(
            @Parameter(description = "Adapter ID") @PathVariable String adapterId) {

        log.info("Checking health for adapter: {}", adapterId);
        boolean healthy = adapterExecutionApplicationService.isAdapterHealthy(adapterId);

        Map<String, Object> health = Map.of(
            "adapterId", adapterId,
            "healthy", healthy,
            "timestamp", System.currentTimeMillis()
       );

        return ResponseEntity.ok(health);
    }

    /**
     * Test adapter connection
     * @param adapterId Adapter ID
     * @param request Optional test configuration
     * @return Test result
     */
    @PostMapping("/{adapterId}/test")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @Operation(summary = "Test adapter connection")
    public ResponseEntity<AdapterExecutionResponseDTO> testAdapter(
            @Parameter(description = "Adapter ID") @PathVariable String adapterId,
            @RequestBody(required = false) AdapterExecutionRequestDTO request) {

        log.info("Testing adapter: {}", adapterId);

        if(request == null) {
            request = new AdapterExecutionRequestDTO();
        }
        request.setAdapterId(adapterId);

        // Perform a simple fetch to test the adapter
        AdapterExecutionResponseDTO response = adapterExecutionApplicationService.fetchData(request);

        // Add test metadata
        response.getMetadata().put("testMode", true);
        response.getMetadata().put("testTimestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
