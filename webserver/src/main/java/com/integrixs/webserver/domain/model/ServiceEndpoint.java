package com.integrixs.webserver.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing an external service endpoint configuration
 */
@Data
@Builder
public class ServiceEndpoint {
    private String endpointId;
    private String name;
    private String baseUrl;
    private EndpointType type;
    private String description;
    private AuthenticationConfig defaultAuth;
    @Builder.Default
    private Map<String, String> defaultHeaders = new HashMap<>();
    private ConnectionConfig connectionConfig;
    private boolean active;
    private String version;
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    /**
     * Endpoint types
     */
    public enum EndpointType {
        REST_API,
        SOAP_SERVICE,
        GRAPHQL_API,
        WEBHOOK,
        CUSTOM
    }

    /**
     * Authentication configuration
     */
    @Data
    @Builder
    public static class AuthenticationConfig {
        private AuthType authType;
        @Builder.Default
        private Map<String, String> credentials = new HashMap<>();
        private String tokenUrl; // For OAuth2
        private long tokenExpirySeconds;

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

    /**
     * Connection configuration
     */
    @Data
    @Builder
    public static class ConnectionConfig {
        @Builder.Default
        private int connectionTimeoutSeconds = 10;
        @Builder.Default
        private int readTimeoutSeconds = 30;
        @Builder.Default
        private int maxConnections = 50;
        @Builder.Default
        private int maxConnectionsPerRoute = 10;
        @Builder.Default
        private boolean keepAlive = true;
        @Builder.Default
        private boolean followRedirects = true;
        private ProxyConfig proxyConfig;
    }

    /**
     * Proxy configuration
     */
    @Data
    @Builder
    public static class ProxyConfig {
        private String proxyHost;
        private int proxyPort;
        private String proxyUsername;
        private String proxyPassword;
        private ProxyType proxyType;

        public enum ProxyType {
            HTTP,
            SOCKS
        }
    }

    /**
     * Build full URL from base URL and path
     * @param path Request path
     * @return Full URL
     */
    public String buildUrl(String path) {
        if(path == null || path.isEmpty()) {
            return baseUrl;
        }

        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String cleanPath = path.startsWith("/") ? path : "/" + path;

        return cleanBaseUrl + cleanPath;
    }

    /**
     * Check if endpoint requires authentication
     * @return true if authentication is required
     */
    public boolean requiresAuth() {
        return defaultAuth != null && defaultAuth.getAuthType() != AuthenticationConfig.AuthType.NONE;
    }

    /**
     * Get merged headers(default + request headers)
     * @param requestHeaders Request - specific headers
     * @return Merged headers
     */
    public Map<String, String> getMergedHeaders(Map<String, String> requestHeaders) {
        Map<String, String> merged = new HashMap<>(defaultHeaders);
        if(requestHeaders != null) {
            merged.putAll(requestHeaders);
        }
        return merged;
    }
}
