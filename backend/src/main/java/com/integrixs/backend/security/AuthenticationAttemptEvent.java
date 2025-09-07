package com.integrixs.backend.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing an authentication attempt.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationAttemptEvent {
    private String username;
    private String authMethod;
    private String ipAddress;
    private String userAgent;
    private Long timestamp;
}