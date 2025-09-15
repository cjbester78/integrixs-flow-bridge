package com.integrixs.backend.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for streaming upload progress
 */
@Component
public class StreamingProgressWebSocketHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(StreamingProgressWebSocketHandler.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToStreamingId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);

        // Send welcome message
        TextMessage message = new TextMessage(" {\"type\":\"connected\",\"message\":\"Streaming progress connection established\"}");
        session.sendMessage(message);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if(message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            logger.debug("Received message: {}", payload);

            // Handle subscription to streaming session
            if(payload.contains("\"action\":\"subscribe\"") && payload.contains("\"streamingId\":")) {
                String streamingId = extractStreamingId(payload);
                if(streamingId != null) {
                    sessionToStreamingId.put(session.getId(), streamingId);
                    session.sendMessage(new TextMessage(
                        " {\"type\":\"subscribed\",\"streamingId\":\"" + streamingId + "\"}"
                   ));
                }
            } else if(payload.contains("\"action\":\"unsubscribe\"")) {
                sessionToStreamingId.remove(session.getId());
                session.sendMessage(new TextMessage(" {\"type\":\"unsubscribed\"}"));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session {}: {}",
            session.getId(), exception.getMessage());
        cleanup(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("WebSocket connection closed: {} - {}",
            session.getId(), closeStatus.toString());
        cleanup(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Send progress update to all subscribed sessions
     */
    public void sendProgressUpdate(String streamingId, String progressJson) {
        sessionToStreamingId.forEach((sessionId, subscribedId) -> {
            if(subscribedId.equals(streamingId)) {
                WebSocketSession session = sessions.get(sessionId);
                if(session != null && session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(progressJson));
                    } catch(IOException e) {
                        logger.error("Failed to send progress to session {}: {}",
                            sessionId, e.getMessage());
                        cleanup(session);
                    }
                }
            }
        });
    }

    /**
     * Broadcast message to all connected sessions
     */
    public void broadcast(String message) {
        TextMessage textMessage = new TextMessage(message);
        sessions.values().parallelStream().forEach(session -> {
            if(session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch(IOException e) {
                    logger.error("Failed to broadcast to session {}: {}",
                        session.getId(), e.getMessage());
                }
            }
        });
    }

    /**
     * Clean up session resources
     */
    private void cleanup(WebSocketSession session) {
        sessions.remove(session.getId());
        sessionToStreamingId.remove(session.getId());

        try {
            if(session.isOpen()) {
                session.close();
            }
        } catch(IOException e) {
            logger.error("Error closing session: {}", e.getMessage());
        }
    }

    /**
     * Extract streaming ID from JSON message
     */
    private String extractStreamingId(String json) {
        try {
            int start = json.indexOf("\"streamingId\":\"") + 15;
            int end = json.indexOf("\"", start);
            if(start > 14 && end > start) {
                return json.substring(start, end);
            }
        } catch(Exception e) {
            logger.error("Failed to extract streaming ID", e);
        }
        return null;
    }

    /**
     * Get active session count
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Check if a session is subscribed to a streaming ID
     */
    public boolean isSubscribed(String sessionId, String streamingId) {
        String subscribedId = sessionToStreamingId.get(sessionId);
        return subscribedId != null && subscribedId.equals(streamingId);
    }
}
