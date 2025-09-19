package com.integrixs.webclient.infrastructure.service;

import com.integrixs.webclient.domain.model.InboundMessage;
import com.integrixs.webclient.domain.model.ProcessingResult;
import com.integrixs.webclient.domain.model.ValidationResult;
import com.integrixs.webclient.domain.repository.InboundMessageRepository;
import com.integrixs.webclient.domain.service.InboundMessageService;
import com.integrixs.webclient.infrastructure.client.FlowExecutionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of inbound message service
 */
@Service
public class InboundMessageServiceImpl implements InboundMessageService {

    private static final Logger logger = LoggerFactory.getLogger(InboundMessageServiceImpl.class);

    private final InboundMessageRepository repository;
    private final FlowExecutionClient flowExecutionClient;
    private final MessageTransformer messageTransformer;

    public InboundMessageServiceImpl(
            InboundMessageRepository repository,
            FlowExecutionClient flowExecutionClient,
            MessageTransformer messageTransformer) {
        this.repository = repository;
        this.flowExecutionClient = flowExecutionClient;
        this.messageTransformer = messageTransformer;
    }

    @Override
    public ProcessingResult processMessage(InboundMessage message) {
        logger.info("Processing message {} for flow {}", message.getMessageId(), message.getFlowId());
        long startTime = System.currentTimeMillis();

        try {
            // Update status to processing
            updateMessageStatus(message.getMessageId(), InboundMessage.MessageStatus.PROCESSING);

            // Execute flow with message
            String executionId = flowExecutionClient.executeFlow(message.getFlowId(), message);

            // Update status to processed
            updateMessageStatus(message.getMessageId(), InboundMessage.MessageStatus.PROCESSED);

            return ProcessingResult.success(message.getMessageId(), message.getFlowId(), executionId)
                    .withProcessingTime(startTime);

        } catch(Exception e) {
            logger.error("Failed to process message {}: {}", message.getMessageId(), e.getMessage(), e);
            updateMessageStatus(message.getMessageId(), InboundMessage.MessageStatus.FAILED);

            return ProcessingResult.failureWithCode(message.getMessageId(), "PROCESSING_ERROR", e.getMessage())
                    .withProcessingTime(startTime);
        }
    }

    @Override
    public ValidationResult validateMessage(InboundMessage message) {
        logger.debug("Validating message {}", message.getMessageId());

        ValidationResult result = ValidationResult.builder()
                .valid(true)
                .build();

        // Check required fields
        if(message.getPayload() == null) {
            result.withError("payload", "Payload is required", ValidationResult.ValidationError.ErrorType.MISSING_FIELD);
        }

        if(message.getMessageType() == null) {
            result.withError("messageType", "Message type is required", ValidationResult.ValidationError.ErrorType.MISSING_FIELD);
        }

        if(message.getSource() == null || message.getSource().isEmpty()) {
            result.withError("source", "Source is required", ValidationResult.ValidationError.ErrorType.MISSING_FIELD);
        }

        // Check content type
        if(message.getContentType() == null && message.getPayload() != null) {
            result.withWarning("Content type not specified, defaulting to application/json");
        }

        return result;
    }

    @Override
    public String routeMessage(InboundMessage message) {
        // This is a simplified implementation
        // In a real system, this would use routing rules
        return message.getFlowId();
    }

    @Override
    public Object transformMessage(InboundMessage message, String targetFormat) {
        logger.debug("Transforming message {} to format {}", message.getMessageId(), targetFormat);
        return messageTransformer.transform(message.getPayload(), message.getContentType(), targetFormat);
    }

    @Override
    public boolean isDuplicateMessage(InboundMessage message) {
        return repository.existsDuplicate(message);
    }

    @Override
    public String storeMessage(InboundMessage message) {
        InboundMessage saved = repository.save(message);
        logger.debug("Stored message with ID: {}", saved.getMessageId());
        return saved.getMessageId();
    }

    @Override
    public InboundMessage getMessage(String messageId) {
        return repository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));
    }

    @Override
    public void updateMessageStatus(String messageId, InboundMessage.MessageStatus status) {
        repository.updateStatus(messageId, status);
        logger.debug("Updated message {} status to {}", messageId, status);
    }
}
