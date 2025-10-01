package com.integrixs.shared.events;

import java.time.LocalDateTime;

/**
 * Abstract base class for domain events.
 *
 * <p>Provides common implementation for all domain events.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public abstract class AbstractDomainEvent implements DomainEvent {

    private String eventId;
    private LocalDateTime occurredAt;
    private String aggregateId;
    private Long aggregateVersion;
    private String triggeredBy;

    /**
     * Default constructor
     */
    protected AbstractDomainEvent() {
        this.eventId = DomainEvent.newEventId();
        this.occurredAt = LocalDateTime.now();
        this.aggregateVersion = 1L;
    }

    /**
     * Initializes the event with default values.
     */
    protected AbstractDomainEvent(String aggregateId, String triggeredBy) {
        this.eventId = DomainEvent.newEventId();
        this.occurredAt = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.triggeredBy = triggeredBy;
        this.aggregateVersion = 1L;
    }

    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }

    // Getters
    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public Long getAggregateVersion() {
        return aggregateVersion;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    // Setters
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public void setAggregateVersion(Long aggregateVersion) {
        this.aggregateVersion = aggregateVersion;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
}
