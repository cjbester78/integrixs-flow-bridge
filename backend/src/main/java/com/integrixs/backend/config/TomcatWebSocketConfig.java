package com.integrixs.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@ConditionalOnWebApplication
@Profile("!test")
public class TomcatWebSocketConfig {

    private static final Logger log = LoggerFactory.getLogger(TomcatWebSocketConfig.class);


    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        container.setMaxSessionIdleTimeout(60000L);
        log.info("Creating ServletServerContainerFactoryBean for WebSocket support");
        return container;
    }

    // Removed ServerEndpointExporter - it conflicts with Spring's WebSocket support
}
