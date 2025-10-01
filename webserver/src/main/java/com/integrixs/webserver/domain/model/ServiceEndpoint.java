package com.integrixs.webserver.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing an external service endpoint configuration
 */
public class ServiceEndpoint {
    private String id;
    private String name;
    private String baseUrl;
    private ServiceType type;
    private String description;
    private AuthenticationConfig defaultAuth;
    private Map<String, String> defaultHeaders;
    private ConnectionConfig connectionConfig;
    private boolean active;
    private String version;
    private Map<String, String> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public ServiceEndpoint() {
        this.defaultHeaders = new HashMap<>();
        this.metadata = new HashMap<>();
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // All args constructor
    public ServiceEndpoint(String id, String name, String baseUrl, ServiceType type, String description,
                          AuthenticationConfig defaultAuth, Map<String, String> defaultHeaders,
                          ConnectionConfig connectionConfig, boolean active, String version,
                          Map<String, String> metadata, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.baseUrl = baseUrl;
        this.type = type;
        this.description = description;
        this.defaultAuth = defaultAuth;
        this.defaultHeaders = defaultHeaders != null ? defaultHeaders : new HashMap<>();
        this.connectionConfig = connectionConfig;
        this.active = active;
        this.version = version;
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Business methods
    public void updateEndpoint(String baseUrl, String description, AuthenticationConfig authConfig,
                              Map<String, String> headers, ConnectionConfig connConfig, String version) {
        if (baseUrl != null) this.baseUrl = baseUrl;
        if (description != null) this.description = description;
        if (authConfig != null) this.defaultAuth = authConfig;
        if (headers != null) this.defaultHeaders = headers;
        if (connConfig != null) this.connectionConfig = connConfig;
        if (version != null) this.version = version;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasAuthentication() {
        return defaultAuth != null && defaultAuth.getAuthType() != null && !"NONE".equals(defaultAuth.getAuthType());
    }

    public Map<String, String> buildRequestHeaders(Map<String, String> additionalHeaders) {
        Map<String, String> headers = new HashMap<>(defaultHeaders);
        if (additionalHeaders != null) {
            headers.putAll(additionalHeaders);
        }
        return headers;
    }

    public String buildUrl(String path) {
        if (path == null || path.isEmpty()) {
            return baseUrl;
        }
        String cleanedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String cleanedPath = path.startsWith("/") ? path : "/" + path;
        return cleanedBaseUrl + cleanedPath;
    }

    public Map<String, String> getMergedHeaders(Map<String, String> requestHeaders) {
        Map<String, String> mergedHeaders = new HashMap<>(defaultHeaders);
        if (requestHeaders != null) {
            mergedHeaders.putAll(requestHeaders);
        }
        return mergedHeaders;
    }

    public boolean requiresAuth() {
        return defaultAuth != null && defaultAuth.getAuthType() != null &&
               defaultAuth.getAuthType() != AuthenticationConfig.AuthType.NONE;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEndpointId() { return id; }
    public void setEndpointId(String endpointId) { this.id = endpointId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public ServiceType getType() { return type; }
    public void setType(ServiceType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AuthenticationConfig getDefaultAuth() { return defaultAuth; }
    public void setDefaultAuth(AuthenticationConfig defaultAuth) { this.defaultAuth = defaultAuth; }

    public Map<String, String> getDefaultHeaders() { return defaultHeaders; }
    public void setDefaultHeaders(Map<String, String> defaultHeaders) { this.defaultHeaders = defaultHeaders; }

    public ConnectionConfig getConnectionConfig() { return connectionConfig; }
    public void setConnectionConfig(ConnectionConfig connectionConfig) { this.connectionConfig = connectionConfig; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static ServiceEndpointBuilder builder() {
        return new ServiceEndpointBuilder();
    }

    public static class ServiceEndpointBuilder {
        private String id;
        private String name;
        private String baseUrl;
        private ServiceType type;
        private String description;
        private AuthenticationConfig defaultAuth;
        private Map<String, String> defaultHeaders = new HashMap<>();
        private ConnectionConfig connectionConfig;
        private boolean active = true;
        private String version;
        private Map<String, String> metadata = new HashMap<>();
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public ServiceEndpointBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ServiceEndpointBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ServiceEndpointBuilder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public ServiceEndpointBuilder type(ServiceType type) {
            this.type = type;
            return this;
        }

        public ServiceEndpointBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ServiceEndpointBuilder defaultAuth(AuthenticationConfig defaultAuth) {
            this.defaultAuth = defaultAuth;
            return this;
        }

        public ServiceEndpointBuilder defaultHeaders(Map<String, String> defaultHeaders) {
            this.defaultHeaders = defaultHeaders;
            return this;
        }

        public ServiceEndpointBuilder connectionConfig(ConnectionConfig connectionConfig) {
            this.connectionConfig = connectionConfig;
            return this;
        }

        public ServiceEndpointBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public ServiceEndpointBuilder version(String version) {
            this.version = version;
            return this;
        }

        public ServiceEndpointBuilder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ServiceEndpointBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ServiceEndpointBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ServiceEndpoint build() {
            return new ServiceEndpoint(id, name, baseUrl, type, description, defaultAuth,
                                      defaultHeaders, connectionConfig, active, version,
                                      metadata, createdAt, updatedAt);
        }
    }

    /**
     * Authentication configuration
     */
    public static class AuthenticationConfig {
        private AuthType authType;
        private String username;
        private String password;
        private String clientId;
        private String clientSecret;
        private String tokenUrl;
        private Long tokenExpirySeconds;
        private Map<String, String> credentials;

        public enum AuthType {
            NONE,
            BASIC,
            BEARER,
            API_KEY,
            OAUTH2,
            CUSTOM
        }

        // Default constructor
        public AuthenticationConfig() {
            this.credentials = new HashMap<>();
        }

        // All args constructor
        public AuthenticationConfig(AuthType authType, String username, String password, String clientId,
                                   String clientSecret, String tokenUrl, Long tokenExpirySeconds,
                                   Map<String, String> credentials) {
            this.authType = authType;
            this.username = username;
            this.password = password;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.tokenUrl = tokenUrl;
            this.tokenExpirySeconds = tokenExpirySeconds;
            this.credentials = credentials != null ? credentials : new HashMap<>();
        }

        // Getters
        public AuthType getAuthType() { return authType; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getClientId() { return clientId; }
        public String getClientSecret() { return clientSecret; }
        public String getTokenUrl() { return tokenUrl; }
        public Long getTokenExpirySeconds() { return tokenExpirySeconds; }
        public Map<String, String> getCredentials() { return credentials; }

        // Setters
        public void setAuthType(AuthType authType) { this.authType = authType; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
        public void setTokenUrl(String tokenUrl) { this.tokenUrl = tokenUrl; }
        public void setTokenExpirySeconds(Long tokenExpirySeconds) { this.tokenExpirySeconds = tokenExpirySeconds; }
        public void setCredentials(Map<String, String> credentials) { this.credentials = credentials; }

        // Builder
        public static AuthenticationConfigBuilder builder() {
            return new AuthenticationConfigBuilder();
        }

        public static class AuthenticationConfigBuilder {
            private AuthType authType;
            private String username;
            private String password;
            private String clientId;
            private String clientSecret;
            private String tokenUrl;
            private Long tokenExpirySeconds;
            private Map<String, String> credentials = new HashMap<>();

            public AuthenticationConfigBuilder authType(AuthType authType) {
                this.authType = authType;
                return this;
            }

            public AuthenticationConfigBuilder username(String username) {
                this.username = username;
                return this;
            }

            public AuthenticationConfigBuilder password(String password) {
                this.password = password;
                return this;
            }

            public AuthenticationConfigBuilder clientId(String clientId) {
                this.clientId = clientId;
                return this;
            }

            public AuthenticationConfigBuilder clientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
                return this;
            }

            public AuthenticationConfigBuilder tokenUrl(String tokenUrl) {
                this.tokenUrl = tokenUrl;
                return this;
            }

            public AuthenticationConfigBuilder tokenExpirySeconds(Long tokenExpirySeconds) {
                this.tokenExpirySeconds = tokenExpirySeconds;
                return this;
            }

            public AuthenticationConfigBuilder credentials(Map<String, String> credentials) {
                this.credentials = credentials;
                return this;
            }

            public AuthenticationConfig build() {
                return new AuthenticationConfig(authType, username, password, clientId, clientSecret,
                                               tokenUrl, tokenExpirySeconds, credentials);
            }
        }
    }

    /**
     * Connection configuration
     */
    public static class ConnectionConfig {
        private Integer connectionTimeoutSeconds;
        private Integer readTimeoutSeconds;
        private Integer maxConnections;
        private Integer maxConnectionsPerRoute;
        private Boolean keepAlive;
        private Boolean followRedirects;

        // Default constructor
        public ConnectionConfig() {
            this.connectionTimeoutSeconds = 10;
            this.readTimeoutSeconds = 30;
            this.maxConnections = 50;
            this.maxConnectionsPerRoute = 10;
            this.keepAlive = true;
            this.followRedirects = true;
        }

        // All args constructor
        public ConnectionConfig(Integer connectionTimeoutSeconds, Integer readTimeoutSeconds,
                               Integer maxConnections, Integer maxConnectionsPerRoute,
                               Boolean keepAlive, Boolean followRedirects) {
            this.connectionTimeoutSeconds = connectionTimeoutSeconds;
            this.readTimeoutSeconds = readTimeoutSeconds;
            this.maxConnections = maxConnections;
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            this.keepAlive = keepAlive;
            this.followRedirects = followRedirects;
        }

        // Getters
        public Integer getConnectionTimeoutSeconds() { return connectionTimeoutSeconds; }
        public Integer getReadTimeoutSeconds() { return readTimeoutSeconds; }
        public Integer getMaxConnections() { return maxConnections; }
        public Integer getMaxConnectionsPerRoute() { return maxConnectionsPerRoute; }
        public Boolean getKeepAlive() { return keepAlive; }
        public Boolean getFollowRedirects() { return followRedirects; }

        // Additional getters for primitive boolean
        public boolean isKeepAlive() { return keepAlive != null ? keepAlive : true; }
        public boolean isFollowRedirects() { return followRedirects != null ? followRedirects : true; }

        // Setters
        public void setConnectionTimeoutSeconds(Integer connectionTimeoutSeconds) {
            this.connectionTimeoutSeconds = connectionTimeoutSeconds;
        }
        public void setReadTimeoutSeconds(Integer readTimeoutSeconds) {
            this.readTimeoutSeconds = readTimeoutSeconds;
        }
        public void setMaxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
        }
        public void setMaxConnectionsPerRoute(Integer maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        }
        public void setKeepAlive(Boolean keepAlive) {
            this.keepAlive = keepAlive;
        }
        public void setFollowRedirects(Boolean followRedirects) {
            this.followRedirects = followRedirects;
        }

        // Builder
        public static ConnectionConfigBuilder builder() {
            return new ConnectionConfigBuilder();
        }

        public static class ConnectionConfigBuilder {
            private Integer connectionTimeoutSeconds = 10;
            private Integer readTimeoutSeconds = 30;
            private Integer maxConnections = 50;
            private Integer maxConnectionsPerRoute = 10;
            private Boolean keepAlive = true;
            private Boolean followRedirects = true;

            public ConnectionConfigBuilder connectionTimeoutSeconds(Integer connectionTimeoutSeconds) {
                this.connectionTimeoutSeconds = connectionTimeoutSeconds;
                return this;
            }

            public ConnectionConfigBuilder readTimeoutSeconds(Integer readTimeoutSeconds) {
                this.readTimeoutSeconds = readTimeoutSeconds;
                return this;
            }

            public ConnectionConfigBuilder maxConnections(Integer maxConnections) {
                this.maxConnections = maxConnections;
                return this;
            }

            public ConnectionConfigBuilder maxConnectionsPerRoute(Integer maxConnectionsPerRoute) {
                this.maxConnectionsPerRoute = maxConnectionsPerRoute;
                return this;
            }

            public ConnectionConfigBuilder keepAlive(Boolean keepAlive) {
                this.keepAlive = keepAlive;
                return this;
            }

            public ConnectionConfigBuilder followRedirects(Boolean followRedirects) {
                this.followRedirects = followRedirects;
                return this;
            }

            public ConnectionConfig build() {
                return new ConnectionConfig(connectionTimeoutSeconds, readTimeoutSeconds,
                                          maxConnections, maxConnectionsPerRoute,
                                          keepAlive, followRedirects);
            }
        }
    }

    /**
     * Service types enumeration
     */
    public enum ServiceType {
        REST_API,
        SOAP_SERVICE,
        GRAPHQL_API,
        WEBHOOK,
        WEBSOCKET,
        GRPC,
        CUSTOM
    }
}