package com.integrixs.backend.plugin.loader;

/**
 * Exception thrown when plugin loading fails
 */
public class PluginLoadException extends RuntimeException {

    public PluginLoadException(String message) {
        super(message);
    }

    public PluginLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
