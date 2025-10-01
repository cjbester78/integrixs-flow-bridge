package com.integrixs.adapters.infrastructure.persistence;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.repository.AdapterRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In - memory implementation of adapter repository
 * Can be replaced with SQL repository for database persistence
 */
@Repository
public class InMemoryAdapterRepository implements AdapterRepository {

    private final Map<String, AdapterConfiguration> storage = new ConcurrentHashMap<>();

    @Override
    public AdapterConfiguration save(AdapterConfiguration configuration) {
        storage.put(configuration.getAdapterId(), configuration);
        return configuration;
    }

    @Override
    public Optional<AdapterConfiguration> findById(String adapterId) {
        return Optional.ofNullable(storage.get(adapterId));
    }

    @Override
    public List<AdapterConfiguration> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<AdapterConfiguration> findByType(AdapterConfiguration.AdapterTypeEnum adapterType) {
        return storage.values().stream()
                .filter(config -> config.getAdapterType() == adapterType)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdapterConfiguration> findByMode(AdapterConfiguration.AdapterModeEnum adapterMode) {
        return storage.values().stream()
                .filter(config -> config.getAdapterMode() == adapterMode)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String adapterId) {
        storage.remove(adapterId);
    }

    @Override
    public boolean exists(String adapterId) {
        return storage.containsKey(adapterId);
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }
}
