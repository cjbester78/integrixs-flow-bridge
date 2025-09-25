package com.integrixs.data.model;
import java.io.Serializable;
import java.util.UUID;

public class FlowStructureMessageId implements Serializable {
    private UUID flowStructure;
    private FlowStructureMessage.MessageType messageType;

    public UUID getFlowStructure() {
        return flowStructure;
    }

    public void setFlowStructure(UUID flowStructure) {
        this.flowStructure = flowStructure;
    }

    public FlowStructureMessage.MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(FlowStructureMessage.MessageType messageType) {
        this.messageType = messageType;
    }

    // Builder
    public static FlowStructureMessageIdBuilder builder() {
        return new FlowStructureMessageIdBuilder();
    }

    public static class FlowStructureMessageIdBuilder {
        private UUID flowStructure;
        private FlowStructureMessage.MessageType messageType;

        public FlowStructureMessageIdBuilder flowStructure(UUID flowStructure) {
            this.flowStructure = flowStructure;
            return this;
        }

        public FlowStructureMessageIdBuilder messageType(FlowStructureMessage.MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public FlowStructureMessageId build() {
            FlowStructureMessageId instance = new FlowStructureMessageId();
            instance.setFlowStructure(this.flowStructure);
            instance.setMessageType(this.messageType);
            return instance;
        }
    }
}
