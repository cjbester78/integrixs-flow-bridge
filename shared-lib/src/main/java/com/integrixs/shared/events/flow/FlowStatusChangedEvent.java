package com.integrixs.shared.events.flow;

import com.integrixs.shared.events.AbstractDomainEvent;

/**
 * Event raised when an integration flow status changes.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class FlowStatusChangedEvent extends AbstractDomainEvent {

    private String flowId;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private String changedBy;

    // Default constructor
    public FlowStatusChangedEvent() {
        super();
    }

    // All args constructor
    public FlowStatusChangedEvent(String flowId, String oldStatus, String newStatus,
                                 String reason, String changedBy) {
        super(flowId, changedBy);
        this.flowId = flowId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedBy = changedBy;
    }

    /**
     * Checks if the flow was activated.
     *
     * @return true if flow changed to active status
     */
    public boolean isActivation() {
        return "ACTIVE".equals(newStatus) && !"ACTIVE".equals(oldStatus);
    }

    /**
     * Checks if the flow was deactivated.
     *
     * @return true if flow changed from active status
     */
    public boolean isDeactivation() {
        return "ACTIVE".equals(oldStatus) && !"ACTIVE".equals(newStatus);
    }

    // Getters
    public String getFlowId() {
        return flowId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getReason() {
        return reason;
    }

    public String getChangedBy() {
        return changedBy;
    }

    // Setters
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
}
