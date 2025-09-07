package com.integrixs.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for business logging features.
 */
@Data
@Configuration
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
     * Maximum length for logged payloads (truncate if longer)
     */
    private int maxPayloadLength = 1000;
    
    /**
     * Include stack traces in error logs
     */
    private boolean includeStackTraces = true;
    
    /**
     * Log sensitive data (should be false in production)
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
    private String correlationIdHeader = "X-Correlation-ID";
    
    /**
     * MDC (Mapped Diagnostic Context) configuration
     */
    private MdcConfig mdc = new MdcConfig();
    
    @Data
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
    }
}