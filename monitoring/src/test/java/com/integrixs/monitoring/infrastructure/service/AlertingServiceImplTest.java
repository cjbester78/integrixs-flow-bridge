package com.integrixs.monitoring.infrastructure.service;

import com.integrixs.monitoring.domain.model.Alert;
import com.integrixs.monitoring.domain.repository.AlertRepository;
import com.integrixs.monitoring.infrastructure.service.MonitoringAlertServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MonitoringAlertServiceImpl notification methods
 */
@ExtendWith(MockitoExtension.class)
class AlertingServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MonitoringAlertServiceImpl alertingService;

    @BeforeEach
    void setUp() {
        // Set up test configuration values
        ReflectionTestUtils.setField(alertingService, "emailEnabled", true);
        ReflectionTestUtils.setField(alertingService, "fromEmail", "test@integrix.com");
        ReflectionTestUtils.setField(alertingService, "webhookEnabled", true);
        ReflectionTestUtils.setField(alertingService, "smsEnabled", true);
        ReflectionTestUtils.setField(alertingService, "twilioAccountSid", "test-account");
        ReflectionTestUtils.setField(alertingService, "twilioAuthToken", "test-token");
        ReflectionTestUtils.setField(alertingService, "twilioFromNumber", "+1234567890");
        
        // Inject the mocked dependencies
        ReflectionTestUtils.setField(alertingService, "mailSender", mailSender);
        ReflectionTestUtils.setField(alertingService, "restTemplate", restTemplate);
    }

    @Test
    void testEmailNotification() throws Exception {
        // Given
        Map<String, String> emailParams = new HashMap<>();
        emailParams.put("to", "admin@test.com,ops@test.com");

        Alert alert = createTestAlert(Alert.AlertAction.ActionType.EMAIL, emailParams);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(alertRepository.save(any())).thenReturn(alert);

        // When
        alertingService.triggerAlert(alert);

        // Then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testWebhookNotification() {
        // Given
        Map<String, String> webhookParams = new HashMap<>();
        webhookParams.put("url", "https://api.test.com/alerts");
        webhookParams.put("method", "POST");
        webhookParams.put("auth_type", "bearer");
        webhookParams.put("auth_token", "test-bearer-token");

        Alert alert = createTestAlert(Alert.AlertAction.ActionType.WEBHOOK, webhookParams);

        ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
            .thenReturn(mockResponse);
        when(alertRepository.save(any())).thenReturn(alert);

        // When
        alertingService.triggerAlert(alert);

        // Then
        verify(restTemplate, times(1)).exchange(
            eq("https://api.test.com/alerts"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(String.class)
       );
    }

    @Test
    void testWebhookWithApiKeyAuth() {
        // Given
        Map<String, String> webhookParams = new HashMap<>();
        webhookParams.put("url", "https://api.test.com/alerts");
        webhookParams.put("auth_type", "api_key");
        webhookParams.put("api_key_header", "X-API-Key");
        webhookParams.put("api_key_value", "secret-key");

        Alert alert = createTestAlert(Alert.AlertAction.ActionType.WEBHOOK, webhookParams);

        ResponseEntity<String> mockResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
            .thenReturn(mockResponse);
        when(alertRepository.save(any())).thenReturn(alert);

        // When
        alertingService.triggerAlert(alert);

        // Then
        verify(restTemplate, times(1)).exchange(
            anyString(),
            any(HttpMethod.class),
            argThat(entity -> {
                HttpEntity<?> httpEntity = (HttpEntity<?>) entity;
                return httpEntity.getHeaders().containsKey("X-API-Key") &&
                       httpEntity.getHeaders().get("X-API-Key").contains("secret-key");
            }),
            eq(String.class)
       );
    }

    @Test
    void testDisabledEmailNotification() {
        // Given
        ReflectionTestUtils.setField(alertingService, "emailEnabled", false);

        Map<String, String> emailParams = new HashMap<>();
        emailParams.put("to", "admin@test.com");

        Alert alert = createTestAlert(Alert.AlertAction.ActionType.EMAIL, emailParams);
        when(alertRepository.save(any())).thenReturn(alert);

        // When
        alertingService.triggerAlert(alert);

        // Then
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testNotificationWithoutParameters() {
        // Given
        Alert alert = Alert.builder()
                .alertName("Test Alert")
                .alertType(Alert.AlertType.THRESHOLD)
                .severity(Alert.AlertSeverity.CRITICAL)
                .status(Alert.AlertStatus.TRIGGERED)
                .source("Test")
                .message("Test message")
                .triggeredAt(LocalDateTime.now())
                .action(Alert.AlertAction.builder()
                        .type(Alert.AlertAction.ActionType.EMAIL)
                        .build())
                .build();

        when(alertRepository.save(any())).thenReturn(alert);

        // When
        alertingService.triggerAlert(alert);

        // Then
        verify(mailSender, never()).createMimeMessage();
    }

    private Alert createTestAlert(Alert.AlertAction.ActionType actionType, Map<String, String> parameters) {
        return Alert.builder()
                .alertId("test-123")
                .alertName("Test Alert")
                .alertType(Alert.AlertType.THRESHOLD)
                .severity(Alert.AlertSeverity.CRITICAL)
                .status(Alert.AlertStatus.TRIGGERED)
                .source("Test")
                .message("Test alert message")
                .condition("value > 90")
                .triggeredAt(LocalDateTime.now())
                .domainType("Metric")
                .domainReferenceId("metric-123")
                .metadata(Map.of("test", "value"))
                .action(Alert.AlertAction.builder()
                        .type(actionType)
                        .parameters(parameters)
                        .build())
                .build();
    }
}
