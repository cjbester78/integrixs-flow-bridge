package com.integrixs.backend.infrastructure.notification;

import com.integrixs.backend.config.EmailConfiguration;
import com.integrixs.backend.domain.model.ExecutionAlert;
import com.integrixs.backend.domain.model.ExecutionTrace;
import com.integrixs.backend.infrastructure.email.EmailService;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.IntegrationFlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Infrastructure service for sending alert notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertNotificationService {

    private final EmailService emailService;
    private final EmailConfiguration emailConfig;
    private final IntegrationFlowRepository flowRepository;

    /**
     * Send failure alert for execution trace
     */
    public void sendFailureAlert(ExecutionTrace trace) {
        if(!emailConfig.isAlertsEnabled()) {
            return;
        }

        try {
            Optional<IntegrationFlow> flowOpt = flowRepository.findById(UUID.fromString(trace.getFlowId()));
            if(flowOpt.isEmpty()) {
                return;
            }

            IntegrationFlow flow = flowOpt.get();
            String errorMessage = trace.getErrorMessage() != null ? trace.getErrorMessage() : "Unknown error";

            if(trace.getExceptionDetails() != null) {
                errorMessage += "\n\nException Details:\n" + trace.getExceptionDetails();
            }

            List<String> recipients = getAlertRecipients(flow);
            if(!recipients.isEmpty()) {
                String subject = "Flow Execution Failed: " + flow.getName();
                String body = String.format("Flow: %s\nFlow ID: %s\n\nError Message:\n%s",
                    flow.getName(), flow.getId().toString(), errorMessage);
                emailService.sendEmail(recipients, subject, body, false);
            }
        } catch(Exception e) {
            log.error("Failed to send failure alert", e);
        }
    }

    /**
     * Send error alert for execution trace
     */
    public void sendErrorAlert(ExecutionTrace trace) {
        // Same as failure alert for now
        sendFailureAlert(trace);
    }

    /**
     * Send alerts for a flow
     */
    public void sendAlerts(String flowId, List<ExecutionAlert> alerts) {
        if(!emailConfig.isAlertsEnabled() || alerts.isEmpty()) {
            return;
        }

        try {
            Optional<IntegrationFlow> flowOpt = flowRepository.findById(UUID.fromString(flowId));
            if(flowOpt.isEmpty()) {
                return;
            }

            IntegrationFlow flow = flowOpt.get();
            List<String> recipients = getAlertRecipients(flow);

            if(recipients.isEmpty()) {
                return;
            }

            // Send alerts by type
            for(ExecutionAlert alert : alerts) {
                String subject = String.format("[%s] %s",
                    alert.getType().getDisplayName(),
                    flow.getName()
               );

                String body = String.format("Severity: %s\nSource: Flow Execution Monitor\n\n%s",
                    alert.getType().getSeverity().toUpperCase(), alert.getMessage());
                emailService.sendEmail(recipients, subject, body, false);
            }
        } catch(Exception e) {
            log.error("Failed to send alerts for flow: " + flowId, e);
        }
    }

    /**
     * Get alert recipients for a flow
     */
    private List<String> getAlertRecipients(IntegrationFlow flow) {
        List<String> recipients = new ArrayList<>();

        // Add default recipients
        if(emailConfig.getDefaultAlertRecipients() != null) {
            recipients.addAll(Arrays.asList(emailConfig.getDefaultAlertRecipients()));
        }

        // In test mode, override recipients
        if(emailConfig.isTestMode() && emailConfig.getTestEmailRecipient() != null) {
            recipients.clear();
            recipients.add(emailConfig.getTestEmailRecipient());
        }

        return recipients;
    }
}
