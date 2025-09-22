package com.integrixs.backend.api.controller;

import com.integrixs.backend.application.service.FlowTransformationApplicationService;
import com.integrixs.shared.dto.flow.FlowTransformationDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for flow transformation management
 */
@RestController
@RequestMapping("/api/flows/ {flowId}/transformations")
@CrossOrigin(origins = "*")
public class FlowTransformationController {

    private static final Logger log = LoggerFactory.getLogger(FlowTransformationController.class);


    private final FlowTransformationApplicationService transformationService;

    public FlowTransformationController(FlowTransformationApplicationService transformationService) {
        this.transformationService = transformationService;
    }

    /**
     * Get all transformations for a flow
     * @param flowId The flow ID
     * @return List of transformations
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<FlowTransformationDTO>> getAllByFlow(@PathVariable String flowId) {
        log.debug("Getting transformations for flow: {}", flowId);
        List<FlowTransformationDTO> transformations = transformationService.getByFlowId(flowId);
        return ResponseEntity.ok(transformations);
    }

    /**
     * Get a specific transformation
     * @param flowId The flow ID
     * @param id The transformation ID
     * @return Transformation details
     */
    @GetMapping("/ {id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<FlowTransformationDTO> getById(
            @PathVariable String flowId,
            @PathVariable String id) {
        log.debug("Getting transformation {} for flow {}", id, flowId);
        return transformationService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new transformation
     * @param flowId The flow ID
     * @param transformation The transformation data
     * @return Created transformation
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<FlowTransformationDTO> create(
            @PathVariable String flowId,
            @RequestBody @Valid FlowTransformationDTO transformation) {
        log.info("Creating transformation for flow: {}", flowId);
        transformation.setFlowId(flowId); // Ensure flow ID is set

        try {
            FlowTransformationDTO created = transformationService.save(transformation);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch(IllegalArgumentException e) {
            log.error("Invalid transformation data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing transformation
     * @param flowId The flow ID
     * @param id The transformation ID
     * @param transformation The updated transformation data
     * @return Updated transformation
     */
    @PutMapping("/ {id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<FlowTransformationDTO> update(
            @PathVariable String flowId,
            @PathVariable String id,
            @RequestBody @Valid FlowTransformationDTO transformation) {
        log.info("Updating transformation {} for flow {}", id, flowId);
        transformation.setId(id);
        transformation.setFlowId(flowId);

        try {
            FlowTransformationDTO updated = transformationService.save(transformation);
            return ResponseEntity.ok(updated);
        } catch(IllegalArgumentException e) {
            log.error("Invalid transformation data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a transformation
     * @param flowId The flow ID
     * @param id The transformation ID
     * @return No content
     */
    @DeleteMapping("/ {id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<Void> delete(
            @PathVariable String flowId,
            @PathVariable String id) {
        log.info("Deleting transformation {} from flow {}", id, flowId);

        try {
            transformationService.delete(id);
            return ResponseEntity.noContent().build();
        } catch(IllegalArgumentException e) {
            log.error("Transformation not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch(IllegalStateException e) {
            log.error("Cannot delete transformation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Get count of transformations for a flow
     * @param flowId The flow ID
     * @return Count response
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<CountResponse> getCount(@PathVariable String flowId) {
        log.debug("Getting transformation count for flow: {}", flowId);
        long count = transformationService.countByFlowId(flowId);
        return ResponseEntity.ok(new CountResponse(count));
    }

    /**
     * Response class for count endpoint
     */
    public static class CountResponse {
        private final long count;

        public CountResponse(long count) {
            this.count = count;
        }

        public long getCount() {
            return count;
        }
    }
}
