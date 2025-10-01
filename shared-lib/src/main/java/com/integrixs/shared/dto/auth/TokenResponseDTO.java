package com.integrixs.shared.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO for JWT token responses.
 *
 * <p>Contains both access and refresh tokens with expiry information.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponseDTO {

    /**
     * JWT access token
     */
    private String accessToken;

    /**
     * Refresh token for obtaining new access tokens
     */
    private String refreshToken;

    /**
     * Token type(typically "Bearer")
     */
    private String tokenType = "Bearer";

    /**
     * Access token expiry time in seconds
     */
    private Long expiresIn;

    /**
     * Refresh token expiry time in seconds
     */
    private Long refreshExpiresIn;

    /**
     * Access token expiration timestamp
     */
    private LocalDateTime accessTokenExpiresAt;

    /**
     * Refresh token expiration timestamp
     */
    private LocalDateTime refreshTokenExpiresAt;

    /**
     * User's username
     */
    private String username;

    /**
     * User's role
     */
    private String role;

    /**
     * Default constructor
     */
    public TokenResponseDTO() {
        this.tokenType = "Bearer";
    }

    /**
     * All args constructor
     */
    public TokenResponseDTO(String accessToken, String refreshToken, String tokenType,
                           Long expiresIn, Long refreshExpiresIn, LocalDateTime accessTokenExpiresAt,
                           LocalDateTime refreshTokenExpiresAt, String username, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.username = username;
        this.role = role;
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public Long getExpiresIn() { return expiresIn; }
    public Long getRefreshExpiresIn() { return refreshExpiresIn; }
    public LocalDateTime getAccessTokenExpiresAt() { return accessTokenExpiresAt; }
    public LocalDateTime getRefreshTokenExpiresAt() { return refreshTokenExpiresAt; }
    public String getUsername() { return username; }
    public String getRole() { return role; }

    // Setters
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public void setRefreshExpiresIn(Long refreshExpiresIn) { this.refreshExpiresIn = refreshExpiresIn; }
    public void setAccessTokenExpiresAt(LocalDateTime accessTokenExpiresAt) { this.accessTokenExpiresAt = accessTokenExpiresAt; }
    public void setRefreshTokenExpiresAt(LocalDateTime refreshTokenExpiresAt) { this.refreshTokenExpiresAt = refreshTokenExpiresAt; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }

    /**
     * Builder pattern implementation
     */
    public static TokenResponseDTOBuilder builder() {
        return new TokenResponseDTOBuilder();
    }

    public static class TokenResponseDTOBuilder {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private Long expiresIn;
        private Long refreshExpiresIn;
        private LocalDateTime accessTokenExpiresAt;
        private LocalDateTime refreshTokenExpiresAt;
        private String username;
        private String role;

        public TokenResponseDTOBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public TokenResponseDTOBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public TokenResponseDTOBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public TokenResponseDTOBuilder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public TokenResponseDTOBuilder refreshExpiresIn(Long refreshExpiresIn) {
            this.refreshExpiresIn = refreshExpiresIn;
            return this;
        }

        public TokenResponseDTOBuilder accessTokenExpiresAt(LocalDateTime accessTokenExpiresAt) {
            this.accessTokenExpiresAt = accessTokenExpiresAt;
            return this;
        }

        public TokenResponseDTOBuilder refreshTokenExpiresAt(LocalDateTime refreshTokenExpiresAt) {
            this.refreshTokenExpiresAt = refreshTokenExpiresAt;
            return this;
        }

        public TokenResponseDTOBuilder username(String username) {
            this.username = username;
            return this;
        }

        public TokenResponseDTOBuilder role(String role) {
            this.role = role;
            return this;
        }

        public TokenResponseDTO build() {
            return new TokenResponseDTO(accessToken, refreshToken, tokenType, expiresIn,
                                       refreshExpiresIn, accessTokenExpiresAt, refreshTokenExpiresAt,
                                       username, role);
        }
    }
}
