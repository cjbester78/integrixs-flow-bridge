package com.integrixs.backend.api.controller.v1;

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
import java.util.stream.Collectors;

/**
 * REST controller for communication adapter management - API v1
 * Provides backward compatibility with legacy terminology(SENDER/RECEIVER)
 *
 * @deprecated Use /api/v2/communication - adapters for new integrations
 */
@RestController
@RequestMapping("/api/v1/communication - adapters")
@RequiredArgsConstructor
@Validated
@Slf4j
@Deprecated
public class CommunicationAdapterControllerV1 {

    private final CommunicationAdapterService adapterService;
    private final AdapterTestingService testingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<List<AdapterResponse>> getAllAdapters(
            @RequestParam(required = false) String mode) {

        log.debug("API v1(deprecated): Fetching communication adapters with mode filter: {}", mode);
        logDeprecationWarning();

        // Convert v1 terminology to v2 for filtering
        String v2Mode = convertToV2Mode(mode);

        List<AdapterResponse> adapters = adapterService.getAllAdapters();

        // Filter by mode if provided
        if(v2Mode != null && !v2Mode.isEmpty()) {
            adapters = adapters.stream()
                    .filter(a -> v2Mode.equalsIgnoreCase(a.getMode()))
                    .toList();
        }

        // Convert responses back to v1 terminology
        adapters = adapters.stream()
                .map(this::convertToV1Response)
                .collect(Collectors.toList());

        return ResponseEntity.ok(adapters);
    }

    @GetMapping("/ {id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<AdapterResponse> getAdapterById(@PathVariable String id) {
        log.debug("API v1(deprecated): Fetching communication adapter: {}", id);
        logDeprecationWarning();

        try {
            AdapterResponse adapter = adapterService.getAdapterById(id);
            // Convert response to v1 terminology
            return ResponseEntity.ok(convertToV1Response(adapter));
        } catch(RuntimeException e) {
            log.error("API v1: Adapter not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<AdapterResponse> createAdapter(@Valid @RequestBody CreateAdapterRequest request) {
        log.info("API v1(deprecated): Creating communication adapter: {} with mode: {}",
                request.getName(), request.getMode());
        logDeprecationWarning();

        // Convert v1 request to v2
        CreateAdapterRequest v2Request = convertToV2Request(request);

        AdapterResponse created = adapterService.createAdapter(v2Request);

        // Convert response back to v1 terminology
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToV1Response(created));
    }

    @PutMapping("/ {id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<AdapterResponse> updateAdapter(
            @PathVariable String id,
            @Valid @RequestBody UpdateAdapterRequest request) {

        log.info("API v1(deprecated): Updating communication adapter: {}", id);
        logDeprecationWarning();

        // Convert v1 request to v2
        UpdateAdapterRequest v2Request = convertToV2UpdateRequest(request);

        try {
            AdapterResponse updated = adapterService.updateAdapter(id, v2Request);
            // Convert response back to v1 terminology
            return ResponseEntity.ok(convertToV1Response(updated));
        } catch(RuntimeException e) {
            log.error("API v1: Failed to update adapter: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/ {id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    public ResponseEntity<Void> deleteAdapter(@PathVariable String id) {
        log.info("API v1(deprecated): Deleting communication adapter: {}", id);
        logDeprecationWarning();

        try {
            adapterService.deleteAdapter(id);
            return ResponseEntity.noContent().build();
        } catch(RuntimeException e) {
            log.error("API v1: Failed to delete adapter: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/ {id}/test")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<AdapterTestResponse> testAdapter(
            @PathVariable String id,
            @Valid @RequestBody TestAdapterRequest request) {

        log.info("API v1(deprecated): Testing communication adapter: {}", id);
        logDeprecationWarning();

        try {
            request.setAdapterId(id);
            AdapterTestResponse result = testingService.testAdapter(request);
            return ResponseEntity.ok(result);
        } catch(RuntimeException e) {
            log.error("API v1: Failed to test adapter: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AdapterTestResponse.builder()
                            .success(false)
                            .message("Test failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Convert v1 mode(SENDER/RECEIVER) to v2 mode(INBOUND/OUTBOUND)
     */
    private String convertToV2Mode(String v1Mode) {
        if(v1Mode == null) return null;

        return switch(v1Mode.toUpperCase()) {
            case "SENDER" -> "INBOUND";
            case "RECEIVER" -> "OUTBOUND";
            default -> v1Mode; // Pass through if already v2
        };
    }

    /**
     * Convert v2 mode(INBOUND/OUTBOUND) to v1 mode(SENDER/RECEIVER)
     */
    private String convertToV1Mode(String v2Mode) {
        if(v2Mode == null) return null;

        return switch(v2Mode.toUpperCase()) {
            case "INBOUND" -> "SENDER";
            case "OUTBOUND" -> "RECEIVER";
            default -> v2Mode; // Pass through if already v1
        };
    }

    /**
     * Convert create request from v1 to v2 terminology
     */
    private CreateAdapterRequest convertToV2Request(CreateAdapterRequest v1Request) {
        return CreateAdapterRequest.builder()
                .name(v1Request.getName())
                .type(v1Request.getType())
                .mode(convertToV2Mode(v1Request.getMode()))
                .direction(v1Request.getDirection())
                .configuration(v1Request.getConfiguration())
                .description(v1Request.getDescription())
                .businessComponentId(v1Request.getBusinessComponentId())
                .externalAuthId(v1Request.getExternalAuthId())
                .active(v1Request.isActive())
                .build();
    }

    /**
     * Convert update request from v1 to v2 terminology
     */
    private UpdateAdapterRequest convertToV2UpdateRequest(UpdateAdapterRequest v1Request) {
        UpdateAdapterRequest v2Request = new UpdateAdapterRequest();
        v2Request.setName(v1Request.getName());
        v2Request.setType(v1Request.getType());
        v2Request.setMode(v1Request.getMode() != null ? convertToV2Mode(v1Request.getMode()) : null);
        v2Request.setDirection(v1Request.getDirection());
        v2Request.setConfiguration(v1Request.getConfiguration());
        v2Request.setDescription(v1Request.getDescription());
        v2Request.setBusinessComponentId(v1Request.getBusinessComponentId());
        v2Request.setExternalAuthId(v1Request.getExternalAuthId());
        v2Request.setActive(v1Request.isActive());
        return v2Request;
    }

    /**
     * Convert adapter response from v2 to v1 terminology
     */
    private AdapterResponse convertToV1Response(AdapterResponse v2Response) {
        AdapterResponse v1Response = AdapterResponse.builder()
                .id(v2Response.getId())
                .name(v2Response.getName())
                .type(v2Response.getType())
                .mode(convertToV1Mode(v2Response.getMode()))
                .direction(v2Response.getDirection())
                .configuration(v2Response.getConfiguration())
                .description(v2Response.getDescription())
                .businessComponentId(v2Response.getBusinessComponentId())
                .businessComponentName(v2Response.getBusinessComponentName())
                .externalAuthId(v2Response.getExternalAuthId())
                .active(v2Response.isActive())
                .status(v2Response.getStatus())
                .lastTestDate(v2Response.getLastTestDate())
                .lastTestResult(v2Response.getLastTestResult())
                .createdAt(v2Response.getCreatedAt())
                .updatedAt(v2Response.getUpdatedAt())
                .createdBy(v2Response.getCreatedBy())
                .updatedBy(v2Response.getUpdatedBy())
                .build();

        return v1Response;
    }

    /**
     * Log deprecation warning
     */
    private void logDeprecationWarning() {
        log.warn("DEPRECATION WARNING: API v1 endpoints are deprecated. " +
                "Please migrate to API v2(/api/v2/communication - adapters) which uses " +
                "industry - standard terminology(INBOUND/OUTBOUND instead of SENDER/RECEIVER).");
    }
}
