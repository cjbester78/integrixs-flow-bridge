package com.integrixs.backend.plugin.registry;

import java.util.Properties;

/**
 * Descriptor for a discovered plugin
 */
public class PluginDescriptor {

    /**
     * Unique plugin ID
     */
    private String pluginId;

    /**
     * Fully qualified plugin class name
     */
    private String pluginClass;

    /**
     * Path to JAR file(if loaded from JAR)
     */
    private String jarPath;

    /**
     * Plugin properties from descriptor file
     */
    private Properties properties;

    /**
     * Get a property value
     */
    public String getProperty(String key) {
        return properties != null ? properties.getProperty(key) : null;
    }

    /**
     * Get a property value with default
     */
    public String getProperty(String key, String defaultValue) {
        return properties != null ? properties.getProperty(key, defaultValue) : defaultValue;
    }
}
