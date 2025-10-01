package com.integrixs.webclient.infrastructure.service;

import com.integrixs.webclient.domain.model.InboundMessage;
import com.integrixs.webclient.domain.model.ValidationResult;
import com.integrixs.webclient.domain.service.MessageValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of message validation service
 */
@Service
public class MessageValidationServiceImpl implements MessageValidationService {

    private static final Logger logger = LoggerFactory.getLogger(MessageValidationServiceImpl.class);

    private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public ValidationResult validateFormat(InboundMessage message) {
        logger.debug("Validating format for message {}", message.getMessageId());

        ValidationResult result = ValidationResult.builder()
                .valid(true)
                .build();

        // Validate content type matches payload
        String contentType = message.getContentType();
        Object payload = message.getPayload();

        if(contentType != null && payload != null) {
            if(contentType.contains("json") && !(payload instanceof String || payload instanceof java.util.Map)) {
                result.withError("payload", "Payload format does not match content type",
                    ValidationResult.ValidationError.ErrorType.INVALID_FORMAT);
            }

            if(contentType.contains("xml") && !(payload instanceof String)) {
                result.withError("payload", "XML payload must be a string",
                    ValidationResult.ValidationError.ErrorType.INVALID_FORMAT);
            }
        }

        return result;
    }

    @Override
    public ValidationResult validateSchema(InboundMessage message, String schemaId) {
        logger.debug("Validating schema {} for message {}", schemaId, message.getMessageId());

        // This is a simplified implementation
        // In a real system, this would validate against actual schemas
        return ValidationResult.valid();
    }

    @Override
    public ValidationResult validateSecurity(InboundMessage message) {
        logger.debug("Validating security for message {}", message.getMessageId());

        ValidationResult result = ValidationResult.builder()
                .valid(true)
                .build();

        // Check for required security headers
        if(message.getHeaders() != null) {
            // Example: Check for authorization header
            if(!message.getHeaders().containsKey("Authorization") &&
                !message.getHeaders().containsKey("X - API - Key")) {
                result.withWarning("No authentication headers found");
            }
        }

        return result;
    }

    @Override
    public ValidationResult validateSize(InboundMessage message, long maxSize) {
        logger.debug("Validating size for message {}", message.getMessageId());

        ValidationResult result = ValidationResult.builder()
                .valid(true)
                .build();

        // Estimate payload size
        Object payload = message.getPayload();
        if(payload != null) {
            long estimatedSize = estimatePayloadSize(payload);
            if(estimatedSize > maxSize) {
                result.withError("payload",
                    String.format("Payload size %d exceeds maximum allowed size %d", estimatedSize, maxSize),
                    ValidationResult.ValidationError.ErrorType.SIZE_LIMIT_EXCEEDED);
            }
        }

        return result;
    }

    @Override
    public ValidationResult validate(InboundMessage message) {
        logger.info("Performing complete validation for message {}", message.getMessageId());

        // Combine all validations
        ValidationResult result = ValidationResult.builder()
                .valid(true)
                .build();

        // Basic validation
        if(message.getPayload() == null) {
            result.withError("payload", "Payload is required",
                ValidationResult.ValidationError.ErrorType.MISSING_FIELD);
        }

        if(message.getMessageType() == null) {
            result.withError("messageType", "Message type is required",
                ValidationResult.ValidationError.ErrorType.MISSING_FIELD);
        }

        // Format validation
        ValidationResult formatResult = validateFormat(message);
        if(!formatResult.isValid()) {
            formatResult.getErrors().forEach(error ->
                result.withError(error.getField(), error.getMessage(), error.getType()));
        }

        // Size validation
        ValidationResult sizeResult = validateSize(message, DEFAULT_MAX_SIZE);
        if(!sizeResult.isValid()) {
            sizeResult.getErrors().forEach(error ->
                result.withError(error.getField(), error.getMessage(), error.getType()));
        }

        // Security validation
        ValidationResult securityResult = validateSecurity(message);
        result.getWarnings().addAll(securityResult.getWarnings());

        return result;
    }

    private long estimatePayloadSize(Object payload) {
        if(payload instanceof String) {
            return((String) payload).length();
        } else if(payload instanceof byte[]) {
            return((byte[]) payload).length;
        } else {
            // Rough estimate for objects
            return payload.toString().length();
        }
    }
}
