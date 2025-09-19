package com.integrixs.backend.domain.service;

import com.integrixs.data.model.Message;
import com.integrixs.data.model.IntegrationFlow;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for message processing logic
 */
@Service
public class MessageProcessingService {

    /**
     * Creates a new message for processing
     */
    public Message createMessage(IntegrationFlow flow, String payload, String correlationId, int priority) {
        Message message = new Message();
        message.setFlow(flow);
        message.setPayload(payload);
        message.setCorrelationId(correlationId != null ? correlationId : UUID.randomUUID().toString());
        message.setPriority(priority);
        message.setStatus(Message.MessageStatus.PENDING);
        message.setCreatedAt(LocalDateTime.now());
        message.setRetryCount(0);

        return message;
    }

    /**
     * Updates message status based on processing result
     */
    public void updateMessageStatus(Message message, boolean success, String errorMessage) {
        if(success) {
            message.setStatus(Message.MessageStatus.COMPLETED);
            message.setCompletedAt(LocalDateTime.now());
        } else {
            message.setStatus(Message.MessageStatus.FAILED);
            message.setErrorMessage(errorMessage);
            message.setRetryCount(message.getRetryCount() + 1);
        }
    }

    /**
     * Checks if message should be retried
     */
    public boolean shouldRetry(Message message, int maxRetries) {
        return message.getStatus() == Message.MessageStatus.FAILED &&
               message.getRetryCount() < maxRetries;
    }

    /**
     * Marks message as processing
     */
    public void markAsProcessing(Message message) {
        message.setStatus(Message.MessageStatus.PROCESSING);
        message.setProcessedAt(LocalDateTime.now());
    }

    /**
     * Calculates message execution time
     */
    public Long calculateExecutionTime(Message message) {
        if(message.getProcessedAt() == null || message.getCompletedAt() == null) {
            return null;
        }

        return java.time.Duration.between(
            message.getProcessedAt(),
            message.getCompletedAt()
       ).toMillis();
    }

    /**
     * Validates message for processing
     */
    public void validateMessage(Message message) {
        if(message.getFlow() == null) {
            throw new IllegalArgumentException("Message must have an associated flow");
        }

        if(message.getPayload() == null || message.getPayload().trim().isEmpty()) {
            throw new IllegalArgumentException("Message payload cannot be empty");
        }

        if(message.getStatus() != Message.MessageStatus.PENDING &&
            message.getStatus() != Message.MessageStatus.FAILED) {
            throw new IllegalStateException("Message is not in a processable state: " + message.getStatus());
        }
    }
}
