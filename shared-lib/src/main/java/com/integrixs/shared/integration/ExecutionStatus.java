package com.integrixs.shared.integration;

/**
 * Execution status enumeration
 */
public enum ExecutionStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
    TIMEOUT,
    RETRYING
}
