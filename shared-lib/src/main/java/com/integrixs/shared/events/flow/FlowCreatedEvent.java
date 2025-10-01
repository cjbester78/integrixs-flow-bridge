package com.integrixs.shared.events.flow;

import com.integrixs.shared.events.AbstractDomainEvent;

/**
 * Event raised when a new integration flow is created.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class FlowCreatedEvent extends AbstractDomainEvent {

    private String flowId;
    private String flowName;
    private String inboundAdapterId;
    private String outboundAdapterId;
    private String createdBy;

    // Default constructor
    public FlowCreatedEvent() {
        super();
    }

    // All args constructor
    public FlowCreatedEvent(String flowId, String flowName, String inboundAdapterId,
                           String outboundAdapterId, String createdBy) {
        super(flowId, createdBy);
        this.flowId = flowId;
        this.flowName = flowName;
        this.inboundAdapterId = inboundAdapterId;
        this.outboundAdapterId = outboundAdapterId;
        this.createdBy = createdBy;
    }

    // Getters
    public String getFlowId() {
        return flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public String getInboundAdapterId() {
        return inboundAdapterId;
    }

    public String getOutboundAdapterId() {
        return outboundAdapterId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    // Setters
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public void setInboundAdapterId(String inboundAdapterId) {
        this.inboundAdapterId = inboundAdapterId;
    }

    public void setOutboundAdapterId(String outboundAdapterId) {
        this.outboundAdapterId = outboundAdapterId;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
