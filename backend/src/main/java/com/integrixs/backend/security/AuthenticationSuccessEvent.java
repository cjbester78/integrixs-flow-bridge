package com.integrixs.backend.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

/**
 * Event representing successful authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationSuccessEvent {
    private String username;
    private Set<String> roles;
    private String sessionId;
    private Instant tokenExpiry;
    private String authenticationMethod;
    private Long timestamp;
}
