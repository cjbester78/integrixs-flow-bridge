package com.integrixs.webserver.domain.service;

import com.integrixs.webserver.domain.model.ServiceEndpoint;

import java.util.List;

/**
 * Domain service interface for managing service endpoints
 */
public interface ServiceEndpointService {

    /**
     * Register a new service endpoint
     * @param endpoint The endpoint to register
     * @return Registered endpoint
     */
    ServiceEndpoint registerEndpoint(ServiceEndpoint endpoint);

    /**
     * Update service endpoint
     * @param endpoint Updated endpoint
     * @return Updated endpoint
     */
    ServiceEndpoint updateEndpoint(ServiceEndpoint endpoint);

    /**
     * Get service endpoint by ID
     * @param endpointId Endpoint ID
     * @return Service endpoint
     */
    ServiceEndpoint getEndpoint(String endpointId);

    /**
     * Get service endpoint by name
     * @param name Endpoint name
     * @return Service endpoint
     */
    ServiceEndpoint getEndpointByName(String name);

    /**
     * Get all active endpoints
     * @return List of active endpoints
     */
    List<ServiceEndpoint> getActiveEndpoints();

    /**
     * Get endpoints by type
     * @param type Endpoint type
     * @return List of endpoints
     */
    List<ServiceEndpoint> getEndpointsByType(ServiceEndpoint.ServiceType type);

    /**
     * Test endpoint connectivity
     * @param endpointId Endpoint ID
     * @return true if endpoint is reachable
     */
    boolean testEndpointConnectivity(String endpointId);

    /**
     * Activate endpoint
     * @param endpointId Endpoint ID
     */
    void activateEndpoint(String endpointId);

    /**
     * Deactivate endpoint
     * @param endpointId Endpoint ID
     */
    void deactivateEndpoint(String endpointId);

    /**
     * Delete endpoint
     * @param endpointId Endpoint ID
     */
    void deleteEndpoint(String endpointId);

    /**
     * Validate endpoint configuration
     * @param endpoint Endpoint to validate
     * @return true if valid
     */
    boolean validateEndpointConfiguration(ServiceEndpoint endpoint);
}
