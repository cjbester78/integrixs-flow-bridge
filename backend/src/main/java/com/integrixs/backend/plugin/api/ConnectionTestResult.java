package com.integrixs.backend.plugin.api;

import java.time.Duration;
import java.util.Map;

/**
 * Result of a connection test
 */
public class ConnectionTestResult {

    /**
     * Whether the connection test was successful
     */
    private boolean successful;

    /**
     * Human - readable status message
     */
    private String message;

    /**
     * Error details if test failed
     */
    private String errorDetails;

    /**
     * Response time for the test
     */
    private Duration responseTime;

    /**
     * Additional diagnostic information
     */
    private Map<String, Object> diagnostics;

    /**
     * Server/system information if available
     */
    private SystemInfo systemInfo;

    /**
     * Static factory for successful result
     */
    public static ConnectionTestResult success(String message) {
        return ConnectionTestResult.builder()
                .successful(true)
                .message(message)
                .build();
    }

    /**
     * Static factory for failed result
     */
    public static ConnectionTestResult failure(String message, String errorDetails) {
        return ConnectionTestResult.builder()
                .successful(false)
                .message(message)
                .errorDetails(errorDetails)
                .build();
    }

    /**
     * System information
     */
            public static class SystemInfo {
        private String name;
        private String version;
        private String vendor;
        private Map<String, String> properties;
    }
}
