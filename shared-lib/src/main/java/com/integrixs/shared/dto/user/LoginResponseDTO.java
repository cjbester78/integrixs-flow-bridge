package com.integrixs.shared.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * DTO for successful login responses.
 *
 * <p>Contains authentication token and user information after
 * successful authentication.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {

    /**
     * JWT authentication token
     */
    @NotBlank
    private String token;

    /**
     * Username of authenticated user
     */
    @NotBlank
    private String username;

    /**
     * User role(administrator, integrator, viewer)
     */
    private String role;

    /**
     * Token expiration time
     */
    private LocalDateTime expiresAt;

    /**
     * Token type(typically "Bearer")
     */
    private String tokenType = "Bearer";

    /**
     * Default constructor
     */
    public LoginResponseDTO() {
        this.tokenType = "Bearer";
    }

    /**
     * All args constructor
     */
    public LoginResponseDTO(String token, String username, String role,
                            LocalDateTime expiresAt, String tokenType) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.expiresAt = expiresAt;
        this.tokenType = tokenType;
    }

    // Getters
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public String getTokenType() { return tokenType; }

    // Setters
    public void setToken(String token) { this.token = token; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    /**
     * Builder pattern implementation
     */
    public static LoginResponseDTOBuilder builder() {
        return new LoginResponseDTOBuilder();
    }

    public static class LoginResponseDTOBuilder {
        private String token;
        private String username;
        private String role;
        private LocalDateTime expiresAt;
        private String tokenType = "Bearer";

        public LoginResponseDTOBuilder token(String token) {
            this.token = token;
            return this;
        }

        public LoginResponseDTOBuilder username(String username) {
            this.username = username;
            return this;
        }

        public LoginResponseDTOBuilder role(String role) {
            this.role = role;
            return this;
        }

        public LoginResponseDTOBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public LoginResponseDTOBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public LoginResponseDTO build() {
            return new LoginResponseDTO(token, username, role, expiresAt, tokenType);
        }
    }
}
