package com.integrixs.webclient.api.dto;


import java.time.LocalDateTime;

/**
 * DTO for message search criteria
 */
public class MessageSearchCriteriaDTO {

    private String status;
    private String flowId;
    private String correlationId;
    private String source;
    private String adapterId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer limit;

    // Getters
    public String getStatus() {
        return status;
    }
    public String getFlowId() {
        return flowId;
    }
    public String getCorrelationId() {
        return correlationId;
    }
    public String getSource() {
        return source;
    }
    public String getAdapterId() {
        return adapterId;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public Integer getLimit() {
        return limit;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    // Builder
    public static MessageSearchCriteriaDTOBuilder builder() {
        return new MessageSearchCriteriaDTOBuilder();
    }

    public static class MessageSearchCriteriaDTOBuilder {
        private String status;
        private String flowId;
        private String correlationId;
        private String source;
        private String adapterId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer limit;

        public MessageSearchCriteriaDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public MessageSearchCriteriaDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public MessageSearchCriteriaDTOBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public MessageSearchCriteriaDTOBuilder source(String source) {
            this.source = source;
            return this;
        }

        public MessageSearchCriteriaDTOBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public MessageSearchCriteriaDTOBuilder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public MessageSearchCriteriaDTOBuilder endDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public MessageSearchCriteriaDTOBuilder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public MessageSearchCriteriaDTO build() {
            MessageSearchCriteriaDTO result = new MessageSearchCriteriaDTO();
            result.status = this.status;
            result.flowId = this.flowId;
            result.correlationId = this.correlationId;
            result.source = this.source;
            result.adapterId = this.adapterId;
            result.startDate = this.startDate;
            result.endDate = this.endDate;
            result.limit = this.limit;
            return result;
        }
    }
}
