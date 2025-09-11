package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.factory.AdapterFactory;
import com.integrixs.adapters.core.BaseAdapter;
import com.integrixs.shared.enums.AdapterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Factory for creating Facebook adapters
 */
@Component
public class FacebookAdapterFactory implements AdapterFactory {
    private static final Logger log = LoggerFactory.getLogger(FacebookAdapterFactory.class);

    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private FacebookGraphInboundAdapter inboundAdapter;
    
    @Autowired
    private FacebookGraphOutboundAdapter outboundAdapter;
    
    @PostConstruct
    public void init() {
        log.info("Initializing Facebook Adapter Factory");
    }
    
    @Override
    public boolean supports(AdapterType type, String subType) {
        // Check if this is a Facebook adapter request
        return AdapterType.REST.equals(type) && 
               (subType != null && subType.toLowerCase().contains("facebook"));
    }
    
    @Override
    public BaseAdapter createInboundAdapter() {
        log.debug("Creating Facebook Graph API Inbound Adapter");
        return inboundAdapter;
    }
    
    @Override
    public BaseAdapter createOutboundAdapter() {
        log.debug("Creating Facebook Graph API Outbound Adapter");
        return outboundAdapter;
    }
    
    @Override
    public Class<?> getConfigurationClass() {
        return FacebookGraphApiConfig.class;
    }
    
    @Override
    public String getAdapterName() {
        return "Facebook Graph API";
    }
    
    @Override
    public String getAdapterDescription() {
        return "Facebook Graph API adapter for social media integration";
    }
    
    /**
     * Create adapter with specific configuration
     */
    public FacebookGraphInboundAdapter createInboundAdapter(FacebookGraphApiConfig config) {
        FacebookGraphInboundAdapter adapter = applicationContext.getBean(FacebookGraphInboundAdapter.class);
        // Configure adapter with provided config
        return adapter;
    }
    
    /**
     * Create outbound adapter with specific configuration
     */
    public FacebookGraphOutboundAdapter createOutboundAdapter(FacebookGraphApiConfig config) {
        FacebookGraphOutboundAdapter adapter = applicationContext.getBean(FacebookGraphOutboundAdapter.class);
        // Configure adapter with provided config
        return adapter;
    }
}