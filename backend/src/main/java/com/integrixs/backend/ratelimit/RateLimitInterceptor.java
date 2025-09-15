package com.integrixs.backend.ratelimit;

import com.integrixs.backend.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interceptor for API rate limiting
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler) throws Exception {

        // Skip non - handler methods
        if(!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Check for rate limit annotation
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if(rateLimit == null) {
            rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
        }

        // Skip if no rate limit annotation
        if(rateLimit == null && !isGlobalRateLimitEnabled()) {
            return true;
        }

        // Determine rate limit key
        String rateLimitKey = determineRateLimitKey(request, rateLimit);

        // Check rate limit
        RateLimiterService.RateLimitResult result = rateLimiterService.checkRateLimit(
            rateLimitKey,
            createConfig(rateLimit)
       );

        // Add rate limit headers
        response.addHeader("X - RateLimit - Limit", String.valueOf(result.getLimit()));
        response.addHeader("X - RateLimit - Remaining", String.valueOf(result.getRemainingRequests()));
        response.addHeader("X - RateLimit - Reset", String.valueOf(result.getResetTime() / 1000));

        if(!result.isAllowed()) {
            response.addHeader("Retry - After", String.valueOf(result.getRetryAfter()));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write(" {\"error\": \"Rate limit exceeded\", \"retryAfter\": " +
                                     result.getRetryAfter() + "}");

            logger.warn("Rate limit exceeded for key: {}", rateLimitKey);
            return false;
        }

        return true;
    }

    /**
     * Determine rate limit key based on configuration
     */
    private String determineRateLimitKey(HttpServletRequest request, RateLimit rateLimit) {
        String endpoint = request.getRequestURI();

        // Try to get user ID from JWT token
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                if(username != null) {
                    return "user:" + username + ":" + endpoint;
                }
            } catch(Exception e) {
                logger.debug("Failed to extract username from JWT", e);
            }
        }

        // Try to get API key
        String apiKey = request.getHeader("X - API - Key");
        if(apiKey != null && !apiKey.isEmpty()) {
            return "apikey:" + apiKey;
        }

        // Fall back to IP address
        String ipAddress = getClientIpAddress(request);
        return "ip:" + ipAddress + ":" + endpoint;
    }

    /**
     * Get client IP address considering proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
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
     * Create rate limit configuration from annotation
     */
    private RateLimiterService.RateLimitConfig createConfig(RateLimit rateLimit) {
        if(rateLimit == null) {
            return null; // Use default
        }

        return RateLimiterService.RateLimitConfig.builder()
            .capacity(rateLimit.capacity())
            .refillTokens(rateLimit.refillTokens())
            .refillPeriod(rateLimit.refillPeriod())
            .refillUnit(rateLimit.refillUnit())
            .build();
    }

    /**
     * Check if global rate limiting is enabled
     */
    private boolean isGlobalRateLimitEnabled() {
        // Could be configured via properties
        return true;
    }

    /**
     * Rate limit annotation
     */
    @Target( {ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RateLimit {
        /**
         * Maximum number of requests allowed
         */
        int capacity() default 100;

        /**
         * Number of tokens to refill
         */
        int refillTokens() default 10;

        /**
         * Refill period
         */
        int refillPeriod() default 60;

        /**
         * Refill time unit
         */
        java.util.concurrent.TimeUnit refillUnit() default java.util.concurrent.TimeUnit.SECONDS;

        /**
         * Rate limit key strategy
         */
        KeyStrategy keyStrategy() default KeyStrategy.USER;

        enum KeyStrategy {
            USER,
            IP,
            API_KEY,
            GLOBAL
        }
    }
}
