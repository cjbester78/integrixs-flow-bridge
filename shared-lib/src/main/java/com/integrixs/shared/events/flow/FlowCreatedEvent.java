package com.integrixs.shared.events.flow;

import com.integrixs.shared.events.AbstractDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event raised when a new integration flow is created.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlowCreatedEvent extends AbstractDomainEvent {

    private String flowId;
    private String flowName;
    private String inboundAdapterId;
    private String outboundAdapterId;
    private String createdBy;

    public FlowCreatedEvent(String flowId, String flowName, String inboundAdapterId,
                           String outboundAdapterId, String createdBy) {
        super(flowId, createdBy);
        this.flowId = flowId;
        this.flowName = flowName;
        this.inboundAdapterId = inboundAdapterId;
        this.outboundAdapterId = outboundAdapterId;
        this.createdBy = createdBy;
    }
}
