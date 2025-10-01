package com.integrixs.backend.application.service;

import com.integrixs.backend.domain.service.NotificationManagementService;
import com.integrixs.backend.infrastructure.notification.EmailNotificationService;
import com.integrixs.backend.service.FlowAlertingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for notification management
 * Orchestrates notification operations across domain and infrastructure services
 */
@Service
public class NotificationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationApplicationService.class);


    private final NotificationManagementService notificationManagementService;
    private final EmailNotificationService emailNotificationService;
    private final FlowAlertingService alertingService;

    @Autowired(required = false)
    private AuthenticationService authenticationService;

    public NotificationApplicationService(NotificationManagementService notificationManagementService,
                                        EmailNotificationService emailNotificationService,
                                        FlowAlertingService alertingService) {
        this.notificationManagementService = notificationManagementService;
        this.emailNotificationService = emailNotificationService;
        this.alertingService = alertingService;
    }

    /**
     * Send a system alert notification
     */
    public void sendSystemAlert(String subject, String message) {
        try {
            log.info("Sending system alert: {}", subject);

            // Create notification data
            Map<String, Object> alert = notificationManagementService.createSystemAlert(subject, message, "MEDIUM");

            // Send via email if enabled
            if(emailNotificationService.isEmailEnabled()) {
                emailNotificationService.sendAdminEmail(
                    (String) alert.get("subject"),
                    (String) alert.get("message")
               );
            } else {
                log.info("Email notifications disabled. Alert logged: {} - {}", subject, message);
            }

            // Log the alert for now - FlowAlertingService requires AlertRule which is for flow-based alerts
            log.info("System alert triggered - Subject: {}, Message: {}, Severity: {}, Domain: {}/{}",
                     subject, message, alert.get("severity"),
                     alert.get("domainType"), alert.get("domainReferenceId"));

            // Send via email if available
            if(emailNotificationService.isEmailEnabled() && alert.containsKey("recipientEmail")) {
                emailNotificationService.sendEmail((String) alert.get("recipientEmail"), subject, message);
            }

        } catch(Exception e) {
            log.error("Failed to send system alert: {}", e.getMessage(), e);
        }
    }

    /**
     * Send a user notification
     */
    public void sendUserNotification(String userId, String subject, String message) {
        try {
            log.info("Sending user notification to {}: {}", userId, subject);

            // Look up user email from authentication service
            String userEmail = null;
            String username = null;

            if(authenticationService != null) {
                try {
                    // Assuming userId is a UUID
                    com.integrixs.backend.api.dto.user.UserProfileResponse userProfile =
                        authenticationService.getUserProfile(userId);

                    if(userProfile != null) {
                        userEmail = userProfile.getEmail();
                        username = userProfile.getUsername();
                    }
                } catch(Exception ex) {
                    // Try by username if UUID lookup fails
                    try {
                        com.integrixs.backend.api.dto.user.UserProfileResponse userProfile =
                            authenticationService.getUserProfile(userId);

                        if(userProfile != null) {
                            userEmail = userProfile.getEmail();
                            username = userProfile.getUsername();
                        }
                    } catch(Exception e2) {
                        log.warn("Could not find user with ID/username: {}", userId);
                    }
                }
            }

            if(userEmail != null) {
                // Send email notification
                if(emailNotificationService.isEmailEnabled()) {
                    emailNotificationService.sendEmail(userEmail, subject, message);
                    log.info("Email notification sent to user {} ( {})", username, userEmail);
                }

                // Log user notification for audit purposes
                log.info("User notification sent - UserId: {}, Username: {}, Email: {}, Subject: {}",
                         userId, username, userEmail, subject);
            } else {
                log.warn("No email found for user {}. Notification logged: {} - {}", userId, subject, message);
            }

        } catch(Exception e) {
            log.error("Failed to send user notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify adapter health failure
     */
    public void notifyAdapterHealthFailure(String adapterName, String adapterId, String errorMessage) {
        try {
            log.warn("Adapter health failure: {} ( {})", adapterName, adapterId);

            Map<String, Object> notification = notificationManagementService.createAdapterHealthNotification(
                adapterName, adapterId, errorMessage
           );

            if(emailNotificationService.isEmailEnabled()) {
                emailNotificationService.sendNotification(notification);
            }

        } catch(Exception e) {
            log.error("Failed to notify adapter health failure: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify dead letter message
     */
    public void notifyDeadLetterMessage(String flowId, String messageId, String reason) {
        try {
            log.warn("Dead letter message: {} from flow {}", messageId, flowId);

            Map<String, Object> notification = notificationManagementService.createDeadLetterNotification(
                flowId, messageId, reason
           );

            if(emailNotificationService.isEmailEnabled()) {
                emailNotificationService.sendNotification(notification);
            }

        } catch(Exception e) {
            log.error("Failed to notify dead letter message: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify max retries exceeded
     */
    public void notifyMaxRetriesExceeded(String flowId, String errorMessage) {
        try {
            log.error("Max retries exceeded for flow: {}", flowId);

            Map<String, Object> notification = notificationManagementService.createMaxRetriesNotification(
                flowId, errorMessage
           );

            if(emailNotificationService.isEmailEnabled()) {
                emailNotificationService.sendNotification(notification);
            }

        } catch(Exception e) {
            log.error("Failed to notify max retries exceeded: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify error threshold exceeded
     */
    public void notifyErrorThresholdExceeded(String flowId, int errorCount) {
        try {
            log.error("Error threshold exceeded for flow {}: {} errors", flowId, errorCount);

            Map<String, Object> notification = notificationManagementService.createErrorThresholdNotification(
                flowId, errorCount, 10 // Default threshold
           );

            if(emailNotificationService.isEmailEnabled()) {
                emailNotificationService.sendNotification(notification);
            }

        } catch(Exception e) {
            log.error("Failed to notify error threshold exceeded: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify flow execution failure
     */
    public void notifyFlowExecutionFailure(String flowId, String errorMessage) {
        notifyFlowExecutionFailure(flowId, null, errorMessage);
    }

    /**
     * Notify flow execution failure with flow name
     */
    public void notifyFlowExecutionFailure(String flowId, String flowName, String errorMessage) {
        try {
            log.error("Flow execution failed: {} ( {})", flowName != null ? flowName : flowId, flowId);

            Map<String, Object> notification = notificationManagementService.createFlowExecutionFailureNotification(
                flowId, flowName, errorMessage
           );

            if(emailNotificationService.isEmailEnabled()) {
                emailNotificationService.sendNotification(notification);
            }

        } catch(Exception e) {
            log.error("Failed to notify flow execution failure: {}", e.getMessage(), e);
        }
    }

    /**
     * Test notification configuration
     */
    public boolean testNotificationConfiguration() {
        try {
            if(emailNotificationService.isEmailEnabled()) {
                emailNotificationService.sendAdminEmail(
                    "Notification Test",
                    "This is a test notification from Integrix Flow Bridge. If you received this, notifications are working correctly."
               );
                return true;
            } else {
                log.info("Email notifications are disabled");
                return false;
            }
        } catch(Exception e) {
            log.error("Notification test failed: {}", e.getMessage(), e);
            return false;
        }
    }
}
