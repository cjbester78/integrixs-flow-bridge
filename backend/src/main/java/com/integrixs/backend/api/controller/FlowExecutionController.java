package com.integrixs.backend.api.controller;

import com.integrixs.backend.application.service.FlowExecutionApplicationService;
import com.integrixs.backend.application.service.OrchestrationApplicationService;
import com.integrixs.backend.api.dto.OrchestrationExecutionDTO;
import com.integrixs.backend.logging.BusinessOperation;
import com.integrixs.shared.dto.RecentIntegrationFlowDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * REST controller for flow execution operations
 * Handles asynchronous flow execution requests
 */
@RestController
@RequestMapping("/api/flow-executions")
@CrossOrigin(origins = "*")
public class FlowExecutionController {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionController.class);


    private final FlowExecutionApplicationService flowExecutionApplicationService;
    private final OrchestrationApplicationService orchestrationApplicationService;

    public FlowExecutionController(FlowExecutionApplicationService flowExecutionApplicationService,
                                 OrchestrationApplicationService orchestrationApplicationService) {
        this.flowExecutionApplicationService = flowExecutionApplicationService;
        this.orchestrationApplicationService = orchestrationApplicationService;
    }

    /**
     * Execute a flow asynchronously
     * @param flowId The ID of the flow to execute
     * @return Acknowledgment response
     */
    @PostMapping("/execute/{flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @BusinessOperation(value = "FLOW.EXECUTE", module = "FlowEngine")
    public ResponseEntity<ExecuteFlowResponse> executeFlow(@PathVariable @NotBlank String flowId) {
        log.info("Received request to execute flow: {}", flowId);

        try {
            // Trigger async execution
            flowExecutionApplicationService.executeFlow(flowId);

            ExecuteFlowResponse response = new ExecuteFlowResponse(
                "accepted",
                "Flow execution has been triggered",
                flowId,
                System.currentTimeMillis()
           );

            return ResponseEntity.accepted().body(response);
        } catch(IllegalArgumentException e) {
            log.error("Invalid flow ID: {}", flowId, e);
            return ResponseEntity.badRequest().body(
                new ExecuteFlowResponse(
                    "error",
                    "Invalid flow ID: " + e.getMessage(),
                    flowId,
                    System.currentTimeMillis()
               )
           );
        } catch(Exception e) {
            log.error("Error triggering flow execution: {}", flowId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ExecuteFlowResponse(
                    "error",
                    "Failed to trigger flow execution: " + e.getMessage(),
                    flowId,
                    System.currentTimeMillis()
               )
           );
        }
    }

    /**
     * Bulk execute multiple flows
     * @param request The bulk execution request
     * @return Bulk execution response
     */
    @PostMapping("/execute/bulk")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @BusinessOperation(value = "FLOW.BULK_EXECUTE", module = "FlowEngine")
    public ResponseEntity<BulkExecuteResponse> executeBulkFlows(@RequestBody @Valid BulkExecuteRequest request) {
        log.info("Received request to execute {} flows", request.getFlowIds().size());

        BulkExecuteResponse response = new BulkExecuteResponse();

        for(String flowId : request.getFlowIds()) {
            try {
                flowExecutionApplicationService.executeFlow(flowId);
                response.addSuccess(flowId);
            } catch(Exception e) {
                log.error("Failed to execute flow: {}", flowId, e);
                response.addFailure(flowId, e.getMessage());
            }
        }

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Get recent flow executions
     * @param businessComponentId Optional business component filter
     * @param limit Maximum number of results
     * @return List of recent integration flows
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<RecentIntegrationFlowDTO>> getRecentFlowExecutions(
            @RequestParam(required = false) String businessComponentId,
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Getting recent flow executions for component: {}, limit: {}", businessComponentId, limit);
        
        try {
            List<RecentIntegrationFlowDTO> recentFlows = flowExecutionApplicationService.getRecentFlowExecutions(businessComponentId, limit);
            return ResponseEntity.ok(recentFlows);
        } catch (Exception e) {
            log.error("Error getting recent flow executions", e);
            return ResponseEntity.ok(List.of()); // Return empty list to prevent frontend errors
        }
    }

    /**
     * Get active orchestration executions with state machine states
     * @return List of active executions
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<OrchestrationExecutionDTO>> getActiveExecutions() {
        log.debug("Getting active orchestration executions");
        
        try {
            List<OrchestrationExecutionDTO> activeExecutions = orchestrationApplicationService.getActiveExecutions();
            return ResponseEntity.ok(activeExecutions);
        } catch (Exception e) {
            log.error("Error getting active executions", e);
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Get execution status for a specific flow execution with state machine information
     * @param executionId The execution ID
     * @return Execution status with state information
     */
    @GetMapping("/status/{executionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<OrchestrationExecutionDTO> getExecutionStatus(@PathVariable @NotBlank String executionId) {
        log.debug("Getting execution status for: {}", executionId);
        
        try {
            return orchestrationApplicationService.getExecutionStatus(executionId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting execution status for: {}", executionId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get execution history for a specific flow with state machine transitions
     * @param flowId The flow ID
     * @param limit Maximum number of results
     * @return List of executions with state information
     */
    @GetMapping("/history/{flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<OrchestrationExecutionDTO>> getExecutionHistory(
            @PathVariable @NotBlank String flowId,
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Getting execution history for flow: {}, limit: {}", flowId, limit);
        
        try {
            List<OrchestrationExecutionDTO> history = orchestrationApplicationService.getExecutionHistory(flowId, limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting execution history for flow: {}", flowId, e);
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Request DTO for bulk flow execution
     */
    public static class BulkExecuteRequest {
        @NotBlank(message = "Flow IDs cannot be empty")
        private java.util.List<@NotBlank String> flowIds;

        public java.util.List<String> getFlowIds() {
            return flowIds;
        }

        public void setFlowIds(java.util.List<String> flowIds) {
            this.flowIds = flowIds;
        }
    }

    /**
     * Response DTO for flow execution
     */
    public static class ExecuteFlowResponse {
        private final String status;
        private final String message;
        private final String flowId;
        private final long timestamp;

        public ExecuteFlowResponse(String status, String message, String flowId, long timestamp) {
            this.status = status;
            this.message = message;
            this.flowId = flowId;
            this.timestamp = timestamp;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getFlowId() {
            return flowId;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Response DTO for bulk flow execution
     */
    public static class BulkExecuteResponse {
        private final java.util.List<String> succeeded = new java.util.ArrayList<>();
        private final java.util.Map<String, String> failed = new java.util.HashMap<>();
        private final long timestamp = System.currentTimeMillis();

        public void addSuccess(String flowId) {
            succeeded.add(flowId);
        }

        public void addFailure(String flowId, String reason) {
            failed.put(flowId, reason);
        }

        public java.util.List<String> getSucceeded() {
            return succeeded;
        }

        public java.util.Map<String, String> getFailed() {
            return failed;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public int getTotalCount() {
            return succeeded.size() + failed.size();
        }

        public int getSuccessCount() {
            return succeeded.size();
        }

        public int getFailureCount() {
            return failed.size();
        }
    }
}
