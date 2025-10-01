package com.integrixs.gateway.filter;

import com.integrixs.gateway.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Authentication gateway filter for JWT validation
 */
@Component
public class AuthenticationGatewayFilter extends AbstractGatewayFilterFactory<AuthenticationGatewayFilter.Config>
        implements Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationGatewayFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check if path should be excluded
            String path = request.getURI().getPath();
            if (config.excludedPaths.stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            // Check for authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Validate JWT token
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                }

                // Extract claims and add to headers
                String username = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token);
                String tenantId = jwtUtil.extractTenantId(token);

                // Add user info to headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Auth-Username", username)
                    .header("X-Auth-Roles", String.join(",", roles))
                    .header("X-Auth-Tenant", tenantId != null ? tenantId : "default")
                    .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                logger.error("JWT validation error", e);
                return onError(exchange, "JWT validation failed", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"status\": %d}", error, httpStatus.value());

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100; // Execute early in filter chain
    }

    public static class Config {
        private List<String> excludedPaths = Arrays.asList(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/register",
            "/health",
            "/actuator/health"
       );

        public List<String> getExcludedPaths() {
            return excludedPaths;
        }

        public void setExcludedPaths(List<String> excludedPaths) {
            this.excludedPaths = excludedPaths;
        }
    }
}