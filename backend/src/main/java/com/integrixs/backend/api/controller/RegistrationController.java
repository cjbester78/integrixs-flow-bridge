package com.integrixs.backend.api.controller;

import com.integrixs.backend.application.service.AuthenticationService;
import com.integrixs.backend.logging.BusinessOperation;
import com.integrixs.shared.dto.user.RegisterResponseDTO;
import com.integrixs.shared.dto.user.UserRegisterResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user registration endpoints
 * Admin - only endpoints for user management
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final AuthenticationService authenticationService;

    public RegistrationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Create new user endpoint(Admin only)
     */
    @PostMapping("/create")
    @BusinessOperation(value = "USER.CREATE", module = "User Management", logInput = false)
    public ResponseEntity<RegisterResponseDTO> createUser(
            @Valid @RequestBody UserRegisterResponseDTO request,
            HttpServletRequest httpRequest) {

        try {
            // Extract IP address for audit trail
            String ipAddress = extractIpAddress(httpRequest);

            // Perform registration
            RegisterResponseDTO response = authenticationService.register(request, ipAddress);

            log.info("Admin created new user successfully for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch(IllegalArgumentException e) {
            log.warn("User creation failed - validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RegisterResponseDTO.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch(Exception e) {
            log.error("User creation failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RegisterResponseDTO.builder()
                            .success(false)
                            .message("User creation failed. Please try again later.")
                            .build());
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
