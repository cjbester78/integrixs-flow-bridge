package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.SystemConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for system configurations
 */
public interface SystemConfigurationRepository {

    Optional<SystemConfiguration> findById(UUID id);

    Optional<SystemConfiguration> findByConfigKey(String configKey);

    List<SystemConfiguration> findAll();

    SystemConfiguration save(SystemConfiguration configuration);

    void deleteById(UUID id);

    boolean existsByConfigKey(String configKey);
}
