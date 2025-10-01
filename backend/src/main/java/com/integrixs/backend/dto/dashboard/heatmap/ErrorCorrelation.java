package com.integrixs.backend.dto.dashboard.heatmap;

/**
 * Correlation between different error types.
 */
public class ErrorCorrelation {
    private String errorType1;
    private String errorType2;
    private int correlationCount;

    // Default constructor
    public ErrorCorrelation() {
    }

    public String getErrorType1() {
        return errorType1;
    }

    public void setErrorType1(String errorType1) {
        this.errorType1 = errorType1;
    }

    public String getErrorType2() {
        return errorType2;
    }

    public void setErrorType2(String errorType2) {
        this.errorType2 = errorType2;
    }

    public int getCorrelationCount() {
        return correlationCount;
    }

    public void setCorrelationCount(int correlationCount) {
        this.correlationCount = correlationCount;
    }
}
