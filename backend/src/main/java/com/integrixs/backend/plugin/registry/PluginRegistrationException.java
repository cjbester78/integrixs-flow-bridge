package com.integrixs.backend.plugin.registry;

/**
 * Exception thrown during plugin registration
 */
public class PluginRegistrationException extends RuntimeException {

    public PluginRegistrationException(String message) {
        super(message);
    }

    public PluginRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
