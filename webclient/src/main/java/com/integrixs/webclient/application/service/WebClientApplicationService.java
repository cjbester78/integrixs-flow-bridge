package com.integrixs.webclient.application.service;

import com.integrixs.webclient.api.dto.*;
import com.integrixs.webclient.domain.model.InboundMessage;
import com.integrixs.webclient.domain.model.ProcessingResult;
import com.integrixs.webclient.domain.model.ValidationResult;
import com.integrixs.webclient.domain.repository.InboundMessageRepository;
import com.integrixs.webclient.domain.service.InboundMessageService;
import com.integrixs.webclient.domain.service.WebMessageRoutingService;
import com.integrixs.webclient.domain.service.MessageValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for orchestrating webclient operations
 */
@Service
public class WebClientApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(WebClientApplicationService.class);

    private final InboundMessageService inboundMessageService;
    private final MessageValidationService validationService;
    private final WebMessageRoutingService routingService;
    private final InboundMessageRepository repository;

    public WebClientApplicationService(
            InboundMessageService inboundMessageService,
            MessageValidationService validationService,
            WebMessageRoutingService routingService,
            InboundMessageRepository repository) {
        this.inboundMessageService = inboundMessageService;
        this.validationService = validationService;
        this.routingService = routingService;
        this.repository = repository;
    }

    /**
     * Receive and process an inbound message
     * @param request Message request
     * @return Processing response
     */
    public InboundMessageResponseDTO receiveMessage(InboundMessageRequestDTO request) {
        logger.info("Receiving message from source: {} with adapter: {}", request.getSource(), request.getAdapterId());

        // Convert DTO to domain model
        InboundMessage message = InboundMessage.builder()
                .messageType(InboundMessage.MessageType.valueOf(request.getMessageType()))
                .source(request.getSource())
                .adapterId(request.getAdapterId())
                .payload(request.getPayload())
                .contentType(request.getContentType())
                .headers(request.getHeaders())
                .metadata(request.getMetadata())
                .correlationId(request.getCorrelationId())
                .build();

        // Store message
        String messageId = inboundMessageService.storeMessage(message);
        message.setMessageId(messageId);

        try {
            // Validate message
            ValidationResult validationResult = validationService.validate(message);
            if(!validationResult.isValid()) {
                inboundMessageService.updateMessageStatus(messageId, InboundMessage.MessageStatus.REJECTED);
                logger.warn("Message validation failed: {}", validationResult.getErrorsAsString());

                return InboundMessageResponseDTO.builder()
                        .messageId(messageId)
                        .success(false)
                        .status("REJECTED")
                        .error(validationResult.getErrorsAsString())
                        .validationErrors(convertValidationErrors(validationResult))
                        .receivedAt(message.getReceivedAt())
                        .build();
            }

            // Update status to validated
            inboundMessageService.updateMessageStatus(messageId, InboundMessage.MessageStatus.VALIDATED);

            // Route message
            String flowId = routingService.routeMessage(message);
            if(flowId == null) {
                throw new RuntimeException("No flow found for message");
            }
            message.setFlowId(flowId);

            // Process message
            ProcessingResult result = inboundMessageService.processMessage(message);

            return InboundMessageResponseDTO.builder()
                    .messageId(messageId)
                    .success(result.isSuccess())
                    .status(result.isSuccess() ? "PROCESSED" : "FAILED")
                    .flowId(result.getFlowId())
                    .executionId(result.getExecutionId())
                    .responseData(result.getResponseData())
                    .error(result.getErrorMessage())
                    .receivedAt(message.getReceivedAt())
                    .processedAt(result.getProcessedAt())
                    .processingTimeMillis(result.getProcessingTimeMillis())
                    .build();

        } catch(Exception e) {
            logger.error("Error processing message {}: {}", messageId, e.getMessage(), e);
            inboundMessageService.updateMessageStatus(messageId, InboundMessage.MessageStatus.FAILED);

            return InboundMessageResponseDTO.builder()
                    .messageId(messageId)
                    .success(false)
                    .status("FAILED")
                    .error(e.getMessage())
                    .receivedAt(message.getReceivedAt())
                    .build();
        }
    }

    /**
     * Validate a message without processing
     * @param request Validation request
     * @return Validation response
     */
    public ValidationResponseDTO validateMessage(ValidationRequestDTO request) {
        // Create temporary message for validation
        InboundMessage message = InboundMessage.builder()
                .messageType(InboundMessage.MessageType.valueOf(request.getMessageType()))
                .payload(request.getPayload())
                .contentType(request.getContentType())
                .headers(request.getHeaders())
                .build();

        ValidationResult result = validationService.validate(message);

        return ValidationResponseDTO.builder()
                .valid(result.isValid())
                .errors(convertValidationErrors(result))
                .warnings(result.getWarnings())
                .build();
    }

    /**
     * Get message by ID
     * @param messageId Message ID
     * @return Message details
     */
    public MessageDetailsDTO getMessage(String messageId) {
        InboundMessage message = repository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));

        return convertToDetailsDTO(message);
    }

    /**
     * Search messages
     * @param criteria Search criteria
     * @return List of messages
     */
    public List<MessageDetailsDTO> searchMessages(MessageSearchCriteriaDTO criteria) {
        List<InboundMessage> messages;

        if(criteria.getStatus() != null) {
            messages = repository.findByStatus(InboundMessage.MessageStatus.valueOf(criteria.getStatus()));
        } else if(criteria.getFlowId() != null) {
            messages = repository.findByFlowId(criteria.getFlowId());
        } else if(criteria.getCorrelationId() != null) {
            messages = repository.findByCorrelationId(criteria.getCorrelationId());
        } else if(criteria.getStartDate() != null && criteria.getEndDate() != null) {
            messages = repository.findByReceivedAtBetween(criteria.getStartDate(), criteria.getEndDate());
        } else {
            messages = repository.findByReceivedAtBetween(
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now()
           );
        }

        return messages.stream()
                .map(this::convertToDetailsDTO)
                .collect(Collectors.toList());
    }

    /**
     * Clean up old messages
     * @param daysToKeep Days to keep messages
     * @return Number of deleted messages
     */
    public int cleanupOldMessages(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        return repository.deleteByReceivedAtBefore(cutoff);
    }

    /**
     * Convert validation result to DTO errors
     */
    private List<ValidationErrorDTO> convertValidationErrors(ValidationResult result) {
        return result.getErrors().stream()
                .map(error -> ValidationErrorDTO.builder()
                        .field(error.getField())
                        .message(error.getMessage())
                        .type(error.getType().name())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Convert domain model to details DTO
     */
    private MessageDetailsDTO convertToDetailsDTO(InboundMessage message) {
        return MessageDetailsDTO.builder()
                .messageId(message.getMessageId())
                .messageType(message.getMessageType().name())
                .source(message.getSource())
                .adapterId(message.getAdapterId())
                .payload(message.getPayload())
                .contentType(message.getContentType())
                .headers(message.getHeaders())
                .metadata(message.getMetadata())
                .status(message.getStatus().name())
                .correlationId(message.getCorrelationId())
                .flowId(message.getFlowId())
                .receivedAt(message.getReceivedAt())
                .build();
    }
}
