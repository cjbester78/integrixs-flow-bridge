package com.integrixs.backend.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ServerEndpoint("/direct - ws")
@Profile("!test")
public class DirectWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(DirectWebSocketEndpoint.class);


    @OnOpen
    public void onOpen(Session session) {
        log.info("Direct WebSocket opened: {}", session.getId());
        try {
            session.getBasicRemote().sendText("Connected to Direct WebSocket!");
        } catch(Exception e) {
            log.error("Error sending welcome message", e);
        }
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        log.info("Direct WebSocket received: {}", message);
        return "Echo: " + message;
    }

    @OnClose
    public void onClose(Session session) {
        log.info("Direct WebSocket closed: {}", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("Direct WebSocket error", throwable);
    }
}
