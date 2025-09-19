package com.integrixs.backend.infrastructure.messaging;

import com.integrixs.data.model.Message;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * In - memory message queue implementation
 * In production, this would be replaced with RabbitMQ, Kafka, or similar
 */
@Component
public class MessageQueue {

    private static final Logger log = LoggerFactory.getLogger(MessageQueue.class);


    private final PriorityBlockingQueue<QueuedMessage> queue;
    private final Map<UUID, QueuedMessage> messageMap;
    private final ScheduledExecutorService scheduler;

    public MessageQueue() {
        // Priority queue that orders by priority(lower number = higher priority) and then by timestamp
        this.queue = new PriorityBlockingQueue<>(100, Comparator
            .comparing(QueuedMessage::getPriority)
            .thenComparing(QueuedMessage::getTimestamp));

        this.messageMap = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        // Start cleanup task
        scheduler.scheduleWithFixedDelay(this::cleanupExpiredMessages, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Adds a message to the queue
     */
    public void enqueue(Message message) {
        log.debug("Enqueueing message: {} with priority: {}", message.getId(), message.getPriority());

        QueuedMessage queuedMessage = new QueuedMessage(message);
        messageMap.put(message.getId(), queuedMessage);
        queue.offer(queuedMessage);
    }

    /**
     * Retrieves and removes the next message from the queue
     */
    public Message dequeue() {
        try {
            QueuedMessage queuedMessage = queue.poll(100, TimeUnit.MILLISECONDS);
            if(queuedMessage != null) {
                messageMap.remove(queuedMessage.getMessage().getId());
                return queuedMessage.getMessage();
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for message");
        }
        return null;
    }

    /**
     * Retrieves the next message without removing it from the queue
     */
    public Message peek() {
        QueuedMessage queuedMessage = queue.peek();
        return queuedMessage != null ? queuedMessage.getMessage() : null;
    }

    /**
     * Removes a specific message from the queue
     */
    public boolean remove(UUID messageId) {
        log.debug("Removing message from queue: {}", messageId);

        QueuedMessage queuedMessage = messageMap.remove(messageId);
        if(queuedMessage != null) {
            return queue.remove(queuedMessage);
        }
        return false;
    }

    /**
     * Returns the current queue size
     */
    public int size() {
        return queue.size();
    }

    /**
     * Checks if the queue is empty
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Clears all messages from the queue
     */
    public void clear() {
        log.info("Clearing message queue");
        queue.clear();
        messageMap.clear();
    }

    /**
     * Gets all messages currently in the queue(for monitoring)
     */
    public List<Message> getAllMessages() {
        return new ArrayList<>(queue).stream()
            .map(QueuedMessage::getMessage)
            .toList();
    }

    /**
     * Cleanup task to remove messages that have been in queue too long
     */
    private void cleanupExpiredMessages() {
        log.debug("Running cleanup task for expired messages");

        long expiryTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);

        queue.removeIf(qm -> {
            if(qm.getTimestamp() < expiryTime) {
                log.warn("Removing expired message from queue: {}", qm.getMessage().getId());
                messageMap.remove(qm.getMessage().getId());
                return true;
            }
            return false;
        });
    }

    /**
     * Wrapper class to add timestamp to messages in queue
     */
    private static class QueuedMessage {
        private final Message message;
        private final long timestamp;

        public QueuedMessage(Message message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public Message getMessage() {
            return message;
        }

        public int getPriority() {
            return message.getPriority();
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            QueuedMessage that = (QueuedMessage) o;
            return message.getId().equals(that.message.getId());
        }

        @Override
        public int hashCode() {
            return message.getId().hashCode();
        }
    }
}
