package com.integrixs.backend.auth.service;

import com.integrixs.backend.auth.entity.User;
import com.integrixs.backend.security.JwtUtil;
import com.integrixs.data.sql.repository.UserSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserSqlRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            Optional<com.integrixs.data.model.User> dataUserOpt = userRepository.findByUsername(auth.getName());
            if (dataUserOpt.isPresent()) {
                com.integrixs.data.model.User dataUser = dataUserOpt.get();
                // Convert data model User to auth entity User
                User user = new User();
                user.setId(dataUser.getId());
                user.setUsername(dataUser.getUsername());
                user.setEmail(dataUser.getEmail());
                user.setRole(dataUser.getRole());
                // Set display name from first and last name if available
                String displayName = "";
                if (dataUser.getFirstName() != null) {
                    displayName = dataUser.getFirstName();
                }
                if (dataUser.getLastName() != null) {
                    displayName = displayName.isEmpty() ? dataUser.getLastName() : displayName + " " + dataUser.getLastName();
                }
                if (!displayName.isEmpty()) {
                    user.setDisplayName(displayName);
                }
                user.setActive(dataUser.isActive());
                return user;
            }
        }
        // Fallback for testing
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("placeholder");
        user.setEmail("placeholder@example.com");
        return user;
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRole());
    }

    // Methods from service version
    public Optional<com.integrixs.data.model.User> validateUser(String username, String password) {
        Optional<com.integrixs.data.model.User> userOpt = userRepository.findByUsername(username);
        if(userOpt.isPresent() && BCrypt.checkpw(password, userOpt.get().getPasswordHash())) {
            return userOpt;
        }
        return Optional.empty();
    }

    public String generateToken(String username) {
        Optional<com.integrixs.data.model.User> userOpt = userRepository.findByUsername(username);
        if(!userOpt.isPresent()) {
            throw new RuntimeException("User not found: " + username);
        }
        return jwtUtil.generateToken(username, userOpt.get().getRole());
    }

    public String refreshToken(String token) {
        String username = jwtUtil.extractUsername(token);
        Optional<com.integrixs.data.model.User> userOpt = userRepository.findByUsername(username);
        if(!userOpt.isPresent()) {
            throw new RuntimeException("User not found: " + username);
        }
        return jwtUtil.generateToken(username, userOpt.get().getRole());
    }

    public Optional<com.integrixs.data.model.User> getUserFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        return userRepository.findByUsername(username);
    }
}