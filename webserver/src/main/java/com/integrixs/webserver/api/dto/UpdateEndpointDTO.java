package com.integrixs.webserver.api.dto;

import java.util.Map;

/**
 * DTO for updating service endpoint
 */
public class UpdateEndpointDTO {

    private String baseUrl;
    private String description;
    private AuthenticationConfigDTO defaultAuth;
    private Map<String, String> defaultHeaders;
    private ConnectionConfigDTO connectionConfig;
    private Boolean active;
    private String version;
    private Map<String, String> metadata;

    // Default constructor
    public UpdateEndpointDTO() {
    }

    // All args constructor
    public UpdateEndpointDTO(String baseUrl, String description, AuthenticationConfigDTO defaultAuth, Map<String, String> defaultHeaders, ConnectionConfigDTO connectionConfig, Boolean active, String version, Map<String, String> metadata) {
        this.baseUrl = baseUrl;
        this.description = description;
        this.defaultAuth = defaultAuth;
        this.defaultHeaders = defaultHeaders;
        this.connectionConfig = connectionConfig;
        this.active = active;
        this.version = version;
        this.metadata = metadata;
    }

    // Getters
    public String getBaseUrl() {
        return baseUrl;
    }
    public String getDescription() {
        return description;
    }
    public AuthenticationConfigDTO getDefaultAuth() {
        return defaultAuth;
    }
    public ConnectionConfigDTO getConnectionConfig() {
        return connectionConfig;
    }
    public Boolean getActive() {
        return active;
    }
    public String getVersion() {
        return version;
    }

    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    // Setters
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setDefaultAuth(AuthenticationConfigDTO defaultAuth) {
        this.defaultAuth = defaultAuth;
    }
    public void setConnectionConfig(ConnectionConfigDTO connectionConfig) {
        this.connectionConfig = connectionConfig;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public void setDefaultHeaders(Map<String, String> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static UpdateEndpointDTOBuilder builder() {
        return new UpdateEndpointDTOBuilder();
    }

    public static class UpdateEndpointDTOBuilder {
        private String baseUrl;
        private String description;
        private AuthenticationConfigDTO defaultAuth;
        private Map<String, String> defaultHeaders;
        private ConnectionConfigDTO connectionConfig;
        private Boolean active;
        private String version;
        private Map<String, String> metadata;

        public UpdateEndpointDTOBuilder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public UpdateEndpointDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public UpdateEndpointDTOBuilder defaultAuth(AuthenticationConfigDTO defaultAuth) {
            this.defaultAuth = defaultAuth;
            return this;
        }

        public UpdateEndpointDTOBuilder connectionConfig(ConnectionConfigDTO connectionConfig) {
            this.connectionConfig = connectionConfig;
            return this;
        }

        public UpdateEndpointDTOBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public UpdateEndpointDTOBuilder version(String version) {
            this.version = version;
            return this;
        }

        public UpdateEndpointDTOBuilder defaultHeaders(Map<String, String> defaultHeaders) {
            this.defaultHeaders = defaultHeaders;
            return this;
        }

        public UpdateEndpointDTOBuilder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public UpdateEndpointDTO build() {
            return new UpdateEndpointDTO(baseUrl, description, defaultAuth, defaultHeaders, connectionConfig, active, version, metadata);
        }
    }}
