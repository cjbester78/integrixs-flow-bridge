package com.integrixs.backend.statemachine;

/**
 * Events that trigger state transitions during integration flow execution
 * Used by Spring State Machine to orchestrate flow processing
 */
public enum FlowExecutionEvents {
    /**
     * Start flow execution from PENDING state
     */
    START_FLOW,
    
    /**
     * Transformation has completed successfully
     */
    TRANSFORM_COMPLETE,
    
    /**
     * Routing evaluation completed and targets selected
     */
    ROUTE_SELECTED,
    
    /**
     * Adapter execution completed successfully
     */
    ADAPTER_EXECUTED,
    
    /**
     * Flow execution completed successfully
     */
    FLOW_COMPLETE,
    
    /**
     * Flow execution failed at any stage
     */
    FLOW_FAILED
}