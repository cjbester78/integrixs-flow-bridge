package com.integrixs.backend.domain.service;

import com.integrixs.backend.domain.repository.UserRepositoryPort;
import com.integrixs.shared.exceptions.AuthenticationException;
import com.integrixs.data.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for user authentication
 * Contains core authentication business logic
 */
@Service
public class UserAuthenticationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserAuthenticationService.class);

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAuthenticationService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticate user with username and password
     *
     * @param username Username
     * @param password Plain text password
     * @return Authenticated user
     * @throws AuthenticationException if authentication fails
     */
    public User authenticate(String username, String password) {
        log.debug("Authenticating user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("AUTH_INVALID_CREDENTIALS", "Invalid credentials"));

        if(!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", username);
            throw new AuthenticationException("AUTH_INVALID_CREDENTIALS", "Invalid credentials");
        }

        if(!user.isActive()) {
            log.warn("Login attempt for inactive user: {}", username);
            throw new AuthenticationException("AUTH_ACCOUNT_INACTIVE", "User account is not active");
        }

        log.info("User authenticated successfully: {}", username);
        return user;
    }

    /**
     * Verify if a user exists by username
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Verify if an email is already in use
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Register a new user(Admin - only function)
     * Users are created by admin staff and are immediately active
     */
    public User register(String username, String email, String password, String role) {
        log.info("Admin registering new user with username: {} and email: {}", username, email);

        // Validate username doesn't exist
        if(userExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Validate email doesn't exist
        if(emailExists(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Validate password strength
        if(password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Create new user
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setRole(role != null ? role : "USER");
        newUser.setStatus("active"); // Admin - created users are immediately active
        // Email verification is not a field in User entity
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        // Save user
        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully with ID: {} by admin", savedUser.getId());

        return savedUser;
    }
}
