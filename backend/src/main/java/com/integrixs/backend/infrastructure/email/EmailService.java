package com.integrixs.backend.infrastructure.email;

import com.integrixs.backend.config.EmailConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure service for email operations
 * Handles SMTP connections and email sending
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);


    private final EmailConfiguration emailConfig;
    private Session mailSession;

    public EmailService(EmailConfiguration emailConfig) {
        this.emailConfig = emailConfig;
    }

    /**
     * Initialize email session
     */
    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put("mail.smtp.host", emailConfig.getHost());
        props.put("mail.smtp.port", emailConfig.getPort());
        props.put("mail.smtp.auth", emailConfig.isAuth());
        props.put("mail.smtp.starttls.enable", emailConfig.isStarttlsEnable());
        props.put("mail.smtp.connectiontimeout", emailConfig.getConnectionTimeout());
        props.put("mail.smtp.timeout", emailConfig.getTimeout());
        props.put("mail.smtp.writetimeout", emailConfig.getWriteTimeout());

        // SSL/TLS configuration
        if(emailConfig.getPort() == 465) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", emailConfig.getHost());
        }

        // Create session with authentication if required
        if(emailConfig.isAuth()) {
            mailSession = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailConfig.getUsername(), emailConfig.getPassword());
                }
            });
        } else {
            mailSession = Session.getInstance(props);
        }

        log.info("Email service initialized with SMTP server: {}: {}", emailConfig.getHost(), emailConfig.getPort());
    }

    /**
     * Send a simple text email
     */
    @Async
    public CompletableFuture<Boolean> sendEmail(String to, String subject, String body) {
        return sendEmail(Collections.singletonList(to), subject, body, false);
    }

    /**
     * Send an HTML email
     */
    @Async
    public CompletableFuture<Boolean> sendHtmlEmail(String to, String subject, String htmlBody) {
        return sendEmail(Collections.singletonList(to), subject, htmlBody, true);
    }

    /**
     * Send email to multiple recipients
     */
    @Async
    public CompletableFuture<Boolean> sendEmail(List<String> recipients, String subject, String body, boolean isHtml) {
        try {
            log.debug("Sending email to {} recipients with subject: {}", recipients.size(), subject);

            // Create message
            MimeMessage message = new MimeMessage(mailSession);

            // Set From
            message.setFrom(new InternetAddress(emailConfig.getFrom()));

            // Set Recipients
            InternetAddress[] toAddresses = recipients.stream()
                    .map(email -> {
                        try {
                            return new InternetAddress(email);
                        } catch(AddressException e) {
                            log.error("Invalid email address: {}", email, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toArray(InternetAddress[]::new);

            if(toAddresses.length == 0) {
                log.error("No valid recipients for email");
                return CompletableFuture.completedFuture(false);
            }

            message.setRecipients(Message.RecipientType.TO, toAddresses);

            // Set Subject
            message.setSubject(subject);

            // Set Content
            if(isHtml) {
                message.setContent(body, "text/html; charset = utf-8");
            } else {
                message.setText(body, "UTF-8");
            }

            // Set headers
            message.setHeader("X - Mailer", "Integrix Flow Bridge");
            message.setSentDate(new Date());

            // Send message
            Transport.send(message);

            log.info("Email sent successfully to {} recipients", recipients.size());

            return CompletableFuture.completedFuture(true);

        } catch(Exception e) {
            log.error("Failed to send email", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Test email configuration
     */
    public boolean testEmailConfiguration(String testRecipient) {
        try {
            String subject = "Integrix Flow Bridge - Email Configuration Test";
            String body = "This is a test email from Integrix Flow Bridge.\n\n" +
                         "If you are receiving this email, your email configuration is working correctly.\n\n" +
                         "Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            CompletableFuture<Boolean> result = sendEmail(testRecipient, subject, body);
            return result.get();

        } catch(Exception e) {
            log.error("Email configuration test failed", e);
            return false;
        }
    }
}
