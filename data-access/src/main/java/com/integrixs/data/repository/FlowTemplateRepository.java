package com.integrixs.data.repository;

import com.integrixs.data.model.FlowTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for FlowTemplate entities
 */
@Repository
public interface FlowTemplateRepository {

    FlowTemplate save(FlowTemplate flowTemplate);

    Optional<FlowTemplate> findById(UUID id);

    Optional<FlowTemplate> findBySlug(String slug);

    Page<FlowTemplate> findAll(Pageable pageable);

    Page<FlowTemplate> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<FlowTemplate> findByAuthorId(UUID authorId, Pageable pageable);

    Page<FlowTemplate> findByCategory(String category, Pageable pageable);

    Page<FlowTemplate> findByType(String type, Pageable pageable);

    Page<FlowTemplate> findByVisibility(String visibility, Pageable pageable);

    Page<FlowTemplate> findByCertifiedTrue(Pageable pageable);

    Page<FlowTemplate> findByFeaturedTrueAndFeaturedUntilAfter(LocalDateTime now, Pageable pageable);

    Page<FlowTemplate> searchByNameOrDescriptionContaining(String searchTerm, Pageable pageable);

    List<FlowTemplate> findByTagsContaining(String tag);

    List<FlowTemplate> findTop10ByOrderByDownloadCountDesc();

    List<FlowTemplate> findTop10ByPublishedAtAfterOrderByInstallCountDesc(LocalDateTime after);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsBySlug(String slug);

    long count();

    long countByOrganizationId(UUID organizationId);

    long countByCertifiedTrue();
}