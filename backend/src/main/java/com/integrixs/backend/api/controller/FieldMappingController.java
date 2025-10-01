package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.CreateFieldMappingRequest;
import com.integrixs.backend.api.dto.request.UpdateFieldMappingRequest;
import com.integrixs.backend.api.dto.response.FieldMappingResponse;
import com.integrixs.backend.application.service.FieldMappingApplicationService;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.UserSqlRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for field mapping management
 */
@RestController
@RequestMapping("/api/transformations/ {transformationId}/mappings")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Field Mapping", description = "Field mapping management")
public class FieldMappingController {

    private static final Logger log = LoggerFactory.getLogger(FieldMappingController.class);


    private final FieldMappingApplicationService fieldMappingService;
    private final UserSqlRepository userRepository;

    public FieldMappingController(FieldMappingApplicationService fieldMappingService,
                                  UserSqlRepository userRepository) {
        this.fieldMappingService = fieldMappingService;
        this.userRepository = userRepository;
    }

    /**
     * Get all field mappings for a transformation
     */
    @GetMapping
    @Operation(summary = "Get all field mappings for a transformation")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<FieldMappingResponse>> getAllMappings(
            @PathVariable String transformationId) {

        log.debug("Getting all field mappings for transformation: {}", transformationId);
        List<FieldMappingResponse> mappings = fieldMappingService.getMappingsByTransformationId(transformationId);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Get a specific field mapping
     */
    @GetMapping("/ {id}")
    @Operation(summary = "Get a field mapping by ID")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<FieldMappingResponse> getMapping(
            @PathVariable String transformationId,
            @PathVariable String id) {

        log.debug("Getting field mapping: {} for transformation: {}", id, transformationId);
        FieldMappingResponse mapping = fieldMappingService.getMappingById(id);

        // Verify it belongs to the transformation
        if(!mapping.getTransformationId().equals(transformationId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapping);
    }

    /**
     * Create a new field mapping
     */
    @PostMapping
    @Operation(summary = "Create a new field mapping")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<FieldMappingResponse> createMapping(
            @PathVariable String transformationId,
            @Valid @RequestBody CreateFieldMappingRequest request) {

        log.info("Creating field mapping for transformation: {}", transformationId);

        String username = SecurityUtils.getCurrentUsernameStatic();
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        FieldMappingResponse mapping = fieldMappingService.createMapping(transformationId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapping);
    }

    /**
     * Update an existing field mapping
     */
    @PutMapping("/ {id}")
    @Operation(summary = "Update a field mapping")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<FieldMappingResponse> updateMapping(
            @PathVariable String transformationId,
            @PathVariable String id,
            @Valid @RequestBody UpdateFieldMappingRequest request) {

        log.info("Updating field mapping: {} for transformation: {}", id, transformationId);

        String username = SecurityUtils.getCurrentUsernameStatic();
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        FieldMappingResponse mapping = fieldMappingService.updateMapping(id, transformationId, request, currentUser);
        return ResponseEntity.ok(mapping);
    }

    /**
     * Delete a field mapping
     */
    @DeleteMapping("/ {id}")
    @Operation(summary = "Delete a field mapping")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<Void> deleteMapping(
            @PathVariable String transformationId,
            @PathVariable String id) {

        log.info("Deleting field mapping: {} for transformation: {}", id, transformationId);

        String username = SecurityUtils.getCurrentUsernameStatic();
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        fieldMappingService.deleteMapping(id, transformationId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Count field mappings for a transformation
     */
    @GetMapping("/count")
    @Operation(summary = "Count field mappings for a transformation")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<Long> countMappings(@PathVariable String transformationId) {
        log.debug("Counting field mappings for transformation: {}", transformationId);
        long count = fieldMappingService.countMappingsForTransformation(transformationId);
        return ResponseEntity.ok(count);
    }
}
