package com.integrixs.webserver.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for registering service endpoint
 */
public class RegisterEndpointDTO {

    @NotNull(message = "Endpoint name is required")
    private String name;

    @NotNull(message = "Base URL is required")
    private String baseUrl;

    @NotNull(message = "Endpoint type is required")
    private String type;

    private String description;

    private AuthenticationConfigDTO defaultAuth;
    private Map<String, String> defaultHeaders = new HashMap<>();

    private ConnectionConfigDTO connectionConfig;

    private String version;
    private Map<String, String> metadata = new HashMap<>();

    // Default constructor
    public RegisterEndpointDTO() {
        this.defaultHeaders = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    // All args constructor
    public RegisterEndpointDTO(String name, String baseUrl, String type, String description, AuthenticationConfigDTO defaultAuth, Map<String, String> defaultHeaders, ConnectionConfigDTO connectionConfig, String version, Map<String, String> metadata) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.type = type;
        this.description = description;
        this.defaultAuth = defaultAuth;
        this.defaultHeaders = defaultHeaders != null ? defaultHeaders : new HashMap<>();
        this.connectionConfig = connectionConfig;
        this.version = version;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // Getters
    @NotNull(message = "Endpoint name is required")
    public String getName() {
        return name;
    }
    @NotNull(message = "Base URL is required")
    public String getBaseUrl() {
        return baseUrl;
    }
    @NotNull(message = "Endpoint type is required")
    public String getType() {
        return type;
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
    public void setName(String name) {
        this.name = name;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public void setType(String type) {
        this.type = type;
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
    public static RegisterEndpointDTOBuilder builder() {
        return new RegisterEndpointDTOBuilder();
    }

    public static class RegisterEndpointDTOBuilder {
        private String name;
        private String baseUrl;
        private String type;
        private String description;
        private AuthenticationConfigDTO defaultAuth;
        private Map<String, String> defaultHeaders = new HashMap<>();
        private ConnectionConfigDTO connectionConfig;
        private String version;
        private Map<String, String> metadata = new HashMap<>();

        public RegisterEndpointDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RegisterEndpointDTOBuilder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public RegisterEndpointDTOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public RegisterEndpointDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public RegisterEndpointDTOBuilder defaultAuth(AuthenticationConfigDTO defaultAuth) {
            this.defaultAuth = defaultAuth;
            return this;
        }

        public RegisterEndpointDTOBuilder connectionConfig(ConnectionConfigDTO connectionConfig) {
            this.connectionConfig = connectionConfig;
            return this;
        }

        public RegisterEndpointDTOBuilder version(String version) {
            this.version = version;
            return this;
        }

        public RegisterEndpointDTOBuilder defaultHeaders(Map<String, String> defaultHeaders) {
            this.defaultHeaders = defaultHeaders;
            return this;
        }

        public RegisterEndpointDTOBuilder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public RegisterEndpointDTO build() {
            return new RegisterEndpointDTO(name, baseUrl, type, description, defaultAuth, defaultHeaders, connectionConfig, version, metadata);
        }
    }}
