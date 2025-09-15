package com.integrixs.adapters.factory;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.exceptions.AdapterException;

/**
 * Factory interface for creating adapter instances.
 * Different factories can be implemented for different configuration sources.
 */
public interface AdapterFactory {

    /**
     * Create an inbound adapter instance.
     *
     * @param adapterType the type of adapter to create
     * @param configuration the adapter configuration object
     * @return configured inbound adapter instance
     * @throws AdapterException if adapter creation fails
     */
    InboundAdapterPort createInboundAdapter(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException;

    /**
     * Create an outbound adapter instance.
     *
     * @param adapterType the type of adapter to create
     * @param configuration the adapter configuration object
     * @return configured outbound adapter instance
     * @throws AdapterException if adapter creation fails
     */
    OutboundAdapterPort createOutboundAdapter(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException;

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
