package com.integrixs.shared.dto.structure;

public class FlowStructureMessagePayloadDTO {

    private String flowStructureId;
    private MessageType messageType;
    private MessageStructureDTO messageStructure;

    // Default constructor
    public FlowStructureMessagePayloadDTO() {
    }

    // All args constructor
    public FlowStructureMessagePayloadDTO(String flowStructureId, MessageType messageType, MessageStructureDTO messageStructure) {
        this.flowStructureId = flowStructureId;
        this.messageType = messageType;
        this.messageStructure = messageStructure;
    }

    // Getters
    public String getFlowStructureId() { return flowStructureId; }
    public MessageType getMessageType() { return messageType; }
    public MessageStructureDTO getMessageStructure() { return messageStructure; }

    // Setters
    public void setFlowStructureId(String flowStructureId) { this.flowStructureId = flowStructureId; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public void setMessageStructure(MessageStructureDTO messageStructure) { this.messageStructure = messageStructure; }

    // Builder
    public static FlowStructureMessagePayloadDTOBuilder builder() {
        return new FlowStructureMessagePayloadDTOBuilder();
    }

    public static class FlowStructureMessagePayloadDTOBuilder {
        private String flowStructureId;
        private MessageType messageType;
        private MessageStructureDTO messageStructure;

        public FlowStructureMessagePayloadDTOBuilder flowStructureId(String flowStructureId) {
            this.flowStructureId = flowStructureId;
            return this;
        }

        public FlowStructureMessagePayloadDTOBuilder messageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public FlowStructureMessagePayloadDTOBuilder messageStructure(MessageStructureDTO messageStructure) {
            this.messageStructure = messageStructure;
            return this;
        }

        public FlowStructureMessagePayloadDTO build() {
            return new FlowStructureMessagePayloadDTO(flowStructureId, messageType, messageStructure);
        }
    }

    /**
     * Message type enum
     */
    public enum MessageType {
        REQUEST,
        RESPONSE,
        FAULT
    }
}
