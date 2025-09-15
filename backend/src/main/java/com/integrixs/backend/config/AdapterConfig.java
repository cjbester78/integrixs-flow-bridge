package com.integrixs.backend.config;

import com.integrixs.adapters.factory.AdapterFactoryManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for adapter - related beans
 */
@Configuration
public class AdapterConfig {

    @Bean
    public AdapterFactoryManager adapterFactoryManager() {
        return AdapterFactoryManager.getInstance();
    }
}
