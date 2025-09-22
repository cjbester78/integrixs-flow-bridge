package com.integrixs.backend.marketplace.repository;

import com.integrixs.backend.marketplace.entity.TemplateInstallation;
import com.integrixs.backend.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for template installations
 */
@Repository
public interface TemplateInstallationRepository extends JpaRepository<TemplateInstallation, UUID> {
    
    boolean existsByTemplateIdAndUserId(UUID templateId, UUID userId);
    
    Page<TemplateInstallation> findByUser(User user, Pageable pageable);
}