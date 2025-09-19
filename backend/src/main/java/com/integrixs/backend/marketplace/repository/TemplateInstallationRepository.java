package com.integrixs.backend.marketplace.repository;

import com.integrixs.backend.marketplace.entity.TemplateInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for template installations
 */
@Repository
public interface TemplateInstallationRepository extends JpaRepository<TemplateInstallation, UUID> {
}