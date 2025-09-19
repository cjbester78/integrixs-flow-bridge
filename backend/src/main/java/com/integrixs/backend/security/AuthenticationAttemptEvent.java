package com.integrixs.backend.security;

/**
 * Event representing an authentication attempt.
 */
public class AuthenticationAttemptEvent {
    private String username;
    private String authMethod;
    private String ipAddress;
    private String userAgent;
    private Long timestamp;
}
