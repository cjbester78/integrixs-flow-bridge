package com.integrixs.backend.security;

import java.time.Instant;
import java.util.Set;

/**
 * Event representing successful authentication.
 */
public class AuthenticationSuccessEvent {
    private String username;
    private Set<String> roles;
    private String sessionId;
    private Instant tokenExpiry;
    private String authenticationMethod;
    private Long timestamp;
}
