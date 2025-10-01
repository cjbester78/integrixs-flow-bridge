package com.integrixs.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main gateway configuration
 */
@Configuration
public class GatewayConfig {

    @Value("${gateway.backend.url:http://localhost:8080}")
    private String backendUrl;

    @Value("${gateway.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Configure gateway routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Backend API routes
            .route("backend-api", r -> r
                .path("/api/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .requestRateLimiter(c -> c
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(apiKeyResolver())
                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                    .circuitBreaker(c -> c
                        .setName("backend-cb")
                        .setFallbackUri("forward:/fallback/api"))
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setMethods(HttpMethod.GET, HttpMethod.HEAD)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                    .addRequestHeader("X-Gateway-Request-Time", LocalDateTime.now()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .addResponseHeader("X-Gateway-Response-Time", LocalDateTime.now()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .uri(backendUrl))

            // WebSocket routes
            .route("websocket", r -> r
                .path("/ws/**")
                .filters(f -> f
                    .stripPrefix(0))
                .uri(backendUrl.replace("http", "ws")))

            // Health check route
            .route("health", r -> r
                .path("/health/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .setStatus(HttpStatus.OK))
                .uri(backendUrl))

            // Metrics route
            .route("metrics", r -> r
                .path("/actuator/**")
                .filters(f -> f
                    .stripPrefix(0))
                .uri(backendUrl))

            // Static resources route
            .route("static", r -> r
                .path("/static/**", "/uploads/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .addResponseHeader("Cache-Control", "public, max-age=3600"))
                .uri(backendUrl))

            // Frontend route (if serving frontend through gateway)
            .route("frontend", r -> r
                .path("/**")
                .and()
                .not(p -> p.path("/api/**", "/ws/**", "/health/**", "/actuator/**"))
                .filters(f -> f
                    .addResponseHeader("Cache-Control", "no-cache"))
                .uri(frontendUrl))

            .build();
    }

    /**
     * Configure Redis rate limiter
     */
    @Bean
    @Primary
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(100, 200, 1);
    }

    /**
     * Configure different rate limiters for different endpoints
     */
    @Bean
    public RedisRateLimiter strictRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    @Bean
    public RedisRateLimiter publicRateLimiter() {
        return new RedisRateLimiter(20, 40, 1);
    }

    /**
     * Key resolver for rate limiting based on different strategies
     */
    @Bean
    @Primary
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            // Try API key first
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null) {
                return Mono.just("api-key:" + apiKey);
            }

            // Try JWT user
            String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                // In production, decode JWT to get username
                return Mono.just("user:" + auth.substring(7, Math.min(auth.length(), 20)));
            }

            // Fall back to IP address
            String ip = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getHostString() : "unknown";
            return Mono.just("ip:" + ip);
        };
    }

    /**
     * IP-based key resolver
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getHostString() : "unknown";
            return Mono.just("ip:" + ip);
        };
    }

    /**
     * User-based key resolver
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            return exchange.getPrincipal()
                .cast(Object.class)
                .map(Object::toString)
                .map(user -> "user:" + user)
                .switchIfEmpty(Mono.just("anonymous"));
        };
    }

    /**
     * Configure circuit breaker
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .build())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .build())
            .build());
    }
}