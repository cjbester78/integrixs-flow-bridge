package com.integrixs.backend.domain.service;

import com.integrixs.backend.domain.repository.UserRepository;
import com.integrixs.backend.shared.exception.AuthenticationException;
import com.integrixs.data.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Domain service for user authentication
 * Contains core authentication business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
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
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", username);
            throw new AuthenticationException("Invalid credentials");
        }
        
        if (!user.isActive()) {
            log.warn("Login attempt for inactive user: {}", username);
            throw new AuthenticationException("User account is not active");
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
}