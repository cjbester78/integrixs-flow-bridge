package com.integrixs.backend.controller;

import com.integrixs.backend.dto.dashboard.health.*;
import com.integrixs.backend.service.AdapterHealthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * REST controller for adapter health monitoring.
 */
@RestController
@RequestMapping("/api/adapter-health")
public class AdapterHealthController {

    private final AdapterHealthService adapterHealthService;

    public AdapterHealthController(AdapterHealthService adapterHealthService) {
        this.adapterHealthService = adapterHealthService;
    }

    /**
     * Get the main health dashboard.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdapterHealthDashboard> getHealthDashboard() {
        return ResponseEntity.ok(adapterHealthService.getHealthDashboard());
    }

    /**
     * Get detailed health information for a specific adapter.
     */
    @GetMapping("/{adapterId}")
    public ResponseEntity<AdapterHealthDetail> getAdapterHealth(@PathVariable String adapterId) {
        return ResponseEntity.ok(adapterHealthService.getAdapterHealth(adapterId));
    }

    /**
     * Perform health check on an adapter.
     */
    @PostMapping("/{adapterId}/check")
    public ResponseEntity<HealthCheckResult> performHealthCheck(@PathVariable String adapterId) {
        return ResponseEntity.ok(adapterHealthService.performHealthCheck(adapterId));
    }

    /**
     * Get health history for an adapter.
     */
    @GetMapping("/{adapterId}/history")
    public ResponseEntity<AdapterHealthHistory> getHealthHistory(
            @PathVariable String adapterId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(adapterHealthService.getHealthHistory(adapterId, days));
    }

    /**
     * Get recovery suggestions for an adapter.
     */
    @GetMapping("/{adapterId}/recovery-suggestions")
    public ResponseEntity<List<RecoverySuggestion>> getRecoverySuggestions(@PathVariable String adapterId) {
        return ResponseEntity.ok(adapterHealthService.getRecoverySuggestions(adapterId));
    }

    /**
     * Get connection pool metrics for an adapter.
     */
    @GetMapping("/{adapterId}/connection-pool")
    public ResponseEntity<ConnectionPoolMetrics> getConnectionPoolMetrics(@PathVariable String adapterId) {
        return ResponseEntity.ok(adapterHealthService.getConnectionPoolMetrics(adapterId));
    }

    /**
     * Get resource usage metrics for an adapter.
     */
    @GetMapping("/{adapterId}/resource-usage")
    public ResponseEntity<ResourceUsageMetrics> getResourceUsageMetrics(@PathVariable String adapterId) {
        return ResponseEntity.ok(adapterHealthService.getResourceUsageMetrics(adapterId));
    }

    /**
     * Stream real - time health updates.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AdapterHealthDashboard> streamHealthUpdates() {
        return Flux.interval(Duration.ofSeconds(5))
            .map(sequence -> adapterHealthService.getHealthDashboard());
    }

    /**
     * Update connection pool metrics(called by adapters).
     */
    @PostMapping("/{adapterId}/connection-pool/update")
    public ResponseEntity<Void> updateConnectionPoolMetrics(
            @PathVariable String adapterId,
            @RequestParam int active,
            @RequestParam int idle,
            @RequestParam int waiting) {
        adapterHealthService.updateConnectionPoolMetrics(adapterId, active, idle, waiting);
        return ResponseEntity.ok().build();
    }

    /**
     * Update resource usage metrics(called by adapters).
     */
    @PostMapping("/{adapterId}/resource-usage/update")
    public ResponseEntity<Void> updateResourceUsageMetrics(
            @PathVariable String adapterId,
            @RequestParam double cpuUsage,
            @RequestParam long memoryMB,
            @RequestParam int threads,
            @RequestParam int fileHandles,
            @RequestParam double bandwidthKBps) {
        adapterHealthService.updateResourceUsageMetrics(
            adapterId, cpuUsage, memoryMB, threads, fileHandles, bandwidthKBps);
        return ResponseEntity.ok().build();
    }
}
