package com.integrixs.engine.infrastructure.queue;

import com.integrixs.engine.domain.service.MessageQueueService;
import org.springframework.stereotype.Service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;

/**
 * In-memory implementation of MessageQueueService
 * Can be replaced with external queue systems (RabbitMQ, Kafka, etc.)
 */
@Service
public class InMemoryMessageQueueService implements MessageQueueService {

    private static final Logger log = LoggerFactory.getLogger(InMemoryMessageQueueService.class);

    private final Map<String, BlockingQueue<QueuedMessage>> queues = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public boolean queueMessage(String queueName, Object message) {
        return queueMessage(queueName, message, 0);
    }

    @Override
    public boolean queueMessage(String queueName, Object message, int priority) {
        log.debug("Queueing message to queue: {} with priority: {}", queueName, priority);

        BlockingQueue<QueuedMessage> queue = getOrCreateQueue(queueName);
        QueuedMessage queuedMessage = new QueuedMessage(message, priority);

        try {
            return queue.offer(queuedMessage, 5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted while queueing message", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public Object dequeueMessage(String queueName) {
        return dequeueMessage(queueName, 0);
    }

    @Override
    public Object dequeueMessage(String queueName, long timeoutMs) {
        BlockingQueue<QueuedMessage> queue = queues.get(queueName);
        if (queue == null) {
            return null;
        }

        try {
            QueuedMessage queuedMessage = timeoutMs > 0
                ? queue.poll(timeoutMs, TimeUnit.MILLISECONDS)
                : queue.poll();

            return queuedMessage != null ? queuedMessage.getMessage() : null;
        } catch (InterruptedException e) {
            log.error("Interrupted while dequeuing message", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public CompletableFuture<Object> queueAsync(String queueName, Object message) {
        return CompletableFuture.supplyAsync(() -> {
            if (queueMessage(queueName, message)) {
                log.debug("Message queued asynchronously to queue: {}", queueName);
                return message;
            } else {
                throw new RuntimeException("Failed to queue message");
            }
        }, executorService);
    }

    @Override
    public int getQueueSize(String queueName) {
        BlockingQueue<QueuedMessage> queue = queues.get(queueName);
        return queue != null ? queue.size() : 0;
    }

    @Override
    public void clearQueue(String queueName) {
        BlockingQueue<QueuedMessage> queue = queues.get(queueName);
        if (queue != null) {
            queue.clear();
            log.info("Cleared queue: {}", queueName);
        }
    }

    @Override
    public boolean queueExists(String queueName) {
        return queues.containsKey(queueName);
    }

    @Override
    public boolean createQueue(String queueName, int capacity) {
        if (queues.containsKey(queueName)) {
            return false;
        }

        BlockingQueue<QueuedMessage> queue = capacity > 0
            ? new PriorityBlockingQueue<>(capacity)
            : new PriorityBlockingQueue<>();

        queues.put(queueName, queue);
        log.info("Created queue: {} with capacity: {}", queueName, capacity);
        return true;
    }

    private BlockingQueue<QueuedMessage> getOrCreateQueue(String queueName) {
        return queues.computeIfAbsent(queueName, k -> {
            log.info("Auto-creating queue: {}", k);
            return new PriorityBlockingQueue<>();
        });
    }

    /**
     * Shutdown the queue service
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Internal class for queued messages with priority
     */
    private static class QueuedMessage implements Comparable<QueuedMessage> {
        private final Object message;
        private final int priority;
        private final long timestamp;

        public QueuedMessage(Object message, int priority) {
            this.message = message;
            this.priority = priority;
            this.timestamp = System.currentTimeMillis();
        }

        public Object getMessage() {
            return message;
        }

        @Override
        public int compareTo(QueuedMessage other) {
            // Higher priority first
            int priorityCompare = Integer.compare(other.priority, this.priority);
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            // If same priority, older messages first (FIFO)
            return Long.compare(this.timestamp, other.timestamp);
        }
    }
}