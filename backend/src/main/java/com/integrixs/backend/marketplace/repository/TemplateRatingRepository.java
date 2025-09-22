package com.integrixs.backend.marketplace.repository;

import com.integrixs.backend.marketplace.entity.TemplateRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for template ratings
 */
@Repository
public interface TemplateRatingRepository extends JpaRepository<TemplateRating, UUID> {
    
    Optional<TemplateRating> findByTemplateIdAndUserId(UUID templateId, UUID userId);
    
    List<TemplateRating> findByTemplateId(UUID templateId);
}