package com.integrixs.webclient.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for WebClient module
 */
@Configuration
@ComponentScan(basePackages = {
    "com.integrixs.webclient.api",
    "com.integrixs.webclient.application",
    "com.integrixs.webclient.domain",
    "com.integrixs.webclient.infrastructure"
})
public class WebClientConfiguration {

    // Additional bean configurations can be added here if needed
}
