package com.integrixs.webserver.api.dto;

import java.time.LocalDateTime;

/**
 * DTO for request history search criteria
 */
public class RequestHistoryCriteriaDTO {

    private String flowId;
    private String adapterId;
    private String endpointId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean onlyFailed;
    private Integer limit;

    // Default constructor
    public RequestHistoryCriteriaDTO() {
    }

    // All args constructor
    public RequestHistoryCriteriaDTO(String flowId, String adapterId, String endpointId, LocalDateTime startDate, LocalDateTime endDate, Boolean onlyFailed, Integer limit) {
        this.flowId = flowId;
        this.adapterId = adapterId;
        this.endpointId = endpointId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.onlyFailed = onlyFailed;
        this.limit = limit;
    }

    // Getters
    public String getFlowId() {
        return flowId;
    }
    public String getAdapterId() {
        return adapterId;
    }
    public String getEndpointId() {
        return endpointId;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public Boolean getOnlyFailed() {
        return onlyFailed;
    }
    public Integer getLimit() {
        return limit;
    }

    // Setters
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }
    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    public void setOnlyFailed(Boolean onlyFailed) {
        this.onlyFailed = onlyFailed;
    }
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    // Builder
    public static RequestHistoryCriteriaDTOBuilder builder() {
        return new RequestHistoryCriteriaDTOBuilder();
    }

    public static class RequestHistoryCriteriaDTOBuilder {
        private String flowId;
        private String adapterId;
        private String endpointId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean onlyFailed;
        private Integer limit;

        public RequestHistoryCriteriaDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public RequestHistoryCriteriaDTOBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public RequestHistoryCriteriaDTOBuilder endpointId(String endpointId) {
            this.endpointId = endpointId;
            return this;
        }

        public RequestHistoryCriteriaDTOBuilder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public RequestHistoryCriteriaDTOBuilder endDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public RequestHistoryCriteriaDTOBuilder onlyFailed(Boolean onlyFailed) {
            this.onlyFailed = onlyFailed;
            return this;
        }

        public RequestHistoryCriteriaDTOBuilder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public RequestHistoryCriteriaDTO build() {
            return new RequestHistoryCriteriaDTO(flowId, adapterId, endpointId, startDate, endDate, onlyFailed, limit);
        }
    }}
