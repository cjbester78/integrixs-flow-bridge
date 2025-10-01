package com.integrixs.gateway.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Fallback handler for circuit breaker
 */
@Component
public class FallbackHandler {

    /**
     * API fallback handler
     */
    public HandlerFunction<ServerResponse> apiFallback = request -> {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service temporarily unavailable");
        response.put("message", "The service is currently unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("path", request.path());
        response.put("fallback", true);

        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(response));
    };

    /**
     * Health check fallback
     */
    public HandlerFunction<ServerResponse> healthFallback = request -> {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "DOWN");
        health.put("gateway", "UP");
        health.put("backend", "DOWN");
        health.put("timestamp", LocalDateTime.now());

        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(health));
    };

    /**
     * Generic fallback
     */
    public HandlerFunction<ServerResponse> genericFallback = request -> {
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.TEXT_PLAIN)
            .body(BodyInserters.fromValue("Service temporarily unavailable"));
    };

    /**
     * Fallback routes
     */
    public RouterFunction<ServerResponse> fallbackRoutes() {
        return route(GET("/fallback/api").or(POST("/fallback/api")), apiFallback)
            .andRoute(GET("/fallback/health"), healthFallback)
            .andRoute(GET("/fallback/**"), genericFallback);
    }
}