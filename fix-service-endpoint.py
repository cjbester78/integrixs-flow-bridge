#!/usr/bin/env python3
import re

def fix_service_endpoint():
    file_path = "webserver/src/main/java/com/integrixs/webserver/domain/model/ServiceEndpoint.java"
    
    # Read the entire file
    with open(file_path, 'r') as f:
        content = f.read()
    
    # Extract the main class structure (before AuthenticationConfig)
    main_class_match = re.search(r'(package[^{]+public class ServiceEndpoint[^{]+\{[^{]+?)(?=public static class AuthenticationConfig)', content, re.DOTALL)
    
    if not main_class_match:
        print("Could not find main class structure")
        return
    
    main_class = main_class_match.group(1).strip()
    
    # Build clean version
    clean_content = main_class + """
    
    // Getters and setters for main class
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    
    public ServiceType getType() { return type; }
    public void setType(ServiceType type) { this.type = type; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Map<String, String> getDefaultHeaders() { return defaultHeaders; }
    public void setDefaultHeaders(Map<String, String> defaultHeaders) { this.defaultHeaders = defaultHeaders; }
    
    public AuthenticationConfig getDefaultAuth() { return defaultAuth; }
    public void setDefaultAuth(AuthenticationConfig defaultAuth) { this.defaultAuth = defaultAuth; }
    
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

    /**
     * Authentication configuration
     */
    public static class AuthenticationConfig {
        private String authType;
        private String username;
        private String password;
        private String clientId;
        private String clientSecret;
        private String tokenUrl;
        private Long tokenExpirySeconds;
        private Map<String, String> credentials;

        // Default constructor
        public AuthenticationConfig() {
            this.credentials = new HashMap<>();
        }

        // All args constructor
        public AuthenticationConfig(String authType, String username, String password, String clientId,
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
        public String getAuthType() { return authType; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getClientId() { return clientId; }
        public String getClientSecret() { return clientSecret; }
        public String getTokenUrl() { return tokenUrl; }
        public Long getTokenExpirySeconds() { return tokenExpirySeconds; }
        public Map<String, String> getCredentials() { return credentials; }

        // Setters
        public void setAuthType(String authType) { this.authType = authType; }
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
            private String authType;
            private String username;
            private String password;
            private String clientId;
            private String clientSecret;
            private String tokenUrl;
            private Long tokenExpirySeconds;
            private Map<String, String> credentials = new HashMap<>();

            public AuthenticationConfigBuilder authType(String authType) {
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
            private Integer connectionTimeoutSeconds;
            private Integer readTimeoutSeconds;
            private Integer maxConnections;
            private Integer maxConnectionsPerRoute;
            private Boolean keepAlive;
            private Boolean followRedirects;

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
        REST,
        SOAP,
        GRAPHQL,
        WEBHOOK,
        WEBSOCKET,
        GRPC
    }

    /**
     * Authentication type enumeration
     */
    public enum AuthType {
        NONE,
        BASIC,
        BEARER,
        API_KEY,
        OAUTH2,
        MUTUAL_TLS,
        CUSTOM
    }
}
"""

    # Write back the clean content
    with open(file_path, 'w') as f:
        f.write(clean_content)
    
    print(f"Fixed {file_path}")

if __name__ == "__main__":
    fix_service_endpoint()