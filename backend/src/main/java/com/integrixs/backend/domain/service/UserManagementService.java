package com.integrixs.backend.domain.service;

import com.integrixs.data.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for user management logic
 */
@Service
public class UserManagementService {

    private final PasswordEncoder passwordEncoder;

    public UserManagementService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user
     */
    public User createUser(String username, String email, String firstName,
                          String lastName, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus("active");
        user.setPermissions(" {}");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    /**
     * Updates user information
     */
    public void updateUserInfo(User user, String email, String firstName,
                              String lastName, String role, String status) {
        if(email != null) {
            user.setEmail(email);
        }
        if(firstName != null) {
            user.setFirstName(firstName);
        }
        if(lastName != null) {
            user.setLastName(lastName);
        }
        if(role != null) {
            user.setRole(role);
        }
        if(status != null) {
            user.setStatus(status);
        }
        user.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Changes user password
     */
    public void changePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Validates password against stored hash
     */
    public boolean validatePassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    /**
     * Activates a user
     */
    public void activateUser(User user) {
        user.setStatus("active");
        user.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Deactivates a user
     */
    public void deactivateUser(User user) {
        user.setStatus("inactive");
        user.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Updates last login time
     */
    public void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
    }

    /**
     * Validates if user can be deleted
     */
    public void validateDeletion(User user) {
        if("system".equals(user.getUsername())) {
            throw new IllegalStateException("Cannot delete system user");
        }
    }
}
