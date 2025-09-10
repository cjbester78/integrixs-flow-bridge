package com.integrixs.testing.runners;

import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.testing.core.FlowTestContext;
import com.integrixs.testing.engine.FlowEngine;
import com.integrixs.testing.engine.StepExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Core flow runner for executing integration flows in tests
 */
public class FlowRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowRunner.class);
    
    private final FlowTestContext context;
    private final FlowEngine engine;
    private final ExecutorService executorService;
    private final Map<String, StepExecutor> stepExecutors;
    
    public FlowRunner(FlowTestContext context) {
        this.context = context;
        this.engine = new FlowEngine(context);
        this.executorService = Executors.newCachedThreadPool();
        this.stepExecutors = new ConcurrentHashMap<>();
        initializeStepExecutors();
    }
    
    /**
     * Initialize step executors
     */
    private void initializeStepExecutors() {
        // Register built-in step executors
        stepExecutors.put("http", new HttpStepExecutor());
        stepExecutors.put("file", new FileStepExecutor());
        stepExecutors.put("database", new DatabaseStepExecutor());
        stepExecutors.put("messageQueue", new MessageQueueStepExecutor());
        stepExecutors.put("transform", new TransformStepExecutor());
        stepExecutors.put("filter", new FilterStepExecutor());
        stepExecutors.put("split", new SplitStepExecutor());
        stepExecutors.put("aggregate", new AggregateStepExecutor());
        stepExecutors.put("enrich", new EnrichStepExecutor());
        stepExecutors.put("validate", new ValidateStepExecutor());
        stepExecutors.put("script", new ScriptStepExecutor());
        stepExecutors.put("conditional", new ConditionalStepExecutor());
        stepExecutors.put("parallel", new ParallelStepExecutor());
        stepExecutors.put("loop", new LoopStepExecutor());
        stepExecutors.put("retry", new RetryStepExecutor());
        stepExecutors.put("cache", new CacheStepExecutor());
        stepExecutors.put("log", new LogStepExecutor());
        stepExecutors.put("delay", new DelayStepExecutor());
        stepExecutors.put("errorHandler", new ErrorHandlerStepExecutor());
    }
    
    /**
     * Run a flow execution
     */
    public void run(FlowExecution execution) {
        try {
            execution.start();
            IntegrationFlow flow = execution.getFlow();
            
            // Initialize flow context
            FlowContext flowContext = new FlowContext();
            flowContext.setExecution(execution);
            flowContext.setVariables(new ConcurrentHashMap<>());
            flowContext.setHeaders(execution.getHeaders());
            flowContext.setPayload(execution.getInput());
            
            // Execute flow steps
            executeFlow(flow, flowContext, execution);
            
            // Set output
            execution.setOutput(flowContext.getPayload());
            execution.setHeaders(flowContext.getHeaders());
            execution.complete();
            
        } catch (Exception e) {
            logger.error("Flow execution failed", e);
            execution.fail(e.getMessage());
            execution.setException(e);
        }
    }
    
    /**
     * Execute flow steps
     */
    private void executeFlow(IntegrationFlow flow, FlowContext flowContext, FlowExecution execution) throws Exception {
        if (flow.getSteps() == null || flow.getSteps().isEmpty()) {
            logger.warn("No steps defined in flow");
            return;
        }
        
        for (IntegrationFlow.Step step : flow.getSteps()) {
            if (execution.isCancelled()) {
                break;
            }
            
            executeStep(step, flowContext, execution);
        }
    }
    
    /**
     * Execute a single step
     */
    private void executeStep(IntegrationFlow.Step step, FlowContext flowContext, FlowExecution execution) throws Exception {
        String stepType = step.getType();
        StepExecutor executor = stepExecutors.get(stepType);
        
        if (executor == null) {
            throw new UnsupportedOperationException("No executor found for step type: " + stepType);
        }
        
        execution.startStep(step.getName());
        
        try {
            // Pre-process step
            preprocessStep(step, flowContext);
            
            // Execute step
            StepResult result = executor.execute(step, flowContext);
            
            // Post-process step
            postprocessStep(step, flowContext, result);
            
            execution.completeStep(step.getName());
            
        } catch (Exception e) {
            execution.failStep(step.getName(), e.getMessage());
            handleStepError(step, flowContext, e);
        }
    }
    
    /**
     * Pre-process step before execution
     */
    private void preprocessStep(IntegrationFlow.Step step, FlowContext flowContext) {
        // Apply input mapping
        if (step.getInputMapping() != null) {
            Map<String, Object> mappedInput = applyMapping(step.getInputMapping(), flowContext);
            flowContext.setStepInput(mappedInput);
        }
        
        // Evaluate conditions
        if (step.getCondition() != null) {
            boolean shouldExecute = evaluateCondition(step.getCondition(), flowContext);
            if (!shouldExecute) {
                throw new SkipStepException("Condition not met: " + step.getCondition());
            }
        }
    }
    
    /**
     * Post-process step after execution
     */
    private void postprocessStep(IntegrationFlow.Step step, FlowContext flowContext, StepResult result) {
        // Apply output mapping
        if (step.getOutputMapping() != null && result.getOutput() != null) {
            Map<String, Object> mappedOutput = applyMapping(step.getOutputMapping(), result.getOutput());
            flowContext.setPayload(mappedOutput);
        } else if (result.getOutput() != null) {
            flowContext.setPayload(result.getOutput());
        }
        
        // Store in variables if specified
        if (step.getStoreAs() != null) {
            flowContext.setVariable(step.getStoreAs(), result.getOutput());
        }
    }
    
    /**
     * Handle step errors
     */
    private void handleStepError(IntegrationFlow.Step step, FlowContext flowContext, Exception error) throws Exception {
        if (step.getErrorHandler() != null) {
            IntegrationFlow.ErrorHandler errorHandler = step.getErrorHandler();
            
            if (errorHandler.getRetry() != null) {
                // Handle retry
                int attempts = errorHandler.getRetry().getAttempts();
                long delay = errorHandler.getRetry().getDelay();
                
                for (int i = 0; i < attempts; i++) {
                    try {
                        Thread.sleep(delay);
                        executeStep(step, flowContext, flowContext.getExecution());
                        return; // Success, exit retry loop
                    } catch (Exception e) {
                        if (i == attempts - 1) {
                            // Last attempt failed
                            error = e;
                        }
                    }
                }
            }
            
            if (errorHandler.getFallback() != null) {
                // Execute fallback
                flowContext.setPayload(errorHandler.getFallback());
                return;
            }
            
            if (errorHandler.isContinue()) {
                // Continue with error
                logger.warn("Continuing after error in step: " + step.getName(), error);
                return;
            }
        }
        
        // Re-throw error
        throw error;
    }
    
    /**
     * Apply mapping
     */
    private Map<String, Object> applyMapping(Map<String, String> mapping, Object source) {
        Map<String, Object> result = new HashMap<>();
        
        mapping.forEach((targetPath, sourcePath) -> {
            Object value = extractValue(source, sourcePath);
            setValueByPath(result, targetPath, value);
        });
        
        return result;
    }
    
    /**
     * Apply mapping between contexts
     */
    private Map<String, Object> applyMapping(Map<String, String> mapping, FlowContext context) {
        Map<String, Object> result = new HashMap<>();
        
        mapping.forEach((targetPath, sourcePath) -> {
            Object value;
            if (sourcePath.startsWith("${") && sourcePath.endsWith("}")) {
                // Expression
                value = evaluateExpression(sourcePath, context);
            } else if (sourcePath.startsWith("var.")) {
                // Variable reference
                String varName = sourcePath.substring(4);
                value = context.getVariable(varName);
            } else if (sourcePath.startsWith("header.")) {
                // Header reference
                String headerName = sourcePath.substring(7);
                value = context.getHeaders().get(headerName);
            } else {
                // Payload path
                value = extractValue(context.getPayload(), sourcePath);
            }
            
            setValueByPath(result, targetPath, value);
        });
        
        return result;
    }
    
    /**
     * Extract value by path
     */
    private Object extractValue(Object source, String path) {
        if (source == null || path == null || path.isEmpty()) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        Object current = source;
        
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else if (part.matches("\\d+") && current instanceof List) {
                int index = Integer.parseInt(part);
                List<?> list = (List<?>) current;
                current = index < list.size() ? list.get(index) : null;
            } else {
                // Use reflection for objects
                try {
                    java.lang.reflect.Field field = current.getClass().getDeclaredField(part);
                    field.setAccessible(true);
                    current = field.get(current);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        
        return current;
    }
    
    /**
     * Set value by path
     */
    private void setValueByPath(Map<String, Object> target, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = target;
        
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.containsKey(part)) {
                current.put(part, new HashMap<String, Object>());
            }
            current = (Map<String, Object>) current.get(part);
        }
        
        current.put(parts[parts.length - 1], value);
    }
    
    /**
     * Evaluate condition
     */
    private boolean evaluateCondition(String condition, FlowContext context) {
        // Simple condition evaluation
        // In a real implementation, this would use a proper expression language
        
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            String left = parts[0].trim();
            String right = parts[1].trim();
            
            Object leftValue = evaluateExpression(left, context);
            Object rightValue = evaluateExpression(right, context);
            
            return Objects.equals(leftValue, rightValue);
        }
        
        // Default to true for unsupported conditions
        return true;
    }
    
    /**
     * Evaluate expression
     */
    private Object evaluateExpression(String expression, FlowContext context) {
        if (expression.startsWith("${") && expression.endsWith("}")) {
            expression = expression.substring(2, expression.length() - 1);
        }
        
        if (expression.startsWith("var.")) {
            String varName = expression.substring(4);
            return context.getVariable(varName);
        }
        
        if (expression.startsWith("header.")) {
            String headerName = expression.substring(7);
            return context.getHeaders().get(headerName);
        }
        
        if (expression.startsWith("'") && expression.endsWith("'")) {
            return expression.substring(1, expression.length() - 1);
        }
        
        // Try as number
        try {
            return Integer.parseInt(expression);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(expression);
            } catch (NumberFormatException e2) {
                // Return as string
                return expression;
            }
        }
    }
    
    /**
     * Shutdown the runner
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Exception to skip step execution
     */
    private static class SkipStepException extends RuntimeException {
        public SkipStepException(String message) {
            super(message);
        }
    }
}