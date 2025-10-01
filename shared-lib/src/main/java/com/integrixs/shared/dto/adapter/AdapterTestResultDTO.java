package com.integrixs.shared.dto.adapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * DTO for adapter test results.
 *
 * <p>Contains the outcome of an adapter connectivity test including
 * success status, error messages, and diagnostic information.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdapterTestResultDTO {

    /**
     * Name or type of adapter that was tested
     */
    @NotBlank(message = "Adapter name is required")
    private String adapter;

    /**
     * Whether the test was successful
     */
    @NotNull(message = "Success status is required")
    private boolean success;

    /**
     * Result message or error description
     */
    @NotBlank(message = "Message is required")
    private String message;

    /**
     * Timestamp of when the test was performed
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Response time in milliseconds(optional)
     */
    private Long responseTimeMs;

    /**
     * Additional diagnostic information(optional)
     */
    private Map<String, Object> diagnostics;

    // Default constructor
    public AdapterTestResultDTO() {
    }

    // All args constructor
    public AdapterTestResultDTO(String adapter, boolean success, String message,
                               LocalDateTime timestamp, Long responseTimeMs,
                               Map<String, Object> diagnostics) {
        this.adapter = adapter;
        this.success = success;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.responseTimeMs = responseTimeMs;
        this.diagnostics = diagnostics;
    }

    // Getters
    public String getAdapter() {
        return adapter;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public Map<String, Object> getDiagnostics() {
        return diagnostics;
    }

    // Setters
    public void setAdapter(String adapter) {
        this.adapter = adapter;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public void setDiagnostics(Map<String, Object> diagnostics) {
        this.diagnostics = diagnostics;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String adapter;
        private boolean success;
        private String message;
        private LocalDateTime timestamp = LocalDateTime.now();
        private Long responseTimeMs;
        private Map<String, Object> diagnostics;

        public Builder adapter(String adapter) {
            this.adapter = adapter;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder responseTimeMs(Long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public Builder diagnostics(Map<String, Object> diagnostics) {
            this.diagnostics = diagnostics;
            return this;
        }

        public AdapterTestResultDTO build() {
            return new AdapterTestResultDTO(adapter, success, message, timestamp,
                                          responseTimeMs, diagnostics);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdapterTestResultDTO that = (AdapterTestResultDTO) o;
        return success == that.success &&
               Objects.equals(adapter, that.adapter) &&
               Objects.equals(message, that.message) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(responseTimeMs, that.responseTimeMs) &&
               Objects.equals(diagnostics, that.diagnostics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adapter, success, message, timestamp, responseTimeMs, diagnostics);
    }

    @Override
    public String toString() {
        return "AdapterTestResultDTO{" +
                "adapter='" + adapter + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", responseTimeMs=" + responseTimeMs +
                ", diagnostics=" + diagnostics +
                '}';
    }
}
