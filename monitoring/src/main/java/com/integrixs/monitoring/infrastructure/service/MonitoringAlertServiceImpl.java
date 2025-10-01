package com.integrixs.monitoring.infrastructure.service;

import com.integrixs.monitoring.domain.model.Alert;
import com.integrixs.monitoring.domain.model.MetricSnapshot;
import com.integrixs.monitoring.domain.model.MonitoringEvent;
import com.integrixs.monitoring.domain.repository.AlertRepository;
import com.integrixs.monitoring.domain.service.MonitoringAlertService;
import com.integrixs.monitoring.domain.service.MonitoringAlertService.AlertRule;
import com.integrixs.monitoring.domain.service.MonitoringAlertService.AlertQueryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure implementation of alerting service
 */
@Service
public class MonitoringAlertServiceImpl implements MonitoringAlertService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringAlertServiceImpl.class);
    private final AlertRepository alertRepository;

    public MonitoringAlertServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${notifications.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notifications.email.from:alerts@integrix.com}")
    private String fromEmail;

    @Value("${notifications.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notifications.sms.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${notifications.sms.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${notifications.sms.twilio.from-number:}")
    private String twilioFromNumber;

    @Value("${notifications.webhook.enabled:true}")
    private boolean webhookEnabled;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Alert triggerAlert(Alert alert) {
        try {
            if(alert.getAlertId() == null) {
                alert.setAlertId(UUID.randomUUID().toString());
            }
            if(alert.getTriggeredAt() == null) {
                alert.setTriggeredAt(LocalDateTime.now());
            }
            if(alert.getStatus() == null) {
                alert.setStatus(Alert.AlertStatus.TRIGGERED);
            }

            Alert savedAlert = alertRepository.save(alert);
            log.info("Alert triggered: {} - {}", alert.getAlertName(), alert.getMessage());

            // Execute alert actions
            executeAlertAction(savedAlert);

            return savedAlert;
        } catch(Exception e) {
            log.error("Error triggering alert: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to trigger alert", e);
        }
    }

    @Override
    public List<Alert> evaluateMetric(MetricSnapshot metric) {
        List<Alert> triggeredAlerts = new ArrayList<>();

        try {
            // Get enabled rules for this metric
            List<AlertRule> rules = alertRepository.findEnabledRules().stream()
                    .filter(rule -> rule.getTargetMetric() != null &&
                                  rule.getTargetMetric().equals(metric.getMetricName()))
                    .collect(java.util.stream.Collectors.toList());

            for(AlertRule rule : rules) {
                if(evaluateRule(rule, metric.getValue())) {
                    Alert alert = Alert.builder()
                            .alertName(rule.getRuleName())
                            .alertType(rule.getAlertType())
                            .severity(rule.getSeverity())
                            .source("MetricEvaluation")
                            .message(String.format("Metric %s %s threshold: %.2f %s %.2f",
                                    metric.getMetricName(), rule.getComparison(),
                                    metric.getValue(), rule.getComparison(), rule.getThreshold()))
                            .condition(rule.getCondition())
                            .domainType("Metric")
                            .domainReferenceId(metric.getMetricId())
                            .action(rule.getAction())
                            .build();

                    alert.addMetadata("metricName", metric.getMetricName());
                    alert.addMetadata("metricValue", metric.getValue());
                    alert.addMetadata("threshold", rule.getThreshold());
                    alert.addMetadata("ruleId", rule.getRuleId());

                    triggeredAlerts.add(triggerAlert(alert));
                }
            }
        } catch(Exception e) {
            log.error("Error evaluating metric for alerts: {}", e.getMessage(), e);
        }

        return triggeredAlerts;
    }

    @Override
    public List<Alert> evaluateEvent(MonitoringEvent event) {
        List<Alert> triggeredAlerts = new ArrayList<>();

        try {
            // Check for critical events
            if(event.isCritical()) {
                Alert alert = Alert.builder()
                        .alertName("Critical Event")
                        .alertType(Alert.AlertType.ERROR_RATE)
                        .severity(Alert.AlertSeverity.CRITICAL)
                        .source("EventEvaluation")
                        .message("Critical event detected: " + event.getMessage())
                        .domainType(event.getDomainType())
                        .domainReferenceId(event.getDomainReferenceId())
                        .build();

                alert.addMetadata("eventId", event.getEventId());
                alert.addMetadata("eventType", event.getEventType().name());
                alert.addMetadata("correlationId", event.getCorrelationId());

                triggeredAlerts.add(triggerAlert(alert));
            }

            // Check for error patterns
            if(event.isError()) {
                // Could implement more sophisticated error pattern detection here
                log.debug("Error event detected, checking for patterns: {}", event.getMessage());
            }

        } catch(Exception e) {
            log.error("Error evaluating event for alerts: {}", e.getMessage(), e);
        }

        return triggeredAlerts;
    }

    @Override
    public Alert acknowledgeAlert(String alertId, String userId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        alert.acknowledge(userId);

        Alert updatedAlert = alertRepository.save(alert);
        log.info("Alert acknowledged: {} by {}", alertId, userId);

        return updatedAlert;
    }

    @Override
    public Alert resolveAlert(String alertId, String resolution) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        alert.resolve();
        alert.addMetadata("resolution", resolution);

        Alert updatedAlert = alertRepository.save(alert);
        log.info("Alert resolved: {} - {}", alertId, resolution);

        return updatedAlert;
    }

    @Override
    public Alert suppressAlert(String alertId, String reason, int duration) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        alert.setStatus(Alert.AlertStatus.SUPPRESSED);
        alert.addMetadata("suppressionReason", reason);
        alert.addMetadata("suppressionDuration", duration);
        alert.addMetadata("suppressedUntil", LocalDateTime.now().plusMinutes(duration));

        Alert updatedAlert = alertRepository.save(alert);
        log.info("Alert suppressed: {} for {} minutes", alertId, duration);

        return updatedAlert;
    }

    @Override
    public List<Alert> getActiveAlerts() {
        return alertRepository.findActiveAlerts();
    }

    @Override
    public List<Alert> getAlertsBySeverity(Alert.AlertSeverity severity) {
        return alertRepository.findBySeverity(severity);
    }

    @Override
    public Alert getAlert(String alertId) {
        return alertRepository.findById(alertId).orElse(null);
    }

    @Override
    public List<Alert> queryAlerts(AlertQueryCriteria criteria) {
        return alertRepository.query(criteria);
    }

    @Override
    public String createAlertRule(AlertRule rule) {
        if(rule.getRuleId() == null) {
            rule.setRuleId(UUID.randomUUID().toString());
        }

        AlertRule savedRule = alertRepository.saveRule(rule);
        log.info("Alert rule created: {}", savedRule.getRuleName());

        return savedRule.getRuleId();
    }

    @Override
    public void updateAlertRule(String ruleId, AlertRule rule) {
        rule.setRuleId(ruleId);
        alertRepository.saveRule(rule);
        log.info("Alert rule updated: {}", rule.getRuleName());
    }

    @Override
    public void deleteAlertRule(String ruleId) {
        alertRepository.deleteRule(ruleId);
        log.info("Alert rule deleted: {}", ruleId);
    }

    @Override
    public List<AlertRule> getAlertRules() {
        return alertRepository.findAllRules();
    }

    @Override
    public void executeAlertAction(Alert alert) {
        if(alert.getAction() == null) {
            return;
        }

        try {
            switch(alert.getAction().getType()) {
                case EMAIL:
                    sendEmailNotification(alert);
                    break;

                case WEBHOOK:
                    sendWebhookNotification(alert);
                    break;

                case SMS:
                    sendSmsNotification(alert);
                    break;

                default:
                    log.warn("Unsupported alert action type: {}", alert.getAction().getType());
            }
        } catch(Exception e) {
            log.error("Error executing alert action: {}", e.getMessage(), e);
        }
    }

    /**
     * Evaluate rule against metric value
     * @param rule Alert rule
     * @param value Metric value
     * @return true if rule is triggered
     */
    private boolean evaluateRule(AlertRule rule, double value) {
        switch(rule.getComparison()) {
            case "GT":
                return value > rule.getThreshold();
            case "GTE":
                return value >= rule.getThreshold();
            case "LT":
                return value < rule.getThreshold();
            case "LTE":
                return value <= rule.getThreshold();
            case "EQ":
                return Math.abs(value - rule.getThreshold()) < 0.0001;
            case "NEQ":
                return Math.abs(value - rule.getThreshold()) >= 0.0001;
            default:
                log.warn("Unknown comparison operator: {}", rule.getComparison());
                return false;
        }
    }

    /**
     * Send email notification for alert
     */
    private void sendEmailNotification(Alert alert) {
        if(!emailEnabled || mailSender == null) {
            log.info("Email notifications disabled. Would have sent alert: {}", alert.getAlertName());
            return;
        }

        if(alert.getAction() == null || alert.getAction().getParameters() == null) {
            log.warn("No email parameters configured for alert: {}", alert.getAlertName());
            return;
        }

        try {
            String to = alert.getAction().getParameters().get("to");
            if(to == null || to.isEmpty()) {
                log.warn("No recipient email configured for alert: {}", alert.getAlertName());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to.split(","));
            helper.setSubject(formatEmailSubject(alert));
            helper.setText(formatEmailBody(alert), true);

            mailSender.send(message);
            log.info("Email alert sent successfully for: {} to {}", alert.getAlertName(), to);

        } catch(Exception e) {
            log.error("Failed to send email notification for alert: {}", alert.getAlertName(), e);
        }
    }

    /**
     * Send webhook notification for alert
     */
    private void sendWebhookNotification(Alert alert) {
        if(!webhookEnabled) {
            log.info("Webhook notifications disabled. Would have sent alert: {}", alert.getAlertName());
            return;
        }

        if(alert.getAction() == null || alert.getAction().getParameters() == null) {
            log.warn("No webhook parameters configured for alert: {}", alert.getAlertName());
            return;
        }

        try {
            String url = alert.getAction().getParameters().get("url");
            if(url == null || url.isEmpty()) {
                log.warn("No webhook URL configured for alert: {}", alert.getAlertName());
                return;
            }

            // Build request body
            Map<String, Object> body = new HashMap<>();
            body.put("alert_id", alert.getAlertId());
            body.put("alert_name", alert.getAlertName());
            body.put("alert_type", alert.getAlertType().name());
            body.put("severity", alert.getSeverity().name());
            body.put("message", alert.getMessage());
            body.put("source", alert.getSource());
            body.put("triggered_at", alert.getTriggeredAt().format(DATE_FORMAT));
            body.put("domain_type", alert.getDomainType());
            body.put("domain_reference_id", alert.getDomainReferenceId());
            body.put("metadata", alert.getMetadata());

            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Add authentication if configured
            String authType = alert.getAction().getParameters().get("auth_type");
            if("bearer".equalsIgnoreCase(authType)) {
                String token = alert.getAction().getParameters().get("auth_token");
                if(token != null) {
                    headers.setBearerAuth(token);
                }
            } else if("api_key".equalsIgnoreCase(authType)) {
                String keyHeader = alert.getAction().getParameters().get("api_key_header");
                String keyValue = alert.getAction().getParameters().get("api_key_value");
                if(keyHeader != null && keyValue != null) {
                    headers.add(keyHeader, keyValue);
                }
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            String method = alert.getAction().getParameters().getOrDefault("method", "POST");
            ResponseEntity<String> response;

            if("PUT".equalsIgnoreCase(method)) {
                response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            } else {
                response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            }

            if(response.getStatusCode().is2xxSuccessful()) {
                log.info("Webhook alert sent successfully for: {} to {}", alert.getAlertName(), url);
            } else {
                log.warn("Webhook returned non - success status {} for alert: {}",
                        response.getStatusCode(), alert.getAlertName());
            }

        } catch(Exception e) {
            log.error("Failed to send webhook notification for alert: {}", alert.getAlertName(), e);
        }
    }

    /**
     * Send SMS notification for alert
     */
    private void sendSmsNotification(Alert alert) {
        if(!smsEnabled) {
            log.info("SMS notifications disabled. Would have sent alert: {}", alert.getAlertName());
            return;
        }

        if(alert.getAction() == null || alert.getAction().getParameters() == null) {
            log.warn("No SMS parameters configured for alert: {}", alert.getAlertName());
            return;
        }

        try {
            String to = alert.getAction().getParameters().get("to");
            if(to == null || to.isEmpty()) {
                log.warn("No recipient phone number configured for alert: {}", alert.getAlertName());
                return;
            }

            // Initialize Twilio if not already done
            if(twilioAccountSid != null && !twilioAccountSid.isEmpty() &&
                twilioAuthToken != null && !twilioAuthToken.isEmpty()) {

                Twilio.init(twilioAccountSid, twilioAuthToken);

                String messageBody = formatSmsMessage(alert);

                Message message = Message.creator(
                        new PhoneNumber(to),
                        new PhoneNumber(twilioFromNumber),
                        messageBody
                   ).create();

                log.info("SMS alert sent successfully for: {} to {} with SID: {}",
                        alert.getAlertName(), to, message.getSid());
            } else {
                log.warn("Twilio credentials not configured for SMS alerts");
            }

        } catch(Exception e) {
            log.error("Failed to send SMS notification for alert: {}", alert.getAlertName(), e);
        }
    }

    /**
     * Format email subject
     */
    private String formatEmailSubject(Alert alert) {
        return String.format("[%s] %s - %s",
                alert.getSeverity(),
                alert.getAlertType(),
                alert.getAlertName());
    }

    /**
     * Format email body
     */
    private String formatEmailBody(Alert alert) {
        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h2>").append(alert.getAlertName()).append("</h2>");
        body.append("<p><strong>Alert ID:</strong> ").append(alert.getAlertId()).append("</p>");
        body.append("<p><strong>Type:</strong> ").append(alert.getAlertType()).append("</p>");
        body.append("<p><strong>Severity:</strong> ").append(alert.getSeverity()).append("</p>");
        body.append("<p><strong>Time:</strong> ").append(alert.getTriggeredAt().format(DATE_FORMAT)).append("</p>");
        body.append("<p><strong>Source:</strong> ").append(alert.getSource()).append("</p>");

        if(alert.getDomainType() != null) {
            body.append("<p><strong>Domain:</strong> ").append(alert.getDomainType())
                .append(" (").append(alert.getDomainReferenceId()).append(")</p>");
        }

        body.append("<hr/>");
        body.append("<h3>Message</h3>");
        body.append("<p>").append(alert.getMessage()).append("</p>");

        if(alert.getCondition() != null) {
            body.append("<p><strong>Condition:</strong> ").append(alert.getCondition()).append("</p>");
        }

        if(!alert.getMetadata().isEmpty()) {
            body.append("<h3>Additional Details</h3>");
            body.append("<ul>");
            for(Map.Entry<String, Object> entry : alert.getMetadata().entrySet()) {
                body.append("<li><strong>").append(entry.getKey()).append(":</strong> ")
                    .append(entry.getValue()).append("</li>");
            }
            body.append("</ul>");
        }

        body.append("<hr/>");
        body.append("<p><small>This is an automated message from Integrix Monitoring System</small></p>");
        body.append("</body></html>");

        return body.toString();
    }

    /**
     * Format SMS message(must be concise)
     */
    private String formatSmsMessage(Alert alert) {
        return String.format("%s ALERT: %s - %s. %s",
                alert.getSeverity(),
                alert.getAlertType(),
                alert.getAlertName(),
                alert.getMessage().length() > 100 ?
                    alert.getMessage().substring(0, 97) + "..." :
                    alert.getMessage());
    }
}
