package com.integrixs.backend.plugin.registry;

/**
 * Plugin lifecycle status
 */
public enum PluginStatus {
    /**
     * Plugin is registered but not yet instantiated
     */
    REGISTERED,

    /**
     * Plugin is being loaded
     */
    LOADING,

    /**
     * Plugin is active and ready for use
     */
    ACTIVE,

    /**
     * Plugin is temporarily disabled
     */
    DISABLED,

    /**
     * Plugin failed to load or initialize
     */
    FAILED,

    /**
     * Plugin is being unloaded
     */
    UNLOADING
}
