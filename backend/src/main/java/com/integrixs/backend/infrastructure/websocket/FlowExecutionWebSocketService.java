package com.integrixs.backend.infrastructure.websocket;

import com.integrixs.backend.websocket.FlowExecutionWebSocketHandler;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure service for WebSocket notifications
 */
@Service
public class FlowExecutionWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionWebSocketService.class);


    private final FlowExecutionWebSocketHandler webSocketHandler;

    public FlowExecutionWebSocketService(FlowExecutionWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * Broadcast execution started event
     */
    public void broadcastExecutionStarted(String flowId, String executionId) {
        try {
            webSocketHandler.broadcastFlowExecutionStarted(flowId, executionId);
        } catch(Exception e) {
            log.error("Failed to broadcast execution started event", e);
        }
    }

    /**
     * Broadcast execution progress update
     */
    public void broadcastExecutionProgress(String flowId, String executionId, String step, String message) {
        try {
            webSocketHandler.broadcastFlowExecutionProgress(flowId, executionId, step, message);
        } catch(Exception e) {
            log.error("Failed to broadcast execution progress event", e);
        }
    }

    /**
     * Broadcast execution completed event
     */
    public void broadcastExecutionCompleted(String flowId, String executionId, boolean success) {
        try {
            webSocketHandler.broadcastFlowExecutionCompleted(flowId, executionId, success);
        } catch(Exception e) {
            log.error("Failed to broadcast execution completed event", e);
        }
    }

    /**
     * Broadcast execution error event
     */
    public void broadcastExecutionError(String flowId, String executionId, String errorMessage) {
        try {
            webSocketHandler.broadcastFlowExecutionError(flowId, executionId, errorMessage);
        } catch(Exception e) {
            log.error("Failed to broadcast execution error event", e);
        }
    }

    /**
     * Broadcast execution cancelled event
     */
    public void broadcastExecutionCancelled(String flowId, String executionId) {
        try {
            webSocketHandler.broadcastFlowExecutionCancelled(flowId, executionId);
        } catch(Exception e) {
            log.error("Failed to broadcast execution cancelled event", e);
        }
    }
}
