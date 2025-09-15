package com.integrix.tests.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all integration tests
 */
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Testcontainers
public abstract class BaseIntegrationTest {

    protected static final String TEST_USER = "test - user";
    protected static final String TEST_PASSWORD = "test - password";
    protected static final String TEST_ROLE = "ADMINISTRATOR";

    /**
     * Common test setup that can be overridden by subclasses
     */
    protected void commonSetup() {
        // Common setup logic
    }

    /**
     * Common test cleanup that can be overridden by subclasses
     */
    protected void commonCleanup() {
        // Common cleanup logic
    }
}
