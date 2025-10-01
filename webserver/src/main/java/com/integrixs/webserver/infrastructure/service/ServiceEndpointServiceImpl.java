package com.integrixs.webserver.infrastructure.service;

import com.integrixs.webserver.domain.model.OutboundRequest;
import com.integrixs.webserver.domain.model.OutboundResponse;
import com.integrixs.webserver.domain.model.ServiceEndpoint;
import com.integrixs.webserver.domain.repository.ServiceEndpointRepository;
import com.integrixs.webserver.domain.service.HttpClientService;
import com.integrixs.webserver.domain.service.ServiceEndpointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of service endpoint management
 */
@Service
public class ServiceEndpointServiceImpl implements ServiceEndpointService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceEndpointServiceImpl.class);

    private final ServiceEndpointRepository repository;
    private final HttpClientService httpClientService;

    public ServiceEndpointServiceImpl(ServiceEndpointRepository repository, HttpClientService httpClientService) {
        this.repository = repository;
        this.httpClientService = httpClientService;
    }

    @Override
    public ServiceEndpoint registerEndpoint(ServiceEndpoint endpoint) {
        logger.info("Registering endpoint: {}", endpoint.getName());

        // Generate ID if not provided
        if(endpoint.getEndpointId() == null) {
            endpoint.setEndpointId(UUID.randomUUID().toString());
        }

        // Set default values
        if(endpoint.getConnectionConfig() == null) {
            endpoint.setConnectionConfig(ServiceEndpoint.ConnectionConfig.builder().build());
        }

        // Save endpoint
        return repository.save(endpoint);
    }

    @Override
    public ServiceEndpoint updateEndpoint(ServiceEndpoint endpoint) {
        logger.info("Updating endpoint: {}", endpoint.getEndpointId());

        // Verify endpoint exists
        if(!repository.existsById(endpoint.getEndpointId())) {
            throw new RuntimeException("Endpoint not found: " + endpoint.getEndpointId());
        }

        return repository.update(endpoint);
    }

    @Override
    public ServiceEndpoint getEndpoint(String endpointId) {
        return repository.findById(endpointId)
                .orElseThrow(() -> new RuntimeException("Endpoint not found: " + endpointId));
    }

    @Override
    public ServiceEndpoint getEndpointByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Endpoint not found with name: " + name));
    }

    @Override
    public List<ServiceEndpoint> getActiveEndpoints() {
        return repository.findByActive(true);
    }

    @Override
    public List<ServiceEndpoint> getEndpointsByType(ServiceEndpoint.ServiceType type) {
        return repository.findByType(type);
    }

    @Override
    public boolean testEndpointConnectivity(String endpointId) {
        logger.info("Testing connectivity for endpoint: {}", endpointId);

        ServiceEndpoint endpoint = getEndpoint(endpointId);

        // Create test request
        OutboundRequest testRequest = OutboundRequest.builder()
                .requestType(mapEndpointTypeToRequestType(endpoint.getType()))
                .targetUrl(endpoint.getBaseUrl())
                .httpMethod(OutboundRequest.HttpMethod.GET)
                .timeoutSeconds(5) // Short timeout for connectivity test
                .build();

        try {
            OutboundResponse response = httpClientService.executeRestCall(testRequest);
            return response.getStatusCode() > 0 && response.getStatusCode() < 500;
        } catch(Exception e) {
            logger.error("Connectivity test failed for endpoint {}: {}", endpointId, e.getMessage());
            return false;
        }
    }

    @Override
    public void activateEndpoint(String endpointId) {
        logger.info("Activating endpoint: {}", endpointId);

        ServiceEndpoint endpoint = getEndpoint(endpointId);
        endpoint.setActive(true);
        repository.update(endpoint);
    }

    @Override
    public void deactivateEndpoint(String endpointId) {
        logger.info("Deactivating endpoint: {}", endpointId);

        ServiceEndpoint endpoint = getEndpoint(endpointId);
        endpoint.setActive(false);
        repository.update(endpoint);
    }

    @Override
    public void deleteEndpoint(String endpointId) {
        logger.info("Deleting endpoint: {}", endpointId);
        repository.deleteById(endpointId);
    }

    @Override
    public boolean validateEndpointConfiguration(ServiceEndpoint endpoint) {
        if(endpoint == null) return false;
        if(endpoint.getName() == null || endpoint.getName().isEmpty()) return false;
        if(endpoint.getBaseUrl() == null || endpoint.getBaseUrl().isEmpty()) return false;
        if(endpoint.getType() == null) return false;

        // Validate URL format
        try {
            new java.net.URL(endpoint.getBaseUrl());
        } catch(Exception e) {
            logger.warn("Invalid base URL: {}", endpoint.getBaseUrl());
            return false;
        }

        // Validate authentication if present
        if(endpoint.getDefaultAuth() != null) {
            ServiceEndpoint.AuthenticationConfig auth = endpoint.getDefaultAuth();
            if(auth.getAuthType() != ServiceEndpoint.AuthenticationConfig.AuthType.NONE) {
                if(auth.getCredentials() == null || auth.getCredentials().isEmpty()) {
                    logger.warn("Authentication configured but no credentials provided");
                    return false;
                }
            }
        }

        return true;
    }

    private OutboundRequest.RequestType mapEndpointTypeToRequestType(ServiceEndpoint.ServiceType type) {
        switch(type) {
            case SOAP_SERVICE:
                return OutboundRequest.RequestType.SOAP_SERVICE;
            case GRAPHQL_API:
                return OutboundRequest.RequestType.GRAPHQL;
            case WEBHOOK:
                return OutboundRequest.RequestType.WEBHOOK;
            default:
                return OutboundRequest.RequestType.REST_API;
        }
    }
}
