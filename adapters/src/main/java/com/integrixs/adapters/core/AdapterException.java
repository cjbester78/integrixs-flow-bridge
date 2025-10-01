package com.integrixs.adapters.core;

/**
 * Exception thrown by adapters when errors occur during processing
 */
public class AdapterException extends Exception {

    private String errorCode;
    private Object details;

    public AdapterException(String message) {
        super(message);
    }

    public AdapterException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdapterException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AdapterException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public AdapterException(String message, String errorCode, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public AdapterException(String message, String errorCode, Object details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getDetails() {
        return details;
    }
}