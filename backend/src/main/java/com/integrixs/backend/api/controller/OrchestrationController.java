package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.OrchestrationDTO;
import com.integrixs.backend.api.dto.OrchestrationExecutionDTO;
import com.integrixs.backend.api.dto.ValidationResultDTO;
import com.integrixs.backend.application.service.OrchestrationApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for orchestration operations
 */
@RestController
@RequestMapping("/api/orchestration")
@Tag(name = "Orchestration", description = "Orchestration flow execution endpoints")
public class OrchestrationController {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationController.class);


    private final OrchestrationApplicationService orchestrationApplicationService;

    public OrchestrationController(OrchestrationApplicationService orchestrationApplicationService) {
        this.orchestrationApplicationService = orchestrationApplicationService;
    }

    /**
     * Execute an orchestration flow synchronously
     */
    @PostMapping("/execute/{flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Execute orchestration flow", description = "Execute an orchestration flow synchronously")
    public ResponseEntity<OrchestrationDTO> executeFlow(
            @Parameter(description = "Flow ID", required = true)
            @PathVariable @NotBlank String flowId,
            @Parameter(description = "Input data for the flow")
            @RequestBody(required = false) Map<String, Object> inputData) {

        log.info("Executing orchestration flow: {}", flowId);
        OrchestrationDTO result = orchestrationApplicationService.executeOrchestrationFlow(flowId, inputData);

        if(result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Execute an orchestration flow asynchronously
     */
    @PostMapping("/execute-async/{flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Execute orchestration flow async", description = "Execute an orchestration flow asynchronously")
    public ResponseEntity<Map<String, String>> executeFlowAsync(
            @Parameter(description = "Flow ID", required = true)
            @PathVariable @NotBlank String flowId,
            @Parameter(description = "Input data for the flow")
            @RequestBody(required = false) Map<String, Object> inputData) {

        log.info("Executing orchestration flow asynchronously: {}", flowId);
        CompletableFuture<OrchestrationDTO> future = orchestrationApplicationService.executeOrchestrationFlowAsync(flowId, inputData);

        // Return immediately with execution ID
        String executionId = "async-" + System.currentTimeMillis();
        return ResponseEntity.accepted().body(Map.of(
            "message", "Orchestration flow execution started",
            "executionId", executionId
       ));
    }

    /**
     * Get execution status
     */
    @GetMapping("/execution/ {executionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Operation(summary = "Get execution status", description = "Get the status of an orchestration execution")
    public ResponseEntity<OrchestrationExecutionDTO> getExecutionStatus(
            @Parameter(description = "Execution ID", required = true)
            @PathVariable @NotBlank String executionId) {

        return orchestrationApplicationService.getExecutionStatus(executionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cancel an execution
     */
    @PostMapping("/execution/ {executionId}/cancel")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Cancel execution", description = "Cancel an active orchestration execution")
    public ResponseEntity<Map<String, Object>> cancelExecution(
            @Parameter(description = "Execution ID", required = true)
            @PathVariable @NotBlank String executionId) {

        log.info("Cancelling orchestration execution: {}", executionId);
        boolean cancelled = orchestrationApplicationService.cancelExecution(executionId);

        if(cancelled) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Execution cancelled successfully"
           ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Unable to cancel execution - it may have already completed or does not exist"
           ));
        }
    }

    /**
     * Validate orchestration flow
     */
    @GetMapping("/validate/ {flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Operation(summary = "Validate flow", description = "Validate an orchestration flow configuration")
    public ResponseEntity<ValidationResultDTO> validateFlow(
            @Parameter(description = "Flow ID", required = true)
            @PathVariable @NotBlank String flowId) {

        log.info("Validating orchestration flow: {}", flowId);
        ValidationResultDTO result = orchestrationApplicationService.validateOrchestrationFlow(flowId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get execution history for a flow
     */
    @GetMapping("/history/ {flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Operation(summary = "Get execution history", description = "Get execution history for a specific flow")
    public ResponseEntity<List<OrchestrationExecutionDTO>> getExecutionHistory(
            @Parameter(description = "Flow ID", required = true)
            @PathVariable @NotBlank String flowId,
            @Parameter(description = "Number of records to return")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {

        List<OrchestrationExecutionDTO> history = orchestrationApplicationService.getExecutionHistory(flowId, limit);
        return ResponseEntity.ok(history);
    }

    /**
     * Get all active executions
     */
    @GetMapping("/executions/active")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Operation(summary = "Get active executions", description = "Get all currently active orchestration executions")
    public ResponseEntity<List<OrchestrationExecutionDTO>> getActiveExecutions() {

        List<OrchestrationExecutionDTO> executions = orchestrationApplicationService.getActiveExecutions();
        return ResponseEntity.ok(executions);
    }

    /**
     * Test endpoint for orchestration health check
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check orchestration service health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Orchestration Service",
            "timestamp", String.valueOf(System.currentTimeMillis())
       ));
    }
}
