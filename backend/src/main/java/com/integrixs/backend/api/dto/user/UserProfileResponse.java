package com.integrixs.backend.api.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for user profile information
 */
public class UserProfileResponse {
    private UUID id;
    private String username;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserProfileResponseBuilder builder() {
        return new UserProfileResponseBuilder();
    }

            public static class UserProfileResponseBuilder {
        private UUID id;
        private String username;
        private String email;
        private String role;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public UserProfileResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public UserProfileResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserProfileResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserProfileResponseBuilder role(String role) {
            this.role = role;
            return this;
        }

        public UserProfileResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public UserProfileResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserProfileResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserProfileResponse build() {
            UserProfileResponse response = new UserProfileResponse();
            response.id = this.id;
            response.username = this.username;
            response.email = this.email;
            response.role = this.role;
            response.status = this.status;
            response.createdAt = this.createdAt;
            response.updatedAt = this.updatedAt;
            return response;
        }
    }

    // Default constructor
    public UserProfileResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
