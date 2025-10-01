package com.integrixs.data.model;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a system user.
 *
 * <p>This entity maps to the 'users' table and contains all user
 * authentication and profile information.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class User {

    /**
     * Unique identifier(UUID) for the entity
     */
    private UUID id;

    /**
     * Unique username for authentication
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    /**
     * User email address
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    /**
     * Hashed password(BCrypt)
     */
    @NotBlank(message = "Password is required")
    private String passwordHash;

    /**
     * User's first name
     */
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    /**
     * User's last name
     */
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    /**
     * Role ID reference
     */
    private UUID roleId;

    /**
     * User role(ADMINISTRATOR, DEVELOPER, INTEGRATOR, VIEWER)
     */
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(ADMINISTRATOR|DEVELOPER|INTEGRATOR|VIEWER|administrator|developer|integrator|viewer)$",
             message = "Role must be ADMINISTRATOR, DEVELOPER, INTEGRATOR, or VIEWER")
    private String role;

    /**
     * User account status(active, inactive, locked)
     */
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(active|inactive|locked)$",
             message = "Status must be active, inactive, or locked")
    private String status = "active";

    /**
     * JSON string containing user permissions
     */
    private String permissions;

    /**
     * Tenant ID for multi-tenancy support
     */
    private UUID tenantId;

    /**
     * Timestamp of entity creation
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp of last entity update
     */
    private LocalDateTime updatedAt;

    /**
     * Timestamp of last successful login
     */
    private LocalDateTime lastLoginAt;

    /**
     * Token for password reset
     */
    private String passwordResetToken;

    /**
     * Expiration time for password reset token
     */
    private LocalDateTime passwordResetExpiresAt;

    /**
     * Lifecycle callback to ensure timestamps are set
     */

    /**
     * Check if user account is active
     */
    public boolean isActive() {
        return "active".equals(status);
    }

    // Default constructor
    public User() {
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
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

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
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

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public LocalDateTime getPasswordResetExpiresAt() {
        return passwordResetExpiresAt;
    }

    public void setPasswordResetExpiresAt(LocalDateTime passwordResetExpiresAt) {
        this.passwordResetExpiresAt = passwordResetExpiresAt;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    // Builder
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private UUID id;
        private String username;
        private String email;
        private String passwordHash;
        private String firstName;
        private String lastName;
        private UUID roleId;
        private String role;
        private String status;
        private String permissions;
        private UUID tenantId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastLoginAt;
        private String passwordResetToken;
        private LocalDateTime passwordResetExpiresAt;

        public UserBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder roleId(UUID roleId) {
            this.roleId = roleId;
            return this;
        }

        public UserBuilder role(String role) {
            this.role = role;
            return this;
        }

        public UserBuilder status(String status) {
            this.status = status;
            return this;
        }

        public UserBuilder permissions(String permissions) {
            this.permissions = permissions;
            return this;
        }

        public UserBuilder tenantId(UUID tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public UserBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserBuilder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public UserBuilder passwordResetToken(String passwordResetToken) {
            this.passwordResetToken = passwordResetToken;
            return this;
        }

        public UserBuilder passwordResetExpiresAt(LocalDateTime passwordResetExpiresAt) {
            this.passwordResetExpiresAt = passwordResetExpiresAt;
            return this;
        }

        public User build() {
            User instance = new User();
            instance.setId(this.id);
            instance.setUsername(this.username);
            instance.setEmail(this.email);
            instance.setPasswordHash(this.passwordHash);
            instance.setFirstName(this.firstName);
            instance.setLastName(this.lastName);
            instance.setRoleId(this.roleId);
            instance.setRole(this.role);
            instance.setStatus(this.status);
            instance.setPermissions(this.permissions);
            instance.setTenantId(this.tenantId);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            instance.setLastLoginAt(this.lastLoginAt);
            instance.setPasswordResetToken(this.passwordResetToken);
            instance.setPasswordResetExpiresAt(this.passwordResetExpiresAt);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id + "username=" + username + "email=" + email + "firstName=" + firstName + "lastName=" + lastName + "..." +
                '}';
    }
}
