package com.integrixs.shared.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for User information.
 *
 * <p>This DTO is used to transfer user data between different layers of the application,
 * particularly between the REST API and service layers. It includes all user attributes
 * including authentication status, permissions, and timestamps.</p>
 *
 * @author Integration Team
 * @since 1.0.0
 * @see com.integrixs.model.User
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    /**
     * Unique identifier for the user(UUID format).
     */
    private String id;

    /**
     * Unique username for authentication.
     * Must be between 3-50 characters and contain only alphanumeric characters and underscores.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a - zA - Z0-9_] + $", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    /**
     * User's email address.
     * Used for notifications and password recovery.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * User's first name.
     */
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    /**
     * User's last name.
     */
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    /**
     * User's role identifier.
     * References the role that determines user permissions.
     */
    @NotNull(message = "Role is required")
    private String role;

    /**
     * Current status of the user account.
     * Possible values: active, inactive, pending, locked
     */
    @NotNull(message = "Status is required")
    @Pattern(regexp = "^(active|inactive|pending|locked)$", message = "Status must be one of: active, inactive, pending, locked")
    private String status;

    /**
     * Timestamp when the user account was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user account was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Timestamp of the user's last successful login.
     */
    private LocalDateTime lastLoginAt;

    /**
     * Map of user permissions organized by resource.
     * Key: resource name(e.g., "flows", "adapters")
     * Value: array of allowed actions(e.g., ["read", "write", "delete"])
     */
    private Map<String, String[]> permissions;

    /**
     * Default constructor
     */
    public UserDTO() {
    }

    /**
     * All args constructor
     */
    public UserDTO(String id, String username, String email, String firstName, String lastName,
                   String role, String status, LocalDateTime createdAt, LocalDateTime updatedAt,
                   LocalDateTime lastLoginAt, Map<String, String[]> permissions) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
        this.permissions = permissions;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public Map<String, String[]> getPermissions() { return permissions; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public void setPermissions(Map<String, String[]> permissions) { this.permissions = permissions; }

    /**
     * Builder pattern implementation
     */
    public static UserDTOBuilder builder() {
        return new UserDTOBuilder();
    }

    public static class UserDTOBuilder {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastLoginAt;
        private Map<String, String[]> permissions;

        public UserDTOBuilder id(String id) { this.id = id; return this; }
        public UserDTOBuilder username(String username) { this.username = username; return this; }
        public UserDTOBuilder email(String email) { this.email = email; return this; }
        public UserDTOBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public UserDTOBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public UserDTOBuilder role(String role) { this.role = role; return this; }
        public UserDTOBuilder status(String status) { this.status = status; return this; }
        public UserDTOBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UserDTOBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public UserDTOBuilder lastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }
        public UserDTOBuilder permissions(Map<String, String[]> permissions) { this.permissions = permissions; return this; }

        public UserDTO build() {
            return new UserDTO(id, username, email, firstName, lastName, role, status,
                             createdAt, updatedAt, lastLoginAt, permissions);
        }
    }
}
