package com.integrixs.backend.infrastructure.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure service for email notifications
 * Handles the actual sending of emails
 */
@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);


    private final JavaMailSender mailSender;

    public EmailNotificationService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${notifications.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notifications.email.from:noreply@integrix.com}")
    private String fromEmail;

    @Value("${notifications.email.admin:admin@integrix.com}")
    private String adminEmail;

    /**
     * Send email notification
     */
    @Async
    public void sendEmail(String to, String subject, String message) {
        if(!emailEnabled) {
            log.info("Email notifications disabled. Would have sent: {}-{} to {}", subject, message, to);
            return;
        }

        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(fromEmail);
            email.setTo(to);
            email.setSubject(subject);
            email.setText(message);

            mailSender.send(email);
            log.info("Email sent successfully to: {} with subject: {}", to, subject);

        } catch(Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email notification", e);
        }
    }

    /**
     * Send email to admin
     */
    @Async
    public void sendAdminEmail(String subject, String message) {
        sendEmail(adminEmail, subject, message);
    }

    /**
     * Send notification based on map data
     */
    @Async
    public void sendNotification(Map<String, Object> notification) {
        String subject = (String) notification.get("subject");
        String message = (String) notification.get("message");
        String to = (String) notification.getOrDefault("recipient", adminEmail);

        sendEmail(to, "[Integrix Alert] " + subject, message);
    }

    /**
     * Check if email is enabled
     */
    public boolean isEmailEnabled() {
        return emailEnabled && mailSender != null;
    }
}
