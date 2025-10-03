package com.integrixs.backend.statemachine;

/**
 * States for integration flow execution lifecycle
 * Used by Spring State Machine to track flow orchestration progress
 */
public enum FlowExecutionStates {
    /**
     * Initial state - flow execution has been requested but not started
     */
    PENDING,
    
    /**
     * Flow execution has started and is being processed
     */
    PROCESSING,
    
    /**
     * Data transformation is in progress
     */
    TRANSFORMING,
    
    /**
     * Routing rules are being evaluated and targets selected
     */
    ROUTING,
    
    /**
     * Adapters are being executed (outbound calls)
     */
    EXECUTING,
    
    /**
     * Flow execution completed successfully
     */
    COMPLETED,
    
    /**
     * Flow execution failed at any stage
     */
    FAILED
}