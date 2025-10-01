package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.CreateTargetFieldMappingRequest;
import com.integrixs.backend.api.dto.request.UpdateTargetFieldMappingRequest;
import com.integrixs.backend.api.dto.response.TargetFieldMappingResponse;
import com.integrixs.backend.application.service.TargetFieldMappingService;
import com.integrixs.backend.logging.BusinessOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for target - specific field mapping management
 */
@RestController
@RequestMapping("/api/flows/{flowId}/targets/{targetId}/mappings")
@Tag(name = "Target Field Mappings", description = "Manage field mappings for orchestration targets")
public class TargetFieldMappingController {

    private static final Logger log = LoggerFactory.getLogger(TargetFieldMappingController.class);


    private final TargetFieldMappingService mappingService;

    public TargetFieldMappingController(TargetFieldMappingService mappingService) {
        this.mappingService = mappingService;
    }

    @GetMapping
    @Operation(summary = "Get all mappings for a target", description = "Retrieve all field mappings for a specific orchestration target")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "TARGET.MAPPING.LIST", module = "FieldMapping")
    public ResponseEntity<List<TargetFieldMappingResponse>> getTargetMappings(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId,
            @Parameter(description = "Include only active mappings") @RequestParam(defaultValue = "false") boolean activeOnly) {

        log.debug("Fetching field mappings for target: {} in flow: {}", targetId, flowId);
        List<TargetFieldMappingResponse> mappings = mappingService.getTargetMappings(flowId, targetId, activeOnly);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/{mappingId}")
    @Operation(summary = "Get mapping by ID", description = "Retrieve a specific field mapping")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "TARGET.MAPPING.GET", module = "FieldMapping")
    public ResponseEntity<TargetFieldMappingResponse> getMapping(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId,
            @Parameter(description = "Mapping ID") @PathVariable String mappingId) {

        log.debug("Fetching field mapping: {} for target: {}", mappingId, targetId);
        return mappingService.getMapping(flowId, targetId, mappingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create field mapping", description = "Create a new field mapping for a target")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "TARGET.MAPPING.CREATE", module = "FieldMapping", logInput = true)
    public ResponseEntity<TargetFieldMappingResponse> createMapping(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId,
            @Valid @RequestBody CreateTargetFieldMappingRequest request) {

        log.debug("Creating field mapping for target: {} in flow: {}", targetId, flowId);
        TargetFieldMappingResponse mapping = mappingService.createMapping(flowId, targetId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapping);
    }

    @PutMapping("/{mappingId}")
    @Operation(summary = "Update field mapping", description = "Update an existing field mapping")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "TARGET.MAPPING.UPDATE", module = "FieldMapping", logInput = true)
    public ResponseEntity<TargetFieldMappingResponse> updateMapping(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId,
            @Parameter(description = "Mapping ID") @PathVariable String mappingId,
            @Valid @RequestBody UpdateTargetFieldMappingRequest request) {

        log.debug("Updating field mapping: {} for target: {}", mappingId, targetId);
        return mappingService.updateMapping(flowId, targetId, mappingId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{mappingId}")
    @Operation(summary = "Delete field mapping", description = "Delete a field mapping from a target")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER')")
    @BusinessOperation(value = "TARGET.MAPPING.DELETE", module = "FieldMapping")
    public ResponseEntity<Void> deleteMapping(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId,
            @Parameter(description = "Mapping ID") @PathVariable String mappingId) {

        log.debug("Deleting field mapping: {} from target: {}", mappingId, targetId);
        boolean deleted = mappingService.deleteMapping(flowId, targetId, mappingId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple mappings", description = "Create multiple field mappings for a target")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "TARGET.MAPPING.BATCH_CREATE", module = "FieldMapping", logInput = true)
    public ResponseEntity<List<TargetFieldMappingResponse>> createMappings(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId,
            @Valid @RequestBody List<CreateTargetFieldMappingRequest> requests) {

        log.debug("Creating {} field mappings for target: {}", requests.size(), targetId);
        List<TargetFieldMappingResponse> mappings = mappingService.createMappings(flowId, targetId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(mappings);
    }

    @DeleteMapping
    @Operation(summary = "Delete all mappings", description = "Delete all field mappings for a target")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER')")
    @BusinessOperation(value = "TARGET.MAPPING.DELETE_ALL", module = "FieldMapping")
    public ResponseEntity<Void> deleteAllMappings(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId) {

        log.debug("Deleting all field mappings for target: {}", targetId);
        mappingService.deleteAllMappings(flowId, targetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{mappingId}/activate")
    @Operation(summary = "Activate mapping", description = "Activate a field mapping")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "TARGET.MAPPING.ACTIVATE", module = "FieldMapping")
    public ResponseEntity<TargetFieldMappingResponse> activateMapping(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId,
            @Parameter(description = "Mapping ID") @PathVariable String mappingId) {

        log.debug("Activating field mapping: {} for target: {}", mappingId, targetId);
        return mappingService.activateMapping(flowId, targetId, mappingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{mappingId}/deactivate")
    @Operation(summary = "Deactivate mapping", description = "Deactivate a field mapping")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "TARGET.MAPPING.DEACTIVATE", module = "FieldMapping")
    public ResponseEntity<TargetFieldMappingResponse> deactivateMapping(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId,
            @Parameter(description = "Mapping ID") @PathVariable String mappingId) {

        log.debug("Deactivating field mapping: {} for target: {}", mappingId, targetId);
        return mappingService.deactivateMapping(flowId, targetId, mappingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/reorder")
    @Operation(summary = "Reorder mappings", description = "Update the execution order of field mappings")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "TARGET.MAPPING.REORDER", module = "FieldMapping", logInput = true)
    public ResponseEntity<List<TargetFieldMappingResponse>> reorderMappings(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId,
            @RequestBody List<MappingOrderRequest> orderRequests) {

        log.debug("Reordering field mappings for target: {}", targetId);
        List<TargetFieldMappingResponse> mappings = mappingService.reorderMappings(flowId, targetId, orderRequests);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate mappings", description = "Validate all mappings for a target")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "TARGET.MAPPING.VALIDATE", module = "FieldMapping")
    public ResponseEntity<MappingValidationResult> validateMappings(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId) {

        log.debug("Validating field mappings for target: {}", targetId);
        MappingValidationResult result = mappingService.validateMappings(flowId, targetId);
        return ResponseEntity.ok(result);
    }

    /**
     * Request for updating mapping order
     */
    public static class MappingOrderRequest {
        private String mappingId;
        private Integer mappingOrder;

        public MappingOrderRequest() {
        }

        public MappingOrderRequest(String mappingId, Integer mappingOrder) {
            this.mappingId = mappingId;
            this.mappingOrder = mappingOrder;
        }

        public String getMappingId() {
            return mappingId;
        }

        public void setMappingId(String mappingId) {
            this.mappingId = mappingId;
        }

        public Integer getMappingOrder() {
            return mappingOrder;
        }

        public void setMappingOrder(Integer mappingOrder) {
            this.mappingOrder = mappingOrder;
        }
    }

    /**
     * Mapping validation result
     */
    public static class MappingValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        private Integer totalMappings;
        private Integer validMappings;
        private Integer requiredMappings;
        private Integer missingRequiredMappings;

        public MappingValidationResult() {
        }

        public MappingValidationResult(boolean valid, List<String> errors, List<String> warnings,
                                      Integer totalMappings, Integer validMappings,
                                      Integer requiredMappings, Integer missingRequiredMappings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
            this.totalMappings = totalMappings;
            this.validMappings = validMappings;
            this.requiredMappings = requiredMappings;
            this.missingRequiredMappings = missingRequiredMappings;
        }

        // Getters and setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }

        public Integer getTotalMappings() {
            return totalMappings;
        }

        public void setTotalMappings(Integer totalMappings) {
            this.totalMappings = totalMappings;
        }

        public Integer getValidMappings() {
            return validMappings;
        }

        public void setValidMappings(Integer validMappings) {
            this.validMappings = validMappings;
        }

        public Integer getRequiredMappings() {
            return requiredMappings;
        }

        public void setRequiredMappings(Integer requiredMappings) {
            this.requiredMappings = requiredMappings;
        }

        public Integer getMissingRequiredMappings() {
            return missingRequiredMappings;
        }

        public void setMissingRequiredMappings(Integer missingRequiredMappings) {
            this.missingRequiredMappings = missingRequiredMappings;
        }

        // Builder
        public static MappingValidationResultBuilder builder() {
            return new MappingValidationResultBuilder();
        }

        public static class MappingValidationResultBuilder {
            private boolean valid;
            private List<String> errors;
            private List<String> warnings;
            private Integer totalMappings;
            private Integer validMappings;
            private Integer requiredMappings;
            private Integer missingRequiredMappings;

            public MappingValidationResultBuilder valid(boolean valid) {
                this.valid = valid;
                return this;
            }

            public MappingValidationResultBuilder errors(List<String> errors) {
                this.errors = errors;
                return this;
            }

            public MappingValidationResultBuilder warnings(List<String> warnings) {
                this.warnings = warnings;
                return this;
            }

            public MappingValidationResultBuilder totalMappings(Integer totalMappings) {
                this.totalMappings = totalMappings;
                return this;
            }

            public MappingValidationResultBuilder validMappings(Integer validMappings) {
                this.validMappings = validMappings;
                return this;
            }

            public MappingValidationResultBuilder requiredMappings(Integer requiredMappings) {
                this.requiredMappings = requiredMappings;
                return this;
            }

            public MappingValidationResultBuilder missingRequiredMappings(Integer missingRequiredMappings) {
                this.missingRequiredMappings = missingRequiredMappings;
                return this;
            }

            public MappingValidationResult build() {
                return new MappingValidationResult(valid, errors, warnings, totalMappings,
                                                  validMappings, requiredMappings, missingRequiredMappings);
            }
        }
    }
}
