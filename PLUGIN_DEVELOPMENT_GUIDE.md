# Integrix Flow Bridge Plugin Development Guide

## Overview

The Integrix Flow Bridge plugin system allows developers to create custom adapters that extend the platform's integration capabilities. Plugins are packaged as JAR files and can be dynamically loaded at runtime.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Plugin Architecture](#plugin-architecture)
3. [Development Setup](#development-setup)
4. [Plugin Interface](#plugin-interface)
5. [Configuration Schema](#configuration-schema)
6. [Message Handling](#message-handling)
7. [Testing Your Plugin](#testing-your-plugin)
8. [Packaging and Deployment](#packaging-and-deployment)
9. [Best Practices](#best-practices)
10. [API Reference](#api-reference)

## Quick Start

### 1. Add Dependencies

```xml
<dependency>
    <groupId>com.integrixs</groupId>
    <artifactId>integrix-plugin-api</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### 2. Implement the Plugin Interface

```java
package com.example.myplugin;

import com.integrixs.backend.plugin.api.*;

public class MyCustomPlugin implements AdapterPlugin {
    
    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .id("my-custom-adapter")
                .name("My Custom Adapter")
                .version("1.0.0")
                .vendor("My Company")
                .description("Connects to my custom service")
                .category("custom")
                .build();
    }
    
    @Override
    public void initialize(Map<String, Object> configuration) {
        // Initialize your plugin
    }
    
    // Implement other required methods...
}
```

### 3. Package as JAR

```bash
mvn clean package
```

### 4. Upload to Integrix

Upload the generated JAR file through the Plugin Marketplace UI or API.

## Plugin Architecture

### Core Components

1. **AdapterPlugin**: Main interface that all plugins must implement
2. **InboundHandler**: Handles incoming data from external systems
3. **OutboundHandler**: Sends data to external systems
4. **ConfigurationSchema**: Defines the plugin's configuration requirements
5. **AdapterMetadata**: Provides plugin information

### Plugin Lifecycle

1. **Registration**: Plugin JAR is uploaded and validated
2. **Instantiation**: Plugin class is loaded and instance created
3. **Configuration**: User provides configuration values
4. **Initialization**: Plugin is initialized with configuration
5. **Operation**: Plugin handles messages
6. **Shutdown**: Plugin is destroyed when unregistered

## Development Setup

### Maven Project Structure

```
my-plugin/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/myplugin/
│   │   │       ├── MyCustomPlugin.java
│   │   │       ├── MyInboundHandler.java
│   │   │       └── MyOutboundHandler.java
│   │   └── resources/
│   │       └── plugin.properties
│   └── test/
│       └── java/
│           └── com/example/myplugin/
│               └── MyCustomPluginTest.java
```

### Sample pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>my-custom-plugin</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <integrix.api.version>1.0.0</integrix.api.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>com.integrixs</groupId>
            <artifactId>integrix-plugin-api</artifactId>
            <version>${integrix.api.version}</version>
            <scope>provided</scope>
        </dependency>
        
        <!-- Add your plugin-specific dependencies here -->
        
        <!-- Testing -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                        </manifest>
                        <manifestEntries>
                            <Plugin-Class>com.example.myplugin.MyCustomPlugin</Plugin-Class>
                            <Plugin-Id>my-custom-adapter</Plugin-Id>
                            <Plugin-Version>${project.version}</Plugin-Version>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Plugin Interface

### Complete Example Implementation

```java
package com.example.myplugin;

import com.integrixs.backend.plugin.api.*;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
public class MyCustomPlugin implements AdapterPlugin {
    
    private Map<String, Object> configuration;
    private MyInboundHandler inboundHandler;
    private MyOutboundHandler outboundHandler;
    
    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .id("my-custom-adapter")
                .name("My Custom Adapter")
                .version("1.0.0")
                .vendor("My Company")
                .description("Connects to my custom service")
                .icon("custom-icon")
                .category("custom")
                .supportedProtocols(List.of("HTTP", "HTTPS"))
                .supportedFormats(List.of("JSON", "XML"))
                .authenticationMethods(List.of("APIKey", "OAuth2"))
                .capabilities(Map.of(
                    "streaming", true,
                    "batch", true,
                    "retry", true
                ))
                .documentationUrl("https://example.com/docs")
                .minPlatformVersion("1.0.0")
                .license("Apache-2.0")
                .tags(List.of("custom", "api", "integration"))
                .build();
    }
    
    @Override
    public void initialize(Map<String, Object> configuration) {
        log.info("Initializing My Custom Plugin");
        this.configuration = configuration;
        
        // Validate required configuration
        String apiKey = (String) configuration.get("apiKey");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new PluginInitializationException("API key is required");
        }
        
        // Initialize handlers
        this.inboundHandler = new MyInboundHandler(configuration);
        this.outboundHandler = new MyOutboundHandler(configuration);
    }
    
    @Override
    public void destroy() {
        log.info("Destroying My Custom Plugin");
        if (inboundHandler != null) {
            inboundHandler.stop();
        }
    }
    
    @Override
    public InboundHandler getInboundHandler() {
        return inboundHandler;
    }
    
    @Override
    public OutboundHandler getOutboundHandler() {
        return outboundHandler;
    }
    
    @Override
    public ConnectionTestResult testConnection(Direction direction) {
        try {
            // Implement connection test logic
            boolean success = testApiConnection();
            
            if (success) {
                return ConnectionTestResult.builder()
                        .successful(true)
                        .message("Connection successful")
                        .responseTime(Duration.ofMillis(250))
                        .build();
            } else {
                return ConnectionTestResult.failure(
                    "Connection failed", 
                    "Unable to reach API endpoint"
                );
            }
        } catch (Exception e) {
            return ConnectionTestResult.failure(
                "Connection test error",
                e.getMessage()
            );
        }
    }
    
    @Override
    public HealthStatus checkHealth() {
        List<HealthStatus.ComponentHealth> components = new ArrayList<>();
        
        // Check API connectivity
        components.add(HealthStatus.ComponentHealth.builder()
                .name("API Connection")
                .state(HealthStatus.HealthState.HEALTHY)
                .message("API is reachable")
                .build());
        
        // Check handler status
        if (inboundHandler != null) {
            components.add(HealthStatus.ComponentHealth.builder()
                    .name("Inbound Handler")
                    .state(inboundHandler.isRunning() ? 
                        HealthStatus.HealthState.HEALTHY : 
                        HealthStatus.HealthState.STOPPED)
                    .message("Handler status")
                    .build());
        }
        
        return HealthStatus.builder()
                .state(HealthStatus.HealthState.HEALTHY)
                .message("Plugin is healthy")
                .components(components)
                .build();
    }
    
    @Override
    public ConfigurationSchema getConfigurationSchema() {
        return ConfigurationSchema.builder()
                .sections(List.of(
                    ConfigurationSchema.Section.builder()
                            .id("connection")
                            .title("API Connection")
                            .fields(List.of(
                                ConfigurationSchema.Field.builder()
                                        .name("apiKey")
                                        .type("password")
                                        .label("API Key")
                                        .required(true)
                                        .help("Your API authentication key")
                                        .build(),
                                ConfigurationSchema.Field.builder()
                                        .name("baseUrl")
                                        .type("text")
                                        .label("Base URL")
                                        .defaultValue("https://api.example.com")
                                        .placeholder("https://api.example.com")
                                        .validation(ConfigurationSchema.Validation.builder()
                                                .pattern("^https?://.*")
                                                .message("Must be a valid HTTP(S) URL")
                                                .build())
                                        .build()
                            ))
                            .build()
                ))
                .build();
    }
    
    private boolean testApiConnection() {
        // Implement actual connection test
        return true;
    }
}
```

## Configuration Schema

### Field Types

- **text**: Single-line text input
- **password**: Password input (masked)
- **textarea**: Multi-line text input
- **number**: Numeric input
- **boolean/checkbox**: Boolean toggle
- **select**: Single selection dropdown
- **multiselect**: Multiple selection
- **radio**: Radio button group
- **date**: Date picker
- **datetime**: Date and time picker
- **json**: JSON editor
- **file**: File upload

### Validation Rules

```java
ConfigurationSchema.Field.builder()
    .name("port")
    .type("number")
    .label("Port")
    .required(true)
    .validation(ConfigurationSchema.Validation.builder()
            .min(1)
            .max(65535)
            .message("Port must be between 1 and 65535")
            .build())
    .build()
```

### Conditional Fields

```java
ConfigurationSchema.Field.builder()
    .name("authMethod")
    .type("select")
    .options(List.of(
        ConfigurationSchema.Option.builder()
                .value("basic")
                .label("Basic Auth")
                .build(),
        ConfigurationSchema.Option.builder()
                .value("oauth2")
                .label("OAuth 2.0")
                .build()
    ))
    .build(),

ConfigurationSchema.Field.builder()
    .name("clientId")
    .type("text")
    .label("Client ID")
    .condition(ConfigurationSchema.Condition.builder()
            .field("authMethod")
            .operator("equals")
            .value("oauth2")
            .build())
    .build()
```

## Message Handling

### Inbound Handler Example

```java
public class MyInboundHandler implements InboundHandler {
    
    private final Map<String, Object> configuration;
    private ScheduledExecutorService scheduler;
    private MessageCallback callback;
    private boolean listening = false;
    
    public MyInboundHandler(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public void startListening(MessageCallback callback) throws PluginException {
        if (listening) {
            throw new PluginException("Already listening");
        }
        
        this.callback = callback;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Start polling every 30 seconds
        scheduler.scheduleWithFixedDelay(
            this::pollForData,
            0,
            30,
            TimeUnit.SECONDS
        );
        
        listening = true;
    }
    
    @Override
    public void stopListening() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        listening = false;
    }
    
    @Override
    public PollingResult poll() {
        List<PluginMessage> messages = fetchData();
        return PollingResult.builder()
                .messages(messages)
                .hasMore(false)
                .build();
    }
    
    private void pollForData() {
        try {
            List<PluginMessage> messages = fetchData();
            for (PluginMessage message : messages) {
                callback.onMessage(message);
            }
        } catch (Exception e) {
            callback.onError(e);
        }
    }
    
    private List<PluginMessage> fetchData() {
        // Implement data fetching logic
        return List.of(
            PluginMessage.builder()
                    .headers(Map.of(
                        "source", "api",
                        "timestamp", System.currentTimeMillis()
                    ))
                    .body(Map.of(
                        "data", "example",
                        "value", 123
                    ))
                    .contentType("application/json")
                    .build()
        );
    }
}
```

### Outbound Handler Example

```java
public class MyOutboundHandler implements OutboundHandler {
    
    private final Map<String, Object> configuration;
    private final HttpClient httpClient;
    
    public MyOutboundHandler(Map<String, Object> configuration) {
        this.configuration = configuration;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    @Override
    public SendResult send(PluginMessage message) throws PluginException {
        try {
            String apiKey = (String) configuration.get("apiKey");
            String baseUrl = (String) configuration.get("baseUrl");
            
            // Build request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/data"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", message.getContentType())
                    .POST(HttpRequest.BodyPublishers.ofString(
                        toJson(message.getBody())
                    ))
                    .build();
            
            // Send request
            HttpResponse<String> response = httpClient.send(
                request, 
                HttpResponse.BodyHandlers.ofString()
            );
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return SendResult.builder()
                        .successful(true)
                        .messageId(message.getId())
                        .externalMessageId(extractId(response.body()))
                        .response(response.body())
                        .build();
            } else {
                return SendResult.failure(
                    message.getId(),
                    "HTTP " + response.statusCode() + ": " + response.body()
                );
            }
            
        } catch (Exception e) {
            return SendResult.failure(message.getId(), e.getMessage());
        }
    }
    
    @Override
    public BatchSendResult sendBatch(List<PluginMessage> messages) {
        List<SendResult> results = messages.stream()
                .map(this::send)
                .collect(Collectors.toList());
        
        long successCount = results.stream()
                .filter(SendResult::isSuccessful)
                .count();
        
        return BatchSendResult.builder()
                .totalMessages(messages.size())
                .successCount((int) successCount)
                .failureCount(messages.size() - (int) successCount)
                .results(results)
                .build();
    }
    
    @Override
    public boolean supportsBatch() {
        return false; // Set to true if your API supports batch operations
    }
}
```

## Testing Your Plugin

### Unit Testing

```java
public class MyCustomPluginTest {
    
    private MyCustomPlugin plugin;
    
    @Before
    public void setup() {
        plugin = new MyCustomPlugin();
    }
    
    @Test
    public void testMetadata() {
        AdapterMetadata metadata = plugin.getMetadata();
        assertEquals("my-custom-adapter", metadata.getId());
        assertEquals("My Custom Adapter", metadata.getName());
    }
    
    @Test
    public void testInitialization() {
        Map<String, Object> config = Map.of(
            "apiKey", "test-key",
            "baseUrl", "https://api.example.com"
        );
        
        plugin.initialize(config);
        
        assertNotNull(plugin.getInboundHandler());
        assertNotNull(plugin.getOutboundHandler());
    }
    
    @Test(expected = PluginInitializationException.class)
    public void testInitializationWithoutApiKey() {
        Map<String, Object> config = Map.of(
            "baseUrl", "https://api.example.com"
        );
        
        plugin.initialize(config);
    }
    
    @Test
    public void testConnectionTest() {
        Map<String, Object> config = Map.of(
            "apiKey", "test-key",
            "baseUrl", "https://api.example.com"
        );
        
        plugin.initialize(config);
        ConnectionTestResult result = plugin.testConnection(Direction.OUTBOUND);
        
        assertTrue(result.isSuccessful());
    }
}
```

### Integration Testing

Use the Integrix Plugin Test Framework:

```java
public class MyCustomPluginIntegrationTest {
    
    @Test
    public void testEndToEndFlow() {
        // Create test harness
        PluginTestHarness harness = new PluginTestHarness(MyCustomPlugin.class);
        
        // Configure plugin
        harness.configure(Map.of(
            "apiKey", "test-key",
            "baseUrl", "http://localhost:8080"
        ));
        
        // Send test message
        PluginMessage testMessage = PluginMessage.builder()
                .body(Map.of("test", "data"))
                .contentType("application/json")
                .build();
        
        SendResult result = harness.send(testMessage);
        
        assertTrue(result.isSuccessful());
        assertNotNull(result.getExternalMessageId());
    }
}
```

## Packaging and Deployment

### Building the Plugin

```bash
# Clean and build
mvn clean package

# Run tests
mvn test

# Generate plugin descriptor
mvn plugin:descriptor
```

### Plugin Manifest

Include a `plugin.properties` file in your JAR:

```properties
plugin.class=com.example.myplugin.MyCustomPlugin
plugin.id=my-custom-adapter
plugin.version=1.0.0
plugin.author=My Company
plugin.description=Connects to my custom service
plugin.license=Apache-2.0
```

### Deployment Options

1. **Upload via UI**: Use the Plugin Marketplace UI to upload your JAR
2. **REST API**: Upload programmatically using the plugin API
3. **File System**: Copy JAR to the plugin directory (requires restart)

### API Upload Example

```bash
curl -X POST http://localhost:8080/api/plugins/upload \
  -H "Content-Type: multipart/form-data" \
  -F "file=@target/my-custom-plugin-1.0.0.jar" \
  -F "validate=true"
```

## Best Practices

### 1. Error Handling

Always handle exceptions gracefully:

```java
@Override
public SendResult send(PluginMessage message) {
    try {
        // Your logic here
    } catch (IOException e) {
        log.error("Network error sending message", e);
        return SendResult.failure(message.getId(), "Network error: " + e.getMessage());
    } catch (Exception e) {
        log.error("Unexpected error sending message", e);
        return SendResult.failure(message.getId(), "Unexpected error: " + e.getMessage());
    }
}
```

### 2. Resource Management

Clean up resources properly:

```java
@Override
public void destroy() {
    if (httpClient != null) {
        httpClient.close();
    }
    if (connectionPool != null) {
        connectionPool.shutdown();
    }
}
```

### 3. Configuration Validation

Validate configuration during initialization:

```java
@Override
public void initialize(Map<String, Object> configuration) {
    // Required fields
    requireNonNull(configuration.get("apiKey"), "API key is required");
    
    // Type validation
    if (!(configuration.get("timeout") instanceof Number)) {
        throw new PluginInitializationException("Timeout must be a number");
    }
    
    // Range validation
    int timeout = ((Number) configuration.get("timeout")).intValue();
    if (timeout < 1 || timeout > 300) {
        throw new PluginInitializationException("Timeout must be between 1 and 300 seconds");
    }
}
```

### 4. Logging

Use appropriate log levels:

```java
log.debug("Processing message: {}", message.getId());
log.info("Successfully sent message to external system");
log.warn("Retry attempt {} failed", retryCount);
log.error("Failed to connect to API", exception);
```

### 5. Performance

- Use connection pooling for HTTP clients
- Implement efficient batching for bulk operations
- Cache frequently accessed data
- Use async operations where appropriate

### 6. Security

- Never log sensitive data (passwords, API keys)
- Validate all input data
- Use secure communication protocols
- Implement proper authentication

## API Reference

### Core Interfaces

#### AdapterPlugin

```java
public interface AdapterPlugin {
    AdapterMetadata getMetadata();
    void initialize(Map<String, Object> configuration);
    void destroy();
    InboundHandler getInboundHandler();
    OutboundHandler getOutboundHandler();
    ConnectionTestResult testConnection(Direction direction);
    HealthStatus checkHealth();
    ConfigurationSchema getConfigurationSchema();
}
```

#### InboundHandler

```java
public interface InboundHandler {
    void startListening(MessageCallback callback) throws PluginException;
    void stopListening();
    PollingResult poll();
    boolean isListening();
}
```

#### OutboundHandler

```java
public interface OutboundHandler {
    SendResult send(PluginMessage message) throws PluginException;
    BatchSendResult sendBatch(List<PluginMessage> messages) throws PluginException;
    boolean supportsBatch();
}
```

### Data Classes

#### PluginMessage

```java
@Data
@Builder
public class PluginMessage {
    private String id;
    private Map<String, Object> headers;
    private Object body;
    private String contentType;
    private Long timestamp;
    private Map<String, Object> metadata;
}
```

#### SendResult

```java
@Data
@Builder
public class SendResult {
    private boolean successful;
    private String messageId;
    private String externalMessageId;
    private String response;
    private String error;
    private Map<String, Object> metadata;
}
```

### Exceptions

- **PluginException**: Base exception for all plugin errors
- **PluginInitializationException**: Thrown during initialization
- **PluginConfigurationException**: Invalid configuration
- **PluginConnectionException**: Connection-related errors

## Support

For questions and support:

- Documentation: https://integrix.example.com/docs
- Issues: https://github.com/integrixs/flow-bridge/issues
- Community: https://community.integrix.com