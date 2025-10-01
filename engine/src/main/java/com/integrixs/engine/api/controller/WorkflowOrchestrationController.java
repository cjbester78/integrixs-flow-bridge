package com.integrixs.engine.api.controller;

import com.integrixs.engine.api.dto.WorkflowExecutionRequestDTO;
import com.integrixs.engine.api.dto.WorkflowExecutionResponseDTO;
import com.integrixs.engine.application.service.ApplicationOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for workflow orchestration operations
 */
@RestController
@RequestMapping("/api/workflow-orchestration")
@Tag(name = "Workflow Orchestration", description = "Operations for orchestrating complex workflows")
public class WorkflowOrchestrationController {

    private static final Logger log = LoggerFactory.getLogger(WorkflowOrchestrationController.class);


    private final ApplicationOrchestrator applicationOrchestrator;

    public WorkflowOrchestrationController(ApplicationOrchestrator applicationOrchestrator) {
        this.applicationOrchestrator = applicationOrchestrator;
    }

    /**
     * Execute a complex workflow
     * @param request Workflow execution request
     * @param principal User principal
     * @return Workflow execution response
     */
    @PostMapping("/execute")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Execute a complex workflow")
    public ResponseEntity<WorkflowExecutionResponseDTO> executeWorkflow(
            @Valid @RequestBody WorkflowExecutionRequestDTO request,
            Principal principal) {

        log.info("Executing workflow for flow: {} by user: {}", request.getFlowId(), principal.getName());

        if(request.getInitiatedBy() == null) {
            request.setInitiatedBy(principal.getName());
        }

        WorkflowExecutionResponseDTO response = applicationOrchestrator.executeWorkflow(request);

        return response.isSuccess() ?
            ResponseEntity.ok(response) :
            ResponseEntity.internalServerError().body(response);
    }

    /**
     * Execute a workflow asynchronously
     * @param request Workflow execution request
     * @param principal User principal
     * @return Future with workflow response
     */
    @PostMapping("/execute - async")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Execute a workflow asynchronously")
    public CompletableFuture<ResponseEntity<WorkflowExecutionResponseDTO>> executeWorkflowAsync(
            @Valid @RequestBody WorkflowExecutionRequestDTO request,
            Principal principal) {

        log.info("Executing workflow asynchronously for flow: {} by user: {}",
                request.getFlowId(), principal.getName());

        request.setAsync(true);
        if(request.getInitiatedBy() == null) {
            request.setInitiatedBy(principal.getName());
        }

        return applicationOrchestrator.executeWorkflowAsync(request)
                .thenApply(response ->
                    response.isSuccess() ?
                        ResponseEntity.ok(response) :
                        ResponseEntity.internalServerError().body(response)
               );
    }

    /**
     * Execute a simple flow(adapter to adapter)
     * @param flowId Flow ID
     * @param inputData Input data
     * @return Workflow response
     */
    @PostMapping("/execute - simple/ {flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Execute a simple adapter - to - adapter flow")
    public ResponseEntity<WorkflowExecutionResponseDTO> executeSimpleFlow(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @RequestBody Object inputData) {

        log.info("Executing simple flow: {}", flowId);

        WorkflowExecutionResponseDTO response = applicationOrchestrator.executeSimpleFlow(flowId, inputData);

        return response.isSuccess() ?
            ResponseEntity.ok(response) :
            ResponseEntity.internalServerError().body(response);
    }

    /**
     * Get workflow status
     * @param workflowId Workflow ID
     * @return Workflow status
     */
    @GetMapping("/status/ {workflowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Operation(summary = "Get workflow execution status")
    public ResponseEntity<WorkflowExecutionResponseDTO> getWorkflowStatus(
            @Parameter(description = "Workflow ID") @PathVariable String workflowId) {

        log.info("Getting status for workflow: {}", workflowId);

        try {
            WorkflowExecutionResponseDTO response = applicationOrchestrator.getWorkflowStatus(workflowId);
            return ResponseEntity.ok(response);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cancel a running workflow
     * @param workflowId Workflow ID
     * @return Cancellation result
     */
    @PostMapping("/cancel/ {workflowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @Operation(summary = "Cancel a running workflow")
    public ResponseEntity<Map<String, Object>> cancelWorkflow(
            @Parameter(description = "Workflow ID") @PathVariable String workflowId) {

        log.info("Cancelling workflow: {}", workflowId);

        boolean cancelled = applicationOrchestrator.cancelWorkflow(workflowId);

        return ResponseEntity.ok(Map.of(
            "workflowId", workflowId,
            "cancelled", cancelled,
            "timestamp", System.currentTimeMillis()
       ));
    }

    /**
     * Suspend a running workflow
     * @param workflowId Workflow ID
     * @return Suspension result
     */
    @PostMapping("/suspend/ {workflowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @Operation(summary = "Suspend a running workflow")
    public ResponseEntity<Map<String, Object>> suspendWorkflow(
            @Parameter(description = "Workflow ID") @PathVariable String workflowId) {

        log.info("Suspending workflow: {}", workflowId);

        boolean suspended = applicationOrchestrator.suspendWorkflow(workflowId);

        return ResponseEntity.ok(Map.of(
            "workflowId", workflowId,
            "suspended", suspended,
            "timestamp", System.currentTimeMillis()
       ));
    }

    /**
     * Resume a suspended workflow
     * @param workflowId Workflow ID
     * @return Resume result
     */
    @PostMapping("/resume/ {workflowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @Operation(summary = "Resume a suspended workflow")
    public ResponseEntity<Map<String, Object>> resumeWorkflow(
            @Parameter(description = "Workflow ID") @PathVariable String workflowId) {

        log.info("Resuming workflow: {}", workflowId);

        boolean resumed = applicationOrchestrator.resumeWorkflow(workflowId);

        return ResponseEntity.ok(Map.of(
            "workflowId", workflowId,
            "resumed", resumed,
            "timestamp", System.currentTimeMillis()
       ));
    }

    /**
     * Check workflow health before execution
     * @param flowId Flow ID
     * @return Health check result
     */
    @GetMapping("/health - check/ {flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Operation(summary = "Check if a workflow is ready for execution")
    public ResponseEntity<Map<String, Object>> checkWorkflowHealth(
            @Parameter(description = "Flow ID") @PathVariable String flowId) {

        log.info("Checking health for workflow flow: {}", flowId);

        Map<String, Object> health = applicationOrchestrator.checkWorkflowHealth(flowId);

        return ResponseEntity.ok(health);
    }
}
