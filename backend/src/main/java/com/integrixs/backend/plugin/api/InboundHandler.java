package com.integrixs.backend.plugin.api;

import java.util.concurrent.CompletableFuture;

/**
 * Handler for inbound operations(receiving data from external systems)
 */
public interface InboundHandler {

    /**
     * Start listening for incoming data
     * @param callback callback to invoke when data is received
     * @throws PluginException if start fails
     */
    void startListening(MessageCallback callback) throws PluginException;

    /**
     * Stop listening for incoming data
     */
    void stopListening();

    /**
     * Poll for data(for polling - based adapters)
     * @return polling result with any retrieved data
     */
    PollingResult poll();

    /**
     * Check if the handler is currently listening
     * @return true if actively listening
     */
    boolean isListening();

    /**
     * Get the current configuration
     * @return configuration map
     */
    default InboundConfiguration getConfiguration() {
        return null;
    }

    /**
     * Subscribe to specific events or topics
     * @param subscription subscription details
     * @return future that completes when subscription is active
     */
    default CompletableFuture<Void> subscribe(SubscriptionRequest subscription) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Unsubscribe from events or topics
     * @param subscriptionId subscription to cancel
     * @return future that completes when unsubscribed
     */
    default CompletableFuture<Void> unsubscribe(String subscriptionId) {
        return CompletableFuture.completedFuture(null);
    }
}
