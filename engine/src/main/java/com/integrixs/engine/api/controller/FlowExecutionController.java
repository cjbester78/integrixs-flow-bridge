package com.integrixs.engine.api.controller;

import com.integrixs.engine.api.dto.FlowExecutionRequestDTO;
import com.integrixs.engine.api.dto.FlowExecutionResponseDTO;
import com.integrixs.engine.application.service.FlowExecutionApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for flow execution operations
 */
@RestController("engineFlowExecutionController")
@RequestMapping("/api/engine/flow-execution")
@Tag(name = "Flow Execution", description = "Operations for executing integration flows")
public class FlowExecutionController {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionController.class);


    private final FlowExecutionApplicationService flowExecutionApplicationService;

    public FlowExecutionController(FlowExecutionApplicationService flowExecutionApplicationService) {
        this.flowExecutionApplicationService = flowExecutionApplicationService;
    }

    /**
     * Execute a flow
     * @param flowId Flow ID
     * @param request Execution request
     * @return Execution response
     */
    @PostMapping("/execute/ {flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Execute an integration flow")
    public ResponseEntity<FlowExecutionResponseDTO> executeFlow(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Valid @RequestBody FlowExecutionRequestDTO request) {

        log.info("Executing flow: {}", flowId);
        request.setFlowId(flowId);

        if(request.getExecutionId() == null) {
            request.setExecutionId(UUID.randomUUID().toString());
        }

        FlowExecutionResponseDTO response = flowExecutionApplicationService.executeFlow(request);

        return response.isSuccess() ?
            ResponseEntity.ok(response) :
            ResponseEntity.internalServerError().body(response);
    }

    /**
     * Execute a flow asynchronously
     * @param flowId Flow ID
     * @param request Execution request
     * @return Future with execution response
     */
    @PostMapping("/execute - async/ {flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Execute an integration flow asynchronously")
    public CompletableFuture<ResponseEntity<FlowExecutionResponseDTO>> executeFlowAsync(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Valid @RequestBody FlowExecutionRequestDTO request) {

        log.info("Executing flow asynchronously: {}", flowId);
        request.setFlowId(flowId);
        request.setAsync(true);

        if(request.getExecutionId() == null) {
            request.setExecutionId(UUID.randomUUID().toString());
        }

        return flowExecutionApplicationService.executeFlowAsync(request)
                .thenApply(response ->
                    response.isSuccess() ?
                        ResponseEntity.ok(response) :
                        ResponseEntity.internalServerError().body(response)
               );
    }

    /**
     * Process a message through a flow
     * @param flowId Flow ID
     * @param message Message body
     * @return Execution response
     */
    @PostMapping("/process - message/ {flowId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Process a message through a flow")
    public ResponseEntity<FlowExecutionResponseDTO> processMessage(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @RequestBody Object message) {

        log.info("Processing message through flow: {}", flowId);

        FlowExecutionResponseDTO response = flowExecutionApplicationService.processMessage(flowId, message);

        return response.isSuccess() ?
            ResponseEntity.ok(response) :
            ResponseEntity.internalServerError().body(response);
    }

    /**
     * Check if a flow is ready for execution
     * @param flowId Flow ID
     * @return Readiness status
     */
    @GetMapping("/{flowId}/ready")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Operation(summary = "Check if a flow is ready for execution")
    public ResponseEntity<Map<String, Object>> checkFlowReady(
            @Parameter(description = "Flow ID") @PathVariable String flowId) {

        log.info("Checking readiness for flow: {}", flowId);
        boolean ready = flowExecutionApplicationService.isFlowReady(flowId);

        Map<String, Object> status = Map.of(
            "flowId", flowId,
            "ready", ready,
            "timestamp", System.currentTimeMillis()
       );

        return ResponseEntity.ok(status);
    }

    /**
     * Test flow execution
     * @param flowId Flow ID
     * @param testData Test data
     * @return Test result
     */
    @PostMapping("/{flowId}/test")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @Operation(summary = "Test flow execution with sample data")
    public ResponseEntity<FlowExecutionResponseDTO> testFlow(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @RequestBody Map<String, Object> testData) {

        log.info("Testing flow: {}", flowId);

        FlowExecutionRequestDTO request = new FlowExecutionRequestDTO();
        request.setFlowId(flowId);
        request.setExecutionId("TEST-" + UUID.randomUUID());
        request.setMessage(testData.get("message"));
        request.getMetadata().put("testMode", true);
        request.getMetadata().put("testTimestamp", System.currentTimeMillis());

        FlowExecutionResponseDTO response = flowExecutionApplicationService.executeFlow(request);

        return ResponseEntity.ok(response);
    }
}
