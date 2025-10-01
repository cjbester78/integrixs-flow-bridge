package com.integrixs.webserver.domain.repository;

import com.integrixs.webserver.domain.model.ServiceEndpoint;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for service endpoints
 */
public interface ServiceEndpointRepository {

    /**
     * Save service endpoint
     * @param endpoint Endpoint to save
     * @return Saved endpoint
     */
    ServiceEndpoint save(ServiceEndpoint endpoint);

    /**
     * Find endpoint by ID
     * @param endpointId Endpoint ID
     * @return Endpoint if found
     */
    Optional<ServiceEndpoint> findById(String endpointId);

    /**
     * Find endpoint by name
     * @param name Endpoint name
     * @return Endpoint if found
     */
    Optional<ServiceEndpoint> findByName(String name);

    /**
     * Find all endpoints
     * @return List of all endpoints
     */
    List<ServiceEndpoint> findAll();

    /**
     * Find active endpoints
     * @return List of active endpoints
     */
    List<ServiceEndpoint> findByActive(boolean active);

    /**
     * Find endpoints by type
     * @param type Endpoint type
     * @return List of endpoints
     */
    List<ServiceEndpoint> findByType(ServiceEndpoint.ServiceType type);

    /**
     * Update endpoint
     * @param endpoint Endpoint to update
     * @return Updated endpoint
     */
    ServiceEndpoint update(ServiceEndpoint endpoint);

    /**
     * Delete endpoint
     * @param endpointId Endpoint ID
     */
    void deleteById(String endpointId);

    /**
     * Check if endpoint exists
     * @param endpointId Endpoint ID
     * @return true if exists
     */
    boolean existsById(String endpointId);

    /**
     * Check if endpoint name exists
     * @param name Endpoint name
     * @return true if exists
     */
    boolean existsByName(String name);
}
