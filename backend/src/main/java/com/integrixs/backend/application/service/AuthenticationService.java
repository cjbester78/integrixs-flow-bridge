package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.auth.LoginRequest;
import com.integrixs.backend.api.dto.auth.LoginResponse;
import com.integrixs.backend.api.dto.user.UserProfileResponse;
import com.integrixs.backend.domain.service.UserAuthenticationService;
import com.integrixs.backend.domain.service.UserSessionService;
import com.integrixs.backend.security.JwtUtil;
import com.integrixs.backend.service.AuditTrailService;
// import com.integrixs.backend.service.deprecated.UserService;
import com.integrixs.data.model.AuditTrail;
import com.integrixs.data.model.User;
import com.integrixs.data.model.UserSession;
import com.integrixs.shared.dto.user.UserDTO;
import com.integrixs.shared.dto.user.RegisterResponseDTO;
import com.integrixs.shared.dto.user.UserRegisterResponseDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Application service for authentication use cases
 * Orchestrates authentication flow
 */
@Service
public class AuthenticationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthenticationService.class);

    private final UserAuthenticationService authService;
    private final UserSessionService sessionService;
    private final JwtUtil jwtUtil;
    // private final UserService userService;
    private final AuditTrailService auditTrailService;

    public AuthenticationService(UserAuthenticationService authService,
                                 UserSessionService sessionService,
                                 JwtUtil jwtUtil,
                                 AuditTrailService auditTrailService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.jwtUtil = jwtUtil;
        this.auditTrailService = auditTrailService;
    }

    /**
     * Handle user login
     */
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Login attempt for username: {}", request.getUsername());

        // Authenticate user
        User user = authService.authenticate(request.getUsername(), request.getPassword());

        // Generate tokens
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        long expiresIn = jwtUtil.getExpirationMillis() / 1000;

        // Create session
        UserSession session = sessionService.createSession(user, refreshToken, ipAddress, userAgent);

        // Audit trail
        Map<String, Object> loginDetails = new HashMap<>();
        loginDetails.put("ipAddress", ipAddress);
        loginDetails.put("userAgent", userAgent);
        loginDetails.put("sessionId", session.getId());
        auditTrailService.logAction("User", user.getId().toString(), AuditTrail.AuditAction.LOGIN, loginDetails);

        // Map to DTO
        // UserDTO userDTO = userService.mapToDTO(user);
        UserDTO userDTO = mapUserToDTO(user);

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(userDTO)
                .build();
    }

    /**
     * Handle user logout
     */
    public void logout(String username, String refreshToken) {
        if(refreshToken != null) {
            sessionService.invalidateSession(refreshToken);
        }

        if(username != null) {
            User currentUser = authService.findByUsername(username);
            if(currentUser != null) {
                auditTrailService.logAction("User", currentUser.getId().toString(),
                        AuditTrail.AuditAction.LOGOUT, null);
            }
        }
    }

    /**
     * Refresh authentication token
     */
    public LoginResponse refreshToken(String refreshToken) {
        UserSession session = sessionService.validateAndRefreshSession(refreshToken);
        User user = session.getUser();

        // Generate new access token
        String newToken = jwtUtil.generateToken(user.getUsername(), user.getRole());
        long expiresIn = jwtUtil.getExpirationMillis() / 1000;

        // UserDTO userDTO = userService.mapToDTO(user);
        UserDTO userDTO = mapUserToDTO(user);

        return LoginResponse.builder()
                .token(newToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(userDTO)
                .build();
    }

    // Temporary helper method to replace userService.mapToDTO
    private UserDTO mapUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId().toString());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.isActive() ? "active" : "inactive");
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    /**
     * Get user profile by username
     */
    public UserProfileResponse getUserProfile(String username) {
        User user = authService.findByUsername(username);
        if(user == null) {
            throw new RuntimeException("User not found: " + username);
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.isActive() ? "active" : "inactive")
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Handle user registration(Admin - only)
     * Users are created by admin staff and are immediately active
     */
    public RegisterResponseDTO register(UserRegisterResponseDTO request, String ipAddress) {
        log.info("Admin registration request for email: {}", request.getEmail());

        try {
            // Register user through domain service
            User newUser = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
           );

            // Audit trail
            Map<String, Object> registrationDetails = new HashMap<>();
            registrationDetails.put("ipAddress", ipAddress);
            registrationDetails.put("email", request.getEmail());
            registrationDetails.put("role", request.getRole());
            registrationDetails.put("createdByAdmin", true);
            auditTrailService.logAction("User", newUser.getId().toString(),
                AuditTrail.AuditAction.CREATE, registrationDetails);

            // Return success response
            return RegisterResponseDTO.builder()
                    .success(true)
                    .message("User created successfully. The user can now login with their credentials.")
                    .userId(newUser.getId().toString())
                    .build();

        } catch(Exception e) {
            log.error("User registration failed for email: {}", request.getEmail(), e);
            throw new IllegalArgumentException("Registration failed: " + e.getMessage());
        }
    }
}
