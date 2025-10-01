package com.integrixs.webserver.api.dto;

/**
 * DTO for connection configuration
 */
public class ConnectionConfigDTO {
    private int connectionTimeoutSeconds = 10;
    private int readTimeoutSeconds = 30;
    private int maxConnections = 50;
    private int maxConnectionsPerRoute = 10;
    private boolean keepAlive = true;
    private boolean followRedirects = true;

    // Default constructor
    public ConnectionConfigDTO() {
    }

    // All args constructor
    public ConnectionConfigDTO(int connectionTimeoutSeconds, int readTimeoutSeconds, int maxConnections, int maxConnectionsPerRoute, boolean keepAlive, boolean followRedirects) {
        this.connectionTimeoutSeconds = connectionTimeoutSeconds;
        this.readTimeoutSeconds = readTimeoutSeconds;
        this.maxConnections = maxConnections;
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
        this.keepAlive = keepAlive;
        this.followRedirects = followRedirects;
    }

    // Getters
    public int getConnectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }
    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }
    public int getMaxConnections() {
        return maxConnections;
    }
    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }
    public boolean isKeepAlive() {
        return keepAlive;
    }
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    // Setters
    public void setConnectionTimeoutSeconds(int connectionTimeoutSeconds) {
        this.connectionTimeoutSeconds = connectionTimeoutSeconds;
    }
    public void setReadTimeoutSeconds(int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    // Builder
    public static ConnectionConfigDTOBuilder builder() {
        return new ConnectionConfigDTOBuilder();
    }

    public static class ConnectionConfigDTOBuilder {
        private int connectionTimeoutSeconds;
        private int readTimeoutSeconds;
        private int maxConnections;
        private int maxConnectionsPerRoute;
        private boolean keepAlive;
        private boolean followRedirects;

        public ConnectionConfigDTOBuilder connectionTimeoutSeconds(int connectionTimeoutSeconds) {
            this.connectionTimeoutSeconds = connectionTimeoutSeconds;
            return this;
        }

        public ConnectionConfigDTOBuilder readTimeoutSeconds(int readTimeoutSeconds) {
            this.readTimeoutSeconds = readTimeoutSeconds;
            return this;
        }

        public ConnectionConfigDTOBuilder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public ConnectionConfigDTOBuilder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this;
        }

        public ConnectionConfigDTOBuilder keepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public ConnectionConfigDTOBuilder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public ConnectionConfigDTO build() {
            return new ConnectionConfigDTO(connectionTimeoutSeconds, readTimeoutSeconds, maxConnections, maxConnectionsPerRoute, keepAlive, followRedirects);
        }
    }}
