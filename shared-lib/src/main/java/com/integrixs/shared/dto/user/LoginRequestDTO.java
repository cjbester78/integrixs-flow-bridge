package com.integrixs.shared.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user login requests.
 *
 * <p>Contains credentials for authenticating a user in the system.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class LoginRequestDTO {

    /**
     * Username for authentication
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Password for authentication(write - only)
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * Default constructor
     */
    public LoginRequestDTO() {
    }

    /**
     * All args constructor
     */
    public LoginRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Builder pattern implementation
     */
    public static LoginRequestDTOBuilder builder() {
        return new LoginRequestDTOBuilder();
    }

    public static class LoginRequestDTOBuilder {
        private String username;
        private String password;

        public LoginRequestDTOBuilder username(String username) {
            this.username = username;
            return this;
        }

        public LoginRequestDTOBuilder password(String password) {
            this.password = password;
            return this;
        }

        public LoginRequestDTO build() {
            return new LoginRequestDTO(username, password);
        }
    }
}
