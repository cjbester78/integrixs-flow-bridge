package com.integrixs.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter for enforcing API rate limits.
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);


    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    // Paths to exclude from rate limiting
    private static final String[] EXCLUDED_PATHS = {
        "/api/v1/auth/login",
        "/api/v1/auth/refresh",
        "/api/v1/health",
        "/actuator",
        "/swagger - ui",
        "/v3/api - docs"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip rate limiting for excluded paths
        if(isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get user identifier
        String userId = getUserIdentifier(request);
        String userType = getUserType();

        // Calculate request cost based on endpoint
        int requestCost = calculateRequestCost(method, path);

        // Check rate limit
        boolean allowed = rateLimitService.allowRequest(userId, userType);

        // Check endpoint - specific limit if general limit passed
        if(allowed && requestCost > 1) {
            allowed = rateLimitService.allowRequestForEndpoint(userId, path, requestCost);
        }

        if(allowed) {
            // Add rate limit headers
            addRateLimitHeaders(response, userId);
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            handleRateLimitExceeded(request, response, userId);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't filter OPTIONS requests
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private String getUserIdentifier(HttpServletRequest request) {
        // Try to get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }

        // Try API key
        String apiKey = request.getHeader("X - API - Key");
        if(apiKey != null && !apiKey.isEmpty()) {
            return "api:" + apiKey.substring(0, Math.min(apiKey.length(), 8));
        }

        // Fall back to IP address
        return "ip:" + getClientIp(request);
    }

    private String getUserType() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated()) {
            // Check authorities for user type
            if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "ADMIN";
            }
            if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PREMIUM"))) {
                return "PREMIUM";
            }
            return "STANDARD";
        }
        return "ANONYMOUS";
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X - Forwarded - For");
        if(xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X - Real - IP");
        if(xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private boolean isExcludedPath(String path) {
        for(String excluded : EXCLUDED_PATHS) {
            if(path.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }

    private int calculateRequestCost(String method, String path) {
        // Higher cost for resource - intensive operations
        if("POST".equals(method) && path.contains("/flows/execute")) {
            return 5;
        }
        if("POST".equals(method) && path.contains("/transformations/test")) {
            return 3;
        }
        if("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            return 2;
        }
        return 1;
    }

    private void addRateLimitHeaders(HttpServletResponse response, String userId) {
        RateLimitService.RateLimitStatus status = rateLimitService.getRateLimitStatus(userId);

        response.setHeader("X - RateLimit - Limit", String.valueOf(status.getLimit()));
        response.setHeader("X - RateLimit - Remaining", String.valueOf(status.getRemaining()));
        response.setHeader("X - RateLimit - Reset",
            String.valueOf(status.getResetTime().toEpochSecond(ZoneOffset.UTC)));
    }

    private void handleRateLimitExceeded(HttpServletRequest request,
                                       HttpServletResponse response,
                                       String userId) throws IOException {
        log.warn("Rate limit exceeded for {} on {} {}",
            userId, request.getMethod(), request.getRequestURI());

        RateLimitService.RateLimitStatus status = rateLimitService.getRateLimitStatus(userId);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Add rate limit headers
        addRateLimitHeaders(response, userId);
        response.setHeader("Retry - After",
            String.valueOf(status.getResetTime().toEpochSecond(ZoneOffset.UTC) -
                         LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));

        // Response body
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Too Many Requests");
        error.put("message", "Rate limit exceeded. Please try again later.");
        error.put("limit", status.getLimit());
        error.put("remaining", status.getRemaining());
        error.put("resetTime", status.getResetTime());

        objectMapper.writeValue(response.getWriter(), error);
    }
}
