package com.integrixs.backend.plugin.api;

/**
 * Base exception for plugin - related errors
 */
public class PluginException extends RuntimeException {

    private final String errorCode;
    private final boolean retryable;

    public PluginException(String message) {
        this(message, null, null, false);
    }

    public PluginException(String message, Throwable cause) {
        this(message, null, cause, false);
    }

    public PluginException(String message, String errorCode, Throwable cause, boolean retryable) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Create a retryable exception
     */
    public static PluginException retryable(String message, Throwable cause) {
        return new PluginException(message, null, cause, true);
    }
}
