package com.integrixs.gateway.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing dynamic routes
 */
@Service
public class DynamicRouteService implements RouteDefinitionRepository {

    private static final Logger logger = LoggerFactory.getLogger(DynamicRouteService.class);

    private final Map<String, RouteDefinition> routes = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routes.values());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            routes.put(r.getId(), r);
            logger.info("Added route: {}", r.getId());
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            routes.remove(id);
            logger.info("Removed route: {}", id);
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        });
    }

    /**
     * Add a new route dynamically
     */
    public void addRoute(String id, String path, String uri, Map<String, String> predicates,
                        List<String> filters) {
        RouteDefinition route = new RouteDefinition();
        route.setId(id);
        route.setUri(URI.create(uri));
        route.setOrder(0);

        // Add predicates
        List<PredicateDefinition> predicateDefinitions = new ArrayList<>();

        // Path predicate
        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName("Path");
        pathPredicate.addArg("pattern", path);
        predicateDefinitions.add(pathPredicate);

        // Additional predicates
        predicates.forEach((name, value) -> {
            PredicateDefinition predicate = new PredicateDefinition();
            predicate.setName(name);
            predicate.addArg("value", value);
            predicateDefinitions.add(predicate);
        });

        route.setPredicates(predicateDefinitions);

        // Add filters
        List<FilterDefinition> filterDefinitions = new ArrayList<>();
        filters.forEach(filter -> {
            FilterDefinition filterDef = new FilterDefinition();
            String[] parts = filter.split("=", 2);
            filterDef.setName(parts[0]);
            if (parts.length > 1) {
                filterDef.addArg("value", parts[1]);
            }
            filterDefinitions.add(filterDef);
        });

        route.setFilters(filterDefinitions);

        save(Mono.just(route)).subscribe();
    }

    /**
     * Update an existing route
     */
    public void updateRoute(String id, String uri) {
        RouteDefinition route = routes.get(id);
        if (route != null) {
            route.setUri(URI.create(uri));
            publisher.publishEvent(new RefreshRoutesEvent(this));
            logger.info("Updated route: {} -> {}", id, uri);
        }
    }

    /**
     * Remove a route
     */
    public void removeRoute(String id) {
        delete(Mono.just(id)).subscribe();
    }

    /**
     * Get all routes
     */
    public Map<String, RouteDefinition> getAllRoutes() {
        return new HashMap<>(routes);
    }

    /**
     * Load routes from configuration or external source
     */
    public void loadRoutes() {
        // In production, load from database or configuration service
        // Example: Add a dynamic route
        addRoute(
            "dynamic-service",
            "/api/dynamic/**",
            "http://dynamic-service:8080",
            new HashMap<>(),
            Arrays.asList("StripPrefix=2", "AddRequestHeader=X-Source,Gateway")
       );
    }

    /**
     * Create route for a new microservice
     */
    public void registerMicroservice(String name, String path, String url,
                                   boolean requiresAuth, int rateLimit) {
        List<String> filters = new ArrayList<>();

        // Strip prefix
        filters.add("StripPrefix=1");

        // Add authentication if required
        if (requiresAuth) {
            filters.add("Authentication=true");
        }

        // Add rate limiting
        filters.add("RequestRateLimiter=" + rateLimit);

        // Add request ID
        filters.add("AddRequestHeader=X-Service-Name," + name);

        // Circuit breaker
        filters.add("CircuitBreaker=" + name + "-cb");

        addRoute(name + "-route", path, url, new HashMap<>(), filters);
    }
}