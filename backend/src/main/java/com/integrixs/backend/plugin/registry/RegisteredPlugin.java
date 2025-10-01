package com.integrixs.backend.plugin.registry;

import com.integrixs.backend.plugin.api.AdapterPlugin;
import com.integrixs.backend.plugin.api.AdapterMetadata;
import java.time.LocalDateTime;

/**
 * Represents a registered plugin in the registry
 */
public class RegisteredPlugin {

    /**
     * Plugin metadata
     */
    private AdapterMetadata metadata;

    /**
     * Plugin class
     */
    private Class<? extends AdapterPlugin> pluginClass;

    /**
     * Plugin instance(lazy loaded)
     */
    private AdapterPlugin instance;

    /**
     * Current status
     */
    private PluginStatus status;

    /**
     * Registration timestamp
     */
    private LocalDateTime registeredAt = LocalDateTime.now();

    /**
     * Last activated timestamp
     */
    private LocalDateTime lastActivated;

    /**
     * Plugin JAR file location(if loaded from JAR)
     */
    private String jarPath;

    /**
     * Class loader used for this plugin
     */
    private ClassLoader classLoader;

    /**
     * Error message if failed to load
     */
    private String errorMessage;
}
