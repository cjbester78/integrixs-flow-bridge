package com.integrixs.backend.adapter;

/**
 * Exception thrown by adapter operations
 */
public class AdapterException extends RuntimeException {

    public AdapterException(String message) {
        super(message);
    }

    public AdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}