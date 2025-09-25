package com.integrixs.backend.application.service;

import com.integrixs.backend.domain.service.LogManagementService;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import com.integrixs.backend.infrastructure.email.EmailService;
import com.integrixs.data.model.SystemLog;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for email operations
 * Orchestrates email sending with business logic and logging
 */
@Service
public class EmailApplicationService {

    private static final Logger log = LoggerFactory.getLogger(EmailApplicationService.class);


    private final EmailService emailService;
    private final SystemLogSqlRepository systemLogRepository;
    private final LogManagementService logManagementService;

    public EmailApplicationService(EmailService emailService,
                                   SystemLogSqlRepository systemLogRepository,
                                   LogManagementService logManagementService) {
        this.emailService = emailService;
        this.systemLogRepository = systemLogRepository;
        this.logManagementService = logManagementService;
    }

    /**
     * Send alert email with formatted content
     */
    @Async
    public CompletableFuture<Boolean> sendAlertEmail(String alertType, String title, String message,
                                                    Map<String, Object> details, List<String> recipients) {
        String htmlBody = buildAlertEmailBody(alertType, title, message, details);
        String subject = String.format("[%s Alert] %s", alertType.toUpperCase(), title);

        CompletableFuture<Boolean> result = emailService.sendEmail(recipients, subject, htmlBody, true);

        // Log the email attempt
        result.thenAccept(success -> logEmailSent(recipients, subject, success, success ? null : "Email send failed"));

        return result;
    }

    /**
     * Send flow execution failure notification
     */
    @Async
    public CompletableFuture<Boolean> sendFlowExecutionFailureEmail(String flowName, String flowId,
                                                                   String errorMessage, List<String> recipients) {
        Map<String, Object> details = new HashMap<>();
        details.put("Flow Name", flowName);
        details.put("Flow ID", flowId);
        details.put("Timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return sendAlertEmail("ERROR", "Flow Execution Failed", errorMessage, details, recipients);
    }

    /**
     * Send system alert email
     */
    @Async
    public CompletableFuture<Boolean> sendSystemAlertEmail(String alertLevel, String component,
                                                          String message, List<String> recipients) {
        Map<String, Object> details = new HashMap<>();
        details.put("Component", component);
        details.put("Alert Level", alertLevel);
        details.put("Timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return sendAlertEmail(alertLevel, "System Alert", message, details, recipients);
    }

    /**
     * Send adapter health alert email
     */
    @Async
    public CompletableFuture<Boolean> sendAdapterHealthAlertEmail(String adapterName, String adapterId,
                                                                 String status, String errorMessage,
                                                                 List<String> recipients) {
        Map<String, Object> details = new HashMap<>();
        details.put("Adapter Name", adapterName);
        details.put("Adapter ID", adapterId);
        details.put("Status", status);
        details.put("Timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        String title = status.equalsIgnoreCase("DOWN") ? "Adapter Down" : "Adapter Health Issue";

        return sendAlertEmail("WARNING", title, errorMessage, details, recipients);
    }

    /**
     * Build HTML body for alert emails
     */
    private String buildAlertEmailBody(String alertType, String title, String message, Map<String, Object> details) {
        StringBuilder html = new StringBuilder();

        // Determine color based on alert type
        String color = switch(alertType.toUpperCase()) {
            case "ERROR", "CRITICAL" -> "#dc3545";
            case "WARNING" -> "#ffc107";
            case "INFO" -> "#17a2b8";
            default -> "#6c757d";
        };

        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<style>");
        html.append("body { font - family: Arial, sans - serif; margin: 0; padding: 0; background - color: #f4f4f4; }");
        html.append(".container { max - width: 600px; margin: 20px auto; background - color: #ffffff; border - radius: 8px; overflow: hidden; box - shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        html.append(".header { background - color: ").append(color).append("; color: white; padding: 20px; text - align: center; }");
        html.append(".content { padding: 30px; }");
        html.append(".alert - box { background - color: #f8f9fa; border - left: 4px solid ").append(color).append("; padding: 15px; margin: 20px 0; }");
        html.append(".details - table { width: 100%; border - collapse: collapse; margin - top: 20px; }");
        html.append(".details - table td { padding: 10px; border - bottom: 1px solid #e0e0e0; }");
        html.append(".details - table td:first - child { font - weight: bold; width: 30%; color: #555; }");
        html.append(".footer { background - color: #f8f9fa; padding: 20px; text - align: center; font - size: 12px; color: #666; }");
        html.append("</style>");
        html.append("</head><body>");

        // Container
        html.append("<div class = 'container'>");

        // Header
        html.append("<div class = 'header'>");
        html.append("<h2 style = 'margin: 0;'>").append(title).append("</h2>");
        html.append("<p style = 'margin: 10px 0 0 0; font - size: 14px;'>").append(alertType.toUpperCase()).append(" ALERT</p>");
        html.append("</div>");

        // Content
        html.append("<div class = 'content'>");
        html.append("<div class = 'alert - box'>");
        html.append("<p style = 'margin: 0;'>").append(message).append("</p>");
        html.append("</div>");

        // Details table
        if(details != null && !details.isEmpty()) {
            html.append("<h3 style = 'color: #333;'>Details</h3>");
            html.append("<table class = 'details - table'>");
            for(Map.Entry<String, Object> entry : details.entrySet()) {
                html.append("<tr>");
                html.append("<td>").append(entry.getKey()).append(":</td>");
                html.append("<td>").append(entry.getValue()).append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
        }

        html.append("</div>");

        // Footer
        html.append("<div class = 'footer'>");
        html.append("<p>This is an automated message from Integrix Flow Bridge</p>");
        html.append("<p>Please do not reply to this email</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Log email sending attempt
     */
    private void logEmailSent(List<String> recipients, String subject, boolean success, String error) {
        try {
            SystemLog log = logManagementService.createLog(
                success ? SystemLog.LogLevel.INFO : SystemLog.LogLevel.ERROR,
                success ?
                    "Email sent successfully to " + recipients.size() + " recipients" :
                    "Failed to send email: " + error,
                "EmailService",
                String.format("Recipients: %s; Subject: %s; Success: %s",
                    String.join(", ", recipients),
                    subject,
                    success),
                null,
                "Notification",
                null
           );

            systemLogRepository.save(log);
        } catch(Exception e) {
            log.error("Failed to log email activity", e);
        }
    }
}
