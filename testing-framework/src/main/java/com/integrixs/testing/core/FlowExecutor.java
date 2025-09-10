package com.integrixs.testing.core;

import com.integrixs.testing.runners.FlowExecution;
import com.integrixs.testing.runners.FlowRunner;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Fluent API for executing flows in tests
 */
public class FlowExecutor {
    
    private final FlowTestContext context;
    private final FlowRunner runner;
    
    private Map<String, Object> inputData;
    private Map<String, String> headers;
    private Consumer<FlowExecution> beforeExecution;
    private Consumer<FlowExecution> afterExecution;
    private Predicate<FlowExecution> waitCondition;
    private Duration waitTimeout;
    private boolean async = false;
    
    public FlowExecutor(FlowTestContext context) {
        this.context = context;
        this.runner = new FlowRunner(context);
        this.waitTimeout = Duration.ofSeconds(context.getTimeout());
    }
    
    /**
     * Set input data for the flow
     */
    public FlowExecutor withInput(Map<String, Object> input) {
        this.inputData = input;
        return this;
    }
    
    /**
     * Set input data from test data
     */
    public FlowExecutor withTestData(String testDataKey) {
        this.inputData = (Map<String, Object>) context.getTestDataValue(testDataKey);
        return this;
    }
    
    /**
     * Set headers for the flow execution
     */
    public FlowExecutor withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }
    
    /**
     * Add a single header
     */
    public FlowExecutor withHeader(String key, String value) {
        if (this.headers == null) {
            this.headers = new java.util.HashMap<>();
        }
        this.headers.put(key, value);
        return this;
    }
    
    /**
     * Set callback before execution
     */
    public FlowExecutor beforeExecution(Consumer<FlowExecution> callback) {
        this.beforeExecution = callback;
        return this;
    }
    
    /**
     * Set callback after execution
     */
    public FlowExecutor afterExecution(Consumer<FlowExecution> callback) {
        this.afterExecution = callback;
        return this;
    }
    
    /**
     * Execute asynchronously
     */
    public FlowExecutor async() {
        this.async = true;
        return this;
    }
    
    /**
     * Wait for a specific condition
     */
    public FlowExecutor waitUntil(Predicate<FlowExecution> condition) {
        this.waitCondition = condition;
        return this;
    }
    
    /**
     * Set wait timeout
     */
    public FlowExecutor waitTimeout(long timeout, TimeUnit unit) {
        this.waitTimeout = Duration.of(timeout, unit.toChronoUnit());
        return this;
    }
    
    /**
     * Execute the flow
     */
    public FlowExecution execute() {
        String executionId = UUID.randomUUID().toString();
        FlowExecution execution = new FlowExecution(executionId, context.getFlow());
        execution.setInput(inputData);
        execution.setHeaders(headers);
        
        if (beforeExecution != null) {
            beforeExecution.accept(execution);
        }
        
        try {
            if (async) {
                CompletableFuture<FlowExecution> future = CompletableFuture.supplyAsync(() -> {
                    runner.run(execution);
                    return execution;
                });
                
                if (waitCondition != null) {
                    AtomicReference<FlowExecution> result = new AtomicReference<>();
                    
                    Awaitility.await()
                        .atMost(waitTimeout)
                        .pollInterval(Duration.ofMillis(100))
                        .until(() -> {
                            FlowExecution exec = future.getNow(null);
                            if (exec != null) {
                                result.set(exec);
                                return waitCondition.test(exec);
                            }
                            return false;
                        });
                    
                    execution = result.get();
                } else {
                    execution = future.get(waitTimeout.toMillis(), TimeUnit.MILLISECONDS);
                }
            } else {
                runner.run(execution);
                
                if (waitCondition != null) {
                    Awaitility.await()
                        .atMost(waitTimeout)
                        .until(() -> waitCondition.test(execution));
                }
            }
            
        } catch (ConditionTimeoutException e) {
            execution.fail("Timeout waiting for condition: " + e.getMessage());
        } catch (Exception e) {
            execution.fail("Execution failed: " + e.getMessage());
            execution.setException(e);
        }
        
        if (afterExecution != null) {
            afterExecution.accept(execution);
        }
        
        context.addExecution(execution);
        return execution;
    }
    
    /**
     * Execute the flow and return output
     */
    public <T> T executeAndReturn(Class<T> outputType) {
        FlowExecution execution = execute();
        if (execution.isSuccessful()) {
            return execution.getOutput(outputType);
        }
        throw new FlowExecutionException("Flow execution failed: " + execution.getError());
    }
    
    /**
     * Execute the flow multiple times
     */
    public MultipleExecutions executeMultiple(int times) {
        MultipleExecutions results = new MultipleExecutions();
        
        for (int i = 0; i < times; i++) {
            FlowExecution execution = execute();
            results.add(execution);
        }
        
        return results;
    }
    
    /**
     * Execute with different inputs
     */
    public MultipleExecutions executeWithInputs(Map<String, Object>... inputs) {
        MultipleExecutions results = new MultipleExecutions();
        
        for (Map<String, Object> input : inputs) {
            FlowExecution execution = withInput(input).execute();
            results.add(execution);
        }
        
        return results;
    }
    
    /**
     * Execute flow in parallel
     */
    public MultipleExecutions executeInParallel(int parallelism, int totalExecutions) {
        MultipleExecutions results = new MultipleExecutions();
        
        CompletableFuture<?>[] futures = new CompletableFuture[totalExecutions];
        
        for (int i = 0; i < totalExecutions; i++) {
            final int index = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                FlowExecution execution = execute();
                synchronized (results) {
                    results.add(execution);
                }
            });
        }
        
        CompletableFuture.allOf(futures).join();
        
        return results;
    }
    
    /**
     * Execute with retry
     */
    public FlowExecution executeWithRetry(int maxAttempts, long retryDelay, TimeUnit unit) {
        FlowExecution execution = null;
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            execution = execute();
            
            if (execution.isSuccessful()) {
                return execution;
            }
            
            lastException = execution.getException();
            
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(unit.toMillis(retryDelay));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        if (execution != null && lastException != null) {
            execution.fail("Failed after " + maxAttempts + " attempts: " + lastException.getMessage());
        }
        
        return execution;
    }
    
    /**
     * Container for multiple executions
     */
    public static class MultipleExecutions {
        private final java.util.List<FlowExecution> executions = new java.util.ArrayList<>();
        
        void add(FlowExecution execution) {
            executions.add(execution);
        }
        
        public java.util.List<FlowExecution> all() {
            return executions;
        }
        
        public java.util.List<FlowExecution> successful() {
            return executions.stream()
                .filter(FlowExecution::isSuccessful)
                .collect(java.util.stream.Collectors.toList());
        }
        
        public java.util.List<FlowExecution> failed() {
            return executions.stream()
                .filter(e -> !e.isSuccessful())
                .collect(java.util.stream.Collectors.toList());
        }
        
        public double successRate() {
            return (double) successful().size() / executions.size();
        }
        
        public double averageExecutionTime() {
            return executions.stream()
                .mapToLong(FlowExecution::getExecutionTime)
                .average()
                .orElse(0.0);
        }
    }
    
    public static class FlowExecutionException extends RuntimeException {
        public FlowExecutionException(String message) {
            super(message);
        }
    }
}