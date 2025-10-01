package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.auth.LoginRequest;
import com.integrixs.backend.api.dto.auth.LoginResponse;
import com.integrixs.backend.application.service.AuthenticationService;
import com.integrixs.shared.exceptions.AuthenticationException;
import com.integrixs.backend.logging.BusinessOperation;
import com.integrixs.shared.dto.user.RegisterResponseDTO;
import com.integrixs.shared.dto.user.UserRegisterResponseDTO;
import com.integrixs.backend.api.dto.user.UserProfileResponse;
import com.integrixs.backend.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for authentication endpoints
 * Only handles HTTP concerns - delegates all business logic to services
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    @BusinessOperation(value = "AUTH.LOGIN", module = "Authentication", logInput = false, logOutput = false)
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        try {
            String ipAddress = extractIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User - Agent");

            LoginResponse response = authenticationService.login(request, ipAddress, userAgent);
            return ResponseEntity.ok(response);

        } catch(AuthenticationException e) {
            log.warn("Failed login attempt for username: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }

    /**
     * User logout endpoint
     */
    @PostMapping("/logout")
    @BusinessOperation(value = "AUTH.LOGOUT", module = "Authentication")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Extract refresh token from request body
        String refreshToken = request != null ? request.get("refreshToken") : null;
        String username = userDetails != null ? userDetails.getUsername() : null;

        // Call logout service with refresh token
        authenticationService.logout(username, refreshToken);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    @BusinessOperation(value = "AUTH.REFRESH_TOKEN", module = "Authentication", logInput = false, logOutput = false)
    public ResponseEntity<LoginResponse> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if(refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            LoginResponse response = authenticationService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            String username = SecurityUtils.getCurrentUsernameStatic();
            log.info("Profile request for username: {}", username);

            if(username == null || "system".equals(username)) {
                log.warn("Unauthorized profile request - username: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
            }

            UserProfileResponse profile = authenticationService.getUserProfile(username);
            return ResponseEntity.ok(profile);
        } catch(Exception e) {
            log.error("Error fetching user profile for username: " + SecurityUtils.getCurrentUsernameStatic(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch user profile", "message", e.getMessage()));
        }
    }


    /**
     * Extract IP address from request
     */
    private String extractIpAddress(HttpServletRequest request) {
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
}
