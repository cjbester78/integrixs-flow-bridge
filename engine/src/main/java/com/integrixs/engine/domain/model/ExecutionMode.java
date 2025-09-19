package com.integrixs.engine.domain.model;

/**
 * Execution mode for adapter operations
 */
public enum ExecutionMode {
    INBOUND,
    OUTBOUND,
    BIDIRECTIONAL,
    TRANSFORM,
    ROUTE
}