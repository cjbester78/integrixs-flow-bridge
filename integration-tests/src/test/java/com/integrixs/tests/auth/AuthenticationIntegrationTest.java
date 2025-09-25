package com.integrixs.tests.auth;

import com.integrixs.backend.api.dto.auth.LoginRequest;
import com.integrixs.backend.api.dto.auth.LoginResponse;
import com.integrixs.backend.api.dto.request.CreateUserRequest;
import com.integrixs.tests.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Authentication functionality
 */
public class AuthenticationIntegrationTest extends BaseIntegrationTest {

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
    void testCreateUserRequiresAuthentication() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@integrix.com");
        request.setPassword("NewUser123!");
        request.setFirstName("New");
        request.setLastName("User");

        // When & Then - Creating user without auth should fail
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // Removed duplicate user test as registration is handled through admin-only user creation

    @Test
    void testAccessProtectedEndpointWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessProtectedEndpointWithAuth() throws Exception {
        // Given - Login first
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(ADMIN_USER);
        loginRequest.setPassword(ADMIN_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), 
                LoginResponse.class);
        
        // When & Then - Access protected endpoint with token
        mockMvc.perform(get("/api/users")
                .header("Authorization", getAuthHeader(authResponse.getToken())))
                .andExpect(status().isOk());
    }

    @Test
    void testTokenRefresh() throws Exception {
        // Given - Login first
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(ADMIN_USER);
        loginRequest.setPassword(ADMIN_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), 
                LoginResponse.class);

        // When & Then - Refresh token
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", authResponse.getRefreshToken()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testLogout() throws Exception {
        // Given - Login first
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(ADMIN_USER);
        loginRequest.setPassword(ADMIN_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), 
                LoginResponse.class);

        // When - Logout
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", getAuthHeader(authResponse.getToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", authResponse.getRefreshToken()))))
                .andExpect(status().isOk());

        // Then - Token should no longer be valid
        mockMvc.perform(get("/api/users")
                .header("Authorization", getAuthHeader(authResponse.getToken())))
                .andExpect(status().isUnauthorized());
    }
}