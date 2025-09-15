package com.integrixs.backend.api.controller.v2;

import com.integrixs.backend.api.dto.request.CreateAdapterRequest;
import com.integrixs.backend.api.dto.request.TestAdapterRequest;
import com.integrixs.backend.api.dto.request.UpdateAdapterRequest;
import com.integrixs.backend.api.dto.response.AdapterResponse;
import com.integrixs.backend.api.dto.response.AdapterTestResponse;
import com.integrixs.backend.application.service.AdapterTestingService;
import com.integrixs.backend.application.service.CommunicationAdapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for communication adapter management - API v2
 * Uses industry - standard terminology(INBOUND/OUTBOUND)
 */
@RestController
@RequestMapping("/api/v2/communication - adapters")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CommunicationAdapterControllerV2 {

    private final CommunicationAdapterService adapterService;
    private final AdapterTestingService testingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<List<AdapterResponse>> getAllAdapters(
            @RequestParam(required = false) String mode) {

        log.debug("API v2: Fetching communication adapters with mode filter: {}", mode);

        // V2 uses new terminology
        List<AdapterResponse> adapters = adapterService.getAllAdapters();

        // Filter by mode if provided
        if(mode != null && !mode.isEmpty()) {
            adapters = adapters.stream()
                    .filter(a -> mode.equalsIgnoreCase(a.getMode()))
                    .toList();
        }

        return ResponseEntity.ok(adapters);
    }

    @GetMapping("/ {id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<AdapterResponse> getAdapterById(@PathVariable String id) {
        log.debug("API v2: Fetching communication adapter: {}", id);
        try {
            AdapterResponse adapter = adapterService.getAdapterById(id);
            return ResponseEntity.ok(adapter);
        } catch(RuntimeException e) {
            log.error("API v2: Adapter not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<AdapterResponse> createAdapter(@Valid @RequestBody CreateAdapterRequest request) {
        log.info("API v2: Creating communication adapter: {} with mode: {}",
                request.getName(), request.getMode());

        // V2 expects INBOUND/OUTBOUND
        validateV2Mode(request.getMode());

        AdapterResponse created = adapterService.createAdapter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/ {id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<AdapterResponse> updateAdapter(
            @PathVariable String id,
            @Valid @RequestBody UpdateAdapterRequest request) {

        log.info("API v2: Updating communication adapter: {}", id);

        // V2 expects INBOUND/OUTBOUND if mode is being updated
        if(request.getMode() != null) {
            validateV2Mode(request.getMode());
        }

        try {
            AdapterResponse updated = adapterService.updateAdapter(id, request);
            return ResponseEntity.ok(updated);
        } catch(RuntimeException e) {
            log.error("API v2: Failed to update adapter: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/ {id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<Void> deleteAdapter(@PathVariable String id) {
        log.info("API v2: Deleting communication adapter: {}", id);
        try {
            adapterService.deleteAdapter(id);
            return ResponseEntity.noContent().build();
        } catch(RuntimeException e) {
            log.error("API v2: Failed to delete adapter: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/ {id}/test")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<AdapterTestResponse> testAdapter(
            @PathVariable String id,
            @Valid @RequestBody TestAdapterRequest request) {

        log.info("API v2: Testing communication adapter: {}", id);
        try {
            request.setAdapterId(id);
            AdapterTestResponse result = testingService.testAdapter(request);
            return ResponseEntity.ok(result);
        } catch(RuntimeException e) {
            log.error("API v2: Failed to test adapter: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdapterTestResponse.builder()
                            .success(false)
                            .message("Test failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Validate that the mode uses v2 terminology
     */
    private void validateV2Mode(String mode) {
        if(mode != null && !mode.equalsIgnoreCase("INBOUND") && !mode.equalsIgnoreCase("OUTBOUND")) {
            throw new IllegalArgumentException(
                    "Invalid adapter mode for API v2. Use 'INBOUND' or 'OUTBOUND'. " +
                    "For legacy support with 'SENDER'/'RECEIVER', use API v1.");
        }
    }
}
