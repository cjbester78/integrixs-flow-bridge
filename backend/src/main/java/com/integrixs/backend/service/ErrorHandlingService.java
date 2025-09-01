package com.integrixs.backend.service;

// import com.integrixs.backend.service.deprecated.NotificationService;
import com.integrixs.data.model.ErrorRecord;
import com.integrixs.data.model.RetryPolicy;
import com.integrixs.data.model.DeadLetterMessage;
import com.integrixs.data.repository.ErrorRecordRepository;
import com.integrixs.data.repository.RetryPolicyRepository;
import com.integrixs.data.repository.DeadLetterMessageRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Error Handling Service with retry mechanism, circuit breaker, and dead letter queue
 */
@Service
public class ErrorHandlingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingService.class);
    
    @Autowired
    private ErrorRecordRepository errorRecordRepository;
    
    @Autowired
    private RetryPolicyRepository retryPolicyRepository;
    
    @Autowired
    private DeadLetterMessageRepository deadLetterRepository;
    
    // @Autowired
    // private NotificationService notificationService;
    
    @Autowired
    private MessageQueueService messageQueueService;
    
    // Circuit breaker registry
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    // Retry registry
    private RetryRegistry retryRegistry;
    
    // Error counters for monitoring
    private final Map<String, AtomicInteger> errorCounters = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        // Initialize circuit breaker configuration
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // Open circuit if 50% of calls fail
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build();
        
        circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        
        // Initialize retry configuration
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .retryExceptions(Exception.class)
            .build();
        
        retryRegistry = RetryRegistry.of(retryConfig);
        
        logger.info("Error handling service initialized with circuit breaker and retry mechanisms");
    }
    
    /**
     * Execute an operation with error handling
     */
    public <T> T executeWithErrorHandling(String flowId, Supplier<T> operation) {
        String operationKey = "flow_" + flowId;
        
        try {
            // Get or create circuit breaker for this flow
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(operationKey);
            
            // Get retry policy for flow
            RetryPolicy policy = getRetryPolicy(flowId);
            
            // Create retry instance with custom configuration
            Retry retry = retryRegistry.retry(operationKey, RetryConfig.custom()
                .maxAttempts(policy.getMaxRetries())
                .waitDuration(Duration.ofMillis(policy.getRetryDelayMs()))
                .retryExceptions(Exception.class)
                .build()
            );
            
            // Decorate operation with circuit breaker and retry
            Supplier<T> decoratedSupplier = CircuitBreaker
                .decorateSupplier(circuitBreaker, operation);
            
            decoratedSupplier = Retry
                .decorateSupplier(retry, decoratedSupplier);
            
            // Execute with error handling
            return decoratedSupplier.get();
            
        } catch (Exception e) {
            handleError(flowId, e);
            throw e;
        }
    }
    
    /**
     * Execute an operation with retry
     */
    public <T> T executeWithRetry(String flowId, Supplier<T> operation, int maxRetries) {
        String operationKey = "retry_" + flowId;
        AtomicInteger attempts = new AtomicInteger(0);
        Exception lastException = null;
        
        while (attempts.get() < maxRetries) {
            try {
                attempts.incrementAndGet();
                logger.debug("Executing operation for flow {} - attempt {}/{}", 
                    flowId, attempts.get(), maxRetries);
                
                return operation.get();
                
            } catch (Exception e) {
                lastException = e;
                
                if (attempts.get() < maxRetries) {
                    long retryDelay = calculateRetryDelay(attempts.get());
                    logger.warn("Operation failed for flow {} - attempt {}/{}. Retrying in {}ms", 
                        flowId, attempts.get(), maxRetries, retryDelay);
                    
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    logger.error("Operation failed after {} attempts for flow {}", maxRetries, flowId);
                }
            }
        }
        
        // All retries exhausted
        handleMaxRetriesExceeded(flowId, lastException);
        throw new RuntimeException("Operation failed after " + maxRetries + " attempts", lastException);
    }
    
    /**
     * Handle an error occurrence
     */
    @Transactional
    public void handleError(String flowId, Exception error) {
        try {
            // Record error
            ErrorRecord errorRecord = new ErrorRecord();
            errorRecord.setFlowId(UUID.fromString(flowId));
            errorRecord.setErrorType(mapToErrorType(error));
            errorRecord.setErrorMessage(error.getMessage());
            errorRecord.setStackTrace(getStackTrace(error));
            errorRecord.setOccurredAt(LocalDateTime.now());
            
            errorRecordRepository.save(errorRecord);
            
            // Update error counter
            errorCounters.computeIfAbsent(flowId, k -> new AtomicInteger(0)).incrementAndGet();
            
            // Check if error threshold exceeded
            checkErrorThreshold(flowId);
            
            logger.error("Error handled for flow {}: {}", flowId, error.getMessage());
            
        } catch (Exception e) {
            logger.error("Failed to record error for flow {}", flowId, e);
        }
    }
    
    /**
     * Send message to dead letter queue
     */
    @Transactional
    public void sendToDeadLetterQueue(String flowId, String messageId, String payload, String reason) {
        try {
            DeadLetterMessage deadLetter = new DeadLetterMessage();
            deadLetter.setFlowId(UUID.fromString(flowId));
            deadLetter.setMessageId(messageId);
            deadLetter.setPayload(payload);
            deadLetter.setReason(reason);
            deadLetter.setCreatedAt(LocalDateTime.now());
            deadLetter.setRetryCount(0);
            
            deadLetterRepository.save(deadLetter);
            
            // Send notification
            // TODO: Replace notificationService.notifyDeadLetterMessage(flowId, messageId, reason);
            logger.info("Dead letter notification - flowId: {}, messageId: {}, reason: {}", flowId, messageId, reason);
            
            logger.warn("Message {} sent to dead letter queue for flow {}: {}", 
                messageId, flowId, reason);
            
        } catch (Exception e) {
            logger.error("Failed to send message to dead letter queue", e);
        }
    }
    
    /**
     * Retry messages from dead letter queue
     */
    @Scheduled(fixedDelayString = "${integrix.deadletter.retry.interval:300000}") // 5 minutes
    public void retryDeadLetterMessages() {
        try {
            List<DeadLetterMessage> messages = deadLetterRepository
                .findByStatusAndRetryCountLessThanOrderByQueuedAtAsc(
                    DeadLetterMessage.Status.PENDING, 3
                );
            
            for (DeadLetterMessage message : messages) {
                retryDeadLetterMessage(message);
            }
            
            if (!messages.isEmpty()) {
                logger.info("Processed {} dead letter messages for retry", messages.size());
            }
            
        } catch (Exception e) {
            logger.error("Error processing dead letter queue", e);
        }
    }
    
    /**
     * Retry a single dead letter message
     */
    @Transactional
    private void retryDeadLetterMessage(DeadLetterMessage message) {
        try {
            logger.info("Retrying dead letter message {} for flow {}", 
                message.getMessageId(), message.getFlowId());
            
            // Re-enqueue message
            messageQueueService.enqueueMessage(
                message.getFlowId().toString(),
                message.getPayload(),
                1 // Low priority for retry
            );
            
            // Update dead letter message
            message.setRetryCount(message.getRetryCount() + 1);
            message.setLastRetryAt(LocalDateTime.now());
            message.setStatus(DeadLetterMessage.Status.RETRIED);
            
            deadLetterRepository.save(message);
            
        } catch (Exception e) {
            logger.error("Failed to retry dead letter message {}", message.getId(), e);
            
            // Mark as permanently failed if max retries exceeded
            if (message.getRetryCount() >= 3) {
                message.setStatus(DeadLetterMessage.Status.FAILED);
                deadLetterRepository.save(message);
            }
        }
    }
    
    /**
     * Get retry policy for a flow
     */
    private RetryPolicy getRetryPolicy(String flowId) {
        return retryPolicyRepository.findByFlowId(UUID.fromString(flowId))
            .orElse(getDefaultRetryPolicy());
    }
    
    /**
     * Get default retry policy
     */
    private RetryPolicy getDefaultRetryPolicy() {
        RetryPolicy policy = new RetryPolicy();
        policy.setMaxAttempts(3);
        policy.setInitialIntervalMs(1000L);
        policy.setMultiplier(2.0);
        policy.setMaxIntervalMs(30000L);
        return policy;
    }
    
    /**
     * Calculate retry delay with exponential backoff
     */
    private long calculateRetryDelay(int attemptNumber) {
        long baseDelay = 1000; // 1 second
        double multiplier = 2.0;
        long maxDelay = 30000; // 30 seconds
        
        long delay = (long) (baseDelay * Math.pow(multiplier, attemptNumber - 1));
        return Math.min(delay, maxDelay);
    }
    
    /**
     * Check if error threshold exceeded
     */
    private void checkErrorThreshold(String flowId) {
        AtomicInteger errorCount = errorCounters.get(flowId);
        if (errorCount != null && errorCount.get() > 10) {
            // Circuit breaker will handle this
            logger.warn("Error threshold exceeded for flow {} - {} errors", flowId, errorCount.get());
            
            // Send alert
            // TODO: Replace notificationService.notifyErrorThresholdExceeded(flowId, errorCount.get());
            logger.error("Error threshold alert - flowId: {}, errorCount: {}", flowId, errorCount.get());
        }
    }
    
    /**
     * Handle max retries exceeded
     */
    private void handleMaxRetriesExceeded(String flowId, Exception lastException) {
        logger.error("Max retries exceeded for flow {}", flowId, lastException);
        
        // Send notification
        // TODO: Replace notificationService.notifyMaxRetriesExceeded(flowId, lastException.getMessage());
        logger.error("Max retries notification - flowId: {}, error: {}", flowId, lastException.getMessage());
    }
    
    /**
     * Get error statistics for a flow
     */
    public ErrorStatistics getErrorStatistics(String flowId) {
        List<ErrorRecord> recentErrors = errorRecordRepository
            .findByFlowIdAndOccurredAtAfterOrderByOccurredAtDesc(
                UUID.fromString(flowId),
                LocalDateTime.now().minusHours(24)
            );
        
        Map<String, Long> errorTypeCount = new HashMap<>();
        for (ErrorRecord error : recentErrors) {
            errorTypeCount.merge(error.getErrorType().name(), 1L, Long::sum);
        }
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("flow_" + flowId);
        
        return new ErrorStatistics(
            recentErrors.size(),
            errorTypeCount,
            circuitBreaker.getState().toString(),
            circuitBreaker.getMetrics().getFailureRate()
        );
    }
    
    /**
     * Reset error counters
     */
    @Scheduled(fixedDelayString = "${integrix.error.counter.reset.interval:3600000}") // 1 hour
    public void resetErrorCounters() {
        errorCounters.clear();
        logger.info("Error counters reset");
    }
    
    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if (sb.length() > 5000) { // Limit stack trace size
                sb.append("\t... truncated");
                break;
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Map exception to ErrorType enum
     */
    private ErrorRecord.ErrorType mapToErrorType(Exception error) {
        if (error instanceof java.net.ConnectException || error instanceof java.net.SocketException) {
            return ErrorRecord.ErrorType.CONNECTION_ERROR;
        } else if (error instanceof org.springframework.security.core.AuthenticationException || 
                   error.getMessage() != null && error.getMessage().toLowerCase().contains("auth")) {
            return ErrorRecord.ErrorType.AUTHENTICATION_ERROR;
        } else if (error instanceof javax.xml.transform.TransformerException ||
                   error.getClass().getName().contains("Transformation")) {
            return ErrorRecord.ErrorType.TRANSFORMATION_ERROR;
        } else if (error instanceof jakarta.validation.ValidationException ||
                   error.getClass().getName().contains("Validation")) {
            return ErrorRecord.ErrorType.VALIDATION_ERROR;
        } else if (error instanceof java.util.concurrent.TimeoutException) {
            return ErrorRecord.ErrorType.TIMEOUT_ERROR;
        } else if (error.getClass().getName().contains("Adapter")) {
            return ErrorRecord.ErrorType.ADAPTER_ERROR;
        } else if (error.getClass().getName().contains("Config")) {
            return ErrorRecord.ErrorType.CONFIGURATION_ERROR;
        } else if (error instanceof RuntimeException) {
            return ErrorRecord.ErrorType.SYSTEM_ERROR;
        } else {
            return ErrorRecord.ErrorType.UNKNOWN_ERROR;
        }
    }
    
    /**
     * Error statistics
     */
    public static class ErrorStatistics {
        private final long totalErrors;
        private final Map<String, Long> errorTypeCount;
        private final String circuitBreakerState;
        private final float failureRate;
        
        public ErrorStatistics(long totalErrors, Map<String, Long> errorTypeCount, 
                             String circuitBreakerState, float failureRate) {
            this.totalErrors = totalErrors;
            this.errorTypeCount = errorTypeCount;
            this.circuitBreakerState = circuitBreakerState;
            this.failureRate = failureRate;
        }
        
        public long getTotalErrors() { return totalErrors; }
        public Map<String, Long> getErrorTypeCount() { return errorTypeCount; }
        public String getCircuitBreakerState() { return circuitBreakerState; }
        public float getFailureRate() { return failureRate; }
    }
}