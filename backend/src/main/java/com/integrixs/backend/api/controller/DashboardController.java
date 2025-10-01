package com.integrixs.backend.api.controller;

import com.integrixs.backend.application.service.DashboardApplicationService;
import com.integrixs.shared.dto.DashboardStatsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for dashboard metrics and statistics
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Dashboard", description = "Dashboard metrics and statistics")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);


    private final DashboardApplicationService dashboardService;

    public DashboardController(DashboardApplicationService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Get dashboard statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(
            @Parameter(description = "Filter by business component ID")
            @RequestParam(required = false) String businessComponentId) {
        log.debug("Getting dashboard stats for component: {}", businessComponentId);

        DashboardStatsDTO stats = dashboardService.getDashboardStats(businessComponentId);

        return ResponseEntity.ok(stats);
    }

    /**
     * Get dashboard metrics(alias for stats)
     */
    @GetMapping("/metrics")
    @Operation(summary = "Get dashboard metrics")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<DashboardStatsDTO> getDashboardMetrics(
            @Parameter(description = "Filter by business component ID")
            @RequestParam(required = false) String businessComponentId) {
        log.debug("Getting dashboard metrics for component: {}", businessComponentId);

        DashboardStatsDTO metrics = dashboardService.getDashboardMetrics(businessComponentId);

        return ResponseEntity.ok(metrics);
    }
}
