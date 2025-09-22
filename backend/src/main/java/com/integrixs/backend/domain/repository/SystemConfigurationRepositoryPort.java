package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.SystemConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * System configuration repository port - domain layer
 * Acts as a port in hexagonal architecture for system configuration persistence operations
 */
public interface SystemConfigurationRepositoryPort {

    Optional<SystemConfiguration> findById(UUID id);

    Optional<SystemConfiguration> findByConfigKey(String configKey);

    List<SystemConfiguration> findAll();

    SystemConfiguration save(SystemConfiguration configuration);

    void deleteById(UUID id);

    boolean existsByConfigKey(String configKey);
}
