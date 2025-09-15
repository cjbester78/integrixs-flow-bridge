package com.integrixs.adapters.social.auth;

import java.time.LocalDateTime;

/**
 * Represents an OAuth2 token for social media authentication
 */
public class OAuth2Token {
    public OAuth2Token() {
    }


    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private LocalDateTime expiresAt;
    private String scope;
    private String idToken;

    /**
     * Check if the token is expired
     */
    public boolean isExpired() {
        if(expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Calculate and set expiration time based on expiresIn
     */
    public void calculateExpirationTime() {
        if(expiresIn != null && expiresIn > 0) {
            this.expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
        }
    }
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public String getTokenType() {
        return tokenType;
    }
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    public Long getExpiresIn() {
        return expiresIn;
    }
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    public String getScope() {
        return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }
    public String getIdToken() {
        return idToken;
    }
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private LocalDateTime expiresAt;
        private String scope;
        private String idToken;

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder idToken(String idToken) {
            this.idToken = idToken;
            return this;
        }

        public OAuth2Token build() {
            OAuth2Token obj = new OAuth2Token();
            obj.accessToken = this.accessToken;
            obj.refreshToken = this.refreshToken;
            obj.tokenType = this.tokenType;
            obj.expiresIn = this.expiresIn;
            obj.expiresAt = this.expiresAt;
            obj.scope = this.scope;
            obj.idToken = this.idToken;
            return obj;
        }
    }
}
