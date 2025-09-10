package com.integrixs.testing.examples;

import com.integrixs.testing.core.*;
import com.integrixs.testing.adapters.MockHttpAdapter;
import com.integrixs.testing.runners.FlowExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static com.integrixs.testing.core.FlowAssertions.assertThat;

/**
 * Example test demonstrating the Integrixs testing framework
 */
@FlowTest(
    flow = "flows/http-transform-flow.yaml",
    environment = "test",
    useMockAdapters = true,
    collectMetrics = true
)
public class SimpleFlowTest {
    
    @InjectFlowContext
    private FlowTestContext context;
    
    @TestUtility
    private FlowExecutor executor;
    
    @MockAdapter("http")
    private MockHttpAdapter httpMock;
    
    @BeforeEach
    void setUp() {
        // Configure mock HTTP responses
        httpMock.addJsonResponse(200, Map.of(
            "status", "success",
            "data", Map.of(
                "id", "123",
                "name", "Test User",
                "email", "test@example.com"
            )
        ));
    }
    
    @Test
    void testSuccessfulFlowExecution() {
        // Execute flow with input data
        FlowExecution execution = executor
            .withInput(Map.of("userId", "123"))
            .withHeader("X-Request-ID", "test-request-123")
            .execute();
        
        // Assert execution was successful
        assertThat(execution)
            .isSuccessful()
            .hasExecutedStep("fetch-user")
            .hasExecutedStep("transform-data")
            .hasJsonOutput()
            .hasPath("$.user.name", "Test User")
            .hasPath("$.user.email", "test@example.com");
        
        // Verify mock was called
        assertThat(httpMock.getCapturedRequests())
            .hasSize(1)
            .first()
            .satisfies(request -> {
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getPath()).contains("/users/123");
            });
    }
    
    @Test
    @TestData("error-scenarios.json")
    void testErrorHandling(FlowExecutor executor) {
        // Configure mock to return error
        httpMock.reset();
        httpMock.addResponse(500, "Internal Server Error");
        
        // Execute flow
        FlowExecution execution = executor
            .withTestData("errorCase1")
            .execute();
        
        // Assert error handling
        assertThat(execution)
            .hasFailed()
            .hasFailedWith("HTTP request failed")
            .hasExecutedStep("fetch-user")
            .hasExecutedStepsCount(1);
    }
    
    @Test
    void testParallelExecution() {
        // Test parallel execution of flows
        FlowExecutor.MultipleExecutions results = executor
            .withInput(Map.of("userId", "123"))
            .executeInParallel(5, 20);
        
        // Assert all executions succeeded
        assertThat(results.successRate()).isEqualTo(1.0);
        assertThat(results.averageExecutionTime()).isLessThan(1000.0);
        
        // Performance assertion
        FlowAssertions.PerformanceAssert.assertThroughput(
            () -> executor.withInput(Map.of("userId", "456")).execute(),
            100,  // iterations
            50    // minimum throughput (ops/sec)
        );
    }
    
    @Test
    void testConditionalFlow() {
        // Test flow with conditional logic
        FlowExecution execution = executor
            .withInput(Map.of(
                "userId", "123",
                "premium", true
            ))
            .waitUntil(exec -> exec.getState().equals("COMPLETED"))
            .execute();
        
        assertThat(execution)
            .isSuccessful()
            .hasExecutedStep("premium-processing")
            .hasOutput()
            .satisfies(output -> {
                Map<?, ?> result = (Map<?, ?>) output;
                assertThat(result).containsKey("premiumFeatures");
            });
    }
    
    @Test
    void testAsyncFlowExecution() throws Exception {
        // Test async flow execution
        FlowExecution execution = executor
            .async()
            .withInput(Map.of("userId", "789"))
            .beforeExecution(exec -> {
                System.out.println("Starting async execution: " + exec.getExecutionId());
            })
            .afterExecution(exec -> {
                System.out.println("Completed in: " + exec.getExecutionTime() + "ms");
            })
            .execute();
        
        assertThat(execution)
            .isSuccessful()
            .hasExecutionTimeLessThan(java.time.Duration.ofSeconds(5));
    }
    
    @Test
    void testFlowWithRetry() {
        // Configure mock to fail first 2 times
        httpMock.reset();
        int[] callCount = {0};
        httpMock.setRequestHandler(request -> {
            callCount[0]++;
            if (callCount[0] < 3) {
                httpMock.setError("Temporary failure");
            } else {
                httpMock.setError(null);
                httpMock.addJsonResponse(200, Map.of("status", "success"));
            }
        });
        
        // Execute with retry
        FlowExecution execution = executor
            .withInput(Map.of("userId", "999"))
            .executeWithRetry(3, 100, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        assertThat(execution)
            .isSuccessful()
            .hasOutput();
        
        // Verify retry happened
        assertThat(httpMock.getCallCount()).isEqualTo(3);
    }
}