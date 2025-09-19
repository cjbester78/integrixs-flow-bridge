package com.integrixs.backend.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TestWebSocketController extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(TestWebSocketController.class);


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Test WebSocket connection established: {}", session.getId());
        session.sendMessage(new TextMessage("Connected to test WebSocket!"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Test WebSocket received: {}", message.getPayload());
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Test WebSocket connection closed: {}", session.getId());
    }
}
