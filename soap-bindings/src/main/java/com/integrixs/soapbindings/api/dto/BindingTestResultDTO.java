package com.integrixs.soapbindings.api.dto;

import java.time.LocalDateTime;

/**
 * DTO for binding test result
 */
public class BindingTestResultDTO {

    private String bindingId;
    private boolean reachable;
    private LocalDateTime timestamp;
    private String message;
    private Long responseTimeMillis;

    // Default constructor
    public BindingTestResultDTO() {
    }

    // All args constructor
    public BindingTestResultDTO(String bindingId, boolean reachable, LocalDateTime timestamp, String message, Long responseTimeMillis) {
        this.bindingId = bindingId;
        this.reachable = reachable;
        this.timestamp = timestamp;
        this.message = message;
        this.responseTimeMillis = responseTimeMillis;
    }

    // Getters
    public String getBindingId() { return bindingId; }
    public boolean isReachable() { return reachable; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    public Long getResponseTimeMillis() { return responseTimeMillis; }

    // Setters
    public void setBindingId(String bindingId) { this.bindingId = bindingId; }
    public void setReachable(boolean reachable) { this.reachable = reachable; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setMessage(String message) { this.message = message; }
    public void setResponseTimeMillis(Long responseTimeMillis) { this.responseTimeMillis = responseTimeMillis; }

    // Builder
    public static BindingTestResultDTOBuilder builder() {
        return new BindingTestResultDTOBuilder();
    }

    public static class BindingTestResultDTOBuilder {
        private String bindingId;
        private boolean reachable;
        private LocalDateTime timestamp;
        private String message;
        private Long responseTimeMillis;

        public BindingTestResultDTOBuilder bindingId(String bindingId) {
            this.bindingId = bindingId;
            return this;
        }

        public BindingTestResultDTOBuilder reachable(boolean reachable) {
            this.reachable = reachable;
            return this;
        }

        public BindingTestResultDTOBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public BindingTestResultDTOBuilder message(String message) {
            this.message = message;
            return this;
        }

        public BindingTestResultDTOBuilder responseTimeMillis(Long responseTimeMillis) {
            this.responseTimeMillis = responseTimeMillis;
            return this;
        }

        public BindingTestResultDTO build() {
            return new BindingTestResultDTO(bindingId, reachable, timestamp, message, responseTimeMillis);
        }
    }
}
