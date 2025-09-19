package com.integrixs.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.jobs.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for flow execution updates
 */
@Component
public class FlowExecutionWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(FlowExecutionWebSocketHandler.class);

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Flow execution WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Handle incoming messages if needed
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Flow execution WebSocket error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("Flow execution WebSocket connection closed: {}", session.getId());
        sessions.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Send flow execution update to all connected sessions
     */
    public void sendFlowUpdate(UUID flowId, String status, Map<String, Object> details) {
        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", "flowUpdate");
        message.put("flowId", flowId.toString());
        message.put("status", status);
        message.put("timestamp", System.currentTimeMillis());
        message.put("details", details);

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch(Exception e) {
            logger.error("Error serializing flow update", e);
            return;
        }

        TextMessage textMessage = new TextMessage(json);

        sessions.values().parallelStream()
            .filter(WebSocketSession::isOpen)
            .forEach(session -> {
                try {
                    session.sendMessage(textMessage);
                } catch(Exception e) {
                    logger.error("Error sending flow update to session {}", session.getId(), e);
                }
            });
    }

    /**
     * Broadcast flow execution started event
     */
    public void broadcastFlowExecutionStarted(String flowId, String executionId) {
        Map<String, Object> details = new ConcurrentHashMap<>();
        details.put("executionId", executionId);
        sendFlowUpdate(UUID.fromString(flowId), "STARTED", details);
    }

    /**
     * Broadcast flow execution progress event
     */
    public void broadcastFlowExecutionProgress(String flowId, String executionId, String step, String message) {
        Map<String, Object> details = new ConcurrentHashMap<>();
        details.put("executionId", executionId);
        details.put("step", step);
        details.put("message", message);
        sendFlowUpdate(UUID.fromString(flowId), "IN_PROGRESS", details);
    }

    /**
     * Broadcast flow execution completed event
     */
    public void broadcastFlowExecutionCompleted(String flowId, String executionId, boolean success) {
        Map<String, Object> details = new ConcurrentHashMap<>();
        details.put("executionId", executionId);
        details.put("success", success);
        sendFlowUpdate(UUID.fromString(flowId), success ? "COMPLETED" : "FAILED", details);
    }

    /**
     * Broadcast flow execution error event
     */
    public void broadcastFlowExecutionError(String flowId, String executionId, String errorMessage) {
        Map<String, Object> details = new ConcurrentHashMap<>();
        details.put("executionId", executionId);
        details.put("errorMessage", errorMessage);
        sendFlowUpdate(UUID.fromString(flowId), "ERROR", details);
    }

    /**
     * Broadcast flow execution cancelled event
     */
    public void broadcastFlowExecutionCancelled(String flowId, String executionId) {
        Map<String, Object> details = new ConcurrentHashMap<>();
        details.put("executionId", executionId);
        sendFlowUpdate(UUID.fromString(flowId), "CANCELLED", details);
    }
}
