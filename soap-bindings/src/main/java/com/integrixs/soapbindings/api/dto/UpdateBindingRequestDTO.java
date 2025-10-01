package com.integrixs.soapbindings.api.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for updating SOAP binding
 */
public class UpdateBindingRequestDTO {

    private String endpointUrl;
    private Map<String, String> soapHeaders;
    private SecurityConfigurationDTO security;
    private Boolean active;

    // Default constructor
    public UpdateBindingRequestDTO() {
        this.soapHeaders = new HashMap<>();
    }

    // All args constructor
    public UpdateBindingRequestDTO(String endpointUrl, Map<String, String> soapHeaders, SecurityConfigurationDTO security, Boolean active) {
        this.endpointUrl = endpointUrl;
        this.soapHeaders = soapHeaders != null ? soapHeaders : new HashMap<>();
        this.security = security;
        this.active = active;
    }

    // Getters
    public String getEndpointUrl() { return endpointUrl; }
    public Map<String, String> getSoapHeaders() { return soapHeaders; }
    public SecurityConfigurationDTO getSecurity() { return security; }
    public Boolean getActive() { return active; }

    // Setters
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public void setSoapHeaders(Map<String, String> soapHeaders) { this.soapHeaders = soapHeaders; }
    public void setSecurity(SecurityConfigurationDTO security) { this.security = security; }
    public void setActive(Boolean active) { this.active = active; }

    // Builder
    public static UpdateBindingRequestDTOBuilder builder() {
        return new UpdateBindingRequestDTOBuilder();
    }

    public static class UpdateBindingRequestDTOBuilder {
        private String endpointUrl;
        private Map<String, String> soapHeaders = new HashMap<>();
        private SecurityConfigurationDTO security;
        private Boolean active;

        public UpdateBindingRequestDTOBuilder endpointUrl(String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }

        public UpdateBindingRequestDTOBuilder soapHeaders(Map<String, String> soapHeaders) {
            this.soapHeaders = soapHeaders;
            return this;
        }

        public UpdateBindingRequestDTOBuilder security(SecurityConfigurationDTO security) {
            this.security = security;
            return this;
        }

        public UpdateBindingRequestDTOBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public UpdateBindingRequestDTO build() {
            return new UpdateBindingRequestDTO(endpointUrl, soapHeaders, security, active);
        }
    }
}
