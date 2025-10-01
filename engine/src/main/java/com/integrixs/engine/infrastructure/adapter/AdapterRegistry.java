package com.integrixs.engine.infrastructure.adapter;

import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for managing and tracking adapters
 */
@Component
public class AdapterRegistry {

    private static final Logger log = LoggerFactory.getLogger(AdapterRegistry.class);


    private final CommunicationAdapterSqlRepository communicationAdapterRepository;

    // Cache of registered adapters
    private final Map<String, CommunicationAdapter> adapterCache = new ConcurrentHashMap<>();

    /**
     * Check if an adapter is registered
     * @param adapterId Adapter ID
     * @return true if registered
     */
    public AdapterRegistry(CommunicationAdapterSqlRepository communicationAdapterRepository) {
        this.communicationAdapterRepository = communicationAdapterRepository;
    }

    public boolean isAdapterRegistered(String adapterId) {
        try {
            // Check cache first
            if(adapterCache.containsKey(adapterId)) {
                return true;
            }

            // Check database
            boolean exists = communicationAdapterRepository.existsById(UUID.fromString(adapterId));

            // If found in database, cache it
            if(exists) {
                communicationAdapterRepository.findById(UUID.fromString(adapterId))
                        .ifPresent(adapter -> adapterCache.put(adapterId, adapter));
            }

            return exists;
        } catch(Exception e) {
            log.error("Error checking adapter registration for {}: {}", adapterId, e.getMessage());
            return false;
        }
    }

    /**
     * Register an adapter
     * @param adapter Communication adapter
     */
    public void registerAdapter(CommunicationAdapter adapter) {
        if(adapter != null && adapter.getId() != null) {
            adapterCache.put(adapter.getId().toString(), adapter);
            log.info("Registered adapter: {} of type {}", adapter.getId(), adapter.getType());
        }
    }

    /**
     * Unregister an adapter
     * @param adapterId Adapter ID
     */
    public void unregisterAdapter(String adapterId) {
        adapterCache.remove(adapterId);
        log.info("Unregistered adapter: {}", adapterId);
    }

    /**
     * Get adapter from registry
     * @param adapterId Adapter ID
     * @return Communication adapter or null
     */
    public CommunicationAdapter getAdapter(String adapterId) {
        // Check cache first
        CommunicationAdapter cached = adapterCache.get(adapterId);
        if(cached != null) {
            return cached;
        }

        // Load from database and cache
        try {
            return communicationAdapterRepository.findById(UUID.fromString(adapterId))
                    .map(adapter -> {
                        adapterCache.put(adapterId, adapter);
                        return adapter;
                    })
                    .orElse(null);
        } catch(Exception e) {
            log.error("Error getting adapter {}: {}", adapterId, e.getMessage());
            return null;
        }
    }

    /**
     * Clear the adapter cache
     */
    public void clearCache() {
        adapterCache.clear();
        log.info("Cleared adapter cache");
    }

    /**
     * Refresh adapter from database
     * @param adapterId Adapter ID
     */
    public void refreshAdapter(String adapterId) {
        adapterCache.remove(adapterId);
        getAdapter(adapterId); // This will reload and cache
    }
}
