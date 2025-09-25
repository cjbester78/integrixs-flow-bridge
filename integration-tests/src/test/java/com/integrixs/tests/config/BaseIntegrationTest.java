package com.integrixs.tests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all integration tests
 */
@SpringBootTest(classes = com.integrixs.backend.BackendApplication.class)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

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

    @BeforeEach
    protected void baseSetUp() throws Exception {
        // Override in subclasses if needed
    }

    /**
     * Helper method to get auth header
     */
    protected String getAuthHeader(String token) {
        return "Bearer " + token;
    }

    /**
     * Helper method to perform login and get token
     */
    protected String loginAndGetToken(String username, String password) throws Exception {
        // This will be implemented when we create the authentication test
        return "mock-token";
    }
}