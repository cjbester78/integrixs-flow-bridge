package com.integrixs.backend.service;

import com.integrixs.data.model.DeadLetterMessage;
import com.integrixs.data.model.Message;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.ErrorRecord;
import com.integrixs.data.sql.repository.DeadLetterMessageSqlRepository;
import com.integrixs.data.sql.repository.MessageSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.data.model.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced Dead Letter Queue Service with automated retry and analysis
 */
@Service
public class DeadLetterQueueService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueService.class);


    private final DeadLetterMessageSqlRepository deadLetterRepository;
    private final MessageSqlRepository messageRepository;
    private final IntegrationFlowSqlRepository flowRepository;
    private final MessageQueueService messageQueueService;
    private final EnhancedRetryService retryService;

    @Autowired(required = false)
    private FlowAlertingService alertingService;

    public DeadLetterQueueService(DeadLetterMessageSqlRepository deadLetterRepository,
                                 MessageSqlRepository messageRepository,
                                 IntegrationFlowSqlRepository flowRepository,
                                 MessageQueueService messageQueueService,
                                 EnhancedRetryService retryService) {
        this.deadLetterRepository = deadLetterRepository;
        this.messageRepository = messageRepository;
        this.flowRepository = flowRepository;
        this.messageQueueService = messageQueueService;
        this.retryService = retryService;
    }

    @Value("${integrix.dlq.auto-retry.enabled}")
    private boolean autoRetryEnabled;

    @Value("${integrix.dlq.auto-retry.max-attempts}")
    private int maxAutoRetryAttempts;

    @Value("${integrix.dlq.retention-days}")
    private int retentionDays;

    @Value("${integrix.dlq.batch-size}")
    private int batchSize;

    /**
     * Send message to dead letter queue
     */
    public DeadLetterMessage sendToDeadLetterQueue(Message message, String reason, Exception error) {
        try {
            DeadLetterMessage dlq = new DeadLetterMessage();
            dlq.setMessageId(message.getMessageId());
            dlq.setFlowId(message.getFlow() != null ? message.getFlow().getId() : null);
            dlq.setPayload(message.getMessageContent());
            dlq.setHeaders(message.getHeaders());
            dlq.setProperties(message.getProperties());
            dlq.setReason(reason);
            dlq.setErrorMessage(error != null ? error.getMessage() : null);
            dlq.setErrorStackTrace(error != null ? getStackTrace(error) : null);
            dlq.setQueuedAt(LocalDateTime.now());
            dlq.setOriginalReceivedAt(message.getReceivedAt());
            dlq.setRetryCount(0);
            dlq.setStatus(DeadLetterMessage.Status.PENDING);

            // Categorize error
            dlq.setErrorType(categorizeError(error));

            DeadLetterMessage saved = deadLetterRepository.save(dlq);

            // Update original message status
            message.setStatus(Message.MessageStatus.DEAD_LETTER);
            messageRepository.save(message);

            // Send alert for critical errors
            if(isCriticalError(error)) {
                sendCriticalErrorAlert(message, reason, error);
            }

            log.warn("Message {} sent to DLQ. Reason: {}", message.getMessageId(), reason);

            return saved;

        } catch(Exception e) {
            log.error("Failed to send message {} to DLQ", message.getMessageId(), e);
            throw new RuntimeException("Failed to queue message in DLQ", e);
        }
    }

    /**
     * Retry messages from dead letter queue
     */
    @Scheduled(fixedDelayString = "${integrix.dlq.retry.interval}") // 5 minutes
    public void processDeadLetterQueue() {
        if(!autoRetryEnabled) {
            return;
        }

        try {
            Page<DeadLetterMessage> pendingMessages = deadLetterRepository
                .findByStatusAndRetryCountLessThan(
                    DeadLetterMessage.Status.PENDING,
                    maxAutoRetryAttempts,
                    PageRequest.of(0, batchSize)
               );

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for(DeadLetterMessage dlqMessage : pendingMessages) {
                if(shouldRetry(dlqMessage)) {
                    futures.add(retryDeadLetterMessage(dlqMessage));
                }
            }

            // Wait for all retries to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> log.info("Processed {} DLQ messages", futures.size()))
                .exceptionally(ex -> {
                    log.error("Error processing DLQ batch", ex);
                    return null;
                });

        } catch(Exception e) {
            log.error("Error processing dead letter queue", e);
        }
    }

    /**
     * Retry a single dead letter message
     */
    public CompletableFuture<Void> retryDeadLetterMessage(DeadLetterMessage dlqMessage) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Retrying DLQ message {} (attempt {})",
                    dlqMessage.getMessageId(), dlqMessage.getRetryCount() + 1);

                // Create retry message
                Message retryMessage = new Message();
                retryMessage.setMessageId(dlqMessage.getMessageId() + "_retry_" + System.currentTimeMillis());
                retryMessage.setMessageContent(dlqMessage.getPayload());
                retryMessage.setHeaders(dlqMessage.getHeaders());
                retryMessage.setProperties(dlqMessage.getProperties());
                retryMessage.setStatus(Message.MessageStatus.QUEUED);
                retryMessage.setReceivedAt(LocalDateTime.now());
                retryMessage.setPriority(1); // High priority for DLQ retry

                if(dlqMessage.getFlowId() != null) {
                    flowRepository.findById(dlqMessage.getFlowId())
                        .ifPresent(retryMessage::setFlow);
                }

                Message saved = messageRepository.save(retryMessage);

                // Enqueue for processing
                messageQueueService.enqueueMessage(
                    dlqMessage.getFlowId().toString(),
                    dlqMessage.getPayload(),
                    1
               );

                // Update DLQ message
                dlqMessage.setRetryCount(dlqMessage.getRetryCount() + 1);
                dlqMessage.setLastRetryAt(LocalDateTime.now());
                dlqMessage.setRetryMessageId(saved.getMessageId());

                deadLetterRepository.save(dlqMessage);

            } catch(Exception e) {
                log.error("Failed to retry DLQ message {}", dlqMessage.getMessageId(), e);

                // Mark as permanently failed after max retries
                if(dlqMessage.getRetryCount() >= maxAutoRetryAttempts-1) {
                    dlqMessage.setStatus(DeadLetterMessage.Status.FAILED);
                    dlqMessage.setFailedAt(LocalDateTime.now());
                    deadLetterRepository.save(dlqMessage);
                }
            }
        });
    }

    /**
     * Manually retry specific messages
     */
    public Map<String, Boolean> manualRetry(List<String> messageIds, boolean force) {
        Map<String, Boolean> results = new HashMap<>();

        for(String messageId : messageIds) {
            try {
                Optional<DeadLetterMessage> dlqOpt = deadLetterRepository.findByMessageId(messageId);

                if(dlqOpt.isEmpty()) {
                    results.put(messageId, false);
                    continue;
                }

                DeadLetterMessage dlqMessage = dlqOpt.get();

                // Reset retry count if forced
                if(force) {
                    dlqMessage.setRetryCount(0);
                    dlqMessage.setStatus(DeadLetterMessage.Status.PENDING);
                }

                retryDeadLetterMessage(dlqMessage).join();
                results.put(messageId, true);

            } catch(Exception e) {
                log.error("Failed to manually retry message {}", messageId, e);
                results.put(messageId, false);
            }
        }

        return results;
    }

    /**
     * Analyze dead letter patterns
     */
    public DeadLetterAnalysis analyzeDeadLetterQueue(String flowId, LocalDateTime startDate, LocalDateTime endDate) {
        List<DeadLetterMessage> messages;

        if(flowId != null) {
            messages = deadLetterRepository.findByFlowIdAndQueuedAtBetween(
                UUID.fromString(flowId), startDate, endDate
           );
        } else {
            messages = deadLetterRepository.findByQueuedAtBetween(startDate, endDate);
        }

        // Group by error type
        Map<String, Long> errorTypeCounts = messages.stream()
            .collect(Collectors.groupingBy(
                m -> m.getErrorType() != null ? m.getErrorType().name() : "UNKNOWN",
                Collectors.counting()
           ));

        // Group by reason
        Map<String, Long> reasonCounts = messages.stream()
            .collect(Collectors.groupingBy(
                DeadLetterMessage::getReason,
                Collectors.counting()
           ));

        // Calculate retry success rate
        long totalRetried = messages.stream()
            .filter(m -> m.getRetryCount() > 0)
            .count();

        long successfulRetries = messages.stream()
            .filter(m -> m.getStatus() == DeadLetterMessage.Status.RESOLVED)
            .count();

        double retrySuccessRate = totalRetried > 0 ?
            (successfulRetries * 100.0 / totalRetried) : 0;

        return new DeadLetterAnalysis(
            messages.size(),
            errorTypeCounts,
            reasonCounts,
            totalRetried,
            successfulRetries,
            retrySuccessRate
       );
    }

    /**
     * Cleanup old dead letter messages
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldMessages() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        long deleted = deadLetterRepository.deleteByQueuedAtBeforeAndStatus(
            cutoffDate,
            DeadLetterMessage.Status.RESOLVED
       );

        log.info("Cleaned up {} old dead letter messages", deleted);
    }

    /**
     * Determine if message should be retried
     */
    private boolean shouldRetry(DeadLetterMessage dlqMessage) {
        // Don't retry if recently attempted
        if(dlqMessage.getLastRetryAt() != null &&
            dlqMessage.getLastRetryAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
            return false;
        }

        // Check error type for retryable errors
        ErrorRecord.ErrorType errorType = dlqMessage.getErrorType();
        return isRetryableError(errorType);
    }

    /**
     * Check if error type is retryable
     */
    private boolean isRetryableError(ErrorRecord.ErrorType errorType) {
        if(errorType == null) return true;

        Set<ErrorRecord.ErrorType> nonRetryableErrors = Set.of(
            ErrorRecord.ErrorType.AUTHENTICATION_ERROR,
            ErrorRecord.ErrorType.VALIDATION_ERROR,
            ErrorRecord.ErrorType.CONFIGURATION_ERROR
       );

        return !nonRetryableErrors.contains(errorType);
    }

    /**
     * Categorize error type
     */
    private ErrorRecord.ErrorType categorizeError(Exception error) {
        if(error == null) return ErrorRecord.ErrorType.UNKNOWN_ERROR;

        String className = error.getClass().getSimpleName();
        String message = error.getMessage() != null ? error.getMessage().toLowerCase() : "";

        if(error instanceof java.net.ConnectException ||
            error instanceof java.net.SocketException ||
            message.contains("connection")) {
            return ErrorRecord.ErrorType.CONNECTION_ERROR;
        } else if(message.contains("timeout")) {
            return ErrorRecord.ErrorType.TIMEOUT_ERROR;
        } else if(message.contains("auth") || message.contains("credential")) {
            return ErrorRecord.ErrorType.AUTHENTICATION_ERROR;
        } else if(message.contains("validation") || message.contains("invalid")) {
            return ErrorRecord.ErrorType.VALIDATION_ERROR;
        } else if(message.contains("transform")) {
            return ErrorRecord.ErrorType.TRANSFORMATION_ERROR;
        } else {
            return ErrorRecord.ErrorType.SYSTEM_ERROR;
        }
    }

    /**
     * Check if error is critical
     */
    private boolean isCriticalError(Exception error) {
        if(error == null) return false;

        // Check the exception itself
        if(error.getMessage() != null &&
           (error.getMessage().contains("critical") ||
            error.getMessage().contains("fatal"))) {
            return true;
        }

        // Check if the cause is a critical error
        Throwable cause = error.getCause();
        while(cause != null) {
            if(cause instanceof OutOfMemoryError ||
               cause instanceof StackOverflowError) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }

    /**
     * Send critical error alert
     */
    private void sendCriticalErrorAlert(Message message, String reason, Exception error) {
        if(alertingService == null) {
            return;
        }

        try {
            String flowName = message.getFlow() != null ?
                message.getFlow().getName() : "Unknown Flow";

            Map<String, String> details = new HashMap<>();
            details.put("errorType", error.getClass().getName());
            details.put("errorMessage", error.getMessage());
            details.put("messageId", message.getMessageId());
            details.put("flowName", flowName);

            // Create alert using the alerting service method
            String alertMessage = String.format(
                "Critical error in flow '%s': %s. Message moved to DLQ.",
                flowName, reason
            );

            // For critical errors, we'll use the flow as the source if available
            if(message.getFlow() != null) {
                alertingService.createAlert(null, "Critical DLQ Error", alertMessage,
                    com.integrixs.data.model.Alert.SourceType.FLOW,
                    message.getFlow().getId().toString(), details);
            } else {
                // Log if we can't create alert
                log.error("Cannot create alert-no flow associated with message");
            }

        } catch(Exception e) {
            log.error("Failed to send critical error alert", e);
        }
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception e) {
        if(e == null) return null;

        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");

        for(StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if(sb.length() > 5000) {
                sb.append("\t... truncated");
                break;
            }
        }

        return sb.toString();
    }

    /**
     * Dead Letter Analysis
     */
    public static class DeadLetterAnalysis {
        private final long totalMessages;
        private final Map<String, Long> errorTypeCounts;
        private final Map<String, Long> reasonCounts;
        private final long totalRetried;
        private final long successfulRetries;
        private final double retrySuccessRate;

        public DeadLetterAnalysis(long totalMessages,
                                 Map<String, Long> errorTypeCounts,
                                 Map<String, Long> reasonCounts,
                                 long totalRetried,
                                 long successfulRetries,
                                 double retrySuccessRate) {
            this.totalMessages = totalMessages;
            this.errorTypeCounts = errorTypeCounts;
            this.reasonCounts = reasonCounts;
            this.totalRetried = totalRetried;
            this.successfulRetries = successfulRetries;
            this.retrySuccessRate = retrySuccessRate;
        }

        // Getters
        public long getTotalMessages() { return totalMessages; }
        public Map<String, Long> getErrorTypeCounts() { return errorTypeCounts; }
        public Map<String, Long> getReasonCounts() { return reasonCounts; }
        public long getTotalRetried() { return totalRetried; }
        public long getSuccessfulRetries() { return successfulRetries; }
        public double getRetrySuccessRate() { return retrySuccessRate; }
    }
}
