package com.integrixs.soapbindings.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for creating SOAP binding
 */
public class CreateBindingRequestDTO {

    @NotNull(message = "Binding name is required")
    private String bindingName;

    @NotNull(message = "WSDL ID is required")
    private String wsdlId;

    @NotNull(message = "Service name is required")
    private String serviceName;

    @NotNull(message = "Port name is required")
    private String portName;

    @NotNull(message = "Endpoint URL is required")
    private String endpointUrl;

    private String bindingStyle = "DOCUMENT";

    private String transport = "HTTP";

    private Map<String, String> soapHeaders = new HashMap<>();

    private SecurityConfigurationDTO security;

    // Default constructor
    public CreateBindingRequestDTO() {
        this.bindingStyle = "DOCUMENT";
        this.transport = "HTTP";
        this.soapHeaders = new HashMap<>();
    }

    // All args constructor
    public CreateBindingRequestDTO(String bindingName, String wsdlId, String serviceName,
                                  String portName, String endpointUrl, String bindingStyle,
                                  String transport, Map<String, String> soapHeaders,
                                  SecurityConfigurationDTO security) {
        this.bindingName = bindingName;
        this.wsdlId = wsdlId;
        this.serviceName = serviceName;
        this.portName = portName;
        this.endpointUrl = endpointUrl;
        this.bindingStyle = bindingStyle != null ? bindingStyle : "DOCUMENT";
        this.transport = transport != null ? transport : "HTTP";
        this.soapHeaders = soapHeaders != null ? soapHeaders : new HashMap<>();
        this.security = security;
    }

    // Getters
    public String getBindingName() { return bindingName; }
    public String getWsdlId() { return wsdlId; }
    public String getServiceName() { return serviceName; }
    public String getPortName() { return portName; }
    public String getEndpointUrl() { return endpointUrl; }
    public String getBindingStyle() { return bindingStyle; }
    public String getTransport() { return transport; }
    public Map<String, String> getSoapHeaders() { return soapHeaders; }
    public SecurityConfigurationDTO getSecurity() { return security; }

    // Setters
    public void setBindingName(String bindingName) { this.bindingName = bindingName; }
    public void setWsdlId(String wsdlId) { this.wsdlId = wsdlId; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setPortName(String portName) { this.portName = portName; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public void setBindingStyle(String bindingStyle) { this.bindingStyle = bindingStyle; }
    public void setTransport(String transport) { this.transport = transport; }
    public void setSoapHeaders(Map<String, String> soapHeaders) { this.soapHeaders = soapHeaders; }
    public void setSecurity(SecurityConfigurationDTO security) { this.security = security; }

    // Builder
    public static CreateBindingRequestDTOBuilder builder() {
        return new CreateBindingRequestDTOBuilder();
    }

    public static class CreateBindingRequestDTOBuilder {
        private String bindingName;
        private String wsdlId;
        private String serviceName;
        private String portName;
        private String endpointUrl;
        private String bindingStyle = "DOCUMENT";
        private String transport = "HTTP";
        private Map<String, String> soapHeaders = new HashMap<>();
        private SecurityConfigurationDTO security;

        public CreateBindingRequestDTOBuilder bindingName(String bindingName) {
            this.bindingName = bindingName;
            return this;
        }

        public CreateBindingRequestDTOBuilder wsdlId(String wsdlId) {
            this.wsdlId = wsdlId;
            return this;
        }

        public CreateBindingRequestDTOBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public CreateBindingRequestDTOBuilder portName(String portName) {
            this.portName = portName;
            return this;
        }

        public CreateBindingRequestDTOBuilder endpointUrl(String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }

        public CreateBindingRequestDTOBuilder bindingStyle(String bindingStyle) {
            this.bindingStyle = bindingStyle;
            return this;
        }

        public CreateBindingRequestDTOBuilder transport(String transport) {
            this.transport = transport;
            return this;
        }

        public CreateBindingRequestDTOBuilder soapHeaders(Map<String, String> soapHeaders) {
            this.soapHeaders = soapHeaders;
            return this;
        }

        public CreateBindingRequestDTOBuilder security(SecurityConfigurationDTO security) {
            this.security = security;
            return this;
        }

        public CreateBindingRequestDTO build() {
            return new CreateBindingRequestDTO(bindingName, wsdlId, serviceName, portName,
                                             endpointUrl, bindingStyle, transport, soapHeaders, security);
        }
    }
}
