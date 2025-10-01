package com.integrixs.soapbindings.domain.model;

/**
 * WS-Security configuration
 */
public class WsSecurityConfig {

    private boolean enableTimestamp;
    private boolean enableSignature;
    private boolean enableEncryption;
    private String usernameToken;
    private String passwordType;
    private String signatureAlgorithm;
    private String encryptionAlgorithm;
    private int timestampTTL;

    // Default constructor
    public WsSecurityConfig() {
    }

    // All args constructor
    public WsSecurityConfig(boolean enableTimestamp, boolean enableSignature, boolean enableEncryption,
                           String usernameToken, String passwordType, String signatureAlgorithm,
                           String encryptionAlgorithm, int timestampTTL) {
        this.enableTimestamp = enableTimestamp;
        this.enableSignature = enableSignature;
        this.enableEncryption = enableEncryption;
        this.usernameToken = usernameToken;
        this.passwordType = passwordType;
        this.signatureAlgorithm = signatureAlgorithm;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.timestampTTL = timestampTTL;
    }

    // Getters
    public boolean isEnableTimestamp() { return enableTimestamp; }
    public boolean isEnableSignature() { return enableSignature; }
    public boolean isEnableEncryption() { return enableEncryption; }
    public String getUsernameToken() { return usernameToken; }
    public String getPasswordType() { return passwordType; }
    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public String getEncryptionAlgorithm() { return encryptionAlgorithm; }
    public int getTimestampTTL() { return timestampTTL; }

    // Setters
    public void setEnableTimestamp(boolean enableTimestamp) { this.enableTimestamp = enableTimestamp; }
    public void setEnableSignature(boolean enableSignature) { this.enableSignature = enableSignature; }
    public void setEnableEncryption(boolean enableEncryption) { this.enableEncryption = enableEncryption; }
    public void setUsernameToken(String usernameToken) { this.usernameToken = usernameToken; }
    public void setPasswordType(String passwordType) { this.passwordType = passwordType; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
    public void setEncryptionAlgorithm(String encryptionAlgorithm) { this.encryptionAlgorithm = encryptionAlgorithm; }
    public void setTimestampTTL(int timestampTTL) { this.timestampTTL = timestampTTL; }

    // Builder
    public static WsSecurityConfigBuilder builder() {
        return new WsSecurityConfigBuilder();
    }

    public static class WsSecurityConfigBuilder {
        private boolean enableTimestamp;
        private boolean enableSignature;
        private boolean enableEncryption;
        private String usernameToken;
        private String passwordType;
        private String signatureAlgorithm;
        private String encryptionAlgorithm;
        private int timestampTTL;

        public WsSecurityConfigBuilder enableTimestamp(boolean enableTimestamp) {
            this.enableTimestamp = enableTimestamp;
            return this;
        }

        public WsSecurityConfigBuilder enableSignature(boolean enableSignature) {
            this.enableSignature = enableSignature;
            return this;
        }

        public WsSecurityConfigBuilder enableEncryption(boolean enableEncryption) {
            this.enableEncryption = enableEncryption;
            return this;
        }

        public WsSecurityConfigBuilder usernameToken(String usernameToken) {
            this.usernameToken = usernameToken;
            return this;
        }

        public WsSecurityConfigBuilder passwordType(String passwordType) {
            this.passwordType = passwordType;
            return this;
        }

        public WsSecurityConfigBuilder signatureAlgorithm(String signatureAlgorithm) {
            this.signatureAlgorithm = signatureAlgorithm;
            return this;
        }

        public WsSecurityConfigBuilder encryptionAlgorithm(String encryptionAlgorithm) {
            this.encryptionAlgorithm = encryptionAlgorithm;
            return this;
        }

        public WsSecurityConfigBuilder timestampTTL(int timestampTTL) {
            this.timestampTTL = timestampTTL;
            return this;
        }

        public WsSecurityConfig build() {
            return new WsSecurityConfig(enableTimestamp, enableSignature, enableEncryption,
                                       usernameToken, passwordType, signatureAlgorithm,
                                       encryptionAlgorithm, timestampTTL);
        }
    }
}