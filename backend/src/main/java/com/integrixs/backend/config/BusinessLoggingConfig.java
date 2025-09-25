package com.integrixs.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for business logging features.
 */
@ConfigurationProperties(prefix = "logging.business")
public class BusinessLoggingConfig {

    /**
     * Enable/disable business operation logging
     */
    private boolean enableOperationLogging = true;

    /**
     * Enable/disable detailed authentication logging
     */
    private boolean enableAuthenticationDetails = true;

    /**
     * Enable/disable flow execution details logging
     */
    private boolean enableFlowExecutionDetails = true;

    /**
     * Enable/disable transformation step logging
     */
    private boolean enableTransformationSteps = true;

    /**
     * Enable/disable adapter communication logging
     */
    private boolean enableAdapterCommunication = true;

    /**
     * Enable/disable performance metrics in logs
     */
    private boolean enablePerformanceMetrics = true;

    /**
     * Enable/disable message routing logging
     */
    private boolean enableMessageRouting = true;

    /**
     * Maximum length for logged payloads(truncate if longer)
     */
    private int maxPayloadLength = 1000;

    /**
     * Include stack traces in error logs
     */
    private boolean includeStackTraces = true;

    /**
     * Log sensitive data(should be false in production)
     */
    private boolean logSensitiveData = false;

    /**
     * Enable structured logging format
     */
    private boolean enableStructuredFormat = true;

    /**
     * Performance logging threshold in milliseconds
     */
    private long performanceThresholdMs = 1000;

    /**
     * Correlation ID header name
     */
    private String correlationIdHeader = "X - Correlation - ID";

    /**
     * MDC(Mapped Diagnostic Context) configuration
     */
    private MdcConfig mdc = new MdcConfig();

        public static class MdcConfig {
        /**
         * Enable MDC propagation
         */
        private boolean enabled = true;

        /**
         * Include user ID in MDC
         */
        private boolean includeUserId = true;

        /**
         * Include flow ID in MDC
         */
        private boolean includeFlowId = true;

        /**
         * Include tenant ID in MDC
         */
        private boolean includeTenantId = true;

        /**
         * Include session ID in MDC
         */
        private boolean includeSessionId = true;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludeUserId() {
            return includeUserId;
        }

        public void setIncludeUserId(boolean includeUserId) {
            this.includeUserId = includeUserId;
        }

        public boolean isIncludeFlowId() {
            return includeFlowId;
        }

        public void setIncludeFlowId(boolean includeFlowId) {
            this.includeFlowId = includeFlowId;
        }

        public boolean isIncludeTenantId() {
            return includeTenantId;
        }

        public void setIncludeTenantId(boolean includeTenantId) {
            this.includeTenantId = includeTenantId;
        }

        public boolean isIncludeSessionId() {
            return includeSessionId;
        }

        public void setIncludeSessionId(boolean includeSessionId) {
            this.includeSessionId = includeSessionId;
        }
    }

    // Getters and Setters
    public boolean isEnableOperationLogging() {
        return enableOperationLogging;
    }

    public void setEnableOperationLogging(boolean enableOperationLogging) {
        this.enableOperationLogging = enableOperationLogging;
    }

    public boolean isEnableAuthenticationDetails() {
        return enableAuthenticationDetails;
    }

    public void setEnableAuthenticationDetails(boolean enableAuthenticationDetails) {
        this.enableAuthenticationDetails = enableAuthenticationDetails;
    }

    public boolean isEnableFlowExecutionDetails() {
        return enableFlowExecutionDetails;
    }

    public void setEnableFlowExecutionDetails(boolean enableFlowExecutionDetails) {
        this.enableFlowExecutionDetails = enableFlowExecutionDetails;
    }

    public boolean isEnableTransformationSteps() {
        return enableTransformationSteps;
    }

    public void setEnableTransformationSteps(boolean enableTransformationSteps) {
        this.enableTransformationSteps = enableTransformationSteps;
    }

    public boolean isEnableAdapterCommunication() {
        return enableAdapterCommunication;
    }

    public void setEnableAdapterCommunication(boolean enableAdapterCommunication) {
        this.enableAdapterCommunication = enableAdapterCommunication;
    }

    public boolean isEnablePerformanceMetrics() {
        return enablePerformanceMetrics;
    }

    public void setEnablePerformanceMetrics(boolean enablePerformanceMetrics) {
        this.enablePerformanceMetrics = enablePerformanceMetrics;
    }

    public boolean isEnableMessageRouting() {
        return enableMessageRouting;
    }

    public void setEnableMessageRouting(boolean enableMessageRouting) {
        this.enableMessageRouting = enableMessageRouting;
    }

    public int getMaxPayloadLength() {
        return maxPayloadLength;
    }

    public void setMaxPayloadLength(int maxPayloadLength) {
        this.maxPayloadLength = maxPayloadLength;
    }

    public boolean isIncludeStackTraces() {
        return includeStackTraces;
    }

    public void setIncludeStackTraces(boolean includeStackTraces) {
        this.includeStackTraces = includeStackTraces;
    }

    public boolean isLogSensitiveData() {
        return logSensitiveData;
    }

    public void setLogSensitiveData(boolean logSensitiveData) {
        this.logSensitiveData = logSensitiveData;
    }

    public boolean isEnableStructuredFormat() {
        return enableStructuredFormat;
    }

    public void setEnableStructuredFormat(boolean enableStructuredFormat) {
        this.enableStructuredFormat = enableStructuredFormat;
    }

    public long getPerformanceThresholdMs() {
        return performanceThresholdMs;
    }

    public void setPerformanceThresholdMs(long performanceThresholdMs) {
        this.performanceThresholdMs = performanceThresholdMs;
    }

    public String getCorrelationIdHeader() {
        return correlationIdHeader;
    }

    public void setCorrelationIdHeader(String correlationIdHeader) {
        this.correlationIdHeader = correlationIdHeader;
    }

    public MdcConfig getMdc() {
        return mdc;
    }

    public void setMdc(MdcConfig mdc) {
        this.mdc = mdc;
    }
}
