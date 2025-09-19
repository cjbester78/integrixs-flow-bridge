package com.integrixs.webserver.infrastructure.repository;

import com.integrixs.webserver.domain.model.ServiceEndpoint;
import com.integrixs.webserver.domain.repository.ServiceEndpointRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In - memory implementation of service endpoint repository
 */
@Repository
public class InMemoryServiceEndpointRepository implements ServiceEndpointRepository {

    private final Map<String, ServiceEndpoint> endpoints = new ConcurrentHashMap<>();

    @Override
    public ServiceEndpoint save(ServiceEndpoint endpoint) {
        if(endpoint.getEndpointId() == null) {
            endpoint.setEndpointId(UUID.randomUUID().toString());
        }
        endpoints.put(endpoint.getEndpointId(), endpoint);
        return endpoint;
    }

    @Override
    public Optional<ServiceEndpoint> findById(String endpointId) {
        return Optional.ofNullable(endpoints.get(endpointId));
    }

    @Override
    public Optional<ServiceEndpoint> findByName(String name) {
        return endpoints.values().stream()
                .filter(endpoint -> name.equals(endpoint.getName()))
                .findFirst();
    }

    @Override
    public List<ServiceEndpoint> findAll() {
        return new ArrayList<>(endpoints.values());
    }

    @Override
    public List<ServiceEndpoint> findByActive(boolean active) {
        return endpoints.values().stream()
                .filter(endpoint -> endpoint.isActive() == active)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceEndpoint> findByType(ServiceEndpoint.ServiceType type) {
        return endpoints.values().stream()
                .filter(endpoint -> type.equals(endpoint.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public ServiceEndpoint update(ServiceEndpoint endpoint) {
        if(endpoint.getEndpointId() == null || !endpoints.containsKey(endpoint.getEndpointId())) {
            throw new RuntimeException("Endpoint not found for update: " + endpoint.getEndpointId());
        }
        endpoints.put(endpoint.getEndpointId(), endpoint);
        return endpoint;
    }

    @Override
    public void deleteById(String endpointId) {
        endpoints.remove(endpointId);
    }

    @Override
    public boolean existsById(String endpointId) {
        return endpoints.containsKey(endpointId);
    }

    @Override
    public boolean existsByName(String name) {
        return endpoints.values().stream()
                .anyMatch(endpoint -> name.equals(endpoint.getName()));
    }
}
