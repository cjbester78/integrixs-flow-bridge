package com.integrixs.backend.plugin.registry;

import com.integrixs.backend.plugin.api.AdapterPlugin;
import com.integrixs.backend.plugin.api.AdapterMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing adapter plugins
 */
@Component
public class PluginRegistry {

    private static final Logger logger = LoggerFactory.getLogger(PluginRegistry.class);

    private final Map<String, AdapterPlugin> pluginInstances = new ConcurrentHashMap<>();
    private final Map<String, Class<? extends AdapterPlugin>> pluginClasses = new ConcurrentHashMap<>();
    private final Map<String, AdapterMetadata> registeredMetadata = new ConcurrentHashMap<>();
    private final Map<String, Boolean> initializedPlugins = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        logger.info("Initializing plugin registry");
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down plugin registry");
        pluginInstances.values().forEach(plugin -> {
            try {
                plugin.destroy();
            } catch(Exception e) {
                logger.error("Error destroying plugin", e);
            }
        });
        pluginInstances.clear();
    }

    /**
     * Register a plugin class with metadata
     */
    public void registerPlugin(Class<? extends AdapterPlugin> pluginClass, AdapterMetadata metadata) {
        String pluginId = metadata.getId();

        if(pluginClasses.containsKey(pluginId)) {
            throw new PluginRegistrationException("Plugin already registered: " + pluginId);
        }

        pluginClasses.put(pluginId, pluginClass);
        registeredMetadata.put(pluginId, metadata);
        initializedPlugins.put(pluginId, false);

        logger.info("Registered plugin: {} v {} by {}",
            metadata.getName(), metadata.getVersion(), metadata.getVendor());
    }

    /**
     * Get a plugin instance
     */
    public AdapterPlugin getPlugin(String pluginId) {
        // Check if instance already exists
        AdapterPlugin instance = pluginInstances.get(pluginId);
        if(instance != null) {
            return instance;
        }

        // Create new instance if class is registered
        Class<? extends AdapterPlugin> pluginClass = pluginClasses.get(pluginId);
        if(pluginClass == null) {
            return null;
        }

        synchronized(this) {
            // Double - check after acquiring lock
            instance = pluginInstances.get(pluginId);
            if(instance != null) {
                return instance;
            }

            try {
                instance = pluginClass.getDeclaredConstructor().newInstance();
                pluginInstances.put(pluginId, instance);
                return instance;
            } catch(Exception e) {
                throw new PluginCreationException("Failed to create plugin instance: " + pluginId, e);
            }
        }
    }

    /**
     * Get registered plugin metadata
     */
    public Map<String, AdapterMetadata> getRegisteredPlugins() {
        return Collections.unmodifiableMap(registeredMetadata);
    }

    /**
     * Mark plugin as initialized or not
     */
    public void markInitialized(String pluginId, boolean initialized) {
        initializedPlugins.put(pluginId, initialized);
    }

    /**
     * Check if plugin is initialized
     */
    public boolean isInitialized(String pluginId) {
        return initializedPlugins.getOrDefault(pluginId, false);
    }

    /**
     * Unregister a plugin
     */
    public void unregisterPlugin(String pluginId) {
        AdapterPlugin instance = pluginInstances.remove(pluginId);
        if(instance != null) {
            try {
                instance.destroy();
            } catch(Exception e) {
                logger.error("Error destroying plugin: " + pluginId, e);
            }
        }

        pluginClasses.remove(pluginId);
        registeredMetadata.remove(pluginId);
        initializedPlugins.remove(pluginId);

        logger.info("Unregistered plugin: {}", pluginId);
    }

    /**
     * Register a plugin using descriptor
     */
    public void registerPlugin(PluginDescriptor descriptor) throws ClassNotFoundException {
        if (descriptor.getMetadata() == null) {
            throw new PluginRegistrationException("Plugin descriptor must have metadata");
        }

        try {
            Class<?> clazz = Class.forName(descriptor.getPluginClass());
            if (!AdapterPlugin.class.isAssignableFrom(clazz)) {
                throw new PluginRegistrationException("Plugin class must implement AdapterPlugin interface");
            }

            @SuppressWarnings("unchecked")
            Class<? extends AdapterPlugin> pluginClass = (Class<? extends AdapterPlugin>) clazz;

            registerPlugin(pluginClass, descriptor.getMetadata());
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Plugin class not found: " + descriptor.getPluginClass(), e);
        }
    }
}
