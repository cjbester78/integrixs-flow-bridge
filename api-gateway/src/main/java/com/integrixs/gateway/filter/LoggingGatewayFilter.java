package com.integrixs.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Global filter for request/response logging and tracing
 */
@Component
public class LoggingGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingGatewayFilter.class);

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String START_TIME = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Generate or extract request ID
        String existingRequestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        final String requestId = existingRequestId != null ? existingRequestId : UUID.randomUUID().toString();

        // Set MDC for logging
        MDC.put("requestId", requestId);

        // Add request ID to headers
        ServerHttpRequest request = exchange.getRequest().mutate()
            .header(REQUEST_ID_HEADER, requestId)
            .build();

        // Store start time
        exchange.getAttributes().put(START_TIME, Instant.now());

        // Log request
        logRequest(exchange, requestId);

        // Continue chain and log response
        return chain.filter(exchange.mutate().request(request).build())
            .doOnSuccess(aVoid -> logResponse(exchange, requestId))
            .doOnError(throwable -> logError(exchange, requestId, throwable))
            .doFinally(signalType -> {
                // Clear MDC
                MDC.clear();
            });
    }

    private void logRequest(ServerWebExchange exchange, String requestId) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();
        String method = request.getMethod().toString();
        String query = request.getURI().getQuery();
        String clientIp = getClientIp(request);

        // Get route information
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = route != null ? route.getId() : "unknown";
        URI routeUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);

        logger.info("Request: {} {} {} [Route: {} -> {}] [Client: {}] [Request-ID: {}]",
            method, path, query != null ? "?" + query : "",
            routeId, routeUri != null ? routeUri : "unknown",
            clientIp, requestId);

        // Log headers if debug enabled
        if (logger.isDebugEnabled()) {
            request.getHeaders().forEach((name, values) -> {
                // Don't log sensitive headers
                if (!isSensitiveHeader(name)) {
                    logger.debug("Request Header: {} = {}", name, values);
                }
            });
        }
    }

    private void logResponse(ServerWebExchange exchange, String requestId) {
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();

        Instant startTime = exchange.getAttribute(START_TIME);
        Duration duration = Duration.between(startTime, Instant.now());

        HttpStatus status = response.getStatusCode() != null ?
            HttpStatus.valueOf(response.getStatusCode().value()) : HttpStatus.OK;

        String path = request.getURI().getPath();
        String method = request.getMethod().toString();

        logger.info("Response: {} {} -> {} [{}ms] [Request-ID: {}]",
            method, path, status.value(), duration.toMillis(), requestId);

        // Log response headers if debug enabled
        if (logger.isDebugEnabled()) {
            response.getHeaders().forEach((name, values) -> {
                logger.debug("Response Header: {} = {}", name, values);
            });
        }
    }

    private void logError(ServerWebExchange exchange, String requestId, Throwable throwable) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();
        String method = request.getMethod().toString();

        logger.error("Error processing request: {} {} [Request-ID: {}]",
            method, path, requestId, throwable);
    }

    private String getClientIp(ServerHttpRequest request) {
        // Check forwarded headers
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getHostString();
        }

        return "unknown";
    }

    private boolean isSensitiveHeader(String headerName) {
        Set<String> sensitiveHeaders = Set.of(
            "authorization",
            "cookie",
            "x-api-key",
            "x-auth-token"
       );
        return sensitiveHeaders.contains(headerName.toLowerCase());
    }

    @Override
    public int getOrder() {
        return -200; // Execute very early
    }
}