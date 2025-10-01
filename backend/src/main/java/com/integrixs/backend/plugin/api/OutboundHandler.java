package com.integrixs.backend.plugin.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handler for outbound operations(sending data to external systems)
 */
public interface OutboundHandler {

    /**
     * Send a single message
     * @param message the message to send
     * @return result of the send operation
     * @throws PluginException if send fails
     */
    SendResult send(PluginMessage message) throws PluginException;

    /**
     * Send multiple messages in a batch
     * @param messages list of messages to send
     * @return batch send result
     * @throws PluginException if batch send fails
     */
    BatchSendResult sendBatch(List<PluginMessage> messages) throws PluginException;

    /**
     * Send a message asynchronously
     * @param message the message to send
     * @return future with send result
     */
    default CompletableFuture<SendResult> sendAsync(PluginMessage message) {
        return CompletableFuture.supplyAsync(() -> send(message));
    }

    /**
     * Check if batch operations are supported
     * @return true if batch operations are supported
     */
    default boolean supportsBatch() {
        return false;
    }

    /**
     * Get maximum batch size
     * @return max batch size or -1 if unlimited
     */
    default int getMaxBatchSize() {
        return 100;
    }

    /**
     * Prepare/validate message before sending
     * @param message message to prepare
     * @return prepared message
     */
    default PluginMessage prepareMessage(PluginMessage message) {
        return message;
    }

    /**
     * Get the current configuration
     * @return configuration
     */
    default OutboundConfiguration getConfiguration() {
        return null;
    }
}
