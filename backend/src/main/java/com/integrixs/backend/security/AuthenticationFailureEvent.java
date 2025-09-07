package com.integrixs.backend.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event representing failed authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationFailureEvent {
    private String username;
    private String failureReason;
    private String authMethod;
    private String ipAddress;
    private Long timestamp;
}