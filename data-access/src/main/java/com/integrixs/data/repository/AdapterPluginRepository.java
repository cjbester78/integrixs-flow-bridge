package com.integrixs.data.repository;

import com.integrixs.data.model.AdapterPlugin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdapterPluginRepository extends JpaRepository<AdapterPlugin, UUID> {

    List<AdapterPlugin> findByAdapterTypeId(UUID adapterTypeId);

    List<AdapterPlugin> findByIsActive(boolean isActive);

    Optional<AdapterPlugin> findByAdapterTypeIdAndIsActive(UUID adapterTypeId, boolean isActive);

    List<AdapterPlugin> findByJarFileId(UUID jarFileId);

    boolean existsByPluginClassAndPluginVersion(String pluginClass, String pluginVersion);
}
