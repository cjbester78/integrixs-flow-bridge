package com.integrixs.backend.config;

import com.integrixs.backend.ratelimit.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final RateLimiterService rateLimiterService;

    public RateLimitingInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler) throws Exception {

        RateLimiterService.RateLimitResult result;

        // Get user or IP-based rate limit
        String username = request.getRemoteUser();
        if (username != null) {
            // Use endpoint path as resource
            String resource = request.getRequestURI();
            result = rateLimiterService.checkUserRateLimit(username, resource);
        } else {
            // Use IP-based rate limiting
            String clientIp = getClientIp(request);
            String endpoint = request.getRequestURI();
            result = rateLimiterService.checkIpRateLimit(clientIp, endpoint);
        }

        if(result.isAllowed()) {
            // Add rate limit headers
            response.addHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
            response.addHeader("X-RateLimit-Remaining", String.valueOf(result.getRemainingRequests()));
            response.addHeader("X-RateLimit-Reset",
                String.valueOf(result.getResetTime()));
            return true;
        } else {
            // Rate limit exceeded
            long retryAfter = result.getRetryAfter();

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-RateLimit-Retry-After-Seconds", String.valueOf(retryAfter));
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please retry after %d seconds\"}",
                retryAfter
           ));

            String key = username != null ? "user:" + username : "ip:" + getClientIp(request);
            log.warn("Rate limit exceeded for key: {}", key);
            return false;
        }
    }


    /**
     * Get the real client IP considering proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if(xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if(xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

}
