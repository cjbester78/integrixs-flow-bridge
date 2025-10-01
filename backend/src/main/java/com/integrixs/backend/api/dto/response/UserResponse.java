package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for user information
 */
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String status;
    private Map<String, Object> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // Default constructor
    public UserResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Map<String, Object> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Object> permissions) {
        this.permissions = permissions;
    }

    // Builder
    public static UserResponseBuilder builder() {
        return new UserResponseBuilder();
    }

    public static class UserResponseBuilder {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private String status;
        private Map<String, Object> permissions;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastLoginAt;

        public UserResponseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UserResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserResponseBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserResponseBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserResponseBuilder role(String role) {
            this.role = role;
            return this;
        }

        public UserResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public UserResponseBuilder permissions(Map<String, Object> permissions) {
            this.permissions = permissions;
            return this;
        }

        public UserResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserResponseBuilder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public UserResponse build() {
            UserResponse response = new UserResponse();
            response.setId(this.id);
            response.setUsername(this.username);
            response.setEmail(this.email);
            response.setFirstName(this.firstName);
            response.setLastName(this.lastName);
            response.setRole(this.role);
            response.setStatus(this.status);
            response.setPermissions(this.permissions);
            response.setCreatedAt(this.createdAt);
            response.setUpdatedAt(this.updatedAt);
            response.setLastLoginAt(this.lastLoginAt);
            return response;
        }
    }
}
