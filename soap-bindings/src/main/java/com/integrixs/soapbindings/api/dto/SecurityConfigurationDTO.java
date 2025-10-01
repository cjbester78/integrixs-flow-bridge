package com.integrixs.soapbindings.api.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for security configuration
 */
public class SecurityConfigurationDTO {

    private String securityType;
    private Map<String, String> credentials = new HashMap<>();
    private String certificatePath;
    private String keystorePath;
    private String truststorePath;
    private WsSecurityConfigDTO wsSecurityConfig;

    // Default constructor
    public SecurityConfigurationDTO() {
        this.credentials = new HashMap<>();
    }

    // All args constructor
    public SecurityConfigurationDTO(String securityType, Map<String, String> credentials,
                                  String certificatePath, String keystorePath,
                                  String truststorePath, WsSecurityConfigDTO wsSecurityConfig) {
        this.securityType = securityType;
        this.credentials = credentials != null ? credentials : new HashMap<>();
        this.certificatePath = certificatePath;
        this.keystorePath = keystorePath;
        this.truststorePath = truststorePath;
        this.wsSecurityConfig = wsSecurityConfig;
    }

    // Getters
    public String getSecurityType() { return securityType; }
    public Map<String, String> getCredentials() { return credentials; }
    public String getCertificatePath() { return certificatePath; }
    public String getKeystorePath() { return keystorePath; }
    public String getTruststorePath() { return truststorePath; }
    public WsSecurityConfigDTO getWsSecurityConfig() { return wsSecurityConfig; }

    // Setters
    public void setSecurityType(String securityType) { this.securityType = securityType; }
    public void setCredentials(Map<String, String> credentials) { this.credentials = credentials; }
    public void setCertificatePath(String certificatePath) { this.certificatePath = certificatePath; }
    public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }
    public void setTruststorePath(String truststorePath) { this.truststorePath = truststorePath; }
    public void setWsSecurityConfig(WsSecurityConfigDTO wsSecurityConfig) { this.wsSecurityConfig = wsSecurityConfig; }

    // Builder
    public static SecurityConfigurationDTOBuilder builder() {
        return new SecurityConfigurationDTOBuilder();
    }

    public static class SecurityConfigurationDTOBuilder {
        private String securityType;
        private Map<String, String> credentials = new HashMap<>();
        private String certificatePath;
        private String keystorePath;
        private String truststorePath;
        private WsSecurityConfigDTO wsSecurityConfig;

        public SecurityConfigurationDTOBuilder securityType(String securityType) {
            this.securityType = securityType;
            return this;
        }

        public SecurityConfigurationDTOBuilder credentials(Map<String, String> credentials) {
            this.credentials = credentials;
            return this;
        }

        public SecurityConfigurationDTOBuilder certificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
            return this;
        }

        public SecurityConfigurationDTOBuilder keystorePath(String keystorePath) {
            this.keystorePath = keystorePath;
            return this;
        }

        public SecurityConfigurationDTOBuilder truststorePath(String truststorePath) {
            this.truststorePath = truststorePath;
            return this;
        }

        public SecurityConfigurationDTOBuilder wsSecurityConfig(WsSecurityConfigDTO wsSecurityConfig) {
            this.wsSecurityConfig = wsSecurityConfig;
            return this;
        }

        public SecurityConfigurationDTO build() {
            return new SecurityConfigurationDTO(securityType, credentials, certificatePath,
                                              keystorePath, truststorePath, wsSecurityConfig);
        }
    }
}
