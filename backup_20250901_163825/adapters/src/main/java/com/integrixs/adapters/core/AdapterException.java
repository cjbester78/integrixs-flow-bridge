package com.integrixs.adapters.core;

import com.integrixs.adapters.domain.model.AdapterConfiguration;

/**
 * Base exception class for all adapter-related errors.
 */
public class AdapterException extends Exception {
    
    private final AdapterConfiguration.AdapterTypeEnum adapterType;
    private final AdapterConfiguration.AdapterModeEnum adapterMode;
    private final String errorCode;
    
    public AdapterException(String message) {
        super(message);
        this.adapterType = null;
        this.adapterMode = null;
        this.errorCode = null;
    }
    
    public AdapterException(String message, Throwable cause) {
        super(message, cause);
        this.adapterType = null;
        this.adapterMode = null;
        this.errorCode = null;
    }
    
    public AdapterException(AdapterConfiguration.AdapterTypeEnum adapterType, AdapterConfiguration.AdapterModeEnum adapterMode, String message) {
        super(message);
        this.adapterType = adapterType;
        this.adapterMode = adapterMode;
        this.errorCode = null;
    }
    
    public AdapterException(AdapterConfiguration.AdapterTypeEnum adapterType, AdapterConfiguration.AdapterModeEnum adapterMode, String message, Throwable cause) {
        super(message, cause);
        this.adapterType = adapterType;
        this.adapterMode = adapterMode;
        this.errorCode = null;
    }
    
    public AdapterException(AdapterConfiguration.AdapterTypeEnum adapterType, AdapterConfiguration.AdapterModeEnum adapterMode, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.adapterType = adapterType;
        this.adapterMode = adapterMode;
        this.errorCode = errorCode;
    }
    
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return adapterType;
    }
    
    public AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return adapterMode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Connection-related exceptions
     */
    public static class ConnectionException extends AdapterException {
        public ConnectionException(AdapterConfiguration.AdapterTypeEnum adapterType, String message) {
            super(adapterType, null, "CONNECTION_ERROR", message, null);
        }
        
        public ConnectionException(AdapterConfiguration.AdapterTypeEnum adapterType, String message, Throwable cause) {
            super(adapterType, null, "CONNECTION_ERROR", message, cause);
        }
    }
    
    /**
     * Authentication-related exceptions
     */
    public static class AuthenticationException extends AdapterException {
        public AuthenticationException(AdapterConfiguration.AdapterTypeEnum adapterType, String message) {
            super(adapterType, null, "AUTH_ERROR", message, null);
        }
        
        public AuthenticationException(AdapterConfiguration.AdapterTypeEnum adapterType, String message, Throwable cause) {
            super(adapterType, null, "AUTH_ERROR", message, cause);
        }
    }
    
    /**
     * Configuration-related exceptions
     */
    public static class ConfigurationException extends AdapterException {
        public ConfigurationException(AdapterConfiguration.AdapterTypeEnum adapterType, String message) {
            super(adapterType, null, "CONFIG_ERROR", message, null);
        }
        
        public ConfigurationException(AdapterConfiguration.AdapterTypeEnum adapterType, String message, Throwable cause) {
            super(adapterType, null, "CONFIG_ERROR", message, cause);
        }
    }
    
    /**
     * Data validation exceptions
     */
    public static class ValidationException extends AdapterException {
        public ValidationException(AdapterConfiguration.AdapterTypeEnum adapterType, String message) {
            super(adapterType, null, "VALIDATION_ERROR", message, null);
        }
        
        public ValidationException(AdapterConfiguration.AdapterTypeEnum adapterType, String message, Throwable cause) {
            super(adapterType, null, "VALIDATION_ERROR", message, cause);
        }
    }
    
    /**
     * Circuit breaker exceptions
     */
    public static class CircuitBreakerException extends AdapterException {
        public CircuitBreakerException(AdapterConfiguration.AdapterTypeEnum adapterType, String message) {
            super(adapterType, null, "CIRCUIT_BREAKER", message, null);
        }
        
        public CircuitBreakerException(AdapterConfiguration.AdapterTypeEnum adapterType, String message, Throwable cause) {
            super(adapterType, null, "CIRCUIT_BREAKER", message, cause);
        }
    }
    
    /**
     * Timeout exceptions
     */
    public static class TimeoutException extends AdapterException {
        public TimeoutException(AdapterConfiguration.AdapterTypeEnum adapterType, String message) {
            super(adapterType, null, "TIMEOUT_ERROR", message, null);
        }
        
        public TimeoutException(AdapterConfiguration.AdapterTypeEnum adapterType, String message, Throwable cause) {
            super(adapterType, null, "TIMEOUT_ERROR", message, cause);
        }
    }
    
    /**
     * Processing exceptions
     */
    public static class ProcessingException extends AdapterException {
        public ProcessingException(AdapterConfiguration.AdapterTypeEnum adapterType, String message) {
            super(adapterType, null, "PROCESSING_ERROR", message, null);
        }
        
        public ProcessingException(AdapterConfiguration.AdapterTypeEnum adapterType, String message, Throwable cause) {
            super(adapterType, null, "PROCESSING_ERROR", message, cause);
        }
    }
    
    /**
     * Operation exceptions
     */
    public static class OperationException extends AdapterException {
        public OperationException(AdapterConfiguration.AdapterTypeEnum adapterType, String message) {
            super(adapterType, null, "OPERATION_ERROR", message, null);
        }
        
        public OperationException(AdapterConfiguration.AdapterTypeEnum adapterType, String message, Throwable cause) {
            super(adapterType, null, "OPERATION_ERROR", message, cause);
        }
    }
}