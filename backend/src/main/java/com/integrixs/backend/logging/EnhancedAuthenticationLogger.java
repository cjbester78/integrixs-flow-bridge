package com.integrixs.backend.logging;

import com.integrixs.backend.security.AuthenticationFailureEvent;
import com.integrixs.backend.security.AuthenticationAttemptEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced authentication logger providing detailed authentication event logging.
 */
@Component
public class EnhancedAuthenticationLogger {

    private static final Logger log = LoggerFactory.getLogger(EnhancedAuthenticationLogger.class);


    public void logAuthenticationAttempt(String username, String authMethod, String ipAddress, String userAgent) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("AUTHENTICATION.ATTEMPT\n");
        logMessage.append("User: ").append(username).append("\n");
        logMessage.append("Method: ").append(authMethod).append("\n");
        logMessage.append("IP: ").append(ipAddress).append("\n");
        logMessage.append("User - Agent: ").append(userAgent).append("\n");
        logMessage.append("Timestamp: ").append(Instant.now()).append("\n");
        logMessage.append("Thread: ").append(Thread.currentThread().getName());

        log.info(logMessage.toString());
    }

    public void logAuthenticationSuccess(String username, Authentication authentication, String sessionId, Instant tokenExpiry) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("AUTHENTICATION.SUCCESS\n");
        logMessage.append("User: ").append(username).append("\n");
        logMessage.append("Authentication Stack: ").append(authentication.getClass().getSimpleName()).append("\n");

        // Log authorities/roles
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        logMessage.append("Roles: ").append(roles).append("\n");

        logMessage.append("Session: ").append(sessionId).append("\n");

        if(tokenExpiry != null) {
            logMessage.append("Token - Expiry: ").append(tokenExpiry).append("\n");
        }

        // Authentication details
        logMessage.append("Authenticated: ").append(authentication.isAuthenticated()).append("\n");
        logMessage.append("Principal Type: ").append(authentication.getPrincipal().getClass().getSimpleName()).append("\n");

        // Login modules equivalent
        logMessage.append("Login Modules:\n");
        logMessage.append(" 1. JWT Token Validation: ").append(getTokenValidationStatus(authentication)).append("\n");
        logMessage.append(" 2. User Details Loading: SUCCESS\n");
        logMessage.append(" 3. Authority Mapping: SUCCESS\n");

        logMessage.append("Central Checks: PASSED\n");
        logMessage.append("Timestamp: ").append(Instant.now());

        log.info(logMessage.toString());
    }

    public void logAuthenticationFailure(String username, String failureReason, String authMethod, String ipAddress) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("AUTHENTICATION.FAILED\n");
        logMessage.append("User: ").append(username != null ? username : "UNKNOWN").append("\n");
        logMessage.append("Method: ").append(authMethod).append("\n");
        logMessage.append("IP: ").append(ipAddress).append("\n");
        logMessage.append("Failure Reason: ").append(failureReason).append("\n");

        // Detailed failure analysis
        logMessage.append("Login Modules:\n");
        logMessage.append(" 1. JWT Token Validation: ").append(getFailureModuleStatus(failureReason, "token")).append("\n");
        logMessage.append(" 2. User Details Loading: ").append(getFailureModuleStatus(failureReason, "user")).append("\n");
        logMessage.append(" 3. Authority Mapping: ").append(getFailureModuleStatus(failureReason, "authority")).append("\n");

        logMessage.append("Central Checks: FAILED\n");
        logMessage.append("Timestamp: ").append(Instant.now());

        log.warn(logMessage.toString());
    }

    public void logLogout(String username, String sessionId) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("LOGOUT.OK\n");
        logMessage.append("User: ").append(username).append("\n");
        logMessage.append("Session: ").append(sessionId).append("\n");
        logMessage.append("Authentication Stack: JWT\n");

        logMessage.append("Login Modules:\n");
        logMessage.append(" 1. JWT Token Validation: LOGOUT\n");
        logMessage.append(" 2. User Details Loading: LOGOUT\n");
        logMessage.append(" 3. Authority Mapping: LOGOUT\n");

        logMessage.append("Timestamp: ").append(Instant.now());

        log.info(logMessage.toString());
    }

    @EventListener
    public void onAuthenticationSuccess(org.springframework.security.authentication.event.AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        logAuthenticationSuccess(
            auth.getName(),
            auth,
            generateSessionId(),
            calculateTokenExpiry()
       );
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication() != null ?
            event.getAuthentication().getName() : "UNKNOWN";
        logAuthenticationFailure(
            username,
            event.getException().getMessage(),
            "JWT",
            "Unknown" // Would need to extract from request context
       );
    }

    private String getTokenValidationStatus(Authentication authentication) {
        if(authentication.isAuthenticated() && authentication.getCredentials() != null) {
            return "SUCCESS";
        }
        return "SKIPPED";
    }

    private String getFailureModuleStatus(String failureReason, String module) {
        if(failureReason == null) return "UNKNOWN";

        String reason = failureReason.toLowerCase();
        switch(module) {
            case "token":
                if(reason.contains("token") || reason.contains("jwt")) {
                    return "FAILED - " + failureReason;
                }
                return "SUCCESS";
            case "user":
                if(reason.contains("user") || reason.contains("not found")) {
                    return "FAILED - " + failureReason;
                }
                return "NOT_EXECUTED";
            case "authority":
                if(reason.contains("authority") || reason.contains("permission")) {
                    return "FAILED - " + failureReason;
                }
                return "NOT_EXECUTED";
            default:
                return "UNKNOWN";
        }
    }

    private String generateSessionId() {
        return "SID-" + System.currentTimeMillis();
    }

    private Instant calculateTokenExpiry() {
        // Default 24 hours from now
        return Instant.now().plusSeconds(86400);
    }
}
