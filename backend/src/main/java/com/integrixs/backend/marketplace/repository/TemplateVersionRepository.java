package com.integrixs.backend.marketplace.repository;

import com.integrixs.backend.marketplace.entity.TemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for template versions
 */
@Repository
public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, UUID> {
}