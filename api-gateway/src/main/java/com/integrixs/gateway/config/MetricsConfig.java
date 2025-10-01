package com.integrixs.gateway.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Metrics configuration for monitoring
 */
@Configuration
public class MetricsConfig {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "api-gateway");
    }

    @Bean
    public GlobalFilter customMetricsFilter(MeterRegistry meterRegistry) {
        return new GlobalFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                String path = exchange.getRequest().getPath().value();
                String method = exchange.getRequest().getMethod().toString();

                // Start timing
                Instant start = Instant.now();

                return chain.filter(exchange).doFinally(signalType -> {
                    // Calculate duration
                    Duration duration = Duration.between(start, Instant.now());

                    // Get route info
                    Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    String routeId = route != null ? route.getId() : "unknown";

                    // Get status code
                    HttpStatus status = exchange.getResponse().getStatusCode() != null ?
                        HttpStatus.valueOf(exchange.getResponse().getStatusCode().value()) :
                        HttpStatus.OK;

                    // Record metrics
                    Tags tags = Tags.of(
                        "method", method,
                        "route", routeId,
                        "status", String.valueOf(status.value()),
                        "outcome", status.is2xxSuccessful() ? "SUCCESS" : "ERROR"
                   );

                    // Request counter
                    meterRegistry.counter("gateway.requests", tags).increment();

                    // Request duration
                    meterRegistry.timer("gateway.request.duration", tags)
                        .record(duration);

                    // Active requests gauge (simplified)
                    if (status.is5xxServerError()) {
                        meterRegistry.counter("gateway.errors", tags).increment();
                    }
                });
            }
        };
    }
}