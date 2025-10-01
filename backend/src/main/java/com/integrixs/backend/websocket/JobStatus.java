package com.integrixs.backend.websocket;

/**
 * Job status enumeration for WebSocket updates
 */
public enum JobStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
    RETRYING,
    PAUSED
}