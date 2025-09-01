package com.integrixs.adapters.factory;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.domain.port.SenderAdapterPort;
import com.integrixs.adapters.domain.port.ReceiverAdapterPort;
import com.integrixs.adapters.domain.model.AdapterConfiguration;

/**
 * Factory interface for creating adapter instances.
 * Different factories can be implemented for different configuration sources.
 */
public interface AdapterFactory {
    
    /**
     * Create a sender adapter instance.
     * 
     * @param adapterType the type of adapter to create
     * @param configuration the adapter configuration object
     * @return configured sender adapter instance
     * @throws AdapterException if adapter creation fails
     */
    SenderAdapterPort createSender(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException;
    
    /**
     * Create a receiver adapter instance.
     * 
     * @param adapterType the type of adapter to create
     * @param configuration the adapter configuration object
     * @return configured receiver adapter instance
     * @throws AdapterException if adapter creation fails
     */
    ReceiverAdapterPort createReceiver(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException;
    
    /**
     * Check if the factory supports the given adapter type and mode.
     * 
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @return true if supported, false otherwise
     */
    boolean supports(AdapterConfiguration.AdapterTypeEnum adapterType, AdapterConfiguration.AdapterModeEnum adapterMode);
    
    /**
     * Get the factory name/identifier.
     * 
     * @return factory name
     */
    String getFactoryName();
}