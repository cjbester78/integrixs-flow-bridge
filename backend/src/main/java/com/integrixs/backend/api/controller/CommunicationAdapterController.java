package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.CreateAdapterRequest;
import com.integrixs.backend.api.dto.request.TestAdapterRequest;
import com.integrixs.backend.api.dto.request.UpdateAdapterRequest;
import com.integrixs.backend.api.dto.response.AdapterResponse;
import com.integrixs.backend.api.dto.response.AdapterTestResponse;
import com.integrixs.backend.application.service.AdapterTestingService;
import com.integrixs.backend.application.service.CommunicationAdapterService;
import com.integrixs.backend.logging.BusinessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for communication adapter management
 * Handles only HTTP concerns, delegates business logic to application service
 */
@RestController
@RequestMapping("/api/communication-adapters")
@Validated
public class CommunicationAdapterController {

    private static final Logger log = LoggerFactory.getLogger(CommunicationAdapterController.class);

    private final CommunicationAdapterService adapterService;
    private final AdapterTestingService testingService;

    public CommunicationAdapterController(CommunicationAdapterService adapterService,
                                          AdapterTestingService testingService) {
        this.adapterService = adapterService;
        this.testingService = testingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "ADAPTER.LIST", module = "AdapterManagement")
    public ResponseEntity<List<AdapterResponse>> getAllAdapters(
            @RequestParam(required = false) String mode) {
        log.debug("Fetching all communication adapters with mode filter: {}", mode);
        List<AdapterResponse> adapters = adapterService.getAllAdapters();

        // Filter by mode if provided
        if(mode != null && !mode.isEmpty()) {
            adapters = adapters.stream()
                    .filter(a -> mode.equalsIgnoreCase(a.getMode()))
                    .toList();
        }

        return ResponseEntity.ok(adapters);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "ADAPTER.GET", module = "AdapterManagement")
    public ResponseEntity<AdapterResponse> getAdapterById(@PathVariable String id) {
        log.debug("Fetching communication adapter: {}", id);
        try {
            AdapterResponse adapter = adapterService.getAdapterById(id);
            return ResponseEntity.ok(adapter);
        } catch(RuntimeException e) {
            log.error("Adapter not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ADAPTER.CREATE", module = "AdapterManagement", logInput = true)
    public ResponseEntity<AdapterResponse> createAdapter(@Valid @RequestBody CreateAdapterRequest request) {
        log.debug("Creating new communication adapter: {}", request.getName());
        try {
            AdapterResponse adapter = adapterService.createAdapter(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(adapter);
        } catch(IllegalArgumentException e) {
            log.error("Invalid adapter data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ADAPTER.UPDATE", module = "AdapterManagement", logInput = true)
    public ResponseEntity<AdapterResponse> updateAdapter(
            @PathVariable String id,
            @Valid @RequestBody UpdateAdapterRequest request) {
        log.debug("Updating communication adapter: {}", id);
        try {
            AdapterResponse adapter = adapterService.updateAdapter(id, request);
            return ResponseEntity.ok(adapter);
        } catch(RuntimeException e) {
            log.error("Error updating adapter: {}", e.getMessage());
            if(e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER')")
    @BusinessOperation(value = "ADAPTER.DELETE", module = "AdapterManagement")
    public ResponseEntity<Void> deleteAdapter(@PathVariable String id) {
        log.debug("Deleting communication adapter: {}", id);
        try {
            adapterService.deleteAdapter(id);
            return ResponseEntity.noContent().build();
        } catch(RuntimeException e) {
            log.error("Error deleting adapter: {}", e.getMessage());
            if(e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ADAPTER.ACTIVATE", module = "AdapterManagement")
    public ResponseEntity<AdapterResponse> activateAdapter(@PathVariable String id) {
        log.debug("Activating communication adapter: {}", id);
        try {
            AdapterResponse adapter = adapterService.activateAdapter(id);
            return ResponseEntity.ok(adapter);
        } catch(RuntimeException e) {
            log.error("Error activating adapter: {}", e.getMessage());
            if(e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ADAPTER.DEACTIVATE", module = "AdapterManagement")
    public ResponseEntity<AdapterResponse> deactivateAdapter(@PathVariable String id) {
        log.debug("Deactivating communication adapter: {}", id);
        try {
            AdapterResponse adapter = adapterService.deactivateAdapter(id);
            return ResponseEntity.ok(adapter);
        } catch(RuntimeException e) {
            log.error("Error deactivating adapter: {}", e.getMessage());
            if(e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/test")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ADAPTER.TEST", module = "AdapterManagement", includeMetrics = true)
    public ResponseEntity<AdapterTestResponse> testAdapter(@Valid @RequestBody TestAdapterRequest request) {
        log.debug("Testing adapter connection: {}", request.getAdapterId());
        try {
            AdapterTestResponse result = testingService.testAdapter(request);
            return ResponseEntity.ok(result);
        } catch(RuntimeException e) {
            log.error("Error testing adapter: {}", e.getMessage());
            if(e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdapterTestResponse.builder()
                            .success(false)
                            .message("Test failed: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ADAPTER.TEST", module = "AdapterManagement", includeMetrics = true)
    public ResponseEntity<AdapterTestResponse> testAdapterById(
            @PathVariable String id,
            @Valid @RequestBody TestAdapterRequest request) {
        log.debug("Testing adapter connection by ID: {}", id);
        try {
            request.setAdapterId(id);
            AdapterTestResponse result = testingService.testAdapter(request);
            return ResponseEntity.ok(result);
        } catch(RuntimeException e) {
            log.error("Error testing adapter: {}", e.getMessage());
            if(e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdapterTestResponse.builder()
                            .success(false)
                            .message("Test failed: " + e.getMessage())
                            .build());
        }
    }
}
