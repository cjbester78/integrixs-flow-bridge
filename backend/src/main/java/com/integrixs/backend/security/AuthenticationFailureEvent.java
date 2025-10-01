package com.integrixs.backend.security;

/**
 * Event representing failed authentication.
 */
public class AuthenticationFailureEvent {
    private String username;
    private String failureReason;
    private String authMethod;
    private String ipAddress;
    private Long timestamp;
}
