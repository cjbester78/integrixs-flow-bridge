package com.integrixs.tests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for mock integration tests that don't require Docker
 */
@SpringBootTest(classes = com.integrixs.backend.BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("mock-test")
public abstract class MockIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected static final String TEST_USER = "testuser";
    protected static final String TEST_PASSWORD = "Test123!";
    protected static final String TEST_EMAIL = "test@integrix.com";
    protected static final String ADMIN_USER = "admin";
    protected static final String ADMIN_PASSWORD = "Admin123!";
    
    protected String authToken;
    protected String adminToken;

    /**
     * Helper method to get auth header
     */
    protected String getAuthHeader(String token) {
        return "Bearer " + token;
    }
}