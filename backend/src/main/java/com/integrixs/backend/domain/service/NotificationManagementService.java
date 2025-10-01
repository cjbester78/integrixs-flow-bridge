package com.integrixs.backend.domain.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain service for notification management
 * Contains core business logic for notification handling
 */
@Service
public class NotificationManagementService {

    /**
     * Create a system alert notification
     */
    public Map<String, Object> createSystemAlert(String subject, String message, String severity) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("type", "SYSTEM_ALERT");
        alert.put("subject", subject);
        alert.put("message", message);
        alert.put("severity", severity != null ? severity : "MEDIUM");
        alert.put("timestamp", LocalDateTime.now());
        alert.put("requiresAcknowledgement", isHighSeverity(severity));

        return alert;
    }

    /**
     * Create an adapter health notification
     */
    public Map<String, Object> createAdapterHealthNotification(String adapterName, String adapterId, String errorMessage) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ADAPTER_HEALTH_FAILURE");
        notification.put("subject", String.format("Adapter Health Failure: %s", adapterName));
        notification.put("message", String.format("Adapter %s(ID: %s) is experiencing health issues.\n\nError: %s",
                                                 adapterName, adapterId, errorMessage));
        notification.put("severity", "HIGH");
        notification.put("adapterId", adapterId);
        notification.put("adapterName", adapterName);
        notification.put("timestamp", LocalDateTime.now());

        return notification;
    }

    /**
     * Create a dead letter queue notification
     */
    public Map<String, Object> createDeadLetterNotification(String flowId, String messageId, String reason) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "DEAD_LETTER_MESSAGE");
        notification.put("subject", "Message Sent to Dead Letter Queue");
        notification.put("message", String.format("Message %s from flow %s has been sent to dead letter queue.\n\nReason: %s",
                                                 messageId, flowId, reason));
        notification.put("severity", "HIGH");
        notification.put("flowId", flowId);
        notification.put("messageId", messageId);
        notification.put("reason", reason);
        notification.put("timestamp", LocalDateTime.now());

        return notification;
    }

    /**
     * Create a max retries exceeded notification
     */
    public Map<String, Object> createMaxRetriesNotification(String flowId, String errorMessage) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "MAX_RETRIES_EXCEEDED");
        notification.put("subject", String.format("Max Retries Exceeded for Flow: %s", flowId));
        notification.put("message", String.format("Flow %s has exceeded maximum retry attempts.\n\nLast Error: %s",
                                                 flowId, errorMessage));
        notification.put("severity", "HIGH");
        notification.put("flowId", flowId);
        notification.put("errorMessage", errorMessage);
        notification.put("timestamp", LocalDateTime.now());

        return notification;
    }

    /**
     * Create an error threshold exceeded notification
     */
    public Map<String, Object> createErrorThresholdNotification(String flowId, int errorCount, int threshold) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ERROR_THRESHOLD_EXCEEDED");
        notification.put("subject", String.format("Error Threshold Exceeded for Flow: %s", flowId));
        notification.put("message", String.format("Flow %s has exceeded the error threshold with %d errors(threshold: %d).",
                                                 flowId, errorCount, threshold));
        notification.put("severity", "HIGH");
        notification.put("flowId", flowId);
        notification.put("errorCount", errorCount);
        notification.put("threshold", threshold);
        notification.put("timestamp", LocalDateTime.now());

        return notification;
    }

    /**
     * Create a flow execution failure notification
     */
    public Map<String, Object> createFlowExecutionFailureNotification(String flowId, String flowName, String errorMessage) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "FLOW_EXECUTION_FAILURE");
        notification.put("subject", String.format("Flow Execution Failed: %s", flowName != null ? flowName : flowId));
        notification.put("message", String.format("Flow %s has failed execution.\n\nError: %s",
                                                 flowName != null ? flowName : flowId, errorMessage));
        notification.put("severity", "HIGH");
        notification.put("flowId", flowId);
        notification.put("flowName", flowName);
        notification.put("errorMessage", errorMessage);
        notification.put("timestamp", LocalDateTime.now());

        return notification;
    }

    /**
     * Validate notification configuration
     */
    public void validateNotificationConfig(String type, String recipient) {
        if(type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification type is required");
        }

        if(!isValidNotificationType(type)) {
            throw new IllegalArgumentException("Invalid notification type: " + type);
        }

        if(recipient != null && !isValidRecipient(recipient)) {
            throw new IllegalArgumentException("Invalid recipient format: " + recipient);
        }
    }

    /**
     * Check if notification type is valid
     */
    private boolean isValidNotificationType(String type) {
        return "EMAIL".equals(type) ||
               "SMS".equals(type) ||
               "WEBHOOK".equals(type) ||
               "IN_APP".equals(type);
    }

    /**
     * Check if recipient format is valid
     */
    private boolean isValidRecipient(String recipient) {
        // Basic email validation
        if(recipient.contains("@")) {
            return recipient.matches("^[A - Za - z0-9 + _. - ] + @(. + )$");
        }
        // Could add phone number validation for SMS
        return true;
    }

    /**
     * Check if severity is high
     */
    private boolean isHighSeverity(String severity) {
        return "HIGH".equals(severity) || "CRITICAL".equals(severity);
    }

    /**
     * Get notification priority based on type and severity
     */
    public int getNotificationPriority(String type, String severity) {
        if("CRITICAL".equals(severity)) {
            return 1; // Highest priority
        }

        if("HIGH".equals(severity)) {
            return 2;
        }

        if("FLOW_EXECUTION_FAILURE".equals(type) ||
            "ADAPTER_HEALTH_FAILURE".equals(type) ||
            "DEAD_LETTER_MESSAGE".equals(type)) {
            return 3;
        }

        return 5; // Default priority
    }
}
