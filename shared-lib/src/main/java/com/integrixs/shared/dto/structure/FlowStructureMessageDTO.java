package com.integrixs.shared.dto.structure;

public class FlowStructureMessageDTO {

    private String flowStructureId;
    private MessageType messageType;
    private MessageStructureDTO messageStructure;

    // Default constructor
    public FlowStructureMessageDTO() {
    }

    // All args constructor
    public FlowStructureMessageDTO(String flowStructureId, MessageType messageType, MessageStructureDTO messageStructure) {
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
    public static FlowStructureMessageDTOBuilder builder() {
        return new FlowStructureMessageDTOBuilder();
    }

    public static class FlowStructureMessageDTOBuilder {
        private String flowStructureId;
        private MessageType messageType;
        private MessageStructureDTO messageStructure;

        public FlowStructureMessageDTOBuilder flowStructureId(String flowStructureId) {
            this.flowStructureId = flowStructureId;
            return this;
        }

        public FlowStructureMessageDTOBuilder messageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public FlowStructureMessageDTOBuilder messageStructure(MessageStructureDTO messageStructure) {
            this.messageStructure = messageStructure;
            return this;
        }

        public FlowStructureMessageDTO build() {
            return new FlowStructureMessageDTO(flowStructureId, messageType, messageStructure);
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
