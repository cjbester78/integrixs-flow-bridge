package com.integrixs.data.model;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for event sourcing - stores all domain events.
 *
 * <p>Provides an immutable audit trail of all system events.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class EventStore {

    /**
     * Unique event ID
     */
        private UUID eventId;

    /**
     * Type of the aggregate this event belongs to
     */
    @NotBlank(message = "Aggregate type is required")
    private String aggregateType;

    /**
     * ID of the aggregate this event belongs to
     */
    @NotNull(message = "Aggregate ID is required")
    private UUID aggregateId;

    /**
     * Version of the aggregate when this event was created
     */
    @NotNull(message = "Aggregate version is required")
    private Long aggregateVersion;

    /**
     * Type of the event
     */
    @NotBlank(message = "Event type is required")
    private String eventType;

    /**
     * Event data in JSON format
     */
    @NotBlank(message = "Event data is required")
    private String eventData;

    /**
     * Event metadata in JSON format
     */
    private String eventMetadata;

    /**
     * When the event occurred
     */
    @NotNull(message = "Occurred at is required")
    private LocalDateTime occurredAt;

    /**
     * User who triggered the event
     */
    private UUID triggeredBy;

    /**
     * When the event was stored
     */
        private LocalDateTime storedAt;

    /**
     * Correlation ID for tracking related events
     */
    private UUID correlationId;

    /**
     * Causation ID - ID of the event that caused this event
     */
    private UUID causationId;

    // Default constructor
    public EventStore() {
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Long getAggregateVersion() {
        return aggregateVersion;
    }

    public void setAggregateVersion(Long aggregateVersion) {
        this.aggregateVersion = aggregateVersion;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public String getEventMetadata() {
        return eventMetadata;
    }

    public void setEventMetadata(String eventMetadata) {
        this.eventMetadata = eventMetadata;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public UUID getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(UUID triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public LocalDateTime getStoredAt() {
        return storedAt;
    }

    public void setStoredAt(LocalDateTime storedAt) {
        this.storedAt = storedAt;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public UUID getCausationId() {
        return causationId;
    }

    public void setCausationId(UUID causationId) {
        this.causationId = causationId;
    }

    // Builder
    public static EventStoreBuilder builder() {
        return new EventStoreBuilder();
    }

    public static class EventStoreBuilder {
        private UUID eventId;
        private String aggregateType;
        private UUID aggregateId;
        private Long aggregateVersion;
        private String eventType;
        private String eventData;
        private String eventMetadata;
        private LocalDateTime occurredAt;
        private UUID triggeredBy;
        private LocalDateTime storedAt;
        private UUID correlationId;
        private UUID causationId;

        public EventStoreBuilder eventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }

        public EventStoreBuilder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public EventStoreBuilder aggregateId(UUID aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public EventStoreBuilder aggregateVersion(Long aggregateVersion) {
            this.aggregateVersion = aggregateVersion;
            return this;
        }

        public EventStoreBuilder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public EventStoreBuilder eventData(String eventData) {
            this.eventData = eventData;
            return this;
        }

        public EventStoreBuilder eventMetadata(String eventMetadata) {
            this.eventMetadata = eventMetadata;
            return this;
        }

        public EventStoreBuilder occurredAt(LocalDateTime occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public EventStoreBuilder triggeredBy(UUID triggeredBy) {
            this.triggeredBy = triggeredBy;
            return this;
        }

        public EventStoreBuilder storedAt(LocalDateTime storedAt) {
            this.storedAt = storedAt;
            return this;
        }

        public EventStoreBuilder correlationId(UUID correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public EventStoreBuilder causationId(UUID causationId) {
            this.causationId = causationId;
            return this;
        }

        public EventStore build() {
            EventStore instance = new EventStore();
            instance.setEventId(this.eventId);
            instance.setAggregateType(this.aggregateType);
            instance.setAggregateId(this.aggregateId);
            instance.setAggregateVersion(this.aggregateVersion);
            instance.setEventType(this.eventType);
            instance.setEventData(this.eventData);
            instance.setEventMetadata(this.eventMetadata);
            instance.setOccurredAt(this.occurredAt);
            instance.setTriggeredBy(this.triggeredBy);
            instance.setStoredAt(this.storedAt);
            instance.setCorrelationId(this.correlationId);
            instance.setCausationId(this.causationId);
            return instance;
        }
    }
}
