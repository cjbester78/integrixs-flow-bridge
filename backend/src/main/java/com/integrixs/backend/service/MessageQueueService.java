package com.integrixs.backend.service;

// import com.integrixs.backend.service.deprecated.FlowExecutionAsyncService;
// import com.integrixs.backend.service.deprecated.NotificationService;
// import com.integrixs.backend.service.deprecated.FlowExecutionMonitoringService;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.FlowExecution;
import com.integrixs.data.model.Message;
import com.integrixs.data.sql.repository.FlowExecutionSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.data.sql.repository.MessageSqlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Message Queue Service for managing asynchronous message processing
 * Implements a persistent queue with worker threads for processing messages
 */
@Service
public class MessageQueueService {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueueService.class);

    @Autowired
    private IntegrationFlowSqlRepository flowRepository;

    @Autowired
    private FlowExecutionSqlRepository executionRepository;

    @Autowired
    private MessageSqlRepository messageRepository;

    // @Autowired
    // private FlowExecutionAsyncService flowExecutionService;

    @Autowired
    private EnhancedAdapterExecutionService adapterExecutionService;

    @Autowired(required = false)
    private MessageLazyLoadingService messageLazyLoadingService;

    // @Autowired
    // private NotificationService notificationService;

    // @Autowired
    // private FlowExecutionMonitoringService monitoringService;

    @Value("${integrix.queue.worker.threads:5}")
    private int workerThreads;

    @Value("${integrix.queue.poll.interval:1000}")
    private long pollIntervalMs;

    @Value("${integrix.queue.batch.size:10}")
    private int batchSize;

    private ExecutorService workerPool;
    private ScheduledExecutorService schedulerPool;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong processedMessages = new AtomicLong(0);
    private final AtomicLong failedMessages = new AtomicLong(0);

    // In-memory priority queue for fast processing
    private final PriorityBlockingQueue<QueuedMessage> priorityQueue =
        new PriorityBlockingQueue<>(1000, (m1, m2) -> {
            // Higher priority first, then older messages first
            int priorityCompare = Integer.compare(m2.getPriority(), m1.getPriority());
            if(priorityCompare != 0) return priorityCompare;
            return m1.getCreatedAt().compareTo(m2.getCreatedAt());
        });

    @PostConstruct
    public void initialize() {
        logger.info("Initializing MessageQueueService with {} worker threads", workerThreads);

        // Initialize worker thread pool
        workerPool = Executors.newFixedThreadPool(workerThreads, r -> {
            Thread t = new Thread(r);
            t.setName("MessageQueueWorker-" + t.getId());
            t.setDaemon(true);
            return t;
        });

        // Initialize scheduler for queue monitoring
        schedulerPool = Executors.newScheduledThreadPool(2);

        // Start the queue processing
        start();
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down MessageQueueService");
        stop();

        try {
            workerPool.shutdown();
            if(!workerPool.awaitTermination(30, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }

            schedulerPool.shutdown();
            if(!schedulerPool.awaitTermination(10, TimeUnit.SECONDS)) {
                schedulerPool.shutdownNow();
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted during shutdown", e);
        }
    }

    /**
     * Start the message queue processing
     */
    public void start() {
        if(running.compareAndSet(false, true)) {
            logger.info("Starting message queue processing");

            // Load pending messages from database
            loadPendingMessages();

            // Start queue monitor
            schedulerPool.scheduleWithFixedDelay(
                this::processMessageQueue,
                0,
                pollIntervalMs,
                TimeUnit.MILLISECONDS
           );

            // Start statistics logger
            schedulerPool.scheduleWithFixedDelay(
                this::logStatistics,
                30,
                30,
                TimeUnit.SECONDS
           );
        }
    }

    /**
     * Stop the message queue processing
     */
    public void stop() {
        running.set(false);
        logger.info("Stopped message queue processing");
    }

    /**
     * Enqueue a message for processing
     */
    public String enqueueMessage(String flowId, String payload, int priority) {
        try {
            IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
                .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));

            // Create message record
            Message message = new Message();
            message.setFlowId(flow.getId());
            message.setPayload(payload);
            message.setStatus(Message.MessageStatus.QUEUED);
            message.setPriority(priority);
            message.setCorrelationId(UUID.randomUUID().toString());
            message.setCreatedAt(LocalDateTime.now());

            // Use lazy loading service if available
            if(messageLazyLoadingService != null) {
                message = messageLazyLoadingService.saveMessageWithLazyLoading(message);
            } else {
                message = messageRepository.save(message);
            }

            // Create flow execution record
            FlowExecution execution = new FlowExecution();
            execution.setFlow(flow);
            execution.setMessageId(message.getId());
            execution.setStatus(FlowExecution.ExecutionStatus.QUEUED);
            execution.setStartTime(LocalDateTime.now());

            executionRepository.save(execution);

            // Add to in-memory queue
            QueuedMessage queuedMessage = new QueuedMessage(
                message.getId().toString(),
                flowId,
                payload,
                priority,
                message.getCreatedAt()
           );
            priorityQueue.offer(queuedMessage);

            logger.info("Enqueued message {} for flow {} with priority {}",
                message.getId(), flow.getName(), priority);

            return message.getId().toString();

        } catch(Exception e) {
            logger.error("Failed to enqueue message for flow {}", flowId, e);
            throw new RuntimeException("Failed to enqueue message: " + e.getMessage(), e);
        }
    }

    /**
     * Process messages from the queue
     */
    private void processMessageQueue() {
        if(!running.get()) return;

        try {
            int processed = 0;

            while(processed < batchSize && !priorityQueue.isEmpty()) {
                QueuedMessage message = priorityQueue.poll();
                if(message != null) {
                    // Submit to worker pool for processing
                    workerPool.submit(() -> processMessage(message));
                    processed++;
                }
            }

            if(processed > 0) {
                logger.debug("Submitted {} messages for processing", processed);
            }

        } catch(Exception e) {
            logger.error("Error processing message queue", e);
        }
    }

    /**
     * Process a single message
     */
    @Async
    private void processMessage(QueuedMessage queuedMessage) {
        String messageId = queuedMessage.getMessageId();
        String flowId = queuedMessage.getFlowId();
        String executionId = null;

        try {
            logger.debug("Processing message {}", messageId);

            // Update message status to processing
            updateMessageStatus(messageId, Message.MessageStatus.PROCESSING);

            // Start monitoring for this flow execution
            String correlationId = messageRepository.findById(UUID.fromString(messageId))
                .map(Message::getCorrelationId)
                .orElse(UUID.randomUUID().toString());
            // executionId = monitoringService.startMonitoring(flowId, correlationId, "ASYNC");

            // Execute the flow using the new adapter execution service
            var executionResult = adapterExecutionService.executeFlow(
                flowId,
                correlationId,
                queuedMessage.getPayload()
           ).get();

            // Update message status to completed
            updateMessageStatus(messageId, Message.MessageStatus.COMPLETED);
            processedMessages.incrementAndGet();

            // Complete monitoring with success
            // monitoringService.completeExecution(executionId, true, "Message processed successfully");

            logger.info("Successfully processed message {}", messageId);

        } catch(Exception e) {
            logger.error("Failed to process message {}", messageId, e);
            failedMessages.incrementAndGet();

            // Record error in monitoring if execution was started
            // if(executionId != null) {
            //     monitoringService.recordExecutionError(executionId, e.getMessage(), e);
            // }

            try {
                // Update message status to failed
                updateMessageStatus(messageId, Message.MessageStatus.FAILED, e.getMessage());

                // Send notification
                // notificationService.notifyFlowExecutionFailure(
                //     queuedMessage.getFlowId(),
                //     "Message processing failed: " + e.getMessage()
                // );

            } catch(Exception ex) {
                logger.error("Failed to update message status for {}", messageId, ex);
            }
        }
    }

    /**
     * Update message status in database
     */
    private void updateMessageStatus(String messageId, Message.MessageStatus status) {
        updateMessageStatus(messageId, status, null);
    }

    private void updateMessageStatus(String messageId, Message.MessageStatus status, String error) {
        try {
            Message message = messageRepository.findById(UUID.fromString(messageId))
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

            message.setStatus(status);
            if(error != null) {
                message.setErrorMessage(error);
            }
            if(status == Message.MessageStatus.COMPLETED || status == Message.MessageStatus.FAILED) {
                message.setProcessedAt(LocalDateTime.now());
            }

            messageRepository.save(message);

            // Update flow execution status
            executionRepository.findByMessageId(message.getId()).ifPresent(execution -> {
                switch(status) {
                    case PROCESSING:
                        execution.setStatus(FlowExecution.ExecutionStatus.RUNNING);
                        break;
                    case COMPLETED:
                        execution.setStatus(FlowExecution.ExecutionStatus.COMPLETED);
                        execution.setEndTime(LocalDateTime.now());
                        break;
                    case FAILED:
                        execution.setStatus(FlowExecution.ExecutionStatus.FAILED);
                        execution.setEndTime(LocalDateTime.now());
                        execution.setErrorMessage(error);
                        break;
                }
                executionRepository.save(execution);
            });

        } catch(Exception e) {
            logger.error("Failed to update message status for {}", messageId, e);
        }
    }

    /**
     * Load pending messages from database on startup
     */
    private void loadPendingMessages() {
        try {
            var pendingMessages = messageRepository.findByStatusInOrderByPriorityDescCreatedAtAsc(
                java.util.Arrays.asList(Message.MessageStatus.QUEUED, Message.MessageStatus.PROCESSING)
           );

            for(Message message : pendingMessages) {
                // Reset processing messages to queued
                if(message.getStatus() == Message.MessageStatus.PROCESSING) {
                    message.setStatus(Message.MessageStatus.QUEUED);
                    messageRepository.save(message);
                }

                // Load message with payload if using lazy loading
                String actualPayload = message.getPayload();
                if(messageLazyLoadingService != null && messageLazyLoadingService.hasExternalPayload(message)) {
                    Message loadedMessage = messageLazyLoadingService.loadMessageWithPayload(message.getId())
                        .orElse(message);
                    actualPayload = loadedMessage.getPayload();
                }

                // Add to queue
                QueuedMessage queuedMessage = new QueuedMessage(
                    message.getId().toString(),
                    message.getFlowId().toString(),
                    actualPayload,
                    message.getPriority(),
                    message.getCreatedAt()
               );
                priorityQueue.offer(queuedMessage);
            }

            logger.info("Loaded {} pending messages from database", pendingMessages.size());

        } catch(Exception e) {
            logger.error("Failed to load pending messages", e);
        }
    }

    /**
     * Log queue statistics
     */
    private void logStatistics() {
        logger.info("Message Queue Statistics-Queue Size: {}, Processed: {}, Failed: {}",
            priorityQueue.size(),
            processedMessages.get(),
            failedMessages.get()
       );
    }

    /**
     * Get queue statistics
     */
    public QueueStatistics getStatistics() {
        return new QueueStatistics(
            priorityQueue.size(),
            processedMessages.get(),
            failedMessages.get(),
            running.get()
       );
    }

    /**
     * Internal class representing a queued message
     */
    private static class QueuedMessage {
        private final String messageId;
        private final String flowId;
        private final String payload;
        private final int priority;
        private final LocalDateTime createdAt;

        public QueuedMessage(String messageId, String flowId, String payload,
                           int priority, LocalDateTime createdAt) {
            this.messageId = messageId;
            this.flowId = flowId;
            this.payload = payload;
            this.priority = priority;
            this.createdAt = createdAt;
        }

        public String getMessageId() { return messageId; }
        public String getFlowId() { return flowId; }
        public String getPayload() { return payload; }
        public int getPriority() { return priority; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    /**
     * Queue statistics
     */
    public static class QueueStatistics {
        private final int queueSize;
        private final long processedCount;
        private final long failedCount;
        private final boolean running;

        public QueueStatistics(int queueSize, long processedCount, long failedCount, boolean running) {
            this.queueSize = queueSize;
            this.processedCount = processedCount;
            this.failedCount = failedCount;
            this.running = running;
        }

        public int getQueueSize() { return queueSize; }
        public long getProcessedCount() { return processedCount; }
        public long getFailedCount() { return failedCount; }
        public boolean isRunning() { return running; }
    }
}
