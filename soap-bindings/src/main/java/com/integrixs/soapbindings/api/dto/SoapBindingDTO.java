package com.integrixs.soapbindings.api.dto;

import java.time.LocalDateTime;

/**
 * DTO for SOAP binding details
 */
public class SoapBindingDTO {

    private String bindingId;
    private String bindingName;
    private String wsdlId;
    private String serviceName;
    private String portName;
    private String endpointUrl;
    private String bindingStyle;
    private String transport;
    private boolean active;
    private boolean requiresAuth;
    private boolean secureTransport;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public SoapBindingDTO() {
    }

    // All args constructor
    public SoapBindingDTO(String bindingId, String bindingName, String wsdlId, String serviceName, String portName, String endpointUrl, String bindingStyle, String transport, boolean active, boolean requiresAuth, boolean secureTransport, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.bindingId = bindingId;
        this.bindingName = bindingName;
        this.wsdlId = wsdlId;
        this.serviceName = serviceName;
        this.portName = portName;
        this.endpointUrl = endpointUrl;
        this.bindingStyle = bindingStyle;
        this.transport = transport;
        this.active = active;
        this.requiresAuth = requiresAuth;
        this.secureTransport = secureTransport;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getBindingId() { return bindingId; }
    public String getBindingName() { return bindingName; }
    public String getWsdlId() { return wsdlId; }
    public String getServiceName() { return serviceName; }
    public String getPortName() { return portName; }
    public String getEndpointUrl() { return endpointUrl; }
    public String getBindingStyle() { return bindingStyle; }
    public String getTransport() { return transport; }
    public boolean isActive() { return active; }
    public boolean isRequiresAuth() { return requiresAuth; }
    public boolean isSecureTransport() { return secureTransport; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setBindingId(String bindingId) { this.bindingId = bindingId; }
    public void setBindingName(String bindingName) { this.bindingName = bindingName; }
    public void setWsdlId(String wsdlId) { this.wsdlId = wsdlId; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setPortName(String portName) { this.portName = portName; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public void setBindingStyle(String bindingStyle) { this.bindingStyle = bindingStyle; }
    public void setTransport(String transport) { this.transport = transport; }
    public void setActive(boolean active) { this.active = active; }
    public void setRequiresAuth(boolean requiresAuth) { this.requiresAuth = requiresAuth; }
    public void setSecureTransport(boolean secureTransport) { this.secureTransport = secureTransport; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static SoapBindingDTOBuilder builder() {
        return new SoapBindingDTOBuilder();
    }

    public static class SoapBindingDTOBuilder {
        private String bindingId;
        private String bindingName;
        private String wsdlId;
        private String serviceName;
        private String portName;
        private String endpointUrl;
        private String bindingStyle;
        private String transport;
        private boolean active;
        private boolean requiresAuth;
        private boolean secureTransport;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public SoapBindingDTOBuilder bindingId(String bindingId) {
            this.bindingId = bindingId;
            return this;
        }

        public SoapBindingDTOBuilder bindingName(String bindingName) {
            this.bindingName = bindingName;
            return this;
        }

        public SoapBindingDTOBuilder wsdlId(String wsdlId) {
            this.wsdlId = wsdlId;
            return this;
        }

        public SoapBindingDTOBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public SoapBindingDTOBuilder portName(String portName) {
            this.portName = portName;
            return this;
        }

        public SoapBindingDTOBuilder endpointUrl(String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }

        public SoapBindingDTOBuilder bindingStyle(String bindingStyle) {
            this.bindingStyle = bindingStyle;
            return this;
        }

        public SoapBindingDTOBuilder transport(String transport) {
            this.transport = transport;
            return this;
        }

        public SoapBindingDTOBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public SoapBindingDTOBuilder requiresAuth(boolean requiresAuth) {
            this.requiresAuth = requiresAuth;
            return this;
        }

        public SoapBindingDTOBuilder secureTransport(boolean secureTransport) {
            this.secureTransport = secureTransport;
            return this;
        }

        public SoapBindingDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SoapBindingDTOBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SoapBindingDTO build() {
            return new SoapBindingDTO(bindingId, bindingName, wsdlId, serviceName, portName, endpointUrl, bindingStyle, transport, active, requiresAuth, secureTransport, createdAt, updatedAt);
        }
    }
}
