package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.request.QueueMessageRequest;
import com.integrixs.backend.api.dto.response.MessageResponse;
import com.integrixs.backend.domain.service.MessageProcessingService;
import com.integrixs.backend.infrastructure.messaging.MessageQueue;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.Message;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.data.sql.repository.MessageSqlRepository;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for message queue management and processing
 */
@Service
public class MessageQueueManagementService {

    private static final Logger log = LoggerFactory.getLogger(MessageQueueManagementService.class);


    private final MessageSqlRepository messageRepository;
    private final IntegrationFlowSqlRepository flowRepository;
    private final SystemLogSqlRepository systemLogRepository;
    private final MessageProcessingService processingService;
    private final MessageQueue messageQueue;
    private final FlowExecutionApplicationService flowExecutionService;

    private static final int DEFAULT_PRIORITY = 5;
    private static final int MAX_RETRY_COUNT = 3;

    public MessageQueueManagementService(MessageSqlRepository messageRepository,
                                         IntegrationFlowSqlRepository flowRepository,
                                         SystemLogSqlRepository systemLogRepository,
                                         MessageProcessingService processingService,
                                         MessageQueue messageQueue,
                                         FlowExecutionApplicationService flowExecutionService) {
        this.messageRepository = messageRepository;
        this.flowRepository = flowRepository;
        this.systemLogRepository = systemLogRepository;
        this.processingService = processingService;
        this.messageQueue = messageQueue;
        this.flowExecutionService = flowExecutionService;
    }

    public MessageResponse queueMessage(QueueMessageRequest request) {
        log.debug("Queueing message for flow: {}", request.getFlowId());

        // Find the flow
        IntegrationFlow flow = flowRepository.findById(UUID.fromString(request.getFlowId()))
            .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + request.getFlowId()));

        // Create the message
        Message message = processingService.createMessage(
            flow,
            request.getPayload(),
            request.getCorrelationId(),
            request.getPriority()
       );

        // Validate message
        processingService.validateMessage(message);

        // Save to database
        message = messageRepository.save(message);

        // Add to queue for processing
        messageQueue.enqueue(message);

        // Log the action
        logMessageAction(message, "QUEUED", "Message queued for processing");

        return convertToResponse(message);
    }

    public MessageResponse processMessage(String messageId) {
        log.debug("Processing message: {}", messageId);

        Message message = messageRepository.findById(UUID.fromString(messageId))
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        // Validate message can be processed
        processingService.validateMessage(message);

        // Mark as processing
        processingService.markAsProcessing(message);
        messageRepository.save(message);

        try {
            // Execute the flow asynchronously
            flowExecutionService.executeFlow(message.getFlow().getId().toString());
            log.info("Flow execution triggered for flow: {}", message.getFlow().getId());

            // Mark as completed
            processingService.updateMessageStatus(message, true, null);
            messageRepository.save(message);

            logMessageAction(message, "COMPLETED", "Message processed successfully");

        } catch(Exception e) {
            log.error("Error processing message: {}", messageId, e);

            // Mark as failed
            processingService.updateMessageStatus(message, false, e.getMessage());
            messageRepository.save(message);

            // Check if should retry
            if(processingService.shouldRetry(message, MAX_RETRY_COUNT)) {
                message.setStatus(Message.MessageStatus.PENDING);
                messageRepository.save(message);
                messageQueue.enqueue(message);
                logMessageAction(message, "RETRY_QUEUED", "Message queued for retry");
            } else {
                logMessageAction(message, "FAILED", "Message processing failed: " + e.getMessage());
            }
        }

        return convertToResponse(message);
    }

    public MessageResponse retryMessage(String messageId) {
        log.debug("Retrying message: {}", messageId);

        Message message = messageRepository.findById(UUID.fromString(messageId))
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        if(message.getStatus() != Message.MessageStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed messages");
        }

        // Reset status and queue for retry
        message.setStatus(Message.MessageStatus.PENDING);
        messageRepository.save(message);

        messageQueue.enqueue(message);

        logMessageAction(message, "RETRY_REQUESTED", "Manual retry requested");

        return convertToResponse(message);
    }

    public void cancelMessage(String messageId) {
        log.debug("Cancelling message: {}", messageId);

        Message message = messageRepository.findById(UUID.fromString(messageId))
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        if(message.getStatus() == Message.MessageStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed messages");
        }

        // Update status
        message.setStatus(Message.MessageStatus.CANCELLED);
        messageRepository.save(message);

        // Remove from queue if present
        messageQueue.remove(message.getId());

        logMessageAction(message, "CANCELLED", "Message cancelled by user");
    }

    public List<MessageResponse> getPendingMessages(int limit) {
        log.debug("Getting pending messages, limit: {}", limit);

        List<Message> messages = messageRepository.findByStatusOrderByPriorityAndReceivedAt(Message.MessageStatus.PENDING)
            .stream()
            .limit(limit)
            .collect(Collectors.toList());

        return messages.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public long getQueueSize() {
        return messageRepository.countByStatus(Message.MessageStatus.PENDING);
    }

    public long getProcessingCount() {
        return messageRepository.countByStatus(Message.MessageStatus.PROCESSING);
    }

    public long getFailedCount() {
        return messageRepository.countByStatus(Message.MessageStatus.FAILED);
    }

    public void processNextInQueue() {
        List<Message> pendingMessages = messageRepository.findByStatusOrderByPriorityAndReceivedAt(Message.MessageStatus.PENDING)
            .stream()
            .limit(1)
            .collect(Collectors.toList());

        if(!pendingMessages.isEmpty()) {
            Message message = pendingMessages.get(0);
            processMessage(message.getId().toString());
        }
    }

    private MessageResponse convertToResponse(Message message) {
        Long executionTime = processingService.calculateExecutionTime(message);

        return MessageResponse.builder()
            .id(message.getId().toString())
            .correlationId(message.getCorrelationId())
            .flowId(message.getFlow().getId().toString())
            .flowName(message.getFlow().getName())
            .status(message.getStatus().name())
            .payload(message.getPayload())
            .errorMessage(message.getErrorMessage())
            .retryCount(message.getRetryCount())
            .priority(message.getPriority())
            .createdAt(message.getCreatedAt())
            .processedAt(message.getProcessedAt())
            .completedAt(message.getCompletedAt())
            .executionTimeMs(executionTime)
            .build();
    }

    private void logMessageAction(Message message, String action, String details) {
        SystemLog log = new SystemLog();
        log.setCategory("MESSAGE_QUEUE");
        log.setLevel(SystemLog.LogLevel.INFO);
        log.setTimestamp(LocalDateTime.now());
        log.setComponentId("MessageQueue");
        log.setCorrelationId(message.getCorrelationId());
        log.setDomainType("Message");
        log.setDomainId(message.getId().toString());
        log.setAction(action);
        log.setMessage("Message " + action + " for flow: " + message.getFlow().getName());
        log.setDetails("status: " + message.getStatus() + ", " + details);

        systemLogRepository.save(log);
    }
}
