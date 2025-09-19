package com.integrixs.backend.config;

import com.integrixs.backend.websocket.FlowExecutionWebSocketHandler;
import com.integrixs.backend.websocket.JobProgressWebSocketHandler;
import com.integrixs.backend.websocket.MessageWebSocketHandler;
import com.integrixs.backend.websocket.SimpleWebSocketHandler;
import com.integrixs.backend.websocket.StreamingProgressWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);


    @Autowired
    private MessageWebSocketHandler messageWebSocketHandler;

    @Autowired(required = false)
    private FlowExecutionWebSocketHandler flowExecutionWebSocketHandler;

    @Autowired(required = false)
    private SimpleWebSocketHandler simpleWebSocketHandler;

    @Autowired(required = false)
    private JobProgressWebSocketHandler jobProgressWebSocketHandler;

    @Autowired(required = false)
    private StreamingProgressWebSocketHandler streamingProgressWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("=== Registering WebSocket handlers ===");

        // Message monitoring WebSocket endpoint - this is the critical one
        registry.addHandler(messageWebSocketHandler, "/ws/messages")
                .setAllowedOrigins("*");
        log.info("Registered /ws/messages WebSocket endpoint");

        // Flow execution monitoring
        if(flowExecutionWebSocketHandler != null) {
            registry.addHandler(flowExecutionWebSocketHandler, "/ws/flow - execution")
                    .setAllowedOrigins("*");
            log.info("Registered /ws/flow - execution WebSocket endpoint");
        }

        // Test endpoint
        if(simpleWebSocketHandler != null) {
            registry.addHandler(simpleWebSocketHandler, "/ws/test")
                    .setAllowedOrigins("*");
            log.info("Registered /ws/test WebSocket endpoint");
        }

        // Job progress monitoring
        if(jobProgressWebSocketHandler != null) {
            registry.addHandler(jobProgressWebSocketHandler, "/ws/job - progress")
                    .setAllowedOrigins("*");
            log.info("Registered /ws/job - progress WebSocket endpoint");
        }

        // Streaming upload progress
        if(streamingProgressWebSocketHandler != null) {
            registry.addHandler(streamingProgressWebSocketHandler, "/ws/streaming - progress")
                    .setAllowedOrigins("*");
            log.info("Registered /ws/streaming - progress WebSocket endpoint");
        }

        log.info("=== WebSocket handler registration complete ===");
    }
}
