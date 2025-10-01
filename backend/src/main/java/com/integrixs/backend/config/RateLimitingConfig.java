package com.integrixs.backend.config;

import com.integrixs.backend.ratelimit.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Rate limiting configuration to prevent API abuse and DoS attacks.
 *
 * <p>Implements token bucket algorithm for efficient rate limiting
 * with memory - aware cleanup.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimiterService rateLimiterService;

    /**
     * Rate limiting interceptor.
     */
    @Bean
    public RateLimitingInterceptor rateLimitingInterceptor(RateLimiterService rateLimiterService) {
        return new RateLimitingInterceptor(rateLimiterService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor(rateLimiterService))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/system/logs/batch", // Exclude batch logging endpoint
                    "/api/auth/profile"        // Exclude profile endpoint for auth checks
               );
    }
}
