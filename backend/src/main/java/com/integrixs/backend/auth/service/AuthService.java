package com.integrixs.backend.auth.service;

import com.integrixs.backend.auth.entity.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    
    public User getCurrentUser() {
        // Placeholder implementation - should get from security context
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("placeholder");
        user.setEmail("placeholder@example.com");
        return user;
    }
    
    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }
}