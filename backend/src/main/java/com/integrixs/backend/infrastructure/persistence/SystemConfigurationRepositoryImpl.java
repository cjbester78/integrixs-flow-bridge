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

    private final com.integrixs.data.sql.repository.SystemConfigurationSqlRepository sqlRepository;

    public SystemConfigurationRepositoryImpl(com.integrixs.data.sql.repository.SystemConfigurationSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public Optional<SystemConfiguration> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public Optional<SystemConfiguration> findByConfigKey(String configKey) {
        return sqlRepository.findByConfigKey(configKey);
    }

    @Override
    public List<SystemConfiguration> findAll() {
        return sqlRepository.findAll();
    }

    @Override
    public SystemConfiguration save(SystemConfiguration configuration) {
        return sqlRepository.save(configuration);
    }

    @Override
    public void deleteById(UUID id) {
        sqlRepository.deleteById(id);
    }

    @Override
    public boolean existsByConfigKey(String configKey) {
        return sqlRepository.existsByConfigKey(configKey);
    }
}
