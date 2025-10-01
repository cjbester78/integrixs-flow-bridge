package com.integrixs.backend.plugin.api;

/**
 * Callback interface for processing received messages
 */
@FunctionalInterface
public interface MessageCallback {

    /**
     * Process a received message
     * @param message the received message
     * @throws PluginException if processing fails
     */
    void onMessage(PluginMessage message) throws PluginException;

    /**
     * Handle errors during message reception
     * Default implementation rethrows as PluginException
     * @param error the error that occurred
     */
    default void onError(Throwable error) {
        if(error instanceof PluginException) {
            throw(PluginException) error;
        }
        throw new PluginException("Error in message callback", error);
    }

    /**
     * Called when message reception is complete
     * (e.g., connection closed, subscription ended)
     */
    default void onComplete() {
        // Default: no action
    }
}
