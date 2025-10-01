package com.integrixs.shared.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for refresh token requests.
 *
 * <p>Used to request a new access token using a refresh token.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class RefreshTokenRequestDTO {

    /**
     * The refresh token
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    /**
     * Default constructor
     */
    public RefreshTokenRequestDTO() {
    }

    /**
     * All args constructor
     */
    public RefreshTokenRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getter
    public String getRefreshToken() {
        return refreshToken;
    }

    // Setter
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Builder pattern implementation
     */
    public static RefreshTokenRequestDTOBuilder builder() {
        return new RefreshTokenRequestDTOBuilder();
    }

    public static class RefreshTokenRequestDTOBuilder {
        private String refreshToken;

        public RefreshTokenRequestDTOBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public RefreshTokenRequestDTO build() {
            return new RefreshTokenRequestDTO(refreshToken);
        }
    }
}
