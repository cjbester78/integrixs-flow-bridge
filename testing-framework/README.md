# Integrixs Testing Framework

Comprehensive testing framework for Integrixs Flow Bridge integration flows.

## Features

### Core Testing Capabilities
- **Flow Testing**: Execute and test integration flows with full lifecycle support
- **Mock Adapters**: Built-in mocks for HTTP, File, Database, Message Queue, SOAP, and FTP adapters
- **Assertions**: Fluent assertion API with JSON/XML path support
- **Performance Testing**: Throughput and latency assertions
- **Parallel Execution**: Run flows in parallel for load testing
- **Embedded Servers**: Testcontainers integration for PostgreSQL, RabbitMQ, Redis, Kafka

### Advanced Features
- **Test Data Management**: YAML/JSON test data with path navigation
- **Flow Visualization**: Execution diagrams in test reports
- **Metrics Collection**: Performance metrics and statistics
- **Retry Testing**: Test retry logic and error handling
- **Async Testing**: Support for asynchronous flow execution

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>com.integrixs</groupId>
    <artifactId>integrixs-testing-framework</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### Basic Test Example

```java
@FlowTest(
    flow = "flows/my-flow.yaml",
    environment = "test",
    useMockAdapters = true
)
public class MyFlowTest {
    
    @TestUtility
    private FlowExecutor executor;
    
    @MockAdapter("http")
    private MockHttpAdapter httpMock;
    
    @Test
    void testSuccessfulFlow() {
        // Configure mock
        httpMock.addJsonResponse(200, Map.of(
            "status", "success",
            "data", "test data"
        ));
        
        // Execute flow
        FlowExecution execution = executor
            .withInput(Map.of("id", "123"))
            .execute();
        
        // Assert results
        assertThat(execution)
            .isSuccessful()
            .hasJsonOutput()
            .hasPath("$.status", "success");
    }
}
```

## Testing Annotations

### @FlowTest
Marks a test class for flow testing:

```java
@FlowTest(
    flow = "flows/order-processing.yaml",     // Flow definition file
    environment = "test",                      // Test environment
    useEmbeddedServers = true,                // Start embedded servers
    useMockAdapters = true,                   // Use mock adapters
    testDataDir = "src/test/resources/data",  // Test data directory
    collectMetrics = true,                    // Enable metrics
    timeout = 30                              // Timeout in seconds
)
```

### @TestData
Load test data for a test method:

```java
@Test
@TestData("scenarios/error-cases.json")
void testErrorScenarios() {
    // Test data automatically loaded
}
```

### Dependency Injection

```java
@InjectFlowContext
private FlowTestContext context;  // Inject test context

@TestUtility
private FlowExecutor executor;    // Inject flow executor

@MockAdapter("http")
private MockHttpAdapter httpMock; // Inject mock adapter
```

## Flow Executor API

### Basic Execution

```java
// Simple execution
FlowExecution result = executor
    .withInput(Map.of("key", "value"))
    .execute();

// With headers
result = executor
    .withInput(data)
    .withHeader("X-Request-ID", "test-123")
    .execute();

// With test data
result = executor
    .withTestData("scenario1")
    .execute();
```

### Async Execution

```java
// Async with wait condition
FlowExecution result = executor
    .async()
    .withInput(data)
    .waitUntil(exec -> exec.getState().equals("COMPLETED"))
    .waitTimeout(10, TimeUnit.SECONDS)
    .execute();
```

### Multiple Executions

```java
// Execute multiple times
MultipleExecutions results = executor
    .withInput(data)
    .executeMultiple(10);

// Execute in parallel
results = executor
    .withInput(data)
    .executeInParallel(5, 20); // 5 threads, 20 executions

// With different inputs
results = executor.executeWithInputs(
    Map.of("id", "1"),
    Map.of("id", "2"),
    Map.of("id", "3")
);
```

### Retry Testing

```java
FlowExecution result = executor
    .withInput(data)
    .executeWithRetry(
        3,    // max attempts
        100,  // retry delay
        TimeUnit.MILLISECONDS
    );
```

## Mock Adapters

### HTTP Mock

```java
// Using builder
MockHttpAdapter httpMock = new MockAdapterBuilder()
    .http()
    .withJsonResponse(200, responseObject)
    .withHeader("Content-Type", "application/json")
    .withDelay(100)
    .captureRequests()
    .build();

// Configure responses
httpMock.addResponse("GET", "/api/users", 200, "{...}");
httpMock.setError("Connection refused");

// Verify calls
List<HttpRequest> requests = httpMock.getCapturedRequests();
```

### File Mock

```java
MockFileAdapter fileMock = new MockAdapterBuilder()
    .file()
    .withFile("/data/input.csv", "col1,col2\nval1,val2")
    .captureWrites()
    .build();
```

### Database Mock

```java
MockDatabaseAdapter dbMock = new MockAdapterBuilder()
    .database()
    .withQueryResult(
        "SELECT * FROM users",
        new Object[][]{
            {"1", "John", "john@example.com"},
            {"2", "Jane", "jane@example.com"}
        }
    )
    .withUpdateResult("UPDATE users SET ...", 2)
    .captureQueries()
    .build();
```

### Message Queue Mock

```java
MockMessageQueueAdapter mqMock = new MockAdapterBuilder()
    .messageQueue()
    .withQueue("orders")
    .withMessage("orders", orderMessage)
    .simulateBackpressure(100)
    .build();
```

## Assertions

### Flow Execution Assertions

```java
// Basic assertions
assertThat(execution)
    .isSuccessful()
    .hasExecutedStep("transform-data")
    .hasExecutionTimeLessThan(Duration.ofSeconds(5))
    .hasOutput();

// Error assertions
assertThat(execution)
    .hasFailed()
    .hasFailedWith("Connection timeout")
    .hasFailedWithException(IOException.class);

// Step assertions
assertThat(execution)
    .hasExecutedStepsCount(5)
    .hasExecutedStep("validate-input");
```

### JSON Assertions

```java
assertThat(execution)
    .hasJsonOutput()
    .hasPath("$.user.name", "John Doe")
    .hasPath("$.items[0].price", 29.99)
    .hasPathMatching("$.orderId", "ORD-\\d+");
```

### XML Assertions

```java
assertThat(execution)
    .hasXmlOutput()
    .isValid()
    .hasXPath("//user/name", "John Doe")
    .hasXPath("//order[@status='completed']");
```

### Performance Assertions

```java
// Throughput testing
PerformanceAssert.assertThroughput(
    () -> executor.execute(),
    100,  // iterations
    50    // minimum ops/sec
);

// Latency testing
PerformanceAssert.assertPerformance(
    () -> executor.execute(),
    Duration.ofMillis(500)
);
```

## Test Reports

### HTML Reports
The framework generates comprehensive HTML reports using ExtentReports:

```java
TestReportGenerator reporter = new TestReportGenerator("target/flow-test-reports");
reporter.addTestResult("testName", execution, context);
reporter.generateReport();
```

Reports include:
- Test execution dashboard
- Flow execution diagrams
- Performance metrics
- Step-by-step execution details
- Input/output data

### JSON Reports
JSON summaries are generated for CI/CD integration:

```json
{
  "reportGeneratedAt": "2024-01-15T10:30:00",
  "totalTests": 25,
  "testResults": {
    "testOrderProcessing": {
      "totalExecutions": 10,
      "successfulExecutions": 9,
      "failedExecutions": 1,
      "avgExecutionTime": 245.5
    }
  }
}
```

## Embedded Servers

```java
@FlowTest(useEmbeddedServers = true)
public class IntegrationTest {
    // Automatically starts PostgreSQL, RabbitMQ, Redis
}

// Custom configuration
EmbeddedServers servers = EmbeddedServers.builder()
    .withPostgreSQL()
    .withRabbitMQ()
    .withRedis()
    .withKafka()
    .withElasticSearch()
    .build();

servers.start();
// Run tests
servers.stop();
```

## Best Practices

1. **Test Organization**
   - One test class per flow
   - Group related scenarios
   - Use descriptive test names

2. **Mock Configuration**
   - Reset mocks in @BeforeEach
   - Verify mock calls when needed
   - Use realistic test data

3. **Performance Testing**
   - Run performance tests separately
   - Use appropriate thread counts
   - Monitor resource usage

4. **Error Testing**
   - Test all error scenarios
   - Verify error handling
   - Test retry logic

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Add tests for new features
4. Submit a pull request

## License

Proprietary - Integrixs Flow Bridge