package com.integrixs.backend.plugin.api;

/**
 * Exception thrown during plugin initialization
 */
public class PluginInitializationException extends PluginException {

    public PluginInitializationException(String message) {
        super(message);
    }

    public PluginInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
