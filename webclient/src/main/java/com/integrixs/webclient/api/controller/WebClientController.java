package com.integrixs.webclient.api.controller;

import com.integrixs.webclient.api.dto.*;
import com.integrixs.webclient.application.service.WebClientApplicationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for webclient operations
 */
@RestController
@RequestMapping("/api/webclient")
public class WebClientController {

    private static final Logger logger = LoggerFactory.getLogger(WebClientController.class);

    private final WebClientApplicationService applicationService;

    public WebClientController(WebClientApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Receive an inbound message
     * @param request Message request
     * @return Processing response
     */
    @PostMapping("/messages")
    public ResponseEntity<InboundMessageResponseDTO> receiveMessage(@Valid @RequestBody InboundMessageRequestDTO request) {
        logger.info("Receiving message from source: {}", request.getSource());

        try {
            InboundMessageResponseDTO response = applicationService.receiveMessage(request);

            if(response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch(Exception e) {
            logger.error("Error receiving message: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(InboundMessageResponseDTO.builder()
                            .success(false)
                            .status("ERROR")
                            .error(e.getMessage())
                            .build());
        }
    }

    /**
     * Validate a message
     * @param request Validation request
     * @return Validation response
     */
    @PostMapping("/messages/validate")
    public ResponseEntity<ValidationResponseDTO> validateMessage(@Valid @RequestBody ValidationRequestDTO request) {
        logger.info("Validating message of type: {}", request.getMessageType());

        try {
            ValidationResponseDTO response = applicationService.validateMessage(request);
            return ResponseEntity.ok(response);
        } catch(Exception e) {
            logger.error("Error validating message: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ValidationResponseDTO.builder()
                            .valid(false)
                            .errors(List.of(ValidationErrorDTO.builder()
                                    .message(e.getMessage())
                                    .type("VALIDATION_ERROR")
                                    .build()))
                            .build());
        }
    }

    /**
     * Get message by ID
     * @param messageId Message ID
     * @return Message details
     */
    @GetMapping("/messages/ {messageId}")
    public ResponseEntity<MessageDetailsDTO> getMessage(@PathVariable String messageId) {
        logger.info("Getting message: {}", messageId);

        try {
            MessageDetailsDTO message = applicationService.getMessage(messageId);
            return ResponseEntity.ok(message);
        } catch(Exception e) {
            logger.error("Error getting message {}: {}", messageId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search messages
     * @param criteria Search criteria
     * @return List of messages
     */
    @PostMapping("/messages/search")
    public ResponseEntity<List<MessageDetailsDTO>> searchMessages(@RequestBody MessageSearchCriteriaDTO criteria) {
        logger.info("Searching messages with criteria: {}", criteria);

        try {
            List<MessageDetailsDTO> messages = applicationService.searchMessages(criteria);
            return ResponseEntity.ok(messages);
        } catch(Exception e) {
            logger.error("Error searching messages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Clean up old messages
     * @param daysToKeep Days to keep messages
     * @return Number of deleted messages
     */
    @DeleteMapping("/messages/cleanup")
    public ResponseEntity<Integer> cleanupMessages(@RequestParam(defaultValue = "30") int daysToKeep) {
        logger.info("Cleaning up messages older than {} days", daysToKeep);

        try {
            int deleted = applicationService.cleanupOldMessages(daysToKeep);
            logger.info("Deleted {} old messages", deleted);
            return ResponseEntity.ok(deleted);
        } catch(Exception e) {
            logger.error("Error cleaning up messages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("WebClient service is healthy");
    }
}
