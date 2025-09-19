package com.integrixs.shared.dto.flow;

/**
 * Processing mode for data flows
 */
public enum ProcessingMode {
    SYNCHRONOUS,
    ASYNCHRONOUS,
    BATCH,
    STREAMING,
    SCHEDULED
}