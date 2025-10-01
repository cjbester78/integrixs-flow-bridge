package com.integrixs.backend.resilience;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for resilience management including retry, bulkhead, and error classification.
 */
@RestController
@RequestMapping("/api/v1/resilience")
@Tag(name = "Resilience", description = "Resilience management API")
public class ResilienceController {

    private final RetryService retryService;
    private final BulkheadService bulkheadService;
    private final ErrorClassificationService errorClassificationService;

    public ResilienceController(RetryService retryService,
                               BulkheadService bulkheadService,
                               ErrorClassificationService errorClassificationService) {
        this.retryService = retryService;
        this.bulkheadService = bulkheadService;
        this.errorClassificationService = errorClassificationService;
    }

    // Retry endpoints

    @GetMapping("/retry/metrics")
    @Operation(summary = "Get all retry metrics")
    public ResponseEntity<Map<String, RetryService.RetryMetrics>> getRetryMetrics() {
        return ResponseEntity.ok(retryService.getAllMetrics());
    }

    @GetMapping("/retry/metrics/ {adapterType}/ {adapterId}")
    @Operation(summary = "Get retry metrics for specific adapter")
    public ResponseEntity<RetryService.RetryMetrics> getRetryMetrics(
            @PathVariable String adapterType,
            @PathVariable String adapterId) {
        return ResponseEntity.ok(retryService.getMetrics(adapterType, adapterId));
    }

    @GetMapping("/retry/config/ {adapterType}")
    @Operation(summary = "Get retry configuration for adapter type")
    public ResponseEntity<RetryService.RetryConfigInfo> getRetryConfig(@PathVariable String adapterType) {
        return ResponseEntity.ok(retryService.getRetryConfig(adapterType));
    }

    // Bulkhead endpoints

    @GetMapping("/bulkhead/metrics")
    @Operation(summary = "Get all bulkhead metrics")
    public ResponseEntity<Map<String, BulkheadService.BulkheadMetrics>> getBulkheadMetrics() {
        return ResponseEntity.ok(bulkheadService.getAllMetrics());
    }

    @GetMapping("/bulkhead/metrics/ {adapterType}/ {adapterId}")
    @Operation(summary = "Get bulkhead metrics for specific adapter")
    public ResponseEntity<BulkheadService.BulkheadMetrics> getBulkheadMetrics(
            @PathVariable String adapterType,
            @PathVariable String adapterId) {
        return ResponseEntity.ok(bulkheadService.getMetrics(adapterType, adapterId));
    }

    @GetMapping("/bulkhead/thread - pool/ {adapterType}/ {adapterId}")
    @Operation(summary = "Get thread pool bulkhead metrics")
    public ResponseEntity<BulkheadService.ThreadPoolBulkheadMetrics> getThreadPoolMetrics(
            @PathVariable String adapterType,
            @PathVariable String adapterId) {
        return ResponseEntity.ok(bulkheadService.getThreadPoolMetrics(adapterType, adapterId));
    }

    @GetMapping("/bulkhead/capacity/ {adapterType}/ {adapterId}")
    @Operation(summary = "Check if adapter has available capacity")
    public ResponseEntity<Boolean> hasCapacity(
            @PathVariable String adapterType,
            @PathVariable String adapterId) {
        return ResponseEntity.ok(bulkheadService.hasAvailableCapacity(adapterType, adapterId));
    }

    @GetMapping("/bulkhead/utilization/ {adapterType}/ {adapterId}")
    @Operation(summary = "Get adapter utilization percentage")
    public ResponseEntity<Double> getUtilization(
            @PathVariable String adapterType,
            @PathVariable String adapterId) {
        return ResponseEntity.ok(bulkheadService.getUtilizationPercentage(adapterType, adapterId));
    }

    // Error classification endpoints

    @PostMapping("/error/classify")
    @Operation(summary = "Classify an error by exception details")
    public ResponseEntity<ErrorClassificationService.ErrorClassification> classifyError(
            @RequestBody ErrorClassificationRequest request) {
        // Create a synthetic exception for classification
        Exception exception = createException(request.getExceptionClass(), request.getMessage());
        return ResponseEntity.ok(errorClassificationService.classify(exception));
    }

    @GetMapping("/error/categories")
    @Operation(summary = "Get all error categories")
    public ResponseEntity<ErrorClassificationService.ErrorCategory[]> getErrorCategories() {
        return ResponseEntity.ok(ErrorClassificationService.ErrorCategory.values());
    }

    @GetMapping("/error/severities")
    @Operation(summary = "Get all error severity levels")
    public ResponseEntity<ErrorClassificationService.ErrorSeverity[]> getErrorSeverities() {
        return ResponseEntity.ok(ErrorClassificationService.ErrorSeverity.values());
    }

    private Exception createException(String className, String message) {
        // Create appropriate exception based on class name
        try {
            return switch(className) {
                case "java.net.SocketTimeoutException" -> new java.net.SocketTimeoutException(message);
                case "java.net.ConnectException" -> new java.net.ConnectException(message);
                case "java.sql.SQLException" -> new java.sql.SQLException(message);
                case "java.io.IOException" -> new java.io.IOException(message);
                case "java.lang.IllegalArgumentException" -> new IllegalArgumentException(message);
                case "java.lang.SecurityException" -> new SecurityException(message);
                default -> new RuntimeException(message);
            };
        } catch(Exception e) {
            return new RuntimeException(message);
        }
    }

    public static class ErrorClassificationRequest {
        private String exceptionClass;
        private String message;

        // Getters and Setters
        public String getExceptionClass() {
            return exceptionClass;
        }

        public void setExceptionClass(String exceptionClass) {
            this.exceptionClass = exceptionClass;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
