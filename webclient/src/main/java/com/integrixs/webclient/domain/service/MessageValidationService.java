package com.integrixs.webclient.domain.service;

import com.integrixs.webclient.domain.model.InboundMessage;
import com.integrixs.webclient.domain.model.ValidationResult;

/**
 * Domain service interface for message validation
 */
public interface MessageValidationService {

    /**
     * Validate message format
     * @param message The message to validate
     * @return Validation result
     */
    ValidationResult validateFormat(InboundMessage message);

    /**
     * Validate message schema
     * @param message The message to validate
     * @param schemaId Schema ID to validate against
     * @return Validation result
     */
    ValidationResult validateSchema(InboundMessage message, String schemaId);

    /**
     * Validate message security
     * @param message The message to validate
     * @return Validation result
     */
    ValidationResult validateSecurity(InboundMessage message);

    /**
     * Validate message size constraints
     * @param message The message to validate
     * @param maxSize Maximum allowed size
     * @return Validation result
     */
    ValidationResult validateSize(InboundMessage message, long maxSize);

    /**
     * Perform complete validation
     * @param message The message to validate
     * @return Validation result
     */
    ValidationResult validate(InboundMessage message);
}
