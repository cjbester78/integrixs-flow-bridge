package com.integrixs.backend.domain.model;

/**
 * Domain enum for execution status
 */
public enum ExecutionStatus {
    STARTED,
    RUNNING,
    COMPLETED,
    FAILED,
    ERROR,
    CANCELLED
}
