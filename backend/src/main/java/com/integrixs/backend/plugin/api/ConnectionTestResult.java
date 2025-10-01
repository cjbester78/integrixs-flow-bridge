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

    // Getters and Setters
    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Duration getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Duration responseTime) {
        this.responseTime = responseTime;
    }

    public Map<String, Object> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(Map<String, Object> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

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

    // Builder pattern
    public static ConnectionTestResultBuilder builder() {
        return new ConnectionTestResultBuilder();
    }

    public static class ConnectionTestResultBuilder {
        private boolean successful;
        private String message;
        private String errorDetails;
        private Duration responseTime;
        private Map<String, Object> diagnostics;
        private SystemInfo systemInfo;

        public ConnectionTestResultBuilder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public ConnectionTestResultBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ConnectionTestResultBuilder errorDetails(String errorDetails) {
            this.errorDetails = errorDetails;
            return this;
        }

        public ConnectionTestResultBuilder responseTime(Duration responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public ConnectionTestResultBuilder diagnostics(Map<String, Object> diagnostics) {
            this.diagnostics = diagnostics;
            return this;
        }

        public ConnectionTestResultBuilder systemInfo(SystemInfo systemInfo) {
            this.systemInfo = systemInfo;
            return this;
        }

        public ConnectionTestResult build() {
            ConnectionTestResult result = new ConnectionTestResult();
            result.successful = this.successful;
            result.message = this.message;
            result.errorDetails = this.errorDetails;
            result.responseTime = this.responseTime;
            result.diagnostics = this.diagnostics;
            result.systemInfo = this.systemInfo;
            return result;
        }
    }

    /**
     * System information
     */
    public static class SystemInfo {
        private String name;
        private String version;
        private String vendor;
        private Map<String, String> properties;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getVendor() {
            return vendor;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }

        // Builder pattern
        public static SystemInfoBuilder builder() {
            return new SystemInfoBuilder();
        }

        public static class SystemInfoBuilder {
            private String name;
            private String version;
            private String vendor;
            private Map<String, String> properties;

            public SystemInfoBuilder name(String name) {
                this.name = name;
                return this;
            }

            public SystemInfoBuilder version(String version) {
                this.version = version;
                return this;
            }

            public SystemInfoBuilder vendor(String vendor) {
                this.vendor = vendor;
                return this;
            }

            public SystemInfoBuilder properties(Map<String, String> properties) {
                this.properties = properties;
                return this;
            }

            public SystemInfo build() {
                SystemInfo info = new SystemInfo();
                info.name = this.name;
                info.version = this.version;
                info.vendor = this.vendor;
                info.properties = this.properties;
                return info;
            }
        }
    }
}
