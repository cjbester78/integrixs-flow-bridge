package com.integrixs.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.security.JwtUtil;
import com.integrixs.backend.service.MessageStatsService;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.dto.MessageStatsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageWebSocketHandler.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> sessionFilters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MessageStatsService messageStatsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);

        // Extract query parameters as initial filters
        Map<String, String> filters = new HashMap<>();
        if(session.getUri() != null && session.getUri().getQuery() != null) {
            String[] params = session.getUri().getQuery().split("&");
            for(String param : params) {
                String[] keyValue = param.split(" = ");
                if(keyValue.length == 2) {
                    filters.put(keyValue[0], keyValue[1]);
                }
            }
        }
        sessionFilters.put(session.getId(), filters);

        // Send initial stats with filters
        sendMessageStats(session, filters);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {}", session.getId());
        sessions.remove(session.getId());
        sessionFilters.remove(session.getId());
        sessionToUser.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Received WebSocket message: {}", message.getPayload());

        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String command = (String) payload.get("command");

            if("get_stats".equals(command)) {
                Map<String, String> filters = sessionFilters.getOrDefault(session.getId(), new HashMap<>());
                sendMessageStats(session, filters);
            } else if("update_filters".equals(command)) {
                // Update session filters
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                if(data != null) {
                    Map<String, String> newFilters = new HashMap<>();
                    data.forEach((key, value) -> {
                        if(value != null) {
                            newFilters.put(key, value.toString());
                        }
                    });
                    sessionFilters.put(session.getId(), newFilters);
                    // Send updated stats with new filters
                    sendMessageStats(session, newFilters);
                }
            }
        } catch(Exception e) {
            logger.error("Error handling WebSocket message", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session: " + session.getId(), exception);
        sessions.remove(session.getId());
    }

    /**
     * Broadcast updates to all connected sessions with their specific filters
     */
    @Scheduled(fixedDelay = 5000) // Every 5 seconds
    public void broadcastUpdates() {
        if(sessions.isEmpty()) {
            return;
        }

        // Send filtered stats to each session
        sessions.forEach((sessionId, session) -> {
            if(session.isOpen()) {
                Map<String, String> filters = sessionFilters.getOrDefault(sessionId, new HashMap<>());
                try {
                    MessageStatsDTO stats = messageStatsService.getMessageStats(filters);

                    Map<String, Object> update = new HashMap<>();
                    update.put("type", "stats_update");
                    update.put("stats", stats);
                    update.put("filters", filters);

                    String jsonMessage = objectMapper.writeValueAsString(update);
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch(Exception e) {
                    logger.error("Error sending filtered stats to session: " + sessionId, e);
                }
            }
        });
    }

    /**
     * Send message stats to a specific session with filters
     */
    private void sendMessageStats(WebSocketSession session, Map<String, String> filters) {
        try {
            MessageStatsDTO stats = messageStatsService.getMessageStats(filters);

            Map<String, Object> response = new HashMap<>();
            response.put("type", "stats_update");
            response.put("stats", stats);
            response.put("filters", filters);

            String jsonMessage = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch(Exception e) {
            logger.error("Error sending filtered stats to session: " + session.getId(), e);
        }
    }

    /**
     * Send message update notification
     */
    public void sendMessageUpdate(MessageDTO message) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "message_update");
        update.put("message", message);

        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(update);
        } catch(Exception e) {
            logger.error("Error serializing message update", e);
            return;
        }

        sessions.values().forEach(session -> {
            if(session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch(IOException e) {
                    logger.error("Error sending message update to session: " + session.getId(), e);
                }
            }
        });
    }
}
