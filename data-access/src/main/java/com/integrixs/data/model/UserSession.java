package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Entity representing UserSession.
 * This maps to the corresponding table in the database.
 */
public class UserSession {

        /** Unique identifier(UUID) for the entity */
    private UUID id;

    private User user;

    private String refreshToken;

    private LocalDateTime expiresAt;

    /** Timestamp of entity creation */
    private LocalDateTime createdAt;

    private LocalDateTime lastUsedAt;

    private String ipAddress;

    private String userAgent;

    private boolean invalidated = false;

    private int version;

    // --- Getters and Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
    /** Timestamp of entity creation */
        return createdAt;
    }

    /** Timestamp of entity creation */
    public void setCreatedAt(LocalDateTime createdAt) {
    /** Timestamp of entity creation */
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isInvalidated() {
        return invalidated;
    }

    public void setInvalidated(boolean invalidated) {
        this.invalidated = invalidated;
    }

}
