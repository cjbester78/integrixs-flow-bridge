package com.integrixs.backend.controller;

import com.integrixs.backend.dto.dashboard.ComponentPerformance;
import com.integrixs.backend.dto.dashboard.PerformanceSnapshot;
import com.integrixs.backend.dto.dashboard.RealTimeMetrics;
import com.integrixs.backend.service.PerformanceDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for performance dashboard endpoints.
 */
@RestController
@RequestMapping("/api/v1/dashboard/performance")
@Tag(name = "Performance Dashboard", description = "Real - time performance monitoring")
public class PerformanceDashboardController {

    private final PerformanceDashboardService dashboardService;

    public PerformanceDashboardController(PerformanceDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Get real - time performance metrics.
     */
    @GetMapping("/real - time")
    @Operation(summary = "Get real - time performance metrics")
    public ResponseEntity<RealTimeMetrics> getRealTimeMetrics() {
        return ResponseEntity.ok(dashboardService.getRealTimeMetrics());
    }

    /**
     * Stream real - time metrics via Server - Sent Events.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream real - time metrics")
    public Flux<RealTimeMetrics> streamMetrics() {
        return Flux.interval(Duration.ofSeconds(5))
            .map(tick -> dashboardService.getRealTimeMetrics());
    }

    /**
     * Get historical performance snapshots.
     */
    @GetMapping("/history")
    @Operation(summary = "Get historical performance snapshots")
    public ResponseEntity<List<PerformanceSnapshot>> getHistoricalSnapshots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<PerformanceSnapshot> snapshots = dashboardService.getHistoricalSnapshots(startTime, endTime);
        return ResponseEntity.ok(snapshots);
    }

    /**
     * Get component - specific performance details.
     */
    @GetMapping("/component/ {componentId}")
    @Operation(summary = "Get component performance details")
    public ResponseEntity<ComponentPerformance> getComponentPerformance(
            @PathVariable String componentId) {

        ComponentPerformance performance = dashboardService.getComponentPerformance(componentId);
        return ResponseEntity.ok(performance);
    }

    /**
     * Get performance metrics for multiple components.
     */
    @PostMapping("/components")
    @Operation(summary = "Get performance for multiple components")
    public ResponseEntity<List<ComponentPerformance>> getMultipleComponentPerformance(
            @RequestBody List<String> componentIds) {

        List<ComponentPerformance> performances = componentIds.stream()
            .map(dashboardService::getComponentPerformance)
            .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(performances);
    }

    /**
     * Health check endpoint for dashboard.
     */
    @GetMapping("/health")
    @Operation(summary = "Dashboard health check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Dashboard service is healthy");
    }
}
