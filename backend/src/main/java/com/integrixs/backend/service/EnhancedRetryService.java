package com.integrixs.backend.service;

import com.integrixs.data.model.Message;
import com.integrixs.data.model.RetryPolicy;
import com.integrixs.data.sql.repository.MessageSqlRepository;
import com.integrixs.data.sql.repository.RetryPolicySqlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced retry service with multiple retry strategies
 */
@Service
public class EnhancedRetryService {

    private static final Logger log = LoggerFactory.getLogger(EnhancedRetryService.class);


    private final MessageSqlRepository messageRepository;
    private final RetryPolicySqlRepository retryPolicyRepository;
    private final MessageQueueService messageQueueService;

    public EnhancedRetryService(MessageSqlRepository messageRepository,
                               RetryPolicySqlRepository retryPolicyRepository,
                               MessageQueueService messageQueueService) {
        this.messageRepository = messageRepository;
        this.retryPolicyRepository = retryPolicyRepository;
        this.messageQueueService = messageQueueService;
    }

    @Value("${integrix.retry.max-attempts:5}")
    private int defaultMaxAttempts;

    @Value("${integrix.retry.initial-interval:1000}")
    private long defaultInitialInterval;

    @Value("${integrix.retry.max-interval:300000}") // 5 minutes
    private long defaultMaxInterval;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, ScheduledFuture<?>> scheduledRetries = new ConcurrentHashMap<>();

    /**
     * Retry strategies
     */
    public enum RetryStrategy {
        FIXED_DELAY,          // Fixed delay between retries
        EXPONENTIAL_BACKOFF, // Exponentially increasing delay
        LINEAR_BACKOFF,       // Linearly increasing delay
        FIBONACCI_BACKOFF,    // Fibonacci sequence delay
        RANDOM_JITTER,        // Random delay within bounds
        ADAPTIVE               // Adaptive based on error rate
    }

    /**
     * Schedule a retry for a message with specific strategy
     */
    public CompletableFuture<Boolean> scheduleRetry(String messageId,
                                                    String flowId,
                                                    Exception lastError,
                                                    RetryStrategy strategy) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<Message> messageOpt = messageRepository.findByMessageId(messageId);
                if(messageOpt.isEmpty()) {
                    log.error("Message {} not found for retry", messageId);
                    return false;
                }

                Message message = messageOpt.get();
                RetryPolicy policy = getRetryPolicy(flowId);

                // Check if max retries exceeded
                if(message.getRetryCount() >= policy.getMaxAttempts()) {
                    log.warn("Max retries( {}) exceeded for message {}",
                        policy.getMaxAttempts(), messageId);
                    return false;
                }

                // Calculate next retry delay
                long delay = calculateRetryDelay(
                    message.getRetryCount() + 1,
                    policy,
                    strategy != null ? strategy : RetryStrategy.EXPONENTIAL_BACKOFF
               );

                // Schedule retry
                String retryKey = flowId + "_" + messageId;
                ScheduledFuture<?> future = scheduler.schedule(
                    () -> executeRetry(message, flowId),
                    delay,
                    TimeUnit.MILLISECONDS
               );

                scheduledRetries.put(retryKey, future);

                // Update message status
                message.setStatus(Message.MessageStatus.RETRY);
                message.setRetryCount(message.getRetryCount() + 1);
                message.setErrorMessage(lastError != null ? lastError.getMessage() : "Scheduled for retry");
                messageRepository.save(message);

                log.info("Scheduled retry for message {} in {}ms using {} strategy",
                    messageId, delay, strategy);

                return true;

            } catch(Exception e) {
                log.error("Failed to schedule retry for message {}", messageId, e);
                return false;
            }
        });
    }

    /**
     * Execute retry for a message
     */
    private void executeRetry(Message message, String flowId) {
        try {
            log.info("Executing retry for message {} (attempt {})",
                message.getMessageId(), message.getRetryCount());

            // Re-enqueue message with high priority
            messageQueueService.enqueueMessage(
                flowId,
                message.getMessageContent(),
                1 // High priority for retry
           );

            // Update message status
            message.setStatus(Message.MessageStatus.PROCESSING);
            messageRepository.save(message);

            // Remove from scheduled retries
            String retryKey = flowId + "_" + message.getMessageId();
            scheduledRetries.remove(retryKey);

        } catch(Exception e) {
            log.error("Failed to execute retry for message {}", message.getMessageId(), e);

            // Mark as failed if retry execution fails
            message.setStatus(Message.MessageStatus.FAILED);
            message.setErrorMessage("Retry execution failed: " + e.getMessage());
            messageRepository.save(message);
        }
    }

    /**
     * Calculate retry delay based on strategy
     */
    private long calculateRetryDelay(int attemptNumber, RetryPolicy policy, RetryStrategy strategy) {
        long baseInterval = policy.getInitialIntervalMs() != null ?
            policy.getInitialIntervalMs() : defaultInitialInterval;
        long maxInterval = policy.getMaxIntervalMs() != null ?
            policy.getMaxIntervalMs() : defaultMaxInterval;

        long delay;

        switch(strategy) {
            case FIXED_DELAY:
                delay = baseInterval;
                break;

            case EXPONENTIAL_BACKOFF:
                double multiplier = policy.getMultiplier() != null ? policy.getMultiplier() : 2.0;
                delay = (long) (baseInterval * Math.pow(multiplier, attemptNumber-1));
                break;

            case LINEAR_BACKOFF:
                delay = baseInterval * attemptNumber;
                break;

            case FIBONACCI_BACKOFF:
                delay = baseInterval * fibonacci(attemptNumber);
                break;

            case RANDOM_JITTER:
                delay = baseInterval + ThreadLocalRandom.current().nextLong(0, baseInterval);
                break;

            case ADAPTIVE:
                // Adaptive strategy based on system load
                delay = calculateAdaptiveDelay(baseInterval, attemptNumber);
                break;

            default:
                delay = baseInterval;
        }

        // Apply max interval cap
        return Math.min(delay, maxInterval);
    }

    /**
     * Calculate adaptive delay based on system conditions
     */
    private long calculateAdaptiveDelay(long baseInterval, int attemptNumber) {
        // Get current system load
        double systemLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();

        // Adjust delay based on load
        double loadFactor = systemLoad > 1.0 ? systemLoad : 1.0;
        return(long) (baseInterval * attemptNumber * loadFactor);
    }

    /**
     * Calculate Fibonacci number
     */
    private long fibonacci(int n) {
        if(n <= 1) return n;
        long a = 0, b = 1;
        for(int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }

    /**
     * Get retry policy for flow
     */
    private RetryPolicy getRetryPolicy(String flowId) {
        return retryPolicyRepository.findByFlowId(UUID.fromString(flowId))
            .orElseGet(this::getDefaultRetryPolicy);
    }

    /**
     * Get default retry policy
     */
    private RetryPolicy getDefaultRetryPolicy() {
        RetryPolicy policy = new RetryPolicy();
        policy.setMaxAttempts(defaultMaxAttempts);
        policy.setInitialIntervalMs(defaultInitialInterval);
        policy.setMultiplier(2.0);
        policy.setMaxIntervalMs(defaultMaxInterval);
        policy.setRetryStrategy(RetryPolicy.RetryStrategy.EXPONENTIAL_BACKOFF);
        return policy;
    }

    /**
     * Bulk retry for multiple messages
     */
    public CompletableFuture<Map<String, Boolean>> bulkRetry(List<String> messageIds,
                                                             String flowId,
                                                             RetryStrategy strategy) {
        Map<String, Boolean> results = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for(String messageId : messageIds) {
            CompletableFuture<Void> future = scheduleRetry(messageId, flowId, null, strategy)
                .thenAccept(success -> results.put(messageId, success));
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> results);
    }

    /**
     * Cancel scheduled retry
     */
    public boolean cancelRetry(String flowId, String messageId) {
        String retryKey = flowId + "_" + messageId;
        ScheduledFuture<?> future = scheduledRetries.remove(retryKey);

        if(future != null && !future.isDone()) {
            boolean cancelled = future.cancel(false);
            log.info("Cancelled retry for message {}-success: {}", messageId, cancelled);
            return cancelled;
        }

        return false;
    }

    /**
     * Get retry statistics
     */
    public RetryStatistics getRetryStatistics(String flowId) {
        List<Message> retryMessages = messageRepository.findByStatus(Message.MessageStatus.RETRY);

        long totalRetries = retryMessages.stream()
            .filter(m -> m.getFlow() != null && m.getFlow().getId().toString().equals(flowId))
            .mapToLong(Message::getRetryCount)
            .sum();

        Map<Integer, Long> retriesByAttempt = retryMessages.stream()
            .filter(m -> m.getFlow() != null && m.getFlow().getId().toString().equals(flowId))
            .collect(java.util.stream.Collectors.groupingBy(
                Message::getRetryCount,
                java.util.stream.Collectors.counting()
           ));

        return new RetryStatistics(totalRetries, retriesByAttempt, scheduledRetries.size());
    }

    /**
     * Smart retry with conditional logic
     */
    public CompletableFuture<Boolean> smartRetry(String messageId,
                                                 String flowId,
                                                 Exception error,
                                                 Predicate<Exception> retryCondition,
                                                 Function<Integer, RetryStrategy> strategySelector) {
        // Check if error matches retry condition
        if(!retryCondition.test(error)) {
            log.info("Error does not match retry condition for message {}", messageId);
            return CompletableFuture.completedFuture(false);
        }

        // Get message and determine retry attempt
        Optional<Message> messageOpt = messageRepository.findByMessageId(messageId);
        if(messageOpt.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        Message message = messageOpt.get();
        int nextAttempt = message.getRetryCount() + 1;

        // Select strategy based on attempt number
        RetryStrategy strategy = strategySelector.apply(nextAttempt);

        return scheduleRetry(messageId, flowId, error, strategy);
    }

    /**
     * Periodic cleanup of completed retries
     */
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void cleanupCompletedRetries() {
        scheduledRetries.entrySet().removeIf(entry ->
            entry.getValue().isDone() || entry.getValue().isCancelled()
       );

        log.debug("Cleaned up completed retries. Active retries: {}", scheduledRetries.size());
    }

    /**
     * Retry statistics
     */
    public static class RetryStatistics {
        private final long totalRetries;
        private final Map<Integer, Long> retriesByAttempt;
        private final int activeRetries;

        public RetryStatistics(long totalRetries, Map<Integer, Long> retriesByAttempt, int activeRetries) {
            this.totalRetries = totalRetries;
            this.retriesByAttempt = retriesByAttempt;
            this.activeRetries = activeRetries;
        }

        public long getTotalRetries() { return totalRetries; }
        public Map<Integer, Long> getRetriesByAttempt() { return retriesByAttempt; }
        public int getActiveRetries() { return activeRetries; }
    }
}
