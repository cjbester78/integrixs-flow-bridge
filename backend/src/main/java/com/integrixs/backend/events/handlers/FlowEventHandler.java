package com.integrixs.backend.events.handlers;

import com.integrixs.monitoring.service.SystemLogService;
import com.integrixs.shared.events.flow.FlowCreatedEvent;
import com.integrixs.shared.events.flow.FlowExecutedEvent;
import com.integrixs.shared.events.flow.FlowStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handler for flow - related domain events.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@Component
public class FlowEventHandler {

    private static final Logger log = LoggerFactory.getLogger(FlowEventHandler.class);


    private final SystemLogService systemLogService;

    public FlowEventHandler(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    /**
     * Handles flow created events.
     *
     * @param event the flow created event
     */
    @EventListener
    @Async
    public void handleFlowCreated(FlowCreatedEvent event) {
        log.info("Flow created: {} by user: {}", event.getFlowName(), event.getCreatedBy());

        systemLogService.logFlowActivity(
            "CREATE",
            String.format("Integration flow '%s' created", event.getFlowName()),
            event.getFlowId(),
            event.getCreatedBy(),
            "FlowEventHandler"
       );
    }

    /**
     * Handles flow executed events.
     *
     * <p>Uses TransactionalEventListener to ensure event is processed
     * after the transaction commits successfully.
     *
     * @param event the flow executed event
     */
    @Async
    public void handleFlowExecuted(FlowExecutedEvent event) {
        String message = event.isSuccess()
            ? String.format("Flow executed successfully in %d ms", event.getExecutionDuration().toMillis())
            : String.format("Flow execution failed: %s", event.getErrorMessage());

        log.info("Flow execution: {} - {}", event.getFlowId(), message);

        systemLogService.logFlowExecution(
            event.getFlowId(),
            event.getExecutionId(),
            event.isSuccess() ? "SUCCESS" : "FAILED",
            event.getRecordsProcessed(),
            event.getExecutionDuration().toMillis(),
            event.getErrorMessage(),
            event.getTriggeredBy(),
            "FlowEventHandler"
       );
    }

    /**
     * Handles flow status changed events.
     *
     * @param event the flow status changed event
     */
    @EventListener
    @Async
    public void handleFlowStatusChanged(FlowStatusChangedEvent event) {
        log.info("Flow status changed: {} from {} to {} - Reason: {}",
                event.getFlowId(), event.getOldStatus(), event.getNewStatus(), event.getReason());

        String activity = event.isActivation() ? "ACTIVATE"
                       : event.isDeactivation() ? "DEACTIVATE"
                       : "STATUS_CHANGE";

        systemLogService.logFlowActivity(
            activity,
            String.format("Flow status changed from %s to %s: %s",
                         event.getOldStatus(), event.getNewStatus(), event.getReason()),
            event.getFlowId(),
            event.getChangedBy(),
            "FlowEventHandler"
       );

        // Additional actions based on status change
        if(event.isActivation()) {
            handleFlowActivation(event);
        } else if(event.isDeactivation()) {
            handleFlowDeactivation(event);
        }
    }

    /**
     * Handles flow activation.
     *
     * @param event the status changed event
     */
    private void handleFlowActivation(FlowStatusChangedEvent event) {
        log.debug("Performing flow activation tasks for flow: {}", event.getFlowId());
        // Initialize resources, start monitoring, etc.
    }

    /**
     * Handles flow deactivation.
     *
     * @param event the status changed event
     */
    private void handleFlowDeactivation(FlowStatusChangedEvent event) {
        log.debug("Performing flow deactivation tasks for flow: {}", event.getFlowId());
        // Clean up resources, stop monitoring, etc.
    }
}
