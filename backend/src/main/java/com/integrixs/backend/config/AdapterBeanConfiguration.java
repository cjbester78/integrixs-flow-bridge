package com.integrixs.backend.config;

import com.integrixs.adapters.config.HttpInboundAdapterConfig;
import com.integrixs.adapters.factory.AdapterFactory;
import com.integrixs.adapters.factory.DefaultAdapterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration for Adapter Beans
 * Provides bean definitions for adapter-related components
 */
@Configuration
public class AdapterBeanConfiguration {

    /**
     * Provides the default AdapterFactory bean
     */
    @Bean
    public AdapterFactory adapterFactory() {
        return new DefaultAdapterFactory();
    }

    /**
     * Provides a default HttpInboundAdapterConfig bean for the HTTP adapter controller
     */
    @Bean
    public HttpInboundAdapterConfig httpInboundAdapterConfig() {
        HttpInboundAdapterConfig config = new HttpInboundAdapterConfig();
        // Set default values for development
        config.setEndpointUrl("/api/http - adapter/receive");
        config.setConnectionTimeout(30);
        config.setReadTimeout(60);
        config.setValidateIncomingData(true);
        return config;
    }
}
