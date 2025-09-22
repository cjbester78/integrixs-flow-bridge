package com.integrixs.backend.camunda;

import com.integrixs.backend.service.NotificationService;
import com.integrixs.data.model.Alert;
import com.integrixs.data.model.AlertRule;
import com.integrixs.data.repository.NotificationChannelRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Camunda delegate for handling errors in processes
 * This delegate handles error logging, notifications, and recovery strategies
 */
@Component("integrixErrorHandler")
public class IntegrixErrorDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(IntegrixErrorDelegate.class);

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired(required = false)
    private NotificationChannelRepository notificationChannelRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String errorCode = (String) execution.getVariable("errorCode");
        String errorMessage = (String) execution.getVariable("errorMessage");
        String failedActivity = (String) execution.getVariable("failedActivity");

        logger.error("Process error occurred - Code: {}, Message: {}, Activity: {}",
            errorCode, errorMessage, failedActivity);

        try {
            // Log error details
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("processInstanceId", execution.getProcessInstanceId());
            errorDetails.put("processDefinitionId", execution.getProcessDefinitionId());
            errorDetails.put("errorCode", errorCode);
            errorDetails.put("errorMessage", errorMessage);
            errorDetails.put("failedActivity", failedActivity);
            errorDetails.put("timestamp", LocalDateTime.now().toString());
            errorDetails.put("businessKey", execution.getBusinessKey());

            // Store error details
            execution.setVariable("lastError", errorDetails);
            execution.setVariable("errorCount", getErrorCount(execution) + 1);

            // Determine error handling strategy
            String errorStrategy = determineErrorStrategy(execution);
            execution.setVariable("errorStrategy", errorStrategy);

            // Send notification if configured
            if(shouldSendNotification(execution)) {
                sendErrorNotification(execution, errorDetails);
            }

            // Apply error handling strategy
            applyErrorStrategy(execution, errorStrategy);

        } catch(Exception e) {
            logger.error("Failed to handle error properly", e);
            // Don't throw - we're already in error handling
        }
    }

    /**
     * Get current error count
     */
    private int getErrorCount(DelegateExecution execution) {
        Integer count = (Integer) execution.getVariable("errorCount");
        return count != null ? count : 0;
    }

    /**
     * Determine error handling strategy based on error type and count
     */
    private String determineErrorStrategy(DelegateExecution execution) {
        String errorCode = (String) execution.getVariable("errorCode");
        int errorCount = getErrorCount(execution);

        // Check for specific error strategies
        String configuredStrategy = (String) execution.getVariable("errorHandlingStrategy");
        if(configuredStrategy != null) {
            return configuredStrategy;
        }

        // Default strategies based on error count
        if(errorCount <= 3) {
            return "RETRY";
        } else if(errorCount <= 5) {
            return "RETRY_WITH_BACKOFF";
        } else {
            return "DEAD_LETTER_QUEUE";
        }
    }

    /**
     * Check if notification should be sent
     */
    private boolean shouldSendNotification(DelegateExecution execution) {
        // Check if notifications are enabled
        Boolean notificationsEnabled = (Boolean) execution.getVariable("errorNotificationsEnabled");
        if(notificationsEnabled != null && !notificationsEnabled) {
            return false;
        }

        // Send notification for critical errors or after multiple failures
        String errorCode = (String) execution.getVariable("errorCode");
        int errorCount = getErrorCount(execution);

        return errorCode != null && (errorCode.startsWith("CRITICAL") || errorCount >= 3);
    }

    /**
     * Send error notification
     */
    private void sendErrorNotification(DelegateExecution execution, Map<String, Object> errorDetails) {
        if(notificationService == null) {
            logger.warn("Notification service not available");
            return;
        }

        try {
            String subject = String.format("Process Error: %s - %s",
                execution.getProcessDefinitionId(),
                errorDetails.get("errorCode"));

            String message = String.format(
                "Process Instance: %s\n" +
                "Error Code: %s\n" +
                "Error Message: %s\n" +
                "Failed Activity: %s\n" +
                "Time: %s\n" +
                "Error Count: %d",
                errorDetails.get("processInstanceId"),
                errorDetails.get("errorCode"),
                errorDetails.get("errorMessage"),
                errorDetails.get("failedActivity"),
                errorDetails.get("timestamp"),
                getErrorCount(execution)
           );

            // Create an alert for the error notification
            // Convert Map<String, Object> to Map<String, String> for Alert builder
            Map<String, String> stringDetails = new HashMap<>();
            errorDetails.forEach((key, value) -> stringDetails.put(key, String.valueOf(value)));
            
            Alert errorAlert = Alert.builder()
                .title(subject)
                .message(message)
                .severity(AlertRule.AlertSeverity.HIGH)
                .details(stringDetails)
                .build();
            
            // Send to default error notification channel (first available channel)
            notificationChannelRepository.findByEnabledTrue().stream()
                .findFirst()
                .ifPresent(channel -> notificationService.sendNotification(channel, errorAlert));
            logger.info("Error notification sent");

        } catch(Exception e) {
            logger.error("Failed to send error notification", e);
        }
    }

    /**
     * Apply error handling strategy
     */
    private void applyErrorStrategy(DelegateExecution execution, String strategy) {
        logger.info("Applying error strategy: {}", strategy);

        switch(strategy) {
            case "RETRY":
                // Set retry flag
                execution.setVariable("retryRequired", true);
                execution.setVariable("retryDelay", 5000L); // 5 seconds
                break;

            case "RETRY_WITH_BACKOFF":
                // Exponential backoff
                int errorCount = getErrorCount(execution);
                long backoffDelay = Math.min(5000L * (long) Math.pow(2, errorCount - 1), 300000L); // Max 5 minutes
                execution.setVariable("retryRequired", true);
                execution.setVariable("retryDelay", backoffDelay);
                break;

            case "COMPENSATE":
                // Trigger compensation
                execution.setVariable("compensationRequired", true);
                break;

            case "DEAD_LETTER_QUEUE":
                // Move to DLQ
                execution.setVariable("moveToDeadLetterQueue", true);
                execution.setVariable("dlqReason", execution.getVariable("errorMessage"));
                break;

            case "MANUAL_INTERVENTION":
                // Create user task for manual intervention
                execution.setVariable("manualInterventionRequired", true);
                break;

            case "SKIP":
                // Skip the failed activity and continue
                execution.setVariable("skipFailedActivity", true);
                break;

            default:
                // Fail and stop
                execution.setVariable("processTerminated", true);
        }
    }
}
