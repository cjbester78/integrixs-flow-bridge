package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.CreateFlowRequest;
import com.integrixs.backend.api.dto.request.UpdateFlowRequest;
import com.integrixs.backend.api.dto.response.FlowResponse;
import com.integrixs.backend.application.service.IntegrationFlowService;
import com.integrixs.backend.logging.BusinessOperation;
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
 * REST controller for integration flow management
 * Handles only HTTP concerns, delegates business logic to application service
 */
@RestController
@RequestMapping("/api/integration-flows")
@RequiredArgsConstructor
@Validated
@Slf4j
public class IntegrationFlowController {
    
    private final IntegrationFlowService integrationFlowService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "FLOW.LIST", module = "FlowManagement")
    public ResponseEntity<List<FlowResponse>> getAllFlows() {
        log.debug("Fetching all integration flows");
        List<FlowResponse> flows = integrationFlowService.getAllFlows();
        return ResponseEntity.ok(flows);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "FLOW.GET", module = "FlowManagement")
    public ResponseEntity<FlowResponse> getFlowById(@PathVariable String id) {
        log.debug("Fetching integration flow: {}", id);
        try {
            FlowResponse flow = integrationFlowService.getFlowById(id);
            return ResponseEntity.ok(flow);
        } catch (RuntimeException e) {
            log.error("Flow not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "FLOW.CREATE", module = "FlowManagement", logInput = true)
    public ResponseEntity<FlowResponse> createFlow(@Valid @RequestBody CreateFlowRequest request) {
        log.debug("Creating new integration flow: {}", request.getName());
        try {
            FlowResponse flow = integrationFlowService.createFlow(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(flow);
        } catch (IllegalArgumentException e) {
            log.error("Invalid flow data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    @BusinessOperation(value = "FLOW.UPDATE", module = "FlowManagement", logInput = true)
    public ResponseEntity<FlowResponse> updateFlow(
            @PathVariable String id, 
            @Valid @RequestBody UpdateFlowRequest request) {
        log.debug("Updating integration flow: {}", id);
        try {
            FlowResponse flow = integrationFlowService.updateFlow(id, request);
            return ResponseEntity.ok(flow);
        } catch (RuntimeException e) {
            log.error("Error updating flow: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER')")
    @BusinessOperation(value = "FLOW.DELETE", module = "FlowManagement")
    public ResponseEntity<Void> deleteFlow(@PathVariable String id) {
        log.debug("Deleting integration flow: {}", id);
        try {
            integrationFlowService.deleteFlow(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting flow: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
}