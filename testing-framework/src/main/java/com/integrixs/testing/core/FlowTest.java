package com.integrixs.testing.core;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Annotation to mark a test class as an integration flow test.
 * This enables the Integrixs testing extensions and utilities.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(IntegrixsTestExtension.class)
public @interface FlowTest {
    
    /**
     * The flow definition file to test
     */
    String flow() default "";
    
    /**
     * Environment to run the test in
     */
    String environment() default "test";
    
    /**
     * Whether to start embedded servers
     */
    boolean useEmbeddedServers() default true;
    
    /**
     * Whether to use real adapters or mocks
     */
    boolean useMockAdapters() default true;
    
    /**
     * Test data directory
     */
    String testDataDir() default "src/test/resources/test-data";
    
    /**
     * Enable performance metrics collection
     */
    boolean collectMetrics() default false;
    
    /**
     * Timeout for flow execution in seconds
     */
    int timeout() default 30;
}