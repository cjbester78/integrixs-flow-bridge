package com.integrixs.backend.api.controller;

import com.integrixs.backend.application.service.DashboardApplicationService;
import com.integrixs.shared.dto.DashboardStatsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for dashboard metrics and statistics
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard metrics and statistics")
public class DashboardController {
    
    private final DashboardApplicationService dashboardService;
    
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
     * Get dashboard metrics (alias for stats)
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