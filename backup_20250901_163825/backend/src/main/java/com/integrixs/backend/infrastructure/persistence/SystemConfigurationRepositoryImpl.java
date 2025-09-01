package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.SystemConfigurationRepository;
import com.integrixs.data.model.SystemConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of SystemConfigurationRepository
 */
@Repository("domainSystemConfigurationRepository")
@RequiredArgsConstructor
public class SystemConfigurationRepositoryImpl implements SystemConfigurationRepository {
    
    private final com.integrixs.data.repository.SystemConfigurationRepository jpaRepository;
    
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