package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.SystemConfigurationRepositoryPort;
import com.integrixs.data.model.SystemConfiguration;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of SystemConfigurationRepository
 */
@Repository("domainSystemConfigurationRepository")
public class SystemConfigurationRepositoryImpl implements SystemConfigurationRepositoryPort {

    private final com.integrixs.data.repository.SystemConfigurationRepository jpaRepository;
    
    public SystemConfigurationRepositoryImpl(com.integrixs.data.repository.SystemConfigurationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<SystemConfiguration> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<SystemConfiguration> findByConfigKey(String configKey) {
        return jpaRepository.findByConfigKey(configKey);
    }

    @Override
    public List<SystemConfiguration> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public SystemConfiguration save(SystemConfiguration configuration) {
        return jpaRepository.save(configuration);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByConfigKey(String configKey) {
        return jpaRepository.existsByConfigKey(configKey);
    }
}
