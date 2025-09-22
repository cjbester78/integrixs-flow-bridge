package com.integrixs.backend.api.controller;

import com.integrixs.backend.logging.BusinessOperation;
import com.integrixs.backend.logging.EnhancedFlowExecutionLogger;
import com.integrixs.backend.logging.EnhancedFlowExecutionLogger.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test controller to verify enhanced logging functionality.
 */
@RestController
@RequestMapping("/api/logging - test")
@CrossOrigin(origins = "*")
public class LoggingTestController {

    private static final Logger log = LoggerFactory.getLogger(LoggingTestController.class);


    private final EnhancedFlowExecutionLogger flowLogger;

    public LoggingTestController(EnhancedFlowExecutionLogger flowLogger) {
        this.flowLogger = flowLogger;
    }

    @GetMapping("/test - business - operation")
    @BusinessOperation(value = "TEST.OPERATION", module = "TestModule", includeMetrics = true)
    public ResponseEntity<Map<String, String>> testBusinessOperation() {
        log.info("Inside test business operation");

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Business operation logging test completed");
        response.put("correlationId", UUID.randomUUID().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test - flow - logging")
    public ResponseEntity<Map<String, String>> testFlowLogging() {
        String correlationId = UUID.randomUUID().toString();
        String flowId = UUID.randomUUID().toString();

        // Test flow start
        FlowExecutionContext context = FlowExecutionContext.builder()
            .flowId(flowId)
            .flowName("Test Flow")
            .flowVersion("1.0")
            .sourceSystem("TestSource")
            .targetSystem("TestTarget")
            .correlationId(correlationId)
            .messageId("MSG-" + UUID.randomUUID())
            .payloadSize(1024)
            .build();

        flowLogger.logFlowStart(context);

        // Test transformation
        TransformationContext transformContext = TransformationContext.builder()
            .stepNumber(1)
            .totalSteps(1)
            .transformationName("Test Transformation")
            .transformationType("MAPPING")
            .inputFormat("JSON")
            .outputFormat("XML")
            .build();

        flowLogger.logTransformationStep(transformContext);

        // Test flow complete
        flowLogger.logFlowComplete(context, 150);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Flow logging test completed");
        response.put("correlationId", correlationId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test - error - logging")
    @BusinessOperation(value = "TEST.ERROR", module = "TestModule", logInput = true, logOutput = false)
    public ResponseEntity<Map<String, String>> testErrorLogging(@RequestBody Map<String, String> request) {
        try {
            // Simulate an error
            throw new RuntimeException("Test error for logging verification");
        } catch(RuntimeException e) {
            log.error("Caught test error", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}
