package com.integrixs.backend.controller;

import com.integrixs.backend.security.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * REST controller for rate limit management.
 */
@RestController
@RequestMapping("/api/v1/rate - limits")
@Tag(name = "Rate Limits", description = "API rate limit management")
public class RateLimitController {

    private final RateLimitService rateLimitService;

    public RateLimitController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/status")
    @Operation(summary = "Get current user's rate limit status")
    public ResponseEntity<RateLimitService.RateLimitStatus> getMyRateLimitStatus(Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        return ResponseEntity.ok(rateLimitService.getRateLimitStatus(userId));
    }

    @GetMapping("/status/ {userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get rate limit status for specific user")
    public ResponseEntity<RateLimitService.RateLimitStatus> getUserRateLimitStatus(
            @PathVariable String userId) {
        return ResponseEntity.ok(rateLimitService.getRateLimitStatus(userId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all active rate limits")
    public ResponseEntity<Map<String, RateLimitService.RateLimitStatus>> getAllRateLimits() {
        return ResponseEntity.ok(rateLimitService.getAllRateLimits());
    }

    @PostMapping("/reset/ {userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset rate limit for a user")
    public ResponseEntity<Void> resetUserLimit(@PathVariable String userId) {
        rateLimitService.resetUserLimit(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/ {userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update rate limit configuration for a user")
    public ResponseEntity<Void> updateUserLimit(
            @PathVariable String userId,
            @RequestParam long capacity,
            @RequestParam long refillTokens,
            @RequestParam long refillPeriod) {
        rateLimitService.updateUserLimit(userId, capacity, refillTokens, refillPeriod);
        return ResponseEntity.ok().build();
    }
}
