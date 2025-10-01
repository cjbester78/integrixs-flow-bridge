package com.integrixs.backend.controller;

import com.integrixs.backend.api.dto.AdapterTypeDTO;
import com.integrixs.backend.api.dto.ConfigurationSchemaDTO;
import com.integrixs.backend.dto.response.ApiResponse;
import com.integrixs.backend.service.AdapterTypeService;
import com.integrixs.data.model.AdapterType;
import com.integrixs.data.model.AdapterCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/adapter - types")
@Tag(name = "Adapter Types", description = "Adapter type management APIs")
public class AdapterTypeController {

    private static final Logger log = LoggerFactory.getLogger(AdapterTypeController.class);


    private final AdapterTypeService adapterTypeService;

    public AdapterTypeController(AdapterTypeService adapterTypeService) {
        this.adapterTypeService = adapterTypeService;
    }

    @GetMapping
    @Operation(summary = "Get adapter types with pagination and filters")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<Page<AdapterType>>> getAdapterTypes(
            Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        log.debug("Fetching adapter types with category: {}, search: {}", category, search);
        Page<AdapterType> adapterTypes = adapterTypeService.getAdapterTypes(pageable, category, search);

        return ResponseEntity.ok(ApiResponse.success(adapterTypes));
    }

    @GetMapping("/ {id}")
    @Operation(summary = "Get adapter type by ID")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<AdapterType>> getAdapterType(@PathVariable UUID id) {
        log.debug("Fetching adapter type with id: {}", id);
        AdapterType adapterType = adapterTypeService.getAdapterType(id);

        return ResponseEntity.ok(ApiResponse.success(adapterType));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all adapter categories")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<List<AdapterCategory>>> getAdapterCategories() {
        log.debug("Fetching all adapter categories");
        List<AdapterCategory> categories = adapterTypeService.getAdapterCategories();

        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/ {id}/schema/ {direction}")
    @Operation(summary = "Get configuration schema for adapter type and direction")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<ConfigurationSchemaDTO>> getConfigurationSchema(
            @PathVariable UUID id,
            @PathVariable String direction) {

        log.debug("Fetching configuration schema for adapter type: {}, direction: {}", id, direction);
        ConfigurationSchemaDTO schema = adapterTypeService.getConfigurationSchema(id, direction);

        return ResponseEntity.ok(ApiResponse.success(schema));
    }

    @PostMapping
    @Operation(summary = "Register new adapter type")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AdapterType>> registerAdapterType(@RequestBody AdapterTypeDTO adapterTypeDTO) {
        log.info("Registering new adapter type: {}", adapterTypeDTO.getCode());
        AdapterType adapterType = adapterTypeService.registerAdapterType(adapterTypeDTO);

        return ResponseEntity.ok(ApiResponse.success(adapterType, "Adapter type registered successfully"));
    }

    @PutMapping("/ {id}")
    @Operation(summary = "Update adapter type")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateAdapterType(
            @PathVariable UUID id,
            @RequestBody AdapterTypeDTO adapterTypeDTO) {

        log.info("Updating adapter type with id: {}", id);
        adapterTypeService.updateAdapterType(id, adapterTypeDTO);

        return ResponseEntity.ok(ApiResponse.success(null, "Adapter type updated successfully"));
    }
}
