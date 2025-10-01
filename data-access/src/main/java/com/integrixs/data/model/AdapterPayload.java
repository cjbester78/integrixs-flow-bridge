package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for storing adapter payloads(requests and responses)
 */

    public class AdapterPayload {

    private UUID id;

    private String correlationId;

    private UUID adapterId;

    private String adapterName;

    private String adapterType;

    private String direction; // INBOUND or OUTBOUND

    private String payloadType; // REQUEST or RESPONSE

    // Temporarily commented out as it's causing transaction rollback issues
    // and doesn't appear to be used in the current implementation
    // // private UUID messageStructureId;

    // Adding field back to avoid compilation errors
    private UUID messageStructureId;

    private String payload;

    private Integer payloadSize;

        private LocalDateTime createdAt;

    // Default constructor
    public AdapterPayload() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public UUID getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(UUID adapterId) {
        this.adapterId = adapterId;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    public UUID getMessageStructureId() {
        return messageStructureId;
    }

    public void setMessageStructureId(UUID messageStructureId) {
        this.messageStructureId = messageStructureId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getPayloadSize() {
        return payloadSize;
    }

    public void setPayloadSize(Integer payloadSize) {
        this.payloadSize = payloadSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Builder
    public static AdapterPayloadBuilder builder() {
        return new AdapterPayloadBuilder();
    }

    public static class AdapterPayloadBuilder {
        private UUID id;
        private String correlationId;
        private UUID adapterId;
        private String adapterName;
        private String adapterType;
        private String direction;
        private String payloadType;
        private UUID messageStructureId;
        private String payload;
        private Integer payloadSize;
        private LocalDateTime createdAt;

        public AdapterPayloadBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public AdapterPayloadBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public AdapterPayloadBuilder adapterId(UUID adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public AdapterPayloadBuilder adapterName(String adapterName) {
            this.adapterName = adapterName;
            return this;
        }

        public AdapterPayloadBuilder adapterType(String adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public AdapterPayloadBuilder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public AdapterPayloadBuilder payloadType(String payloadType) {
            this.payloadType = payloadType;
            return this;
        }

        public AdapterPayloadBuilder messageStructureId(UUID messageStructureId) {
            this.messageStructureId = messageStructureId;
            return this;
        }

        public AdapterPayloadBuilder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public AdapterPayloadBuilder payloadSize(Integer payloadSize) {
            this.payloadSize = payloadSize;
            return this;
        }

        public AdapterPayloadBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AdapterPayload build() {
            AdapterPayload instance = new AdapterPayload();
            instance.setId(this.id);
            instance.setCorrelationId(this.correlationId);
            instance.setAdapterId(this.adapterId);
            instance.setAdapterName(this.adapterName);
            instance.setAdapterType(this.adapterType);
            instance.setDirection(this.direction);
            instance.setPayloadType(this.payloadType);
            instance.setMessageStructureId(this.messageStructureId);
            instance.setPayload(this.payload);
            instance.setPayloadSize(this.payloadSize);
            instance.setCreatedAt(this.createdAt);
            return instance;
        }
    }
}
