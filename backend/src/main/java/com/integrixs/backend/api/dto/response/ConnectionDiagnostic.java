package com.integrixs.backend.api.dto.response;

import java.util.Map;
import java.util.HashMap;

public class ConnectionDiagnostic {

    public enum Status {
        SUCCESS, FAILED, WARNING, SKIPPED
    }

    private String step;
    private Status status;
    private String message;
    private long duration; // in milliseconds

    private Map<String, Object> details = new HashMap<>();

    // Optional error details
    private String errorCode;
    private String stackTrace;

    // Default constructor
    public ConnectionDiagnostic() {
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    // Builder
    public static ConnectionDiagnosticBuilder builder() {
        return new ConnectionDiagnosticBuilder();
    }

    public static class ConnectionDiagnosticBuilder {
        private String step;
        private Status status;
        private String message;
        private long duration;
        private Map<String, Object> details = new HashMap<>();
        private String errorCode;
        private String stackTrace;

        public ConnectionDiagnosticBuilder step(String step) {
            this.step = step;
            return this;
        }

        public ConnectionDiagnosticBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public ConnectionDiagnosticBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ConnectionDiagnosticBuilder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public ConnectionDiagnosticBuilder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public ConnectionDiagnosticBuilder addDetail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }

        public ConnectionDiagnosticBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ConnectionDiagnosticBuilder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public ConnectionDiagnostic build() {
            ConnectionDiagnostic diagnostic = new ConnectionDiagnostic();
            diagnostic.setStep(this.step);
            diagnostic.setStatus(this.status);
            diagnostic.setMessage(this.message);
            diagnostic.setDuration(this.duration);
            diagnostic.setDetails(this.details);
            diagnostic.setErrorCode(this.errorCode);
            diagnostic.setStackTrace(this.stackTrace);
            return diagnostic;
        }
    }
}
