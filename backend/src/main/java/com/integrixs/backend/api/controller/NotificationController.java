package com.integrixs.backend.api.controller;

import com.integrixs.backend.application.service.NotificationApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for notification management
 * Handles notification testing and configuration
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);


    private final NotificationApplicationService notificationApplicationService;

    public NotificationController(NotificationApplicationService notificationApplicationService) {
        this.notificationApplicationService = notificationApplicationService;
    }

    /**
     * Test notification configuration
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> testNotifications() {
        log.info("Testing notification configuration");

        boolean success = notificationApplicationService.testNotificationConfiguration();

        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ? "Test notification sent successfully" : "Notifications are disabled or not configured"
       ));
    }

    /**
     * Send a custom system alert(for testing purposes)
     */
    @PostMapping("/alert")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> sendSystemAlert(
            @RequestBody Map<String, String> request) {

        String subject = request.get("subject");
        String message = request.get("message");

        if(subject == null || subject.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Subject is required"
           ));
        }

        if(message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Message is required"
           ));
        }

        log.info("Sending custom system alert: {}", subject);
        notificationApplicationService.sendSystemAlert(subject, message);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Alert sent successfully"
       ));
    }
}
