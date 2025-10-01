package com.integrixs.backend.resilience;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for circuit breaker management.
 */
@RestController
@RequestMapping("/api/v1/circuit - breakers")
@Tag(name = "Circuit Breakers", description = "Circuit breaker management API")
public class CircuitBreakerController {

    private final CircuitBreakerService circuitBreakerService;

    public CircuitBreakerController(CircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }

    @GetMapping
    @Operation(summary = "Get all circuit breaker statuses")
    public ResponseEntity<Map<String, CircuitBreakerService.CircuitBreakerHealthStatus>> getAllStatuses() {
        return ResponseEntity.ok(circuitBreakerService.getAllStatuses());
    }

    @GetMapping("/ {adapterType}/ {adapterId}")
    @Operation(summary = "Get circuit breaker status for specific adapter")
    public ResponseEntity<CircuitBreakerService.CircuitBreakerHealthStatus> getStatus(
            @PathVariable String adapterType,
            @PathVariable String adapterId) {
        return ResponseEntity.ok(circuitBreakerService.getHealthStatus(adapterType, adapterId));
    }

    @PostMapping("/ {adapterType}/ {adapterId}/reset")
    @Operation(summary = "Reset circuit breaker")
    public ResponseEntity<Void> resetCircuitBreaker(
            @PathVariable String adapterType,
            @PathVariable String adapterId) {
        circuitBreakerService.resetCircuitBreaker(adapterType, adapterId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ {adapterType}/ {adapterId}/force - open")
    @Operation(summary = "Force circuit breaker to open state")
    public ResponseEntity<Void> forceOpen(
            @PathVariable String adapterType,
            @PathVariable String adapterId) {
        circuitBreakerService.forceOpen(adapterType, adapterId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ {adapterType}/ {adapterId}/is - open")
    @Operation(summary = "Check if circuit breaker is open")
    public ResponseEntity<Boolean> isOpen(
            @PathVariable String adapterType,
            @PathVariable String adapterId) {
        return ResponseEntity.ok(circuitBreakerService.isCircuitBreakerOpen(adapterType, adapterId));
    }
}
