package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;

/**
 * DTO representing connection status for an adapter
 */
public class ConnectionStatus {
    private Boolean isConnected;
    private String connectionState; // CONNECTED, DISCONNECTED, CONNECTING, ERROR
    private LocalDateTime connectedSince;
    private LocalDateTime lastDisconnect;
    private Long reconnectAttempts;
    private String lastError;
    private LocalDateTime lastErrorTime;
    private Integer ping; // Latency in milliseconds
    private String host;
    private Integer port;
    private String protocol;
    private String connectionDetails;
    private boolean connected;
    private LocalDateTime lastConnectionTest;
    private LocalDateTime lastSuccessfulConnection;
    private int connectionAttempts;
    private int failedAttempts;

    public ConnectionStatus() {
    }

    public ConnectionStatus(Boolean isConnected, String connectionState, LocalDateTime connectedSince,
                           LocalDateTime lastDisconnect, Long reconnectAttempts, String lastError,
                           LocalDateTime lastErrorTime, Integer ping, String host, Integer port,
                           String protocol, String connectionDetails) {
        this.isConnected = isConnected;
        this.connectionState = connectionState;
        this.connectedSince = connectedSince;
        this.lastDisconnect = lastDisconnect;
        this.reconnectAttempts = reconnectAttempts;
        this.lastError = lastError;
        this.lastErrorTime = lastErrorTime;
        this.ping = ping;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.connectionDetails = connectionDetails;
    }

    // Getters and setters
    public Boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public String getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(String connectionState) {
        this.connectionState = connectionState;
    }

    public LocalDateTime getConnectedSince() {
        return connectedSince;
    }

    public void setConnectedSince(LocalDateTime connectedSince) {
        this.connectedSince = connectedSince;
    }

    public LocalDateTime getLastDisconnect() {
        return lastDisconnect;
    }

    public void setLastDisconnect(LocalDateTime lastDisconnect) {
        this.lastDisconnect = lastDisconnect;
    }

    public Long getReconnectAttempts() {
        return reconnectAttempts;
    }

    public void setReconnectAttempts(Long reconnectAttempts) {
        this.reconnectAttempts = reconnectAttempts;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getLastErrorTime() {
        return lastErrorTime;
    }

    public void setLastErrorTime(LocalDateTime lastErrorTime) {
        this.lastErrorTime = lastErrorTime;
    }

    public Integer getPing() {
        return ping;
    }

    public void setPing(Integer ping) {
        this.ping = ping;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getConnectionDetails() {
        return connectionDetails;
    }

    public void setConnectionDetails(String connectionDetails) {
        this.connectionDetails = connectionDetails;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public LocalDateTime getLastConnectionTest() {
        return lastConnectionTest;
    }

    public void setLastConnectionTest(LocalDateTime lastConnectionTest) {
        this.lastConnectionTest = lastConnectionTest;
    }

    public LocalDateTime getLastSuccessfulConnection() {
        return lastSuccessfulConnection;
    }

    public void setLastSuccessfulConnection(LocalDateTime lastSuccessfulConnection) {
        this.lastSuccessfulConnection = lastSuccessfulConnection;
    }

    public int getConnectionAttempts() {
        return connectionAttempts;
    }

    public void setConnectionAttempts(int connectionAttempts) {
        this.connectionAttempts = connectionAttempts;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
}