package com.integrixs.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Filter to sanitize and validate headers
 */
@Component
public class HeaderSanitizationFilter implements GlobalFilter, Ordered {

    private static final Set<String> HEADERS_TO_REMOVE = new HashSet<>(Arrays.asList(
        // Remove internal headers that shouldn't be exposed
        "x-auth-username",
        "x-auth-roles",
        "x-auth-tenant",
        "x-internal-request",
        "x-forwarded-prefix"
   ));

    private static final Set<String> HEADERS_TO_SANITIZE = new HashSet<>(Arrays.asList(
        "user-agent",
        "referer",
        "x-forwarded-for"
   ));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

        // Remove internal headers from incoming requests
        HEADERS_TO_REMOVE.forEach(header ->
            requestBuilder.headers(httpHeaders -> httpHeaders.remove(header))
        );

        // Sanitize specific headers
        HttpHeaders headers = exchange.getRequest().getHeaders();
        HEADERS_TO_SANITIZE.forEach(headerName -> {
            String value = headers.getFirst(headerName);
            if (value != null) {
                String sanitized = sanitizeHeaderValue(value);
                requestBuilder.header(headerName, sanitized);
            }
        });

        // Add security headers to response
        exchange.getResponse().getHeaders().add("X-Content-Type-Options", "nosniff");
        exchange.getResponse().getHeaders().add("X-Frame-Options", "DENY");
        exchange.getResponse().getHeaders().add("X-XSS-Protection", "1; mode=block");
        exchange.getResponse().getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    private String sanitizeHeaderValue(String value) {
        if (value == null) {
            return "";
        }

        // Remove control characters and limit length
        return value.replaceAll("[\\p{Cntrl}]", "")
                   .substring(0, Math.min(value.length(), 1024));
    }

    @Override
    public int getOrder() {
        return -150; // Execute after logging but before authentication
    }
}