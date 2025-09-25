package com.integrixs.testing.engine;

import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.testing.runners.FlowContext;

/**
 * Base interface for step executors
 */
public interface StepExecutor {
    
    /**
     * Execute a step
     */
    StepResult execute(IntegrationFlow.Step step, FlowContext context) throws Exception;
    
    /**
     * Check if this executor can handle the given step type
     */
    default boolean canHandle(String stepType) {
        return getClass().getSimpleName().toLowerCase()
            .startsWith(stepType.toLowerCase());
    }
    
    /**
     * Get the step types this executor handles
     */
    default String[] getSupportedTypes() {
        String className = getClass().getSimpleName();
        if (className.endsWith("StepExecutor")) {
            String type = className.substring(0, className.length() - 12);
            return new String[]{type.toLowerCase()};
        }
        return new String[0];
    }
}