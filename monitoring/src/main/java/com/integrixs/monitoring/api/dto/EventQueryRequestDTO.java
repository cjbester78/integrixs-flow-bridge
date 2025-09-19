package com.integrixs.monitoring.api.dto;


/**
 * DTO for event query request
 */
public class EventQueryRequestDTO {
    private String eventType;
    private String minLevel;
    private String source;
    private String userId;
    private String domainType;
    private String domainReferenceId;
    private String correlationId;
    private Long startTime;
    private Long endTime;
    private Integer limit;


    // Getters
    public String getEventType() {
        return eventType;
    }

    public String getMinLevel() {
        return minLevel;
    }

    public String getSource() {
        return source;
    }

    public String getUserId() {
        return userId;
    }

    public String getDomainType() {
        return domainType;
    }

    public String getDomainReferenceId() {
        return domainReferenceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public Integer getLimit() {
        return limit;
    }

    // Setters
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setMinLevel(String minLevel) {
        this.minLevel = minLevel;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public void setDomainReferenceId(String domainReferenceId) {
        this.domainReferenceId = domainReferenceId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

}
