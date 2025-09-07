package com.integrixs.data.repository;

import com.integrixs.data.model.AdapterConfigTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AdapterConfigTemplateRepository extends JpaRepository<AdapterConfigTemplate, UUID> {
    
    List<AdapterConfigTemplate> findByAdapterTypeId(UUID adapterTypeId);
    
    List<AdapterConfigTemplate> findByAdapterTypeIdAndIsPublic(UUID adapterTypeId, boolean isPublic);
    
    @Query("SELECT act FROM AdapterConfigTemplate act WHERE " +
           "act.adapterType.id = :adapterTypeId AND " +
           "(:direction IS NULL OR act.direction = :direction) " +
           "ORDER BY act.isDefault DESC, act.name")
    List<AdapterConfigTemplate> findByAdapterTypeIdAndDirection(@Param("adapterTypeId") UUID adapterTypeId,
                                                               @Param("direction") String direction);
    
    List<AdapterConfigTemplate> findByCreatedById(UUID userId);
}