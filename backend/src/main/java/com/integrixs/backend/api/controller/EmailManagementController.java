package com.integrixs.backend.api.controller;

import com.integrixs.backend.infrastructure.email.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for email management
 * Handles email configuration testing
 */
@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailManagementController {

    private static final Logger log = LoggerFactory.getLogger(EmailManagementController.class);


    private final EmailService emailService;

    public EmailManagementController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Test email configuration
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> testEmailConfiguration(
            @RequestBody Map<String, String> request) {

        String recipient = request.get("recipient");
        if(recipient == null || recipient.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Recipient email is required"
           ));
        }

        log.info("Testing email configuration with recipient: {}", recipient);

        boolean success = emailService.testEmailConfiguration(recipient);

        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ?
                "Test email sent successfully. Please check your inbox." :
                "Failed to send test email. Please check email configuration and logs."
       ));
    }
}
