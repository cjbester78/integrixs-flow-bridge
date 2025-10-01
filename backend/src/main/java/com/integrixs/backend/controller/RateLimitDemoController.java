package com.integrixs.backend.controller;

import com.integrixs.backend.ratelimit.RateLimitInterceptor.RateLimit;
import com.integrixs.backend.ratelimit.RateLimiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Demo controller showcasing different rate limiting strategies
 */
@RestController
@RequestMapping("/api/demo/ratelimit")
@Tag(name = "Rate Limit Demo", description = "Demonstration of API rate limiting")
public class RateLimitDemoController {

    @Autowired
    private RateLimiterService rateLimiterService;

    /**
     * Standard rate limited endpoint - 100 requests per minute
     */
    @GetMapping("/standard")
    @RateLimit(capacity = 100, refillTokens = 100, refillPeriod = 1, refillUnit = TimeUnit.MINUTES)
    @Operation(summary = "Standard rate limited endpoint",
               description = "Allows 100 requests per minute")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<?> standardEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Standard endpoint response");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Strict rate limited endpoint - 10 requests per minute
     */
    @GetMapping("/strict")
    @RateLimit(capacity = 10, refillTokens = 10, refillPeriod = 1, refillUnit = TimeUnit.MINUTES)
    @Operation(summary = "Strict rate limited endpoint",
               description = "Allows only 10 requests per minute")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<?> strictEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Strict endpoint response");
        response.put("timestamp", System.currentTimeMillis());
        response.put("warning", "This endpoint has strict rate limits");
        return ResponseEntity.ok(response);
    }

    /**
     * Burst rate limited endpoint - 20 requests burst, 2 per second sustained
     */
    @GetMapping("/burst")
    @RateLimit(capacity = 20, refillTokens = 2, refillPeriod = 1, refillUnit = TimeUnit.SECONDS)
    @Operation(summary = "Burst rate limited endpoint",
               description = "Allows 20 request burst, then 2 requests per second")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<?> burstEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Burst endpoint response");
        response.put("timestamp", System.currentTimeMillis());
        response.put("info", "Supports burst traffic with sustained rate limiting");
        return ResponseEntity.ok(response);
    }

    /**
     * Get current rate limit status
     */
    @GetMapping("/status")
    @Operation(summary = "Get rate limit status",
               description = "Returns current rate limit status for the caller")
    public ResponseEntity<?> getRateLimitStatus(
            @RequestHeader(value = "X - API - Key", required = false) String apiKey,
            @RequestParam(required = false) String endpoint) {

        String key;
        if(apiKey != null) {
            key = "ratelimit:apikey:" + apiKey;
        } else {
            key = "ratelimit:user:demo:" + (endpoint != null ? endpoint : "standard");
        }

        int availableTokens = rateLimiterService.getAvailableTokens(key);

        Map<String, Object> status = new HashMap<>();
        status.put("key", key);
        status.put("availableTokens", availableTokens);
        status.put("endpoint", endpoint != null ? endpoint : "all");

        return ResponseEntity.ok(status);
    }

    /**
     * Reset rate limit(admin only)
     */
    @PostMapping("/reset")
    @Operation(summary = "Reset rate limit",
               description = "Resets rate limit for a specific key(admin only)")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Rate limit reset"),
        @ApiResponse(responseCode = "403", description = "Unauthorized")
    })
    public ResponseEntity<?> resetRateLimit(@RequestParam String key) {
        // In production, check for admin permissions
        rateLimiterService.resetRateLimit(key);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rate limit reset successfully");
        response.put("key", key);

        return ResponseEntity.ok(response);
    }

    /**
     * Test different rate limit configurations
     */
    @PostMapping("/test")
    @Operation(summary = "Test rate limit",
               description = "Tests rate limiting with custom configuration")
    public ResponseEntity<?> testRateLimit(
            @RequestParam int capacity,
            @RequestParam int refillTokens,
            @RequestParam int refillPeriod,
            @RequestParam(defaultValue = "SECONDS") TimeUnit refillUnit,
            @RequestParam(defaultValue = "1") int tokensToConsume) {

        String testKey = "ratelimit:test:" + System.nanoTime();

        RateLimiterService.RateLimitConfig config = RateLimiterService.RateLimitConfig.builder()
            .capacity(capacity)
            .refillTokens(refillTokens)
            .refillPeriod(refillPeriod)
            .refillUnit(refillUnit)
            .build();

        RateLimiterService.RateLimitResult result = rateLimiterService.checkRateLimit(testKey, config);

        Map<String, Object> response = new HashMap<>();
        response.put("allowed", result.isAllowed());
        response.put("remainingTokens", result.getRemainingRequests());
        response.put("limit", result.getLimit());
        response.put("resetTime", result.getResetTime());
        response.put("retryAfter", result.getRetryAfter());
        response.put("config", Map.of(
            "capacity", capacity,
            "refillTokens", refillTokens,
            "refillPeriod", refillPeriod,
            "refillUnit", refillUnit
       ));

        return ResponseEntity.ok(response);
    }
}
