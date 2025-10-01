package com.integrixs.webserver.api.dto;

import java.util.Map;

/**
 * DTO for service endpoint details
 */
public class ServiceEndpointDTO {

    private String endpointId;
    private String name;
    private String baseUrl;
    private String type;
    private String description;
    private boolean active;
    private String version;
    private Map<String, String> metadata;
    private boolean requiresAuth;

    // Default constructor
    public ServiceEndpointDTO() {
    }

    // All args constructor
    public ServiceEndpointDTO(String endpointId, String name, String baseUrl, String type, String description, boolean active, String version, Map<String, String> metadata, boolean requiresAuth) {
        this.endpointId = endpointId;
        this.name = name;
        this.baseUrl = baseUrl;
        this.type = type;
        this.description = description;
        this.active = active;
        this.version = version;
        this.metadata = metadata;
        this.requiresAuth = requiresAuth;
    }

    // Getters
    public String getEndpointId() {
        return endpointId;
    }
    public String getName() {
        return name;
    }
    public String getBaseUrl() {
        return baseUrl;
    }
    public String getType() {
        return type;
    }
    public String getDescription() {
        return description;
    }
    public boolean isActive() {
        return active;
    }
    public String getVersion() {
        return version;
    }
    public boolean isRequiresAuth() {
        return requiresAuth;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    // Setters
    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }
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
    public void setActive(boolean active) {
        this.active = active;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public void setRequiresAuth(boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static ServiceEndpointDTOBuilder builder() {
        return new ServiceEndpointDTOBuilder();
    }

    public static class ServiceEndpointDTOBuilder {
        private String endpointId;
        private String name;
        private String baseUrl;
        private String type;
        private String description;
        private boolean active;
        private String version;
        private Map<String, String> metadata;
        private boolean requiresAuth;

        public ServiceEndpointDTOBuilder endpointId(String endpointId) {
            this.endpointId = endpointId;
            return this;
        }

        public ServiceEndpointDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ServiceEndpointDTOBuilder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public ServiceEndpointDTOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ServiceEndpointDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ServiceEndpointDTOBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public ServiceEndpointDTOBuilder version(String version) {
            this.version = version;
            return this;
        }

        public ServiceEndpointDTOBuilder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ServiceEndpointDTOBuilder requiresAuth(boolean requiresAuth) {
            this.requiresAuth = requiresAuth;
            return this;
        }

        public ServiceEndpointDTO build() {
            return new ServiceEndpointDTO(endpointId, name, baseUrl, type, description, active, version, metadata, requiresAuth);
        }
    }}
