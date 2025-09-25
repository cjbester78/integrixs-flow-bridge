package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.RouteDTO;
import com.integrixs.backend.api.dto.RoutingDecisionDTO;
import com.integrixs.backend.api.dto.RouterConfigDTO;
import com.integrixs.backend.application.service.RoutingApplicationService;
import com.integrixs.backend.service.FlowContextService.FlowContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for conditional routing operations
 */
@RestController
@RequestMapping("/api/routing")
@Tag(name = "Routing", description = "Conditional routing and decision endpoints")
public class RoutingController {

    private static final Logger log = LoggerFactory.getLogger(RoutingController.class);

    private final RoutingApplicationService routingApplicationService;

    public RoutingController(RoutingApplicationService routingApplicationService) {
        this.routingApplicationService = routingApplicationService;
    }

    /**
     * Evaluate routing decision for a flow step
     */
    @PostMapping("/evaluate/ {flowId}/ {stepId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Evaluate routing", description = "Evaluate routing decision for a flow step")
    public ResponseEntity<RoutingDecisionDTO> evaluateRouting(
            @Parameter(description = "Flow ID", required = true)
            @PathVariable @NotBlank String flowId,
            @Parameter(description = "Step ID", required = true)
            @PathVariable @NotBlank String stepId,
            @Parameter(description = "Flow context", required = true)
            @RequestBody FlowContext context) {

        log.info("Evaluating routing for flow {} step {}", flowId, stepId);
        RoutingDecisionDTO decision = routingApplicationService.evaluateRouting(flowId, stepId, context);
        return ResponseEntity.ok(decision);
    }

    /**
     * Create a choice router configuration
     */
    @PostMapping("/routers/choice")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @Operation(summary = "Create choice router", description = "Create a choice router configuration")
    public ResponseEntity<RouterConfigDTO> createChoiceRouter(
            @Parameter(description = "Router configuration", required = true)
            @RequestBody @Valid Map<String, Object> request) {

        String routerId = (String) request.get("routerId");
        List<Map<String, Object>> choices = (List<Map<String, Object>>) request.get("choices");

        log.info("Creating choice router: {}", routerId);
        RouterConfigDTO config = routingApplicationService.createChoiceRouter(routerId, choices);
        return ResponseEntity.ok(config);
    }

    /**
     * Create a content - based router configuration
     */
    @PostMapping("/routers/content - based")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @Operation(summary = "Create content router", description = "Create a content - based router configuration")
    public ResponseEntity<RouterConfigDTO> createContentRouter(
            @Parameter(description = "Router configuration", required = true)
            @RequestBody @Valid Map<String, Object> request) {

        String routerId = (String) request.get("routerId");
        String extractionPath = (String) request.get("extractionPath");
        String sourceType = (String) request.get("sourceType");
        Map<String, String> routes = (Map<String, String>) request.get("routes");

        log.info("Creating content - based router: {}", routerId);
        RouterConfigDTO config = routingApplicationService.createContentRouter(
            routerId, extractionPath, sourceType, routes);
        return ResponseEntity.ok(config);
    }

    /**
     * Execute routing and get target steps
     */
    @PostMapping("/execute")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Execute routing", description = "Execute routing and return target steps")
    public ResponseEntity<CompletableFuture<List<String>>> executeRouting(
            @Parameter(description = "Router configuration", required = true)
            @RequestBody @Valid RouterConfigDTO config,
            @Parameter(description = "Flow context")
            @RequestParam(required = false) FlowContext context) {

        log.info("Executing routing for router: {}", config.getRouterId());
        CompletableFuture<List<String>> targets = routingApplicationService.executeRouting(config, context);
        return ResponseEntity.ok(targets);
    }

    /**
     * Get all routes for a flow
     */
    @GetMapping("/flows/ {flowId}/routes")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Operation(summary = "Get flow routes", description = "Get all routes configured for a flow")
    public ResponseEntity<List<RouteDTO>> getRoutesForFlow(
            @Parameter(description = "Flow ID", required = true)
            @PathVariable @NotBlank String flowId) {

        log.info("Getting routes for flow: {}", flowId);
        List<RouteDTO> routes = routingApplicationService.getRoutesForFlow(flowId);
        return ResponseEntity.ok(routes);
    }

    /**
     * Create a new route
     */
    @PostMapping("/flows/ {flowId}/routes")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @Operation(summary = "Create route", description = "Create a new route for a flow")
    public ResponseEntity<RouteDTO> createRoute(
            @Parameter(description = "Flow ID", required = true)
            @PathVariable @NotBlank String flowId,
            @Parameter(description = "Route data", required = true)
            @RequestBody @Valid RouteDTO routeDto) {

        log.info("Creating route for flow {}: {}", flowId, routeDto.getRouteName());
        RouteDTO created = routingApplicationService.createRoute(flowId, routeDto);
        return ResponseEntity.ok(created);
    }

    /**
     * Update an existing route
     */
    @PutMapping("/routes/ {routeId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @Operation(summary = "Update route", description = "Update an existing route")
    public ResponseEntity<RouteDTO> updateRoute(
            @Parameter(description = "Route ID", required = true)
            @PathVariable @NotBlank String routeId,
            @Parameter(description = "Updated route data", required = true)
            @RequestBody @Valid RouteDTO routeDto) {

        log.info("Updating route: {}", routeId);
        RouteDTO updated = routingApplicationService.updateRoute(routeId, routeDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a route
     */
    @DeleteMapping("/routes/ {routeId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    @Operation(summary = "Delete route", description = "Delete an existing route")
    public ResponseEntity<Void> deleteRoute(
            @Parameter(description = "Route ID", required = true)
            @PathVariable @NotBlank String routeId) {

        log.info("Deleting route: {}", routeId);
        routingApplicationService.deleteRoute(routeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Test endpoint for routing health check
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check routing service health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Routing Service",
            "timestamp", String.valueOf(System.currentTimeMillis())
       ));
    }
}
