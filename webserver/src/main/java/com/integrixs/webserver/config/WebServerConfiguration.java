package com.integrixs.webserver.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for WebServer module
 */
@Configuration
@ComponentScan(basePackages = {
    "com.integrixs.webserver.api",
    "com.integrixs.webserver.application",
    "com.integrixs.webserver.domain",
    "com.integrixs.webserver.infrastructure"
})
public class WebServerConfiguration {

    // Additional bean configurations can be added here if needed
}
