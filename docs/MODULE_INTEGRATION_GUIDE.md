# Module Integration Guide

## Overview

This guide provides practical examples and patterns for integrating with each module in the Integrix Flow Bridge system after the clean architecture refactoring.

## Quick Reference

| Module | Purpose | Primary Interface | Port |
|--------|---------|------------------|------|
| Backend | Flow Management | REST API | 8080 |
| Engine | Flow Execution | FlowExecutionClient | Internal |
| Adapters | External Connectivity | AdapterExecutionService | Internal |
| Monitoring | Observability | SystemMonitor | Internal |
| WebClient | Inbound Messages | REST API | 8081 |
| WebServer | Outbound Calls | HTTP/SOAP Clients | Internal |
| SOAP Bindings | WSDL Management | SoapBindingService | Internal |

## Module Integration Examples

### 1. Backend Module Integration

#### Creating an Integration Flow
```java
// REST API
POST /api/integration-flows
{
  "name": "Order Processing Flow",
  "description": "Process orders from HTTP to database",
  "sourceAdapterId": "http-adapter-123",
  "targetAdapterId": "jdbc-adapter-456",
  "retryAttempts": 3,
  "retryDelay": 1000,
  "timeoutMs": 30000
}

// Java Client
@Service
public class FlowManagementClient {
    private final RestTemplate restTemplate;
    
    public IntegrationFlowDTO createFlow(CreateFlowRequest request) {
        return restTemplate.postForObject(
            "/api/integration-flows",
            request,
            IntegrationFlowDTO.class
        );
    }
}
```

#### Managing Field Mappings
```java
// REST API
POST /api/field-mappings
{
  "flowId": "flow-123",
  "sourceField": "$.order.id",
  "targetField": "order_id",
  "transformationType": "EXPRESSION",
  "transformationExpression": "source.toUpperCase()"
}

// Application Service
@Service
public class FieldMappingService {
    public FieldMapping createMapping(FieldMappingDTO dto) {
        // Validate flow exists
        // Create mapping with transformation
        // Return created mapping
    }
}
```

### 2. Engine Module Integration

#### Executing a Flow
```java
// Using FlowExecutionClient interface
@Component
public class OrderProcessor {
    private final FlowExecutionClient flowExecutionClient;
    
    public void processOrder(Order order) {
        Map<String, Object> payload = Map.of(
            "orderId", order.getId(),
            "items", order.getItems(),
            "total", order.getTotal()
        );
        
        ExecutionResult result = flowExecutionClient.executeFlow(
            order.getFlowId(), 
            payload
        );
        
        if (result.getStatus() == ExecutionStatus.FAILED) {
            handleFailure(result.getErrorMessage());
        }
    }
}
```

#### Async Flow Execution
```java
@Service
public class AsyncFlowProcessor {
    private final FlowExecutionClient flowExecutionClient;
    
    @Async
    public CompletableFuture<ExecutionResult> processAsync(String flowId, Map<String, Object> data) {
        return CompletableFuture.supplyAsync(() -> 
            flowExecutionClient.executeFlow(flowId, data)
        );
    }
    
    // Check status
    public ExecutionStatus checkStatus(String executionId) {
        return flowExecutionClient.getExecutionStatus(executionId);
    }
}
```

### 3. Adapters Module Integration

#### Using Adapters Directly
```java
// Via AdapterExecutionService
@Service
public class DataFetcher {
    private final AdapterExecutionService adapterService;
    
    public Map<String, Object> fetchData(String adapterId) {
        AdapterContext context = AdapterContext.builder()
            .executionId(UUID.randomUUID().toString())
            .inputData(Map.of("endpoint", "/api/data"))
            .timeout(5000L)
            .build();
            
        AdapterResult result = adapterService.executeAdapter(adapterId, context);
        
        if (!result.isSuccess()) {
            throw new AdapterException(result.getErrorMessage());
        }
        
        return result.getOutputData();
    }
}
```

#### Creating Custom Adapters
```java
@Component
public class CustomAdapter extends AbstractAdapter {
    
    @Override
    public AdapterResult execute(AdapterContext context) {
        // Custom implementation
        try {
            Map<String, Object> result = performCustomOperation(context.getInputData());
            return AdapterResult.success(result);
        } catch (Exception e) {
            return AdapterResult.failure(e.getMessage(), "CUSTOM_ERROR");
        }
    }
    
    @Override
    public boolean testConnection() {
        // Test connectivity
        return true;
    }
    
    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
            .adapterId(getId())
            .name("Custom Adapter")
            .type("CUSTOM")
            .supportedOperations(List.of("fetch", "push"))
            .build();
    }
}
```

### 4. Monitoring Module Integration

#### Recording Activities
```java
@Component
public class ActivityRecorder {
    private final SystemMonitor monitor;
    
    public void recordFlowExecution(String flowId, long duration) {
        monitor.recordActivity(
            "backend",
            "flow_executed",
            Map.of(
                "flowId", flowId,
                "duration", duration,
                "timestamp", System.currentTimeMillis()
            )
        );
        
        monitor.recordPerformance("backend", "flow_execution", duration);
    }
    
    public void recordError(Exception error, String context) {
        monitor.recordError(
            "backend",
            error,
            Map.of("context", context, "severity", "HIGH")
        );
    }
}
```

#### Creating Custom Metrics
```java
@Component
public class MetricsCollector {
    private final SystemMonitor monitor;
    
    @Scheduled(fixedDelay = 60000)
    public void collectMetrics() {
        // Collect custom metrics
        double cpuUsage = getCpuUsage();
        long activeFlows = getActiveFlowCount();
        
        monitor.recordMetric(
            "system.cpu.usage",
            cpuUsage,
            Map.of("node", getNodeId())
        );
        
        monitor.recordMetric(
            "flows.active.count",
            activeFlows,
            Map.of("environment", getEnvironment())
        );
    }
}
```

### 5. WebClient Module Integration

#### Receiving Inbound Messages
```java
// REST endpoint for external systems
POST /api/inbound/{flowId}
Content-Type: application/json
{
  "messageId": "msg-123",
  "payload": {
    "type": "ORDER",
    "data": { ... }
  }
}

// WebClient processes and routes to Engine
@RestController
@RequestMapping("/api/inbound")
public class InboundController {
    private final MessageRoutingService routingService;
    
    @PostMapping("/{flowId}")
    public ResponseEntity<?> receiveMessage(
            @PathVariable String flowId,
            @RequestBody Map<String, Object> message) {
        
        ExecutionResult result = routingService.routeMessage(flowId, message);
        
        return ResponseEntity.ok(Map.of(
            "status", result.getStatus(),
            "executionId", result.getExecutionId()
        ));
    }
}
```

### 6. WebServer Module Integration

#### Making Outbound HTTP Calls
```java
@Service
public class ExternalApiClient {
    private final HttpClientService httpClient;
    
    public CustomerData fetchCustomer(String customerId) {
        HttpEndpoint endpoint = HttpEndpoint.builder()
            .url("https://api.example.com/customers/{id}")
            .method(HttpMethod.GET)
            .headers(Map.of("Authorization", "Bearer token"))
            .build();
            
        return httpClient.execute(
            endpoint,
            Map.of("id", customerId),
            CustomerData.class
        );
    }
}
```

#### Making SOAP Calls
```java
@Service
public class SoapServiceClient {
    private final SoapClientService soapClient;
    
    public OrderResponse submitOrder(OrderRequest request) {
        SoapEndpoint endpoint = SoapEndpoint.builder()
            .wsdlUrl("http://service.example.com/orders?wsdl")
            .operation("submitOrder")
            .soapAction("http://example.com/submitOrder")
            .build();
            
        return soapClient.invoke(
            endpoint,
            request,
            OrderResponse.class
        );
    }
}
```

### 7. SOAP Bindings Module Integration

#### Managing WSDL Definitions
```java
@Service
public class WsdlManager {
    private final WsdlService wsdlService;
    private final SoapBindingService bindingService;
    
    public SoapBinding createBindingFromWsdl(String wsdlUrl) {
        // Parse WSDL
        WsdlDefinition wsdl = wsdlService.loadWsdlFromUrl(wsdlUrl);
        
        // Validate
        if (!wsdlService.validateWsdl(wsdl)) {
            throw new InvalidWsdlException("WSDL validation failed");
        }
        
        // Generate binding
        GeneratedBinding generated = bindingService.generateBinding(
            wsdl,
            "com.example.generated"
        );
        
        // Create and save binding
        SoapBinding binding = SoapBinding.builder()
            .name(wsdl.getName())
            .wsdlUrl(wsdlUrl)
            .packageName(generated.getPackageName())
            .enabled(true)
            .build();
            
        return bindingService.createBinding(binding);
    }
}
```

## Integration Patterns

### 1. Request-Response Pattern
```java
// Synchronous communication between modules
public class SyncIntegration {
    public Response processRequest(Request request) {
        // Validate request
        ValidationResult validation = validator.validate(request);
        if (!validation.isValid()) {
            return Response.error(validation.getErrors());
        }
        
        // Process
        Result result = processor.process(request);
        
        // Return response
        return Response.success(result);
    }
}
```

### 2. Event-Driven Pattern
```java
// Asynchronous communication via events
@Component
public class EventDrivenIntegration {
    private final ApplicationEventPublisher publisher;
    
    public void processAsync(Data data) {
        // Publish event
        publisher.publishEvent(new DataProcessingEvent(data));
    }
    
    @EventListener
    public void handleProcessingComplete(ProcessingCompleteEvent event) {
        // Handle completion
        updateStatus(event.getId(), "COMPLETED");
    }
}
```

### 3. Batch Processing Pattern
```java
// Processing multiple items efficiently
@Component
public class BatchProcessor {
    private final int BATCH_SIZE = 100;
    
    public void processBatch(List<Item> items) {
        List<List<Item>> batches = Lists.partition(items, BATCH_SIZE);
        
        batches.parallelStream()
            .forEach(batch -> {
                try {
                    processBatchItems(batch);
                    recordSuccess(batch.size());
                } catch (Exception e) {
                    recordFailure(batch, e);
                }
            });
    }
}
```

### 4. Circuit Breaker Pattern
```java
// Protecting against cascading failures
@Component
public class ResilientClient {
    private final CircuitBreaker circuitBreaker;
    
    public Optional<Data> fetchDataWithFallback(String id) {
        return circuitBreaker.executeSupplier(
            () -> fetchData(id),
            throwable -> {
                log.warn("Circuit breaker opened: {}", throwable.getMessage());
                return getFallbackData(id);
            }
        );
    }
}
```

## Testing Module Integrations

### 1. Unit Testing with Mocks
```java
@Test
void testFlowExecution() {
    // Given
    when(flowExecutionClient.executeFlow(anyString(), any()))
        .thenReturn(ExecutionResult.success(Map.of("result", "ok")));
    
    // When
    var result = service.processFlow("flow-123", testData);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo("SUCCESS");
}
```

### 2. Integration Testing
```java
@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTest {
    
    @Test
    void testEndToEndFlow() {
        // Create flow
        var flow = createTestFlow();
        
        // Execute flow
        var execution = executeFlow(flow.getId(), testPayload);
        
        // Verify result
        assertThat(execution.getStatus()).isEqualTo("COMPLETED");
        verifyDataInTarget(execution.getOutputData());
    }
}
```

### 3. Contract Testing
```java
@Test
void testAdapterContract() {
    // Define contract
    var contract = Contract.builder()
        .request(Request.builder()
            .method("POST")
            .path("/execute")
            .body(json("{ 'data': 'test' }"))
            .build())
        .response(Response.builder()
            .status(200)
            .body(json("{ 'result': 'success' }"))
            .build())
        .build();
    
    // Verify contract
    stubFor(contract);
    var result = client.execute("test");
    assertThat(result).isEqualTo("success");
}
```

## Troubleshooting Common Integration Issues

### Issue 1: Module Communication Timeout
```java
// Solution: Configure appropriate timeouts
@Configuration
public class TimeoutConfig {
    @Bean
    public RestTemplate restTemplate() {
        var factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }
}
```

### Issue 2: Message Format Mismatch
```java
// Solution: Use DTOs and proper mapping
@Component
public class MessageMapper {
    public InternalMessage map(ExternalMessage external) {
        return InternalMessage.builder()
            .id(external.getMessageId())
            .content(external.getPayload())
            .timestamp(external.getCreatedAt())
            .build();
    }
}
```

### Issue 3: Transaction Boundaries
```java
// Solution: Manage transactions at appropriate level
@Service
@Transactional
public class TransactionalService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processInNewTransaction(Data data) {
        // Process in separate transaction
    }
}
```

## Performance Considerations

1. **Use Connection Pooling**: Configure connection pools for database and HTTP clients
2. **Implement Caching**: Cache frequently accessed data at appropriate levels
3. **Batch Operations**: Process multiple items together when possible
4. **Async Processing**: Use async for non-blocking operations
5. **Monitor Performance**: Track metrics for all integrations

## Security Best Practices

1. **Authentication**: Use JWT tokens or API keys for module communication
2. **Authorization**: Implement role-based access control
3. **Encryption**: Use TLS for all network communication
4. **Audit Logging**: Log all integration activities
5. **Input Validation**: Validate all inputs at module boundaries