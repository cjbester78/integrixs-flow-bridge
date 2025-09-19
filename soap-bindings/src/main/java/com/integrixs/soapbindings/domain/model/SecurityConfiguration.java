package com.integrixs.soapbindings.domain.model;

import com.integrixs.soapbindings.domain.enums.SecurityType;
import java.util.Map;
import java.util.HashMap;

/**
 * Security configuration for SOAP bindings
 */
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
    private boolean addTimestamp;
    private boolean addUsernameToken;
    private boolean encryptPayload;
    private boolean signPayload;
    private String encryptionAlgorithm;
    private String signatureAlgorithm;
    private Integer timestampTTL;
    private String passwordType;

    // Default constructor
    public SecurityConfiguration() {
    }

    // All args constructor
    public SecurityConfiguration(SecurityType securityType, String username, String password, String certificate, String privateKey, String oauthToken, String oauthClientId, String oauthClientSecret, WsSecurityConfig wsSecurityConfig, boolean addTimestamp, boolean addUsernameToken, boolean encryptPayload, boolean signPayload, String encryptionAlgorithm, String signatureAlgorithm, Integer timestampTTL, String passwordType) {
        this.securityType = securityType;
        this.username = username;
        this.password = password;
        this.certificate = certificate;
        this.privateKey = privateKey;
        this.oauthToken = oauthToken;
        this.oauthClientId = oauthClientId;
        this.oauthClientSecret = oauthClientSecret;
        this.wsSecurityConfig = wsSecurityConfig;
        this.addTimestamp = addTimestamp;
        this.addUsernameToken = addUsernameToken;
        this.encryptPayload = encryptPayload;
        this.signPayload = signPayload;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.signatureAlgorithm = signatureAlgorithm;
        this.timestampTTL = timestampTTL;
        this.passwordType = passwordType;
    }

    // Getters
    public SecurityType getSecurityType() { return securityType; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getCertificate() { return certificate; }
    public String getPrivateKey() { return privateKey; }
    public String getOauthToken() { return oauthToken; }
    public String getOauthClientId() { return oauthClientId; }
    public String getOauthClientSecret() { return oauthClientSecret; }
    public WsSecurityConfig getWsSecurityConfig() { return wsSecurityConfig; }
    public boolean isAddTimestamp() { return addTimestamp; }
    public boolean isAddUsernameToken() { return addUsernameToken; }
    public boolean isEncryptPayload() { return encryptPayload; }
    public boolean isSignPayload() { return signPayload; }
    public String getEncryptionAlgorithm() { return encryptionAlgorithm; }
    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public Integer getTimestampTTL() { return timestampTTL; }
    public String getPasswordType() { return passwordType; }

    // Setters
    public void setSecurityType(SecurityType securityType) { this.securityType = securityType; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setCertificate(String certificate) { this.certificate = certificate; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public void setOauthToken(String oauthToken) { this.oauthToken = oauthToken; }
    public void setOauthClientId(String oauthClientId) { this.oauthClientId = oauthClientId; }
    public void setOauthClientSecret(String oauthClientSecret) { this.oauthClientSecret = oauthClientSecret; }
    public void setWsSecurityConfig(WsSecurityConfig wsSecurityConfig) { this.wsSecurityConfig = wsSecurityConfig; }
    public void setAddTimestamp(boolean addTimestamp) { this.addTimestamp = addTimestamp; }
    public void setAddUsernameToken(boolean addUsernameToken) { this.addUsernameToken = addUsernameToken; }
    public void setEncryptPayload(boolean encryptPayload) { this.encryptPayload = encryptPayload; }
    public void setSignPayload(boolean signPayload) { this.signPayload = signPayload; }
    public void setEncryptionAlgorithm(String encryptionAlgorithm) { this.encryptionAlgorithm = encryptionAlgorithm; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
    public void setTimestampTTL(Integer timestampTTL) { this.timestampTTL = timestampTTL; }
    public void setPasswordType(String passwordType) { this.passwordType = passwordType; }

    // Builder
    public static SecurityConfigurationBuilder builder() {
        return new SecurityConfigurationBuilder();
    }

    public static class SecurityConfigurationBuilder {
        private SecurityType securityType;
        private String username;
        private String password;
        private String certificate;
        private String privateKey;
        private String oauthToken;
        private String oauthClientId;
        private String oauthClientSecret;
        private WsSecurityConfig wsSecurityConfig;
        private boolean addTimestamp;
        private boolean addUsernameToken;
        private boolean encryptPayload;
        private boolean signPayload;
        private String encryptionAlgorithm;
        private String signatureAlgorithm;
        private Integer timestampTTL;
        private String passwordType;

        public SecurityConfigurationBuilder securityType(SecurityType securityType) {
            this.securityType = securityType;
            return this;
        }

        public SecurityConfigurationBuilder username(String username) {
            this.username = username;
            return this;
        }

        public SecurityConfigurationBuilder password(String password) {
            this.password = password;
            return this;
        }

        public SecurityConfigurationBuilder certificate(String certificate) {
            this.certificate = certificate;
            return this;
        }

        public SecurityConfigurationBuilder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public SecurityConfigurationBuilder oauthToken(String oauthToken) {
            this.oauthToken = oauthToken;
            return this;
        }

        public SecurityConfigurationBuilder oauthClientId(String oauthClientId) {
            this.oauthClientId = oauthClientId;
            return this;
        }

        public SecurityConfigurationBuilder oauthClientSecret(String oauthClientSecret) {
            this.oauthClientSecret = oauthClientSecret;
            return this;
        }

        public SecurityConfigurationBuilder wsSecurityConfig(WsSecurityConfig wsSecurityConfig) {
            this.wsSecurityConfig = wsSecurityConfig;
            return this;
        }

        public SecurityConfigurationBuilder addTimestamp(boolean addTimestamp) {
            this.addTimestamp = addTimestamp;
            return this;
        }

        public SecurityConfigurationBuilder addUsernameToken(boolean addUsernameToken) {
            this.addUsernameToken = addUsernameToken;
            return this;
        }

        public SecurityConfigurationBuilder encryptPayload(boolean encryptPayload) {
            this.encryptPayload = encryptPayload;
            return this;
        }

        public SecurityConfigurationBuilder signPayload(boolean signPayload) {
            this.signPayload = signPayload;
            return this;
        }

        public SecurityConfigurationBuilder encryptionAlgorithm(String encryptionAlgorithm) {
            this.encryptionAlgorithm = encryptionAlgorithm;
            return this;
        }

        public SecurityConfigurationBuilder signatureAlgorithm(String signatureAlgorithm) {
            this.signatureAlgorithm = signatureAlgorithm;
            return this;
        }

        public SecurityConfigurationBuilder timestampTTL(Integer timestampTTL) {
            this.timestampTTL = timestampTTL;
            return this;
        }

        public SecurityConfigurationBuilder passwordType(String passwordType) {
            this.passwordType = passwordType;
            return this;
        }

        public SecurityConfiguration build() {
            return new SecurityConfiguration(securityType, username, password, certificate, privateKey, oauthToken, oauthClientId, oauthClientSecret, wsSecurityConfig, addTimestamp, addUsernameToken, encryptPayload, signPayload, encryptionAlgorithm, signatureAlgorithm, timestampTTL, passwordType);
        }
    }
}
