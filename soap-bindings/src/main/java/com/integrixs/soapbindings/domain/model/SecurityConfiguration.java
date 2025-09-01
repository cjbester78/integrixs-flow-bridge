package com.integrixs.soapbindings.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * Security configuration for SOAP bindings
 */
@Data
@Builder
public class SecurityConfiguration {
    
    private SecurityType securityType;
    private String username;
    private String password;
    private String certificate;
    private String privateKey;
    private String oauthToken;
    private String oauthClientId;
    private String oauthClientSecret;
    private WsSecurityConfig wsSecurityConfig;
    
    /**
     * Security type enumeration
     */
    public enum SecurityType {
        NONE,
        BASIC_AUTH,
        WS_SECURITY,
        OAUTH2,
        CERTIFICATE,
        CUSTOM
    }
    
    /**
     * WS-Security specific configuration
     */
    @Data
    @Builder
    public static class WsSecurityConfig {
        private boolean addTimestamp;
        private boolean addUsernameToken;
        private boolean encryptPayload;
        private boolean signPayload;
        private String encryptionAlgorithm;
        private String signatureAlgorithm;
        private Integer timestampTTL;
        private String passwordType;
    }
}