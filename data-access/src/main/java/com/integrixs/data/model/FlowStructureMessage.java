package com.integrixs.data.model;

import jakarta.persistence.*;
@Entity
@Table(name = "flow_structure_messages")
@IdClass(FlowStructureMessageId.class)
public class FlowStructureMessage {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_structure_id", nullable = false)
    private FlowStructure flowStructure;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_structure_id", nullable = false)
    private MessageStructure messageStructure;

    public enum MessageType {
        INPUT,
        OUTPUT,
        FAULT
    }

    // Default constructor
    public FlowStructureMessage() {
    }

    public FlowStructure getFlowStructure() {
        return flowStructure;
    }

    public void setFlowStructure(FlowStructure flowStructure) {
        this.flowStructure = flowStructure;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageStructure getMessageStructure() {
        return messageStructure;
    }

    public void setMessageStructure(MessageStructure messageStructure) {
        this.messageStructure = messageStructure;
    }

    // Builder
    public static FlowStructureMessageBuilder builder() {
        return new FlowStructureMessageBuilder();
    }

    public static class FlowStructureMessageBuilder {
        private FlowStructure flowStructure;
        private MessageType messageType;
        private MessageStructure messageStructure;

        public FlowStructureMessageBuilder flowStructure(FlowStructure flowStructure) {
            this.flowStructure = flowStructure;
            return this;
        }

        public FlowStructureMessageBuilder messageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public FlowStructureMessageBuilder messageStructure(MessageStructure messageStructure) {
            this.messageStructure = messageStructure;
            return this;
        }

        public FlowStructureMessage build() {
            FlowStructureMessage instance = new FlowStructureMessage();
            instance.setFlowStructure(this.flowStructure);
            instance.setMessageType(this.messageType);
            instance.setMessageStructure(this.messageStructure);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "FlowStructureMessage{" + 
                "messageType=" + messageType + 
                '}';
    }
}
