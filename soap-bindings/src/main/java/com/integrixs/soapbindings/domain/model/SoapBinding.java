package com.integrixs.soapbindings.domain.model;

import com.integrixs.soapbindings.domain.enums.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing a SOAP binding configuration
 */
public class SoapBinding {

    private String bindingId;
    private String bindingName;
    private String wsdlId;
    private String serviceName;
    private String portName;
    private String endpointUrl;
    private BindingStyle bindingStyle;
    private TransportProtocol transport;
    private Map<String, String> soapHeaders = new HashMap<>();
    private SecurityConfiguration security;
    private boolean active;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastModified;
    private EncodingStyle encoding;
    private SoapVersion soapVersion;
    private SecurityType securityType;
    private Map<String, String> credentials;
    private String certificatePath;
    private String keystorePath;
    private String truststorePath;
    private boolean enableWsSecurity;
    private WsSecurityConfig wsSecurityConfig;
    private boolean enableTimestamp;
    private boolean enableSignature;
    private boolean enableEncryption;
    private String usernameToken;
    private String passwordType;
    private String signatureAlgorithm;
    private String encryptionAlgorithm;
    private int timestampTTL;

    // Default constructor
    public SoapBinding() {
        this.credentials = new HashMap<>();
    }

    // All args constructor
    public SoapBinding(String bindingId, String bindingName, String wsdlId, String serviceName, String portName, String endpointUrl, BindingStyle bindingStyle, TransportProtocol transport, Map<String, String> soapHeaders, SecurityConfiguration security, boolean active, boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastModified, EncodingStyle encoding, SoapVersion soapVersion, SecurityType securityType, Map<String, String> credentials, String certificatePath, String keystorePath, String truststorePath, boolean enableWsSecurity, WsSecurityConfig wsSecurityConfig, boolean enableTimestamp, boolean enableSignature, boolean enableEncryption, String usernameToken, String passwordType, String signatureAlgorithm, String encryptionAlgorithm, int timestampTTL) {
        this.bindingId = bindingId;
        this.bindingName = bindingName;
        this.wsdlId = wsdlId;
        this.serviceName = serviceName;
        this.portName = portName;
        this.endpointUrl = endpointUrl;
        this.bindingStyle = bindingStyle;
        this.transport = transport;
        this.soapHeaders = soapHeaders != null ? soapHeaders : new HashMap<>();
        this.security = security;
        this.active = active;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastModified = lastModified;
        this.encoding = encoding;
        this.soapVersion = soapVersion;
        this.securityType = securityType;
        this.credentials = credentials != null ? credentials : new HashMap<>();
        this.certificatePath = certificatePath;
        this.keystorePath = keystorePath;
        this.truststorePath = truststorePath;
        this.enableWsSecurity = enableWsSecurity;
        this.wsSecurityConfig = wsSecurityConfig;
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
    public String getBindingId() { return bindingId; }
    public String getBindingName() { return bindingName; }
    public String getWsdlId() { return wsdlId; }
    public String getServiceName() { return serviceName; }
    public String getPortName() { return portName; }
    public String getEndpointUrl() { return endpointUrl; }
    public BindingStyle getBindingStyle() { return bindingStyle; }
    public TransportProtocol getTransport() { return transport; }
    public Map<String, String> getSoapHeaders() { return soapHeaders; }
    public SecurityConfiguration getSecurity() { return security; }
    public boolean isActive() { return active; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastModified() { return lastModified; }
    public EncodingStyle getEncoding() { return encoding; }
    public SoapVersion getSoapVersion() { return soapVersion; }
    public SecurityType getSecurityType() { return securityType; }
    public Map<String, String> getCredentials() { return credentials; }
    public String getCertificatePath() { return certificatePath; }
    public String getKeystorePath() { return keystorePath; }
    public String getTruststorePath() { return truststorePath; }
    public boolean isEnableWsSecurity() { return enableWsSecurity; }
    public WsSecurityConfig getWsSecurityConfig() { return wsSecurityConfig; }
    public boolean isEnableTimestamp() { return enableTimestamp; }
    public boolean isEnableSignature() { return enableSignature; }
    public boolean isEnableEncryption() { return enableEncryption; }
    public String getUsernameToken() { return usernameToken; }
    public String getPasswordType() { return passwordType; }
    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public String getEncryptionAlgorithm() { return encryptionAlgorithm; }
    public int getTimestampTTL() { return timestampTTL; }

    // Setters
    public void setBindingId(String bindingId) { this.bindingId = bindingId; }
    public void setBindingName(String bindingName) { this.bindingName = bindingName; }
    public void setWsdlId(String wsdlId) { this.wsdlId = wsdlId; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setPortName(String portName) { this.portName = portName; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public void setBindingStyle(BindingStyle bindingStyle) { this.bindingStyle = bindingStyle; }
    public void setTransport(TransportProtocol transport) { this.transport = transport; }
    public void setSoapHeaders(Map<String, String> soapHeaders) { this.soapHeaders = soapHeaders; }
    public void setSecurity(SecurityConfiguration security) { this.security = security; }
    public void setActive(boolean active) { this.active = active; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    public void setEncoding(EncodingStyle encoding) { this.encoding = encoding; }
    public void setSoapVersion(SoapVersion soapVersion) { this.soapVersion = soapVersion; }
    public void setSecurityType(SecurityType securityType) { this.securityType = securityType; }
    public void setCredentials(Map<String, String> credentials) { this.credentials = credentials; }
    public void setCertificatePath(String certificatePath) { this.certificatePath = certificatePath; }
    public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }
    public void setTruststorePath(String truststorePath) { this.truststorePath = truststorePath; }
    public void setEnableWsSecurity(boolean enableWsSecurity) { this.enableWsSecurity = enableWsSecurity; }
    public void setWsSecurityConfig(WsSecurityConfig wsSecurityConfig) { this.wsSecurityConfig = wsSecurityConfig; }
    public void setEnableTimestamp(boolean enableTimestamp) { this.enableTimestamp = enableTimestamp; }
    public void setEnableSignature(boolean enableSignature) { this.enableSignature = enableSignature; }
    public void setEnableEncryption(boolean enableEncryption) { this.enableEncryption = enableEncryption; }
    public void setUsernameToken(String usernameToken) { this.usernameToken = usernameToken; }
    public void setPasswordType(String passwordType) { this.passwordType = passwordType; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
    public void setEncryptionAlgorithm(String encryptionAlgorithm) { this.encryptionAlgorithm = encryptionAlgorithm; }
    public void setTimestampTTL(int timestampTTL) { this.timestampTTL = timestampTTL; }

    // Builder
    public static SoapBindingBuilder builder() {
        return new SoapBindingBuilder();
    }

    public static class SoapBindingBuilder {
        private String bindingId;
        private String bindingName;
        private String wsdlId;
        private String serviceName;
        private String portName;
        private String endpointUrl;
        private BindingStyle bindingStyle;
        private TransportProtocol transport;
        private Map<String, String> soapHeaders = new HashMap<>();
        private SecurityConfiguration security;
        private boolean active;
        private boolean enabled;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastModified;
        private EncodingStyle encoding;
        private SoapVersion soapVersion;
        private SecurityType securityType;
        private Map<String, String> credentials = new HashMap<>();
        private String certificatePath;
        private String keystorePath;
        private String truststorePath;
        private boolean enableWsSecurity;
        private WsSecurityConfig wsSecurityConfig;
        private boolean enableTimestamp;
        private boolean enableSignature;
        private boolean enableEncryption;
        private String usernameToken;
        private String passwordType;
        private String signatureAlgorithm;
        private String encryptionAlgorithm;
        private int timestampTTL;

        public SoapBindingBuilder bindingId(String bindingId) {
            this.bindingId = bindingId;
            return this;
        }

        public SoapBindingBuilder bindingName(String bindingName) {
            this.bindingName = bindingName;
            return this;
        }

        public SoapBindingBuilder wsdlId(String wsdlId) {
            this.wsdlId = wsdlId;
            return this;
        }

        public SoapBindingBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public SoapBindingBuilder portName(String portName) {
            this.portName = portName;
            return this;
        }

        public SoapBindingBuilder endpointUrl(String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }

        public SoapBindingBuilder bindingStyle(BindingStyle bindingStyle) {
            this.bindingStyle = bindingStyle;
            return this;
        }

        public SoapBindingBuilder transport(TransportProtocol transport) {
            this.transport = transport;
            return this;
        }

        public SoapBindingBuilder soapHeaders(Map<String, String> soapHeaders) {
            this.soapHeaders = soapHeaders;
            return this;
        }

        public SoapBindingBuilder security(SecurityConfiguration security) {
            this.security = security;
            return this;
        }

        public SoapBindingBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public SoapBindingBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public SoapBindingBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SoapBindingBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SoapBindingBuilder lastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public SoapBindingBuilder encoding(EncodingStyle encoding) {
            this.encoding = encoding;
            return this;
        }

        public SoapBindingBuilder soapVersion(SoapVersion soapVersion) {
            this.soapVersion = soapVersion;
            return this;
        }

        public SoapBindingBuilder securityType(SecurityType securityType) {
            this.securityType = securityType;
            return this;
        }

        public SoapBindingBuilder credentials(Map<String, String> credentials) {
            this.credentials = credentials;
            return this;
        }

        public SoapBindingBuilder certificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
            return this;
        }

        public SoapBindingBuilder keystorePath(String keystorePath) {
            this.keystorePath = keystorePath;
            return this;
        }

        public SoapBindingBuilder truststorePath(String truststorePath) {
            this.truststorePath = truststorePath;
            return this;
        }

        public SoapBindingBuilder enableWsSecurity(boolean enableWsSecurity) {
            this.enableWsSecurity = enableWsSecurity;
            return this;
        }

        public SoapBindingBuilder wsSecurityConfig(WsSecurityConfig wsSecurityConfig) {
            this.wsSecurityConfig = wsSecurityConfig;
            return this;
        }

        public SoapBindingBuilder enableTimestamp(boolean enableTimestamp) {
            this.enableTimestamp = enableTimestamp;
            return this;
        }

        public SoapBindingBuilder enableSignature(boolean enableSignature) {
            this.enableSignature = enableSignature;
            return this;
        }

        public SoapBindingBuilder enableEncryption(boolean enableEncryption) {
            this.enableEncryption = enableEncryption;
            return this;
        }

        public SoapBindingBuilder usernameToken(String usernameToken) {
            this.usernameToken = usernameToken;
            return this;
        }

        public SoapBindingBuilder passwordType(String passwordType) {
            this.passwordType = passwordType;
            return this;
        }

        public SoapBindingBuilder signatureAlgorithm(String signatureAlgorithm) {
            this.signatureAlgorithm = signatureAlgorithm;
            return this;
        }

        public SoapBindingBuilder encryptionAlgorithm(String encryptionAlgorithm) {
            this.encryptionAlgorithm = encryptionAlgorithm;
            return this;
        }

        public SoapBindingBuilder timestampTTL(int timestampTTL) {
            this.timestampTTL = timestampTTL;
            return this;
        }

        public SoapBinding build() {
            return new SoapBinding(bindingId, bindingName, wsdlId, serviceName, portName, endpointUrl, bindingStyle, transport, soapHeaders, security, active, enabled, createdAt, updatedAt, lastModified, encoding, soapVersion, securityType, credentials, certificatePath, keystorePath, truststorePath, enableWsSecurity, wsSecurityConfig, enableTimestamp, enableSignature, enableEncryption, usernameToken, passwordType, signatureAlgorithm, encryptionAlgorithm, timestampTTL);
        }
    }

    // Additional methods
    public boolean isSecureTransport() {
        return transport == TransportProtocol.HTTPS;
    }

    public boolean requiresAuthentication() {
        return securityType != null && securityType != SecurityType.NONE;
    }

    // Inner SecurityConfiguration class
    public static class SecurityConfiguration {
        private SecurityType securityType;
        private Map<String, String> credentials = new HashMap<>();
        private String certificatePath;
        private String keystorePath;
        private String truststorePath;
        private WsSecurityConfig wsSecurityConfig;

        public SecurityConfiguration() {
            this.credentials = new HashMap<>();
        }

        public SecurityConfiguration(SecurityType securityType, Map<String, String> credentials,
                                   String certificatePath, String keystorePath,
                                   String truststorePath, WsSecurityConfig wsSecurityConfig) {
            this.securityType = securityType;
            this.credentials = credentials != null ? credentials : new HashMap<>();
            this.certificatePath = certificatePath;
            this.keystorePath = keystorePath;
            this.truststorePath = truststorePath;
            this.wsSecurityConfig = wsSecurityConfig;
        }

        // Getters
        public SecurityType getSecurityType() { return securityType; }
        public Map<String, String> getCredentials() { return credentials; }
        public String getCertificatePath() { return certificatePath; }
        public String getKeystorePath() { return keystorePath; }
        public String getTruststorePath() { return truststorePath; }
        public WsSecurityConfig getWsSecurityConfig() { return wsSecurityConfig; }

        // Setters
        public void setSecurityType(SecurityType securityType) { this.securityType = securityType; }
        public void setCredentials(Map<String, String> credentials) { this.credentials = credentials; }
        public void setCertificatePath(String certificatePath) { this.certificatePath = certificatePath; }
        public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }
        public void setTruststorePath(String truststorePath) { this.truststorePath = truststorePath; }
        public void setWsSecurityConfig(WsSecurityConfig wsSecurityConfig) { this.wsSecurityConfig = wsSecurityConfig; }

        // Builder
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private SecurityType securityType;
            private Map<String, String> credentials = new HashMap<>();
            private String certificatePath;
            private String keystorePath;
            private String truststorePath;
            private WsSecurityConfig wsSecurityConfig;

            public Builder securityType(SecurityType securityType) {
                this.securityType = securityType;
                return this;
            }

            public Builder credentials(Map<String, String> credentials) {
                this.credentials = credentials;
                return this;
            }

            public Builder certificatePath(String certificatePath) {
                this.certificatePath = certificatePath;
                return this;
            }

            public Builder keystorePath(String keystorePath) {
                this.keystorePath = keystorePath;
                return this;
            }

            public Builder truststorePath(String truststorePath) {
                this.truststorePath = truststorePath;
                return this;
            }

            public Builder wsSecurityConfig(WsSecurityConfig wsSecurityConfig) {
                this.wsSecurityConfig = wsSecurityConfig;
                return this;
            }

            public SecurityConfiguration build() {
                return new SecurityConfiguration(securityType, credentials, certificatePath,
                                               keystorePath, truststorePath, wsSecurityConfig);
            }
        }
    }

    // Inner enums
    public enum BindingStyle {
        RPC_ENCODED,
        RPC_LITERAL,
        DOCUMENT_LITERAL,
        DOCUMENT_LITERAL_WRAPPED
    }

    public enum TransportProtocol {
        HTTP,
        HTTPS,
        JMS
    }
}
