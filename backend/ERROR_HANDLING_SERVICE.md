# Error Handling Service

## Overview

The Error Handling Service provides comprehensive error management for the Integrixs Flow Bridge, including:
- Circuit breaker pattern for fault tolerance
- Multiple retry strategies
- Dead letter queue management
- Automated error recovery
- Alert integration

## Components

### 1. ErrorHandlingService
Main service orchestrating error handling with:
- Circuit breaker implementation (Resilience4j)
- Error recording and tracking
- Threshold monitoring
- Recovery automation
- Alert notifications

### 2. EnhancedRetryService
Advanced retry mechanisms with multiple strategies:
- **Fixed Delay**: Constant delay between retries
- **Exponential Backoff**: Exponentially increasing delays
- **Linear Backoff**: Linearly increasing delays
- **Fibonacci Backoff**: Fibonacci sequence-based delays
- **Random Jitter**: Random delays to avoid thundering herd
- **Adaptive**: System load-based dynamic delays

### 3. DeadLetterQueueService
Manages failed messages with:
- Automatic categorization
- Scheduled retry attempts
- Pattern analysis
- Retention policies
- Critical error alerts

## Configuration

### Application Properties

```yaml
integrix:
  error:
    notification:
      enabled: true
    recovery:
      enabled: true
    counter:
      reset:
        interval: 3600000  # 1 hour
  
  retry:
    max-attempts: 5
    initial-interval: 1000     # 1 second
    max-interval: 300000       # 5 minutes
  
  deadletter:
    retry:
      interval: 300000         # 5 minutes
    auto-retry:
      enabled: true
      max-attempts: 3
    retention-days: 30
    batch-size: 100
```

## Usage Examples

### Basic Error Handling

```java
@Autowired
private ErrorHandlingService errorHandlingService;

// Execute with error handling
String result = errorHandlingService.executeWithErrorHandling(flowId, () -> {
    // Your business logic here
    return processMessage(message);
});
```

### Retry with Custom Strategy

```java
@Autowired
private EnhancedRetryService retryService;

// Schedule retry with exponential backoff
CompletableFuture<Boolean> retryFuture = retryService.scheduleRetry(
    messageId, 
    flowId, 
    lastError,
    RetryStrategy.EXPONENTIAL_BACKOFF
);

// Smart retry with conditions
CompletableFuture<Boolean> smartRetry = retryService.smartRetry(
    messageId,
    flowId,
    error,
    e -> e instanceof ConnectException,  // Only retry connection errors
    attempt -> attempt < 3 ? RetryStrategy.FIXED_DELAY : RetryStrategy.EXPONENTIAL_BACKOFF
);
```

### Dead Letter Queue Operations

```java
@Autowired
private DeadLetterQueueService dlqService;

// Send to DLQ
DeadLetterMessage dlqMessage = dlqService.sendToDeadLetterQueue(
    message, 
    "Max retries exceeded", 
    lastException
);

// Manual retry from DLQ
Map<String, Boolean> results = dlqService.manualRetry(
    Arrays.asList(messageId1, messageId2),
    true  // Force retry
);

// Analyze DLQ patterns
DeadLetterAnalysis analysis = dlqService.analyzeDeadLetterQueue(
    flowId,
    startDate,
    endDate
);
```

## Error Recovery Strategies

### Automated Recovery Actions

1. **RESTART_ADAPTER**: Restarts the adapter and resets circuit breaker
2. **CLEAR_CACHE**: Clears flow cache and error counters
3. **RESET_CONNECTION**: Resets database/network connections
4. **RECONFIGURE**: Reloads flow configuration
5. **MANUAL_INTERVENTION**: Triggers alert for manual resolution

### Recovery Strategy Selection

The system automatically selects recovery strategies based on error type:

```
CONNECTION_ERROR, TIMEOUT_ERROR → RESET_CONNECTION
ADAPTER_ERROR → RESTART_ADAPTER
CONFIGURATION_ERROR → RECONFIGURE
SYSTEM_ERROR (cache-related) → CLEAR_CACHE
Others → MANUAL_INTERVENTION
```

## Circuit Breaker Configuration

### States
- **CLOSED**: Normal operation
- **OPEN**: Failing, reject calls
- **HALF_OPEN**: Testing if service recovered

### Configuration
- Failure Rate Threshold: 50%
- Wait Duration in Open State: 30 seconds
- Sliding Window Size: 10 calls
- Minimum Number of Calls: 5

## Alert Integration

The service integrates with the monitoring module to send alerts for:

### Dead Letter Messages
- Type: ERROR_RATE
- Severity: MAJOR
- Action: Email notification

### Error Threshold Exceeded
- Type: THRESHOLD
- Severity: CRITICAL
- Action: Webhook notification

### Max Retries Exceeded
- Type: ERROR_RATE
- Severity: CRITICAL
- Action: SMS notification

### Manual Intervention Required
- Type: CUSTOM
- Severity: CRITICAL
- Action: Email notification

## Monitoring Endpoints

### Get Error Statistics
```http
GET /api/errors/statistics/{flowId}

Response:
{
  "totalErrors": 150,
  "errorTypeCount": {
    "CONNECTION_ERROR": 80,
    "TIMEOUT_ERROR": 50,
    "VALIDATION_ERROR": 20
  },
  "circuitBreakerState": "CLOSED",
  "failureRate": 15.5
}
```

### Get Retry Statistics
```http
GET /api/retry/statistics/{flowId}

Response:
{
  "totalRetries": 250,
  "retriesByAttempt": {
    "1": 150,
    "2": 75,
    "3": 25
  },
  "activeRetries": 5
}
```

### Dead Letter Queue Analysis
```http
GET /api/dlq/analysis?flowId={flowId}&startDate={date}&endDate={date}

Response:
{
  "totalMessages": 100,
  "errorTypeCounts": {
    "CONNECTION_ERROR": 60,
    "TIMEOUT_ERROR": 30,
    "SYSTEM_ERROR": 10
  },
  "reasonCounts": {
    "Connection refused": 40,
    "Read timeout": 30,
    "Max retries exceeded": 30
  },
  "totalRetried": 80,
  "successfulRetries": 60,
  "retrySuccessRate": 75.0
}
```

## Best Practices

1. **Error Categorization**: Properly categorize errors for appropriate recovery
2. **Retry Limits**: Set reasonable retry limits to avoid infinite loops
3. **Circuit Breaker Tuning**: Adjust thresholds based on service characteristics
4. **DLQ Monitoring**: Regularly review DLQ patterns for systemic issues
5. **Alert Configuration**: Configure appropriate alert channels and recipients

## Database Schema

### error_records
- Stores all error occurrences
- Tracks error types and stack traces
- Links to flows and messages

### retry_policies
- Flow-specific retry configurations
- Strategy and timing parameters

### dead_letter_messages
- Failed messages with metadata
- Retry history and status
- Error categorization

## Performance Considerations

1. **Async Processing**: Recovery and retries run asynchronously
2. **Batch Operations**: DLQ processing in configurable batches
3. **Resource Limits**: Thread pools for controlled concurrency
4. **Retention Policies**: Automatic cleanup of old records

## Troubleshooting

### Common Issues

1. **Circuit Breaker Always Open**
   - Check failure threshold configuration
   - Review error logs for root cause
   - Manually reset if needed

2. **Messages Stuck in DLQ**
   - Check if error type is retryable
   - Review retry attempt limits
   - Use manual retry with force option

3. **High Retry Rates**
   - Analyze error patterns
   - Adjust retry strategies
   - Consider increasing initial delays