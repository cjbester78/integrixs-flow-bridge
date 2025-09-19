package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.CreateOrchestrationTargetRequest;
import com.integrixs.backend.api.dto.request.UpdateOrchestrationTargetRequest;
import com.integrixs.backend.api.dto.response.OrchestrationTargetResponse;
import com.integrixs.backend.application.service.OrchestrationTargetService;
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
 * REST controller for orchestration target management
 */
@RestController
@RequestMapping("/api/flows/ {flowId}/targets")
@Tag(name = "Orchestration Targets", description = "Manage orchestration flow targets")
public class OrchestrationTargetController {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationTargetController.class);


    private final OrchestrationTargetService orchestrationTargetService;

    public OrchestrationTargetController(OrchestrationTargetService orchestrationTargetService) {
        this.orchestrationTargetService = orchestrationTargetService;
    }

    @GetMapping
    @Operation(summary = "Get all targets for a flow", description = "Retrieve all orchestration targets for a specific flow")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "ORCHESTRATION.TARGET.LIST", module = "OrchestrationManagement")
    public ResponseEntity<List<OrchestrationTargetResponse>> getFlowTargets(
            @Parameter(description = "Flow ID") @PathVariable String flowId) {

        log.debug("Fetching orchestration targets for flow: {}", flowId);
        List<OrchestrationTargetResponse> targets = orchestrationTargetService.getFlowTargets(flowId);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/ {targetId}")
    @Operation(summary = "Get target by ID", description = "Retrieve a specific orchestration target")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "ORCHESTRATION.TARGET.GET", module = "OrchestrationManagement")
    public ResponseEntity<OrchestrationTargetResponse> getTarget(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId) {

        log.debug("Fetching orchestration target: {} for flow: {}", targetId, flowId);
        return orchestrationTargetService.getTarget(flowId, targetId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Add target to flow", description = "Add a new orchestration target to a flow")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ORCHESTRATION.TARGET.CREATE", module = "OrchestrationManagement", logInput = true)
    public ResponseEntity<OrchestrationTargetResponse> addTarget(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Valid @RequestBody CreateOrchestrationTargetRequest request) {

        log.debug("Adding orchestration target to flow: {}", flowId);
        OrchestrationTargetResponse target = orchestrationTargetService.addTarget(flowId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(target);
    }

    @PutMapping("/ {targetId}")
    @Operation(summary = "Update target", description = "Update an existing orchestration target")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ORCHESTRATION.TARGET.UPDATE", module = "OrchestrationManagement", logInput = true)
    public ResponseEntity<OrchestrationTargetResponse> updateTarget(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId,
            @Valid @RequestBody UpdateOrchestrationTargetRequest request) {

        log.debug("Updating orchestration target: {} for flow: {}", targetId, flowId);
        return orchestrationTargetService.updateTarget(flowId, targetId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/ {targetId}")
    @Operation(summary = "Remove target", description = "Remove an orchestration target from a flow")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER')")
    @BusinessOperation(value = "ORCHESTRATION.TARGET.DELETE", module = "OrchestrationManagement")
    public ResponseEntity<Void> removeTarget(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId) {

        log.debug("Removing orchestration target: {} from flow: {}", targetId, flowId);
        boolean removed = orchestrationTargetService.removeTarget(flowId, targetId);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/ {targetId}/activate")
    @Operation(summary = "Activate target", description = "Activate an orchestration target")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ORCHESTRATION.TARGET.ACTIVATE", module = "OrchestrationManagement")
    public ResponseEntity<OrchestrationTargetResponse> activateTarget(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId) {

        log.debug("Activating orchestration target: {} for flow: {}", targetId, flowId);
        return orchestrationTargetService.activateTarget(flowId, targetId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ {targetId}/deactivate")
    @Operation(summary = "Deactivate target", description = "Deactivate an orchestration target")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ORCHESTRATION.TARGET.DEACTIVATE", module = "OrchestrationManagement")
    public ResponseEntity<OrchestrationTargetResponse> deactivateTarget(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Target ID") @PathVariable String targetId) {

        log.debug("Deactivating orchestration target: {} for flow: {}", targetId, flowId);
        return orchestrationTargetService.deactivateTarget(flowId, targetId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/reorder")
    @Operation(summary = "Reorder targets", description = "Update the execution order of orchestration targets")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "ORCHESTRATION.TARGET.REORDER", module = "OrchestrationManagement", logInput = true)
    public ResponseEntity<List<OrchestrationTargetResponse>> reorderTargets(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @RequestBody List<TargetOrderRequest> orderRequests) {

        log.debug("Reordering orchestration targets for flow: {}", flowId);
        List<OrchestrationTargetResponse> targets = orchestrationTargetService.reorderTargets(flowId, orderRequests);
        return ResponseEntity.ok(targets);
    }

    /**
     * Request for updating target order
     */
    public static class TargetOrderRequest {
        private String targetId;
        private Integer executionOrder;

        // Default constructor
        public TargetOrderRequest() {
        }

        // Getters and Setters
        public String getTargetId() {
            return targetId;
        }

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        public Integer getExecutionOrder() {
            return executionOrder;
        }

        public void setExecutionOrder(Integer executionOrder) {
            this.executionOrder = executionOrder;
        }
    }
}
