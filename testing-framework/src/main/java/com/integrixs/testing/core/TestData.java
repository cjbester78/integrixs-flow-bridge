package com.integrixs.testing.core;

import java.lang.annotation.*;

/**
 * Annotation to specify test data file for a test method
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestData {
    /**
     * Path to the test data file relative to test data directory
     */
    String value();
    
    /**
     * Format of the test data file (auto-detected if not specified)
     */
    String format() default "";
    
    /**
     * Whether to load the data before test execution
     */
    boolean preload() default true;
}