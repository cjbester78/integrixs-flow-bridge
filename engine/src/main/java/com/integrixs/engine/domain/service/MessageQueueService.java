package com.integrixs.engine.domain.service;

import java.util.concurrent.CompletableFuture;

/**
 * Domain service interface for message queueing
 */
public interface MessageQueueService {

    /**
     * Queue a message for processing
     * @param queueName The name of the queue
     * @param message The message to queue
     * @return true if successfully queued
     */
    boolean queueMessage(String queueName, Object message);

    /**
     * Queue a message with priority
     * @param queueName The name of the queue
     * @param message The message to queue
     * @param priority Priority level(higher = higher priority)
     * @return true if successfully queued
     */
    boolean queueMessage(String queueName, Object message, int priority);

    /**
     * Dequeue a message for processing
     * @param queueName The name of the queue
     * @return The dequeued message or null if queue is empty
     */
    Object dequeueMessage(String queueName);

    /**
     * Dequeue a message with timeout
     * @param queueName The name of the queue
     * @param timeoutMs Timeout in milliseconds
     * @return The dequeued message or null if timeout
     */
    Object dequeueMessage(String queueName, long timeoutMs);

    /**
     * Queue a message for asynchronous processing
     * @param queueName The name of the queue
     * @param message The message to queue
     * @return Future that completes when message is processed
     */
    CompletableFuture<Object> queueAsync(String queueName, Object message);

    /**
     * Get queue size
     * @param queueName The name of the queue
     * @return Current queue size
     */
    int getQueueSize(String queueName);

    /**
     * Clear a queue
     * @param queueName The name of the queue
     */
    void clearQueue(String queueName);

    /**
     * Check if queue exists
     * @param queueName The name of the queue
     * @return true if queue exists
     */
    boolean queueExists(String queueName);

    /**
     * Create a new queue
     * @param queueName The name of the queue
     * @param capacity Maximum queue capacity
     * @return true if created successfully
     */
    boolean createQueue(String queueName, int capacity);
}
