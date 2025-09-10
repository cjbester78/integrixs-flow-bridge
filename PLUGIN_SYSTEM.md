# Plugin System Implementation

## Overview

The Plugin System provides a flexible framework for creating custom adapters in the Integrixs Flow Bridge. This implementation includes a complete plugin archetype with:

- HTTP-based communication (both inbound and outbound)
- Multiple authentication methods (API Key, Basic Auth, OAuth2)
- Comprehensive error handling and retry logic
- Rate limiting and batch processing
- Health monitoring and connection testing
- Extensive configuration schema with validation

## Architecture

### Core Components

1. **Main Plugin Class** (`__pluginClass__.java`)
   - Implements the `AdapterPlugin` interface
   - Manages plugin lifecycle (initialization, destruction)
   - Provides metadata and configuration schema
   - Performs connection testing and health checks

2. **Inbound Handler** (`__pluginClass__InboundHandler.java`)
   - Polls external systems for data
   - Parses and transforms incoming messages
   - Manages deduplication and state tracking
   - Supports configurable polling intervals

3. **Outbound Handler** (`__pluginClass__OutboundHandler.java`)
   - Sends messages to external systems
   - Implements retry logic with exponential backoff
   - Supports both single and batch message sending
   - Includes rate limiting to prevent API overload

## Features

### Authentication Support

The plugin supports multiple authentication methods:

```java
// API Key Authentication
conn.setRequestProperty("X-API-Key", apiKey);

// Basic Authentication
String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

// OAuth 2.0
conn.setRequestProperty("Authorization", "Bearer " + accessToken);
```

### Connection Testing

Comprehensive connection testing for both directions:

```java
// Inbound: Tests polling capability
ConnectionTestResult testInboundConnection() {
    PollingResult result = inboundHandler.poll();
    return ConnectionTestResult.builder()
        .successful(true)
        .message("Successfully connected and polled data")
        .details(Map.of("messagesFound", result.getMessages().size()))
        .build();
}

// Outbound: Tests endpoint connectivity
ConnectionTestResult testOutboundConnection() {
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("HEAD");
    int responseCode = conn.getResponseCode();
    // ... validate response
}
```

### Health Monitoring

Multi-level health checks:

```java
// Check configuration validity
validateConfiguration(configuration);

// Check connection health
ConnectionTestResult connectionTest = testConnection(Direction.OUTBOUND);

// Check handler status
boolean isListening = inboundHandler.isListening();

// Aggregate health state
HealthStatus.HealthState overallState = determineOverallHealth(components);
```

### Error Handling and Retry

Robust error handling with configurable retry:

```java
// Retry with exponential backoff
for (int i = 0; i <= maxRetries; i++) {
    try {
        return doSend(payload, messageId);
    } catch (Exception e) {
        if (i < maxRetries) {
            Thread.sleep((long) Math.pow(2, i) * 1000);
        }
    }
}
```

### Rate Limiting

Built-in rate limiting to respect API limits:

```java
// Semaphore-based rate limiting
if (!rateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
    throw new PluginException("Rate limit exceeded");
}

// Reset permits every minute
scheduler.scheduleAtFixedRate(() -> {
    rateLimiter.release(rateLimit - rateLimiter.availablePermits());
}, 1, 1, TimeUnit.MINUTES);
```

### Batch Processing

Optimized batch message handling:

```java
// Check if service supports batch
if (supportsBatch()) {
    return sendBatchOptimized(messages);
} else {
    // Parallel processing with thread pool
    List<CompletableFuture<SendResult>> futures = messages.stream()
        .map(msg -> CompletableFuture.supplyAsync(() -> send(msg), executor))
        .collect(Collectors.toList());
}
```

## Configuration Schema

The plugin provides a comprehensive configuration schema:

### Connection Settings
- **Endpoint URL**: Required, validated URL format
- **Connection Timeout**: Configurable timeout (1-300 seconds)

### Authentication
- **Type Selection**: None, Basic, API Key, or OAuth2
- **Conditional Fields**: Shows relevant fields based on auth type
- **Secure Storage**: Password fields are properly masked

### Advanced Settings
- **Retry Attempts**: Configure retry behavior (0-10 attempts)
- **Polling Interval**: For inbound operations (10-3600 seconds)
- **Batch Size**: Maximum messages per batch (1-1000)

## Usage Example

### Creating a Custom Plugin

1. Generate from archetype:
```bash
mvn archetype:generate \
    -DarchetypeGroupId=com.integrixs \
    -DarchetypeArtifactId=plugin-archetype \
    -DpluginId=my-custom-adapter \
    -DpluginName="My Custom Adapter"
```

2. Customize the generated code:
```java
// Add custom fields to configuration schema
ConfigurationSchema.Field.builder()
    .name("customField")
    .type("text")
    .label("Custom Field")
    .required(true)
    .build()

// Implement custom message transformation
private List<PluginMessage> parseResponse(String jsonResponse) {
    // Your custom parsing logic
}

// Add custom authentication
if ("custom".equals(authType)) {
    conn.setRequestProperty("X-Custom-Auth", customToken);
}
```

3. Build and deploy:
```bash
mvn clean package
# Deploy the JAR to the plugin directory
```

## Best Practices

1. **Error Handling**: Always wrap external calls in try-catch blocks
2. **Resource Management**: Use try-with-resources for connections
3. **Logging**: Use appropriate log levels (debug for details, info for milestones)
4. **Configuration Validation**: Validate all required fields during initialization
5. **State Management**: Track processed message IDs to prevent duplicates
6. **Performance**: Use thread pools for parallel processing
7. **Security**: Never log sensitive information like passwords or API keys

## Testing

The plugin includes comprehensive test coverage:

```java
@Test
public void testConnectionSuccess() {
    // Test successful connection
    ConnectionTestResult result = plugin.testConnection(Direction.OUTBOUND);
    assertTrue(result.isSuccessful());
}

@Test
public void testMessageSending() {
    // Test message sending with mock server
    SendResult result = outboundHandler.send(testMessage);
    assertTrue(result.isSuccessful());
    assertNotNull(result.getExternalMessageId());
}

@Test
public void testHealthCheck() {
    // Test health monitoring
    HealthStatus health = plugin.checkHealth();
    assertEquals(HealthStatus.HealthState.HEALTHY, health.getState());
}
```

## Troubleshooting

### Common Issues

1. **Connection Timeouts**
   - Increase timeout in configuration
   - Check network connectivity
   - Verify endpoint URL

2. **Authentication Failures**
   - Verify credentials are correct
   - Check auth type matches API requirements
   - Ensure tokens haven't expired

3. **Rate Limiting**
   - Reduce polling frequency
   - Implement backoff strategies
   - Contact API provider for higher limits

4. **Message Parsing Errors**
   - Check response format matches expectations
   - Add more robust error handling
   - Log raw responses for debugging

## Performance Considerations

1. **Connection Pooling**: Reuse connections when possible
2. **Batch Processing**: Use batch APIs when available
3. **Async Operations**: Use CompletableFuture for parallel processing
4. **Memory Management**: Stream large payloads instead of loading into memory
5. **Caching**: Cache authentication tokens and connection metadata

## Security

1. **Credential Storage**: Use secure configuration storage
2. **HTTPS Only**: Always use encrypted connections
3. **Input Validation**: Validate all external input
4. **Error Messages**: Don't expose sensitive information in errors
5. **Audit Logging**: Log security-relevant events