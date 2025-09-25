package com.integrixs.tests.auth;

import com.integrixs.backend.api.dto.auth.LoginRequest;
import com.integrixs.backend.api.dto.auth.LoginResponse;
import com.integrixs.tests.config.MockIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import com.integrixs.data.repository.UserRepository;
import com.integrixs.data.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mock authentication tests that don't require Docker/TestContainers
 */
public class MockAuthenticationTest extends MockIntegrationTest {

    @MockBean
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private User mockAdminUser;
    
    @BeforeEach
    void setUp() {
        // Create mock admin user
        mockAdminUser = new User();
        mockAdminUser.setId(UUID.randomUUID());
        mockAdminUser.setUsername(ADMIN_USER);
        mockAdminUser.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        mockAdminUser.setEmail("admin@integrix.com");
        mockAdminUser.setFirstName("Admin");
        mockAdminUser.setLastName("User");
        mockAdminUser.setStatus("active");
        mockAdminUser.setRole("ADMINISTRATOR");
        
        // Mock repository responses
        when(userRepository.findByUsername(ADMIN_USER)).thenReturn(Optional.of(mockAdminUser));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
    
    @Test
    void testLoginWithValidCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USER);
        request.setPassword(ADMIN_PASSWORD);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value(ADMIN_USER))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        LoginResponse authResponse = objectMapper.readValue(response, LoginResponse.class);
        assertThat(authResponse.getToken()).isNotBlank();
    }
    
    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USER);
        request.setPassword("WrongPassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testAccessProtectedEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}