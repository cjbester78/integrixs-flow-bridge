package com.integrixs.webserver.api.dto;

import java.time.LocalDateTime;

/**
 * DTO for endpoint test result
 */
public class EndpointTestResultDTO {

    private String endpointId;
    private boolean reachable;
    private LocalDateTime timestamp;
    private String message;
    private Long responseTimeMillis;

    // Default constructor
    public EndpointTestResultDTO() {
    }

    // All args constructor
    public EndpointTestResultDTO(String endpointId, boolean reachable, LocalDateTime timestamp, String message, Long responseTimeMillis) {
        this.endpointId = endpointId;
        this.reachable = reachable;
        this.timestamp = timestamp;
        this.message = message;
        this.responseTimeMillis = responseTimeMillis;
    }

    // Getters
    public String getEndpointId() {
        return endpointId;
    }
    public boolean isReachable() {
        return reachable;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public String getMessage() {
        return message;
    }
    public Long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    // Setters
    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }
    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setResponseTimeMillis(Long responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }

    // Builder
    public static EndpointTestResultDTOBuilder builder() {
        return new EndpointTestResultDTOBuilder();
    }

    public static class EndpointTestResultDTOBuilder {
        private String endpointId;
        private boolean reachable;
        private LocalDateTime timestamp;
        private String message;
        private Long responseTimeMillis;

        public EndpointTestResultDTOBuilder endpointId(String endpointId) {
            this.endpointId = endpointId;
            return this;
        }

        public EndpointTestResultDTOBuilder reachable(boolean reachable) {
            this.reachable = reachable;
            return this;
        }

        public EndpointTestResultDTOBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public EndpointTestResultDTOBuilder message(String message) {
            this.message = message;
            return this;
        }

        public EndpointTestResultDTOBuilder responseTimeMillis(Long responseTimeMillis) {
            this.responseTimeMillis = responseTimeMillis;
            return this;
        }

        public EndpointTestResultDTO build() {
            return new EndpointTestResultDTO(endpointId, reachable, timestamp, message, responseTimeMillis);
        }
    }}
