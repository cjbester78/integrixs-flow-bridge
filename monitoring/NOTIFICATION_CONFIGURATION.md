# Alert Notification Configuration

## Overview

The monitoring module now supports three types of notifications for alerts:
1. Email notifications
2. Webhook notifications  
3. SMS notifications (via Twilio)

## Configuration

### Email Notifications

Add the following properties to your `application.yml`:

```yaml
notifications:
  email:
    enabled: true
    from: alerts@integrix.com

spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Webhook Notifications

```yaml
notifications:
  webhook:
    enabled: true
```

### SMS Notifications (Twilio)

```yaml
notifications:
  sms:
    enabled: true
    twilio:
      account-sid: your-twilio-account-sid
      auth-token: your-twilio-auth-token
      from-number: +1234567890  # Your Twilio phone number
```

## Usage

When creating alerts with actions, configure the action parameters:

### Email Alert Example
```java
Alert alert = Alert.builder()
    .alertName("High CPU Usage")
    .alertType(Alert.AlertType.THRESHOLD)
    .severity(Alert.AlertSeverity.CRITICAL)
    .message("CPU usage exceeded 90%")
    .action(Alert.AlertAction.builder()
        .type(Alert.AlertAction.ActionType.EMAIL)
        .parameters(Map.of(
            "to", "admin@company.com,ops@company.com"
        ))
        .build())
    .build();
```

### Webhook Alert Example
```java
Alert alert = Alert.builder()
    .alertName("Service Down")
    .alertType(Alert.AlertType.AVAILABILITY)
    .severity(Alert.AlertSeverity.CRITICAL)
    .message("Service is not responding")
    .action(Alert.AlertAction.builder()
        .type(Alert.AlertAction.ActionType.WEBHOOK)
        .parameters(Map.of(
            "url", "https://api.company.com/alerts",
            "method", "POST",
            "auth_type", "bearer",
            "auth_token", "your-api-token"
        ))
        .build())
    .build();
```

### SMS Alert Example
```java
Alert alert = Alert.builder()
    .alertName("Database Connection Failed")
    .alertType(Alert.AlertType.ERROR_RATE)
    .severity(Alert.AlertSeverity.CRITICAL)
    .message("Cannot connect to primary database")
    .action(Alert.AlertAction.builder()
        .type(Alert.AlertAction.ActionType.SMS)
        .parameters(Map.of(
            "to", "+1234567890"  // Recipient phone number
        ))
        .build())
    .build();
```

## Webhook Payload Format

When a webhook notification is sent, the following JSON payload is included:

```json
{
  "alert_id": "unique-alert-id",
  "alert_name": "Alert Name",
  "alert_type": "THRESHOLD",
  "severity": "CRITICAL",
  "message": "Alert message",
  "source": "MetricEvaluation",
  "triggered_at": "2024-01-15 10:30:45",
  "domain_type": "Flow",
  "domain_reference_id": "flow-123",
  "metadata": {
    "metricName": "cpu_usage",
    "metricValue": 92.5,
    "threshold": 90.0
  }
}
```

## Authentication Options for Webhooks

### Bearer Token
```java
parameters.put("auth_type", "bearer");
parameters.put("auth_token", "your-bearer-token");
```

### API Key
```java
parameters.put("auth_type", "api_key");
parameters.put("api_key_header", "X-API-Key");
parameters.put("api_key_value", "your-api-key");
```

## Testing

To test notifications without actually sending them:

1. **Email**: Set `notifications.email.enabled: false` - logs will show what would have been sent
2. **Webhook**: Set `notifications.webhook.enabled: false` - logs will show the payload
3. **SMS**: Set `notifications.sms.enabled: false` - logs will show the message

## Security Considerations

1. **Email**: Use app-specific passwords, not your main account password
2. **Webhooks**: Always use HTTPS URLs and authentication
3. **SMS**: Store Twilio credentials securely (use environment variables or secret management)

## Troubleshooting

### Email Issues
- Check SMTP settings and firewall rules
- Verify app password is correct
- Check spam folder for test emails

### Webhook Issues
- Verify URL is accessible from your network
- Check authentication credentials
- Monitor webhook endpoint logs

### SMS Issues
- Verify Twilio account is active
- Check phone number format (include country code)
- Monitor Twilio console for errors

## Rate Limiting

Currently, there is no built-in rate limiting for notifications. Consider implementing rate limiting if sending high volumes of alerts to avoid:
- Email provider throttling
- SMS cost overruns
- Webhook endpoint overwhelming