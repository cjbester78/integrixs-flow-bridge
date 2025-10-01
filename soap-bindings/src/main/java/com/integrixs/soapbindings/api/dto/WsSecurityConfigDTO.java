package com.integrixs.soapbindings.api.dto;

/**
 * DTO for WS - Security configuration
 */
public class WsSecurityConfigDTO {

    private boolean enableTimestamp;
    private boolean enableSignature;
    private boolean enableEncryption;
    private String usernameToken;
    private String passwordType;
    private String signatureAlgorithm;
    private String encryptionAlgorithm;
    private int timestampTTL;

    // Default constructor
    public WsSecurityConfigDTO() {
    }

    // All args constructor
    public WsSecurityConfigDTO(boolean enableTimestamp, boolean enableSignature, boolean enableEncryption,
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
    public static WsSecurityConfigDTOBuilder builder() {
        return new WsSecurityConfigDTOBuilder();
    }

    public static class WsSecurityConfigDTOBuilder {
        private boolean enableTimestamp;
        private boolean enableSignature;
        private boolean enableEncryption;
        private String usernameToken;
        private String passwordType;
        private String signatureAlgorithm;
        private String encryptionAlgorithm;
        private int timestampTTL;

        public WsSecurityConfigDTOBuilder enableTimestamp(boolean enableTimestamp) {
            this.enableTimestamp = enableTimestamp;
            return this;
        }

        public WsSecurityConfigDTOBuilder enableSignature(boolean enableSignature) {
            this.enableSignature = enableSignature;
            return this;
        }

        public WsSecurityConfigDTOBuilder enableEncryption(boolean enableEncryption) {
            this.enableEncryption = enableEncryption;
            return this;
        }

        public WsSecurityConfigDTOBuilder usernameToken(String usernameToken) {
            this.usernameToken = usernameToken;
            return this;
        }

        public WsSecurityConfigDTOBuilder passwordType(String passwordType) {
            this.passwordType = passwordType;
            return this;
        }

        public WsSecurityConfigDTOBuilder signatureAlgorithm(String signatureAlgorithm) {
            this.signatureAlgorithm = signatureAlgorithm;
            return this;
        }

        public WsSecurityConfigDTOBuilder encryptionAlgorithm(String encryptionAlgorithm) {
            this.encryptionAlgorithm = encryptionAlgorithm;
            return this;
        }

        public WsSecurityConfigDTOBuilder timestampTTL(int timestampTTL) {
            this.timestampTTL = timestampTTL;
            return this;
        }

        public WsSecurityConfigDTO build() {
            return new WsSecurityConfigDTO(enableTimestamp, enableSignature, enableEncryption,
                                         usernameToken, passwordType, signatureAlgorithm,
                                         encryptionAlgorithm, timestampTTL);
        }
    }
}
