package com.integrixs.adapters.factory;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import com.integrixs.shared.exceptions.AdapterException;

/**
 * Manager for adapter factories that supports multiple factory implementations
 * and provides a unified interface for adapter creation.
 */
public class AdapterFactoryManager {

    private static final Logger logger = LoggerFactory.getLogger(AdapterFactoryManager.class);

    private final Set<AdapterFactory> factories = new CopyOnWriteArraySet<>();
    private final Map<String, AdapterFactory> factoryByName = new ConcurrentHashMap<>();
    private AdapterFactory defaultFactory;

    // Singleton instance
    private static final AdapterFactoryManager INSTANCE = new AdapterFactoryManager();

    private AdapterFactoryManager() {
        // Register the default factory
        registerFactory(new DefaultAdapterFactory(), true);
    }

    public static AdapterFactoryManager getInstance() {
        return INSTANCE;
    }

    /**
     * Register an adapter factory.
     *
     * @param factory the factory to register
     * @param setAsDefault whether to set this as the default factory
     */
    public void registerFactory(AdapterFactory factory, boolean setAsDefault) {
        if(factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }

        factories.add(factory);
        factoryByName.put(factory.getFactoryName(), factory);

        if(setAsDefault) {
            this.defaultFactory = factory;
        }

        logger.info("Registered adapter factory: {} (default: {})",
                factory.getFactoryName(), setAsDefault);
    }

    /**
     * Unregister an adapter factory.
     *
     * @param factoryName the name of the factory to unregister
     */
    public void unregisterFactory(String factoryName) {
        AdapterFactory factory = factoryByName.remove(factoryName);
        if(factory != null) {
            factories.remove(factory);

            // If this was the default factory, set a new default
            if(factory == defaultFactory) {
                defaultFactory = factories.isEmpty() ? null : factories.iterator().next();
            }

            logger.info("Unregistered adapter factory: {}", factoryName);
        }
    }

    /**
     * Create a inbound adapter using the default factory.
     *
     * @param adapterType the adapter type
     * @param configuration the configuration object
     * @return configured inbound adapter
     * @throws AdapterException if creation fails
     */
    public InboundAdapterPort createSender(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException {
        return createSender(adapterType, configuration, null);
    }

    /**
     * Create a inbound adapter using a specific factory.
     *
     * @param adapterType the adapter type
     * @param configuration the configuration object
     * @param factoryName the specific factory to use(null for default)
     * @return configured inbound adapter
     * @throws AdapterException if creation fails
     */
    public InboundAdapterPort createSender(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration, String factoryName)
            throws AdapterException {

        AdapterFactory factory = getFactory(factoryName, adapterType, AdapterConfiguration.AdapterModeEnum.INBOUND);
        return factory.createInboundAdapter(adapterType, configuration);
    }

    /**
     * Create a outbound adapter using the default factory.
     *
     * @param adapterType the adapter type
     * @param configuration the configuration object
     * @return configured outbound adapter
     * @throws AdapterException if creation fails
     */
    public OutboundAdapterPort createReceiver(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration) throws AdapterException {
        return createReceiver(adapterType, configuration, null);
    }

    /**
     * Create a outbound adapter using a specific factory.
     *
     * @param adapterType the adapter type
     * @param configuration the configuration object
     * @param factoryName the specific factory to use(null for default)
     * @return configured outbound adapter
     * @throws AdapterException if creation fails
     */
    public OutboundAdapterPort createReceiver(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration, String factoryName)
            throws AdapterException {

        AdapterFactory factory = getFactory(factoryName, adapterType, AdapterConfiguration.AdapterModeEnum.OUTBOUND);
        return factory.createOutboundAdapter(adapterType, configuration);
    }

    /**
     * Create and initialize an adapter.
     *
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @param configuration the configuration object
     * @return initialized adapter
     * @throws AdapterException if creation or initialization fails
     */
    public Object createAndInitialize(AdapterConfiguration.AdapterTypeEnum adapterType, AdapterConfiguration.AdapterModeEnum adapterMode, Object configuration)
            throws AdapterException {

        Object adapter;

        if(adapterMode == AdapterConfiguration.AdapterModeEnum.INBOUND) {
            adapter = createSender(adapterType, configuration);
        } else {
            adapter = createReceiver(adapterType, configuration);
        }

        // Note: Domain ports don't have initialize() method
        // Initialization should be handled by the implementation
        return adapter;
    }

    /**
     * Get a factory instance.
     *
     * @param factoryName the factory name(null for default)
     * @param adapterType the adapter type(for validation)
     * @param adapterMode the adapter mode(for validation)
     * @return the factory instance
     * @throws AdapterException if factory not found or doesn't support the adapter
     */
    private AdapterFactory getFactory(String factoryName, AdapterConfiguration.AdapterTypeEnum adapterType, AdapterConfiguration.AdapterModeEnum adapterMode)
            throws AdapterException {

        AdapterFactory factory;

        if(factoryName == null) {
            factory = defaultFactory;
            if(factory == null) {
                throw new AdapterException("No default factory available", null);
            }
        } else {
            factory = factoryByName.get(factoryName);
            if(factory == null) {
                throw new AdapterException(
                    String.format("Factory '%s' not found", factoryName));
            }
            if(!factory.supports(adapterType, adapterMode)) {
                throw new AdapterException(
                    String.format("Factory %s does not support %s %s",
                            factory.getFactoryName(), adapterType, adapterMode));
            }
        }

        return factory;
    }

    /**
     * Get all registered factory names.
     *
     * @return set of factory names
     */
    public Set<String> getRegisteredFactoryNames() {
        return Set.copyOf(factoryByName.keySet());
    }

    /**
     * Get the default factory name.
     *
     * @return default factory name or null if none set
     */
    public String getDefaultFactoryName() {
        return defaultFactory != null ? defaultFactory.getFactoryName() : null;
    }

    /**
     * Check if a factory supports the given adapter type and mode.
     *
     * @param factoryName the factory name
     * @param adapterType the adapter type
     * @param adapterMode the adapter mode
     * @return true if supported, false otherwise
     */
    public boolean isSupported(String factoryName, AdapterConfiguration.AdapterTypeEnum adapterType, AdapterConfiguration.AdapterModeEnum adapterMode) {
        AdapterFactory factory = factoryByName.get(factoryName);
        return factory != null && factory.supports(adapterType, adapterMode);
    }
}
