package com.integrixs.backend.api.dto.auth;

import com.integrixs.shared.dto.user.UserDTO;
/**
 * Login response DTO
 */
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private UserDTO user;

    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public static class LoginResponseBuilder {
        private String token;
        private String refreshToken;
        private Long expiresIn;
        private UserDTO user;

        public LoginResponseBuilder token(String token) {
            this.token = token;
            return this;
        }

        public LoginResponseBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public LoginResponseBuilder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public LoginResponseBuilder user(UserDTO user) {
            this.user = user;
            return this;
        }

        public LoginResponse build() {
            LoginResponse response = new LoginResponse();
            response.token = this.token;
            response.refreshToken = this.refreshToken;
            response.expiresIn = this.expiresIn;
            response.user = this.user;
            return response;
        }
    }

    // Default constructor
    public LoginResponse() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
