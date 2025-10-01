package com.integrixs.adapters.api.controller;

import com.integrixs.adapters.api.dto.*;
import com.integrixs.adapters.application.service.AdapterApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for adapter management operations
 */
@RestController
@RequestMapping("/api/adapters")
@Tag(name = "Adapter Management", description = "APIs for managing communication adapters")
public class AdapterController {

    private static final Logger log = LoggerFactory.getLogger(AdapterController.class);

    private final AdapterApplicationService adapterApplicationService;

    public AdapterController(AdapterApplicationService adapterApplicationService) {
        this.adapterApplicationService = adapterApplicationService;
    }

    /**
     * Create a new adapter
     * @param request Create adapter request
     * @return Created adapter response
     */
    @PostMapping
    @Operation(summary = "Create a new adapter", description = "Creates and configures a new communication adapter")
    @ApiResponses( {
            @ApiResponse(responseCode = "201", description = "Adapter created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<CreateAdapterResponseDTO> createAdapter(
            @Valid @RequestBody CreateAdapterRequestDTO request) {

        log.info("Creating adapter: {} type: {} mode: {}",
                request.getName(), request.getAdapterType(), request.getAdapterMode());

        CreateAdapterResponseDTO response = adapterApplicationService.createAdapter(request);

        if(response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update adapter configuration
     * @param adapterId Adapter ID
     * @param request Update request
     * @return Update response
     */
    @PutMapping("/ {adapterId}")
    @Operation(summary = "Update adapter configuration", description = "Updates the configuration of an existing adapter")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Adapter updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Adapter not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<AdapterOperationResponseDTO> updateAdapter(
            @Parameter(description = "Adapter ID", required = true)
            @PathVariable String adapterId,
            @Valid @RequestBody UpdateAdapterRequestDTO request) {

        log.info("Updating adapter: {}", adapterId);

        AdapterOperationResponseDTO response = adapterApplicationService.updateAdapter(adapterId, request);

        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete an adapter
     * @param adapterId Adapter ID
     * @return Delete response
     */
    @DeleteMapping("/ {adapterId}")
    @Operation(summary = "Delete an adapter", description = "Deletes an existing adapter and its configuration")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Adapter deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Adapter not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR')")
    public ResponseEntity<AdapterOperationResponseDTO> deleteAdapter(
            @Parameter(description = "Adapter ID", required = true)
            @PathVariable String adapterId) {

        log.info("Deleting adapter: {}", adapterId);

        AdapterOperationResponseDTO response = adapterApplicationService.deleteAdapter(adapterId);
        return ResponseEntity.ok(response);
    }

    /**
     * Test adapter connection
     * @param adapterId Adapter ID
     * @return Test result
     */
    @PostMapping("/ {adapterId}/test - connection")
    @Operation(summary = "Test adapter connection", description = "Tests the connectivity of the adapter")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Connection test completed"),
            @ApiResponse(responseCode = "404", description = "Adapter not found")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<AdapterOperationResponseDTO> testConnection(
            @Parameter(description = "Adapter ID", required = true)
            @PathVariable String adapterId) {

        log.info("Testing connection for adapter: {}", adapterId);

        AdapterOperationResponseDTO response = adapterApplicationService.testConnection(adapterId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch data using inbound adapter
     * @param adapterId Adapter ID
     * @param request Fetch request
     * @return Fetch response
     */
    @PostMapping("/ {adapterId}/fetch")
    @Operation(summary = "Fetch data", description = "Fetches data using a inbound adapter")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Data fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Adapter not found")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<AdapterOperationResponseDTO> fetchData(
            @Parameter(description = "Adapter ID", required = true)
            @PathVariable String adapterId,
            @Valid @RequestBody FetchDataRequestDTO request) {

        log.info("Fetching data from adapter: {}", adapterId);

        AdapterOperationResponseDTO response = adapterApplicationService.fetchData(adapterId, request);

        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Send data using outbound adapter
     * @param adapterId Adapter ID
     * @param request Send request
     * @return Send response
     */
    @PostMapping("/ {adapterId}/send")
    @Operation(summary = "Send data", description = "Sends data using a outbound adapter")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Data sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Adapter not found")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<AdapterOperationResponseDTO> sendData(
            @Parameter(description = "Adapter ID", required = true)
            @PathVariable String adapterId,
            @Valid @RequestBody SendDataRequestDTO request) {

        log.info("Sending data to adapter: {}", adapterId);

        AdapterOperationResponseDTO response = adapterApplicationService.sendData(adapterId, request);

        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get adapter status
     * @param adapterId Adapter ID
     * @return Status response
     */
    @GetMapping("/ {adapterId}/status")
    @Operation(summary = "Get adapter status", description = "Retrieves the current status of an adapter")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Adapter not found")
    })
    public ResponseEntity<AdapterStatusResponseDTO> getAdapterStatus(
            @Parameter(description = "Adapter ID", required = true)
            @PathVariable String adapterId) {

        AdapterStatusResponseDTO response = adapterApplicationService.getAdapterStatus(adapterId);
        return ResponseEntity.ok(response);
    }

    /**
     * List all adapters
     * @return List of adapter information
     */
    @GetMapping
    @Operation(summary = "List all adapters", description = "Retrieves a list of all configured adapters")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    public ResponseEntity<List<AdapterInfoDTO>> listAdapters() {
        List<AdapterInfoDTO> adapters = adapterApplicationService.listAdapters();
        return ResponseEntity.ok(adapters);
    }

    /**
     * Get adapter metadata
     * @param adapterType Adapter type
     * @param adapterMode Adapter mode
     * @return Adapter metadata
     */
    @GetMapping("/metadata")
    @Operation(summary = "Get adapter metadata", description = "Retrieves metadata for a specific adapter type and mode")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid adapter type or mode")
    })
    public ResponseEntity<AdapterMetadataDTO> getAdapterMetadata(
            @Parameter(description = "Adapter type", required = true)
            @RequestParam String adapterType,
            @Parameter(description = "Adapter mode(SOURCE or TARGET)", required = true)
            @RequestParam String adapterMode) {

        AdapterMetadataDTO metadata = adapterApplicationService.getAdapterMetadata(adapterType, adapterMode);

        if(metadata != null) {
            return ResponseEntity.ok(metadata);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start adapter
     * @param adapterId Adapter ID
     * @return Operation response
     */
    @PostMapping("/ {adapterId}/start")
    @Operation(summary = "Start adapter", description = "Starts an adapter to begin processing")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Adapter started successfully"),
            @ApiResponse(responseCode = "404", description = "Adapter not found"),
            @ApiResponse(responseCode = "409", description = "Adapter already running")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<AdapterOperationResponseDTO> startAdapter(
            @Parameter(description = "Adapter ID", required = true)
            @PathVariable String adapterId) {

        log.info("Starting adapter: {}", adapterId);

        try {
            adapterApplicationService.startAdapter(adapterId);
            return ResponseEntity.ok(AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(true)
                    .message("Adapter started successfully")
                    .build());
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AdapterOperationResponseDTO.builder()
                            .adapterId(adapterId)
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    /**
     * Stop adapter
     * @param adapterId Adapter ID
     * @return Operation response
     */
    @PostMapping("/ {adapterId}/stop")
    @Operation(summary = "Stop adapter", description = "Stops an adapter from processing")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Adapter stopped successfully"),
            @ApiResponse(responseCode = "404", description = "Adapter not found")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<AdapterOperationResponseDTO> stopAdapter(
            @Parameter(description = "Adapter ID", required = true)
            @PathVariable String adapterId) {

        log.info("Stopping adapter: {}", adapterId);

        try {
            adapterApplicationService.stopAdapter(adapterId);
            return ResponseEntity.ok(AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(true)
                    .message("Adapter stopped successfully")
                    .build());
        } catch(Exception e) {
            return ResponseEntity.ok(AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build());
        }
    }

    /**
     * Reset adapter
     * @param adapterId Adapter ID
     * @return Operation response
     */
    @PostMapping("/ {adapterId}/reset")
    @Operation(summary = "Reset adapter", description = "Resets an adapter to clear its state and reinitialize")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Adapter reset successfully"),
            @ApiResponse(responseCode = "404", description = "Adapter not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR')")
    public ResponseEntity<AdapterOperationResponseDTO> resetAdapter(
            @Parameter(description = "Adapter ID", required = true)
            @PathVariable String adapterId) {

        log.info("Resetting adapter: {}", adapterId);

        try {
            adapterApplicationService.resetAdapter(adapterId);
            return ResponseEntity.ok(AdapterOperationResponseDTO.builder()
                    .adapterId(adapterId)
                    .success(true)
                    .message("Adapter reset successfully")
                    .build());
        } catch(Exception e) {
            return ResponseEntity.badRequest()
                    .body(AdapterOperationResponseDTO.builder()
                            .adapterId(adapterId)
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }
}
