package com.integrixs.backend.service;

import com.integrixs.data.model.Alert;
import com.integrixs.data.model.AlertRule;
import com.integrixs.data.model.NotificationChannel;
import com.integrixs.data.sql.repository.NotificationChannelSqlRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for sending notifications through various channels
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);


    private final NotificationChannelSqlRepository channelRepository;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy - MM - dd HH:mm:ss");

    public NotificationService(NotificationChannelSqlRepository channelRepository,
                             JavaMailSender mailSender,
                             RestTemplate restTemplate) {
        this.channelRepository = channelRepository;
        this.mailSender = mailSender;
        this.restTemplate = restTemplate;
    }

    /**
     * Send notification for an alert
     */
    public String sendNotification(NotificationChannel channel, Alert alert) {
        if(!channel.isEnabled()) {
            throw new RuntimeException("Notification channel is disabled: " + channel.getChannelName());
        }

        // Check rate limit
        if(!checkRateLimit(channel)) {
            throw new RuntimeException("Rate limit exceeded for channel: " + channel.getChannelName());
        }

        String notificationId = generateNotificationId();

        try {
            switch(channel.getChannelType()) {
                case EMAIL:
                    sendEmailNotification(channel, alert, notificationId);
                    break;

                case SMS:
                    sendSmsNotification(channel, alert, notificationId);
                    break;

                case WEBHOOK:
                    sendWebhookNotification(channel, alert, notificationId);
                    break;

                case SLACK:
                    sendSlackNotification(channel, alert, notificationId);
                    break;

                case TEAMS:
                    sendTeamsNotification(channel, alert, notificationId);
                    break;

                default:
                    throw new RuntimeException("Unsupported channel type: " + channel.getChannelType());
            }

            // Update channel usage
            updateChannelUsage(channel);

            log.info("Notification sent successfully via {} for alert {}",
                    channel.getChannelName(), alert.getAlertId());

            return notificationId;

        } catch(Exception e) {
            log.error("Failed to send notification via {} for alert {}",
                    channel.getChannelName(), alert.getAlertId(), e);
            throw new RuntimeException("Notification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Send escalation notification
     */
    public String sendEscalationNotification(NotificationChannel channel, Alert alert) {
        // Add escalation prefix to the alert title
        String originalTitle = alert.getTitle();
        alert.setTitle("[ESCALATED] " + originalTitle);

        try {
            return sendNotification(channel, alert);
        } finally {
            alert.setTitle(originalTitle);
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(NotificationChannel channel, Alert alert, String notificationId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Get email configuration
            String from = channel.getConfigValue("from_address");
            String to = channel.getConfigValue("to_addresses");

            helper.setFrom(from);
            helper.setTo(to.split(","));
            helper.setSubject(formatEmailSubject(alert));
            helper.setText(formatEmailBody(alert, notificationId), true);

            mailSender.send(message);

        } catch(Exception e) {
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    /**
     * Send SMS notification
     */
    private void sendSmsNotification(NotificationChannel channel, Alert alert, String notificationId) {
        String provider = channel.getConfigValue("provider");

        switch(provider.toLowerCase()) {
            case "twilio":
                sendTwilioSms(channel, alert, notificationId);
                break;

            default:
                throw new RuntimeException("Unsupported SMS provider: " + provider);
        }
    }

    /**
     * Send webhook notification
     */
    private void sendWebhookNotification(NotificationChannel channel, Alert alert, String notificationId) {
        String url = channel.getConfigValue("url");
        String method = channel.getConfigValue("method");

        // Build request body
        Map<String, Object> body = new HashMap<>();
        body.put("notification_id", notificationId);
        body.put("alert_id", alert.getAlertId());
        body.put("title", alert.getTitle());
        body.put("message", alert.getMessage());
        body.put("severity", alert.getSeverity().toString());
        body.put("triggered_at", alert.getTriggeredAt().format(DATE_FORMAT));
        body.put("source_type", alert.getSourceType().toString());
        body.put("source_id", alert.getSourceId());
        body.put("details", alert.getDetails());

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add custom headers if configured
        String customHeaders = channel.getConfigValue("headers");
        if(customHeaders != null) {
            // Parse JSON headers and add them
        }

        // Add authentication if configured
        String authType = channel.getConfigValue("auth_type");
        if("bearer".equalsIgnoreCase(authType)) {
            String token = channel.getConfigValue("auth_token");
            headers.setBearerAuth(token);
        } else if("api_key".equalsIgnoreCase(authType)) {
            String keyHeader = channel.getConfigValue("api_key_header");
            String keyValue = channel.getConfigValue("api_key_value");
            headers.add(keyHeader, keyValue);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response;
        if("PUT".equalsIgnoreCase(method)) {
            response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        } else {
            response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        }

        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Webhook returned status: " + response.getStatusCode());
        }
    }

    /**
     * Send Slack notification
     */
    private void sendSlackNotification(NotificationChannel channel, Alert alert, String notificationId) {
        String webhookUrl = channel.getConfigValue("webhook_url");
        String channelName = channel.getConfigValue("channel");
        String username = channel.getConfigValue("username");

        Map<String, Object> payload = new HashMap<>();
        payload.put("channel", channelName);
        payload.put("username", username != null ? username : "Integrix Alerts");
        payload.put("text", formatSlackMessage(alert, notificationId));

        // Add color based on severity
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", getSlackColor(alert.getSeverity()));
        attachment.put("fields", buildSlackFields(alert));
        payload.put("attachments", new Object[] {attachment});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);

        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Slack webhook returned status: " + response.getStatusCode());
        }
    }

    /**
     * Send Teams notification
     */
    private void sendTeamsNotification(NotificationChannel channel, Alert alert, String notificationId) {
        String webhookUrl = channel.getConfigValue("webhook_url");

        Map<String, Object> card = new HashMap<>();
        card.put("@type", "MessageCard");
        card.put("@context", "http://schema.org/extensions");
        card.put("themeColor", getTeamsColor(alert.getSeverity()));
        card.put("summary", alert.getTitle());
        card.put("title", alert.getTitle());
        card.put("text", alert.getMessage());

        // Add sections with details
        Map<String, Object> section = new HashMap<>();
        section.put("facts", buildTeamsFacts(alert));
        card.put("sections", new Object[] {section});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(card, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);

        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Teams webhook returned status: " + response.getStatusCode());
        }
    }

    /**
     * Send Twilio SMS
     */
    private void sendTwilioSms(NotificationChannel channel, Alert alert, String notificationId) {
        // This would integrate with Twilio API
        // Placeholder implementation
        log.info("Sending SMS via Twilio for alert {}", alert.getAlertId());
    }

    /**
     * Check rate limit for channel
     */
    private boolean checkRateLimit(NotificationChannel channel) {
        if(channel.getRateLimitPerHour() == null) {
            return true; // No rate limit
        }

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        // Check if we need to reset the counter
        if(channel.getLastNotificationAt() != null &&
            channel.getLastNotificationAt().isBefore(oneHourAgo)) {
            channel.setNotificationCountCurrentHour(0);
        }

        return channel.getNotificationCountCurrentHour() < channel.getRateLimitPerHour();
    }

    /**
     * Update channel usage statistics
     */
    private void updateChannelUsage(NotificationChannel channel) {
        channel.setLastNotificationAt(LocalDateTime.now());
        channel.setNotificationCountCurrentHour(channel.getNotificationCountCurrentHour() + 1);
        channelRepository.save(channel);
    }

    /**
     * Format email subject
     */
    private String formatEmailSubject(Alert alert) {
        return String.format("[%s] %s - %s",
                alert.getSeverity(),
                alert.getSourceType(),
                alert.getTitle());
    }

    /**
     * Format email body
     */
    private String formatEmailBody(Alert alert, String notificationId) {
        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h2>").append(alert.getTitle()).append("</h2>");
        body.append("<p><strong>Alert ID:</strong> ").append(alert.getAlertId()).append("</p>");
        body.append("<p><strong>Severity:</strong> ").append(alert.getSeverity()).append("</p>");
        body.append("<p><strong>Time:</strong> ").append(alert.getTriggeredAt().format(DATE_FORMAT)).append("</p>");
        body.append("<p><strong>Source:</strong> ").append(alert.getSourceType()).append(" - ").append(alert.getSourceName()).append("</p>");
        body.append("<hr/>");
        body.append("<p>").append(alert.getMessage()).append("</p>");

        if(!alert.getDetails().isEmpty()) {
            body.append("<h3>Details:</h3>");
            body.append("<ul>");
            for(Map.Entry<String, String> detail : alert.getDetails().entrySet()) {
                body.append("<li><strong>").append(detail.getKey()).append(":</strong> ")
                    .append(detail.getValue()).append("</li>");
            }
            body.append("</ul>");
        }

        body.append("<hr/>");
        body.append("<p><small>Notification ID: ").append(notificationId).append("</small></p>");
        body.append("</body></html>");

        return body.toString();
    }

    /**
     * Format Slack message
     */
    private String formatSlackMessage(Alert alert, String notificationId) {
        return String.format(":warning: *%s Alert* - %s\n%s\n_Alert ID: %s_",
                alert.getSeverity(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getAlertId());
    }

    /**
     * Get Slack color based on severity
     */
    private String getSlackColor(AlertRule.AlertSeverity severity) {
        switch(severity) {
            case CRITICAL:
                return "#d00000"; // Red
            case HIGH:
                return "#ff6600"; // Orange
            case MEDIUM:
                return "#ffcc00"; // Yellow
            case LOW:
                return "#0066cc"; // Blue
            case INFO:
                return "#00cc00"; // Green
            default:
                return "#808080"; // Gray
        }
    }

    /**
     * Get Teams color based on severity
     */
    private String getTeamsColor(AlertRule.AlertSeverity severity) {
        return getSlackColor(severity); // Same colors work for Teams
    }

    /**
     * Build Slack fields
     */
    private Object[] buildSlackFields(Alert alert) {
        return new Object[] {
            Map.of("title", "Source", "value", alert.getSourceType() + " - " + alert.getSourceName(), "short", true),
            Map.of("title", "Time", "value", alert.getTriggeredAt().format(DATE_FORMAT), "short", true)
        };
    }

    /**
     * Build Teams facts
     */
    private Object[] buildTeamsFacts(Alert alert) {
        return new Object[] {
            Map.of("name", "Alert ID", "value", alert.getAlertId()),
            Map.of("name", "Severity", "value", alert.getSeverity().toString()),
            Map.of("name", "Source", "value", alert.getSourceType() + " - " + alert.getSourceName()),
            Map.of("name", "Time", "value", alert.getTriggeredAt().format(DATE_FORMAT))
        };
    }

    /**
     * Generate unique notification ID
     */
    private String generateNotificationId() {
        return "NTF-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
