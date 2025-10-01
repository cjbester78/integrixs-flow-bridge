package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object for adapter test results
 */
public class AdapterTestResponse {

    private boolean success;
    private String message;
    private String errorDetails;

    private Long responseTimeMs;
    private LocalDateTime testedAt;

    private Map<String, Object> testResults;
    private Map<String, String> connectionDetails;

    private boolean connectionValid = false;

    private boolean authenticationValid = false;

    // Default constructor
    public AdapterTestResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public LocalDateTime getTestedAt() {
        return testedAt;
    }

    public void setTestedAt(LocalDateTime testedAt) {
        this.testedAt = testedAt;
    }

    public boolean isConnectionValid() {
        return connectionValid;
    }

    public void setConnectionValid(boolean connectionValid) {
        this.connectionValid = connectionValid;
    }

    public boolean isAuthenticationValid() {
        return authenticationValid;
    }

    public void setAuthenticationValid(boolean authenticationValid) {
        this.authenticationValid = authenticationValid;
    }

    public Map<String, Object> getTestResults() {
        return testResults;
    }

    public void setTestResults(Map<String, Object> testResults) {
        this.testResults = testResults;
    }

    public Map<String, String> getConnectionDetails() {
        return connectionDetails;
    }

    public void setConnectionDetails(Map<String, String> connectionDetails) {
        this.connectionDetails = connectionDetails;
    }

    // Builder
    public static AdapterTestResponseBuilder builder() {
        return new AdapterTestResponseBuilder();
    }

    public static class AdapterTestResponseBuilder {
        private AdapterTestResponse response = new AdapterTestResponse();

        public AdapterTestResponseBuilder success(boolean success) {
            response.success = success;
            return this;
        }

        public AdapterTestResponseBuilder message(String message) {
            response.message = message;
            return this;
        }

        public AdapterTestResponseBuilder errorDetails(String errorDetails) {
            response.errorDetails = errorDetails;
            return this;
        }

        public AdapterTestResponseBuilder responseTimeMs(Long responseTimeMs) {
            response.responseTimeMs = responseTimeMs;
            return this;
        }

        public AdapterTestResponseBuilder testedAt(LocalDateTime testedAt) {
            response.testedAt = testedAt;
            return this;
        }

        public AdapterTestResponseBuilder testResults(Map<String, Object> testResults) {
            response.testResults = testResults;
            return this;
        }

        public AdapterTestResponseBuilder connectionDetails(Map<String, String> connectionDetails) {
            response.connectionDetails = connectionDetails;
            return this;
        }

        public AdapterTestResponseBuilder connectionValid(boolean connectionValid) {
            response.connectionValid = connectionValid;
            return this;
        }

        public AdapterTestResponseBuilder authenticationValid(boolean authenticationValid) {
            response.authenticationValid = authenticationValid;
            return this;
        }

        public AdapterTestResponse build() {
            return response;
        }
    }
}
