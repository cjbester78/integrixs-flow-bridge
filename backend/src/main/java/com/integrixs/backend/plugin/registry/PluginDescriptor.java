package com.integrixs.backend.plugin.registry;

import java.util.Properties;
import com.integrixs.backend.plugin.api.AdapterMetadata;

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
     * Plugin metadata
     */
    private AdapterMetadata metadata;

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

    // Getters and Setters
    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getPluginClass() {
        return pluginClass;
    }

    public void setPluginClass(String pluginClass) {
        this.pluginClass = pluginClass;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public AdapterMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AdapterMetadata metadata) {
        this.metadata = metadata;
    }

    // Builder pattern
    public static PluginDescriptorBuilder builder() {
        return new PluginDescriptorBuilder();
    }

    public static class PluginDescriptorBuilder {
        private String pluginId;
        private String pluginClass;
        private String jarPath;
        private Properties properties;
        private AdapterMetadata metadata;

        public PluginDescriptorBuilder pluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        public PluginDescriptorBuilder pluginClass(String pluginClass) {
            this.pluginClass = pluginClass;
            return this;
        }

        public PluginDescriptorBuilder jarPath(String jarPath) {
            this.jarPath = jarPath;
            return this;
        }

        public PluginDescriptorBuilder properties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public PluginDescriptorBuilder metadata(AdapterMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public PluginDescriptor build() {
            PluginDescriptor descriptor = new PluginDescriptor();
            descriptor.pluginId = this.pluginId;
            descriptor.pluginClass = this.pluginClass;
            descriptor.jarPath = this.jarPath;
            descriptor.properties = this.properties;
            descriptor.metadata = this.metadata;
            return descriptor;
        }
    }
}
