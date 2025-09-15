package com.integrixs.soapbindings.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing a SOAP binding configuration
 */
@Data
@Builder
public class SoapBinding {
    private String bindingId;
    private String bindingName;
    private String wsdlId;
    private String serviceName;
    private String portName;
    private String endpointUrl;
    private BindingStyle bindingStyle;
    private TransportProtocol transport;
    @Builder.Default
    private Map<String, String> soapHeaders = new HashMap<>();
    private SecurityConfiguration security;
    private boolean active;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastModified;
    private EncodingStyle encoding;
    private SoapVersion soapVersion;

    /**
     * SOAP encoding styles
     */
    public enum EncodingStyle {
        LITERAL,
        ENCODED
    }

    /**
     * SOAP versions
     */
    public enum SoapVersion {
        SOAP_1_1,
        SOAP_1_2
    }

    /**
     * Binding styles
     */
    public enum BindingStyle {
        DOCUMENT,
        RPC
    }

    /**
     * Transport protocols
     */
    public enum TransportProtocol {
        HTTP,
        HTTPS,
        JMS,
        SMTP
    }

    /**
     * Security configuration
     */
    @Data
    @Builder
    public static class SecurityConfiguration {
        private SecurityType securityType;
        private Map<String, String> credentials;
        private String certificatePath;
        private String keystorePath;
        private String truststorePath;
        private boolean enableWsSecurity;
        private WsSecurityConfig wsSecurityConfig;

        public enum SecurityType {
            NONE,
            BASIC_AUTH,
            WS_SECURITY,
            MUTUAL_TLS,
            OAUTH2
        }
    }

    /**
     * WS - Security configuration
     */
    @Data
    @Builder
    public static class WsSecurityConfig {
        private boolean enableTimestamp;
        private boolean enableSignature;
        private boolean enableEncryption;
        private String usernameToken;
        private String passwordType; // TEXT or DIGEST
        private String signatureAlgorithm;
        private String encryptionAlgorithm;
        private int timestampTTL;
    }

    /**
     * Check if binding requires authentication
     * @return true if authentication is required
     */
    public boolean requiresAuthentication() {
        return security != null &&
               security.getSecurityType() != SecurityConfiguration.SecurityType.NONE;
    }

    /**
     * Check if binding uses secure transport
     * @return true if using HTTPS
     */
    public boolean isSecureTransport() {
        return transport == TransportProtocol.HTTPS;
    }

    /**
     * Add SOAP header
     * @param name Header name
     * @param value Header value
     */
    public void addSoapHeader(String name, String value) {
        this.soapHeaders.put(name, value);
    }
}
