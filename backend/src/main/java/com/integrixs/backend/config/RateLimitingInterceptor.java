package com.integrixs.backend.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Interceptor for rate limiting API requests.
 *
 * <p>Implements per - IP and per - user rate limiting with
 * different limits based on authentication status.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingInterceptor.class);


    private final RateLimitingConfig.RateLimiterService rateLimiterService;

    // Rate limit configurations from application.yml
    @Value("${rate.limit.anonymous.capacity:20}")
    private int anonymousCapacity;
    
    @Value("${rate.limit.anonymous.refill.tokens:20}")
    private int anonymousRefill;
    
    @Value("${rate.limit.anonymous.refill.period:60}")
    private int anonymousPeriodSeconds;

    @Value("${rate.limit.authenticated.capacity:100}")
    private int authenticatedCapacity;
    
    @Value("${rate.limit.authenticated.refill.tokens:100}")
    private int authenticatedRefill;
    
    @Value("${rate.limit.authenticated.refill.period:60}")
    private int authenticatedPeriodSeconds;

    @Value("${rate.limit.admin.capacity:1000}")
    private int adminCapacity;
    
    @Value("${rate.limit.admin.refill.tokens:1000}")
    private int adminRefill;
    
    @Value("${rate.limit.admin.refill.period:60}")
    private int adminPeriodSeconds;

    public RateLimitingInterceptor(RateLimitingConfig.RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler) throws Exception {

        String key = resolveKey(request);
        Bucket bucket = resolveBucket(key, request);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if(probe.isConsumed()) {
            // Add rate limit headers
            response.addHeader("X - Rate - Limit - Limit", String.valueOf(bucket.getAvailableTokens() + 1));
            response.addHeader("X - Rate - Limit - Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X - Rate - Limit - Reset",
                String.valueOf(System.currentTimeMillis() + probe.getNanosToWaitForRefill() / 1_000_000));
            return true;
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X - Rate - Limit - Retry - After - Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                " {\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please retry after %d seconds\"}",
                waitForRefill
           ));

            log.warn("Rate limit exceeded for key: {}", key);
            return false;
        }
    }

    /**
     * Resolve the rate limiting key based on user identity.
     */
    private String resolveKey(HttpServletRequest request) {
        // First try to get authenticated user
        String username = request.getRemoteUser();
        if(username != null) {
            return "user:" + username;
        }

        // Fall back to IP address
        String clientIp = getClientIp(request);
        return "ip:" + clientIp;
    }

    /**
     * Get the real client IP considering proxies.
     */
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

    /**
     * Resolve the appropriate bucket based on user role.
     */
    private Bucket resolveBucket(String key, HttpServletRequest request) {
        // Check if admin
        if(request.isUserInRole("ADMINISTRATOR")) {
            return rateLimiterService.resolveBucket(key,
                adminCapacity, adminRefill, Duration.ofSeconds(adminPeriodSeconds));
        }

        // Check if authenticated
        if(request.getRemoteUser() != null) {
            return rateLimiterService.resolveBucket(key,
                authenticatedCapacity, authenticatedRefill, Duration.ofSeconds(authenticatedPeriodSeconds));
        }

        // Anonymous user
        return rateLimiterService.resolveBucket(key,
            anonymousCapacity, anonymousRefill, Duration.ofSeconds(anonymousPeriodSeconds));
    }
}
