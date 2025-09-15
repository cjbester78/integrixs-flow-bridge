package com.integrixs.backend.plugin.registry;

/**
 * Exception thrown when plugin instance creation fails
 */
public class PluginCreationException extends RuntimeException {

    public PluginCreationException(String message) {
        super(message);
    }

    public PluginCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
