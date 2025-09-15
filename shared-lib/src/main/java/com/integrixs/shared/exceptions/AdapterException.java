package com.integrixs.shared.exceptions;

/**
 * Exception thrown by adapters when errors occur during processing
 */
public class AdapterException extends Exception {

    private final String adapterType;
    private final String errorCode;

    public AdapterException(String message) {
        super(message);
        this.adapterType = null;
        this.errorCode = null;
    }

    public AdapterException(String message, Throwable cause) {
        super(message, cause);
        this.adapterType = null;
        this.errorCode = null;
    }

    public AdapterException(String message, String adapterType, String errorCode) {
        super(message);
        this.adapterType = adapterType;
        this.errorCode = errorCode;
    }

    public AdapterException(String message, Throwable cause, String adapterType, String errorCode) {
        super(message, cause);
        this.adapterType = adapterType;
        this.errorCode = errorCode;
    }

    public String getAdapterType() {
        return adapterType;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
