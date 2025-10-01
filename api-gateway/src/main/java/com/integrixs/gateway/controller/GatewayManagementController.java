package com.integrixs.gateway.controller;

import com.integrixs.gateway.routing.DynamicRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Controller for managing API gateway routes and configuration
 */
@RestController
@RequestMapping("/gateway/admin")
public class GatewayManagementController {

    @Autowired
    private DynamicRouteService routeService;

    @Autowired
    private RouteLocator routeLocator;

    /**
     * Get all routes
     */
    @GetMapping("/routes")
    public Flux<Map<String, Object>> getRoutes() {
        return routeLocator.getRoutes().map(route -> {
            Map<String, Object> routeInfo = new HashMap<>();
            routeInfo.put("id", route.getId());
            routeInfo.put("uri", route.getUri().toString());
            routeInfo.put("order", route.getOrder());
            routeInfo.put("predicate", route.getPredicate().toString());
            return routeInfo;
        });
    }

    /**
     * Get dynamic routes
     */
    @GetMapping("/routes/dynamic")
    public Mono<Map<String, RouteDefinition>> getDynamicRoutes() {
        return Mono.just(routeService.getAllRoutes());
    }

    /**
     * Add a new route
     */
    @PostMapping("/routes")
    public Mono<ResponseEntity<Map<String, String>>> addRoute(@RequestBody RouteRequest request) {
        try {
            routeService.addRoute(
                request.getId(),
                request.getPath(),
                request.getUri(),
                request.getPredicates() != null ? request.getPredicates() : new HashMap<>(),
                request.getFilters() != null ? request.getFilters() : new ArrayList<>()
           );

            return Mono.just(ResponseEntity.ok(Map.of(
                "message", "Route added successfully",
                "id", request.getId()
           )));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to add route",
                "message", e.getMessage()
           )));
        }
    }

    /**
     * Update a route
     */
    @PutMapping("/routes/{id}")
    public Mono<ResponseEntity<Map<String, String>>> updateRoute(
            @PathVariable String id,
            @RequestParam String uri) {
        routeService.updateRoute(id, uri);
        return Mono.just(ResponseEntity.ok(Map.of(
            "message", "Route updated successfully",
            "id", id
       )));
    }

    /**
     * Delete a route
     */
    @DeleteMapping("/routes/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteRoute(@PathVariable String id) {
        routeService.removeRoute(id);
        return Mono.just(ResponseEntity.ok(Map.of(
            "message", "Route deleted successfully",
            "id", id
       )));
    }

    /**
     * Register a new microservice
     */
    @PostMapping("/microservices/register")
    public Mono<ResponseEntity<Map<String, String>>> registerMicroservice(
            @RequestBody MicroserviceRegistration registration) {
        try {
            routeService.registerMicroservice(
                registration.getName(),
                registration.getPath(),
                registration.getUrl(),
                registration.isRequiresAuth(),
                registration.getRateLimit()
           );

            return Mono.just(ResponseEntity.ok(Map.of(
                "message", "Microservice registered successfully",
                "name", registration.getName()
           )));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to register microservice",
                "message", e.getMessage()
           )));
        }
    }

    /**
     * Refresh routes
     */
    @PostMapping("/routes/refresh")
    public Mono<ResponseEntity<Map<String, String>>> refreshRoutes() {
        routeService.loadRoutes();
        return Mono.just(ResponseEntity.ok(Map.of(
            "message", "Routes refreshed successfully",
            "timestamp", new Date().toString()
       )));
    }

    // Request DTOs

    static class RouteRequest {
        private String id;
        private String path;
        private String uri;
        private Map<String, String> predicates;
        private List<String> filters;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }

        public Map<String, String> getPredicates() { return predicates; }
        public void setPredicates(Map<String, String> predicates) { this.predicates = predicates; }

        public List<String> getFilters() { return filters; }
        public void setFilters(List<String> filters) { this.filters = filters; }
    }

    static class MicroserviceRegistration {
        private String name;
        private String path;
        private String url;
        private boolean requiresAuth = true;
        private int rateLimit = 100;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public boolean isRequiresAuth() { return requiresAuth; }
        public void setRequiresAuth(boolean requiresAuth) { this.requiresAuth = requiresAuth; }

        public int getRateLimit() { return rateLimit; }
        public void setRateLimit(int rateLimit) { this.rateLimit = rateLimit; }
    }
}