package com.integrixs.backend.marketplace.repository;

import com.integrixs.backend.marketplace.entity.TemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for template versions
 */
@Repository
public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, UUID> {
    
    @Query("SELECT v FROM TemplateVersion v WHERE v.template.id = :templateId AND v.version = :version")
    Optional<TemplateVersion> findByTemplateIdAndVersion(@Param("templateId") UUID templateId, @Param("version") String version);
    
    @Query("SELECT v FROM TemplateVersion v WHERE v.template.id = :templateId AND v.latest = true")
    Optional<TemplateVersion> findLatestByTemplateId(@Param("templateId") UUID templateId);
    
    @Modifying
    @Query("UPDATE TemplateVersion v SET v.latest = false WHERE v.template.id = :templateId")
    void unmarkLatestVersions(@Param("templateId") UUID templateId);
}