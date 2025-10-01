package com.integrixs.backend.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * WebSocket configuration
 */
@Configuration("websocketHandlerConfig")
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(jobProgressWebSocketHandler(), "/ws/jobs")
            .setAllowedOrigins("*") // In production, configure specific origins
            .addInterceptors(httpSessionHandshakeInterceptor());

        registry.addHandler(flowExecutionWebSocketHandler(), "/ws/flows")
            .setAllowedOrigins("*")
            .addInterceptors(httpSessionHandshakeInterceptor());
    }

    @Bean
    public JobProgressWebSocketHandler jobProgressWebSocketHandler() {
        return new JobProgressWebSocketHandler();
    }

    @Bean
    public FlowExecutionWebSocketHandler flowExecutionWebSocketHandler() {
        return new FlowExecutionWebSocketHandler();
    }

    @Bean
    public HandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor();
    }
}
