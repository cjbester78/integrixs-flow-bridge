package com.integrixs.backend.marketplace.repository;

import com.integrixs.backend.marketplace.entity.FlowTemplate;
import com.integrixs.backend.marketplace.entity.FlowTemplate.TemplateCategory;
import com.integrixs.backend.marketplace.entity.FlowTemplate.TemplateType;
import com.integrixs.backend.marketplace.entity.FlowTemplate.TemplateVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlowTemplateRepository extends JpaRepository<FlowTemplate, UUID>, JpaSpecificationExecutor<FlowTemplate> {
    
    Optional<FlowTemplate> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    @Query("SELECT t FROM FlowTemplate t WHERE t.visibility = :visibility AND t.active = true")
    Page<FlowTemplate> findByVisibilityAndActive(
        @Param("visibility") TemplateVisibility visibility, 
        Pageable pageable
    );
    
    @Query("SELECT t FROM FlowTemplate t WHERE t.category = :category AND t.visibility = 'PUBLIC' AND t.active = true")
    Page<FlowTemplate> findByCategoryAndPublic(
        @Param("category") TemplateCategory category, 
        Pageable pageable
    );
    
    @Query("SELECT t FROM FlowTemplate t WHERE t.type = :type AND t.visibility = 'PUBLIC' AND t.active = true")
    Page<FlowTemplate> findByTypeAndPublic(
        @Param("type") TemplateType type, 
        Pageable pageable
    );
    
    @Query("SELECT t FROM FlowTemplate t WHERE t.author.id = :authorId")
    Page<FlowTemplate> findByAuthorId(
        @Param("authorId") UUID authorId, 
        Pageable pageable
    );
    
    @Query("SELECT t FROM FlowTemplate t WHERE t.organization.id = :orgId")
    Page<FlowTemplate> findByOrganizationId(
        @Param("orgId") UUID orgId, 
        Pageable pageable
    );
    
    @Query("SELECT t FROM FlowTemplate t WHERE " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "EXISTS (SELECT 1 FROM t.tags tag WHERE LOWER(tag) LIKE LOWER(CONCAT('%', :query, '%')))) " +
           "AND t.visibility = 'PUBLIC' AND t.active = true")
    Page<FlowTemplate> searchPublicTemplates(
        @Param("query") String query, 
        Pageable pageable
    );
    
    @Query("SELECT t FROM FlowTemplate t WHERE t.featured = true AND t.featuredUntil > :now " +
           "AND t.visibility = 'PUBLIC' AND t.active = true")
    List<FlowTemplate> findFeaturedTemplates(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM FlowTemplate t WHERE t.certified = true " +
           "AND t.visibility = 'PUBLIC' AND t.active = true")
    Page<FlowTemplate> findCertifiedTemplates(Pageable pageable);
    
    @Query("SELECT t FROM FlowTemplate t ORDER BY t.downloadCount DESC")
    List<FlowTemplate> findTopDownloaded(Pageable pageable);
    
    @Query("SELECT t FROM FlowTemplate t WHERE t.publishedAt > :since " +
           "AND t.visibility = 'PUBLIC' AND t.active = true " +
           "ORDER BY t.publishedAt DESC")
    Page<FlowTemplate> findRecentlyPublished(
        @Param("since") LocalDateTime since, 
        Pageable pageable
    );
    
    @Query("SELECT t FROM FlowTemplate t WHERE SIZE(t.ratings) >= 5 " +
           "AND t.visibility = 'PUBLIC' AND t.active = true " +
           "ORDER BY t.averageRating DESC")
    List<FlowTemplate> findTopRated(Pageable pageable);
    
    @Query("SELECT DISTINCT t.tags FROM FlowTemplate t WHERE t.visibility = 'PUBLIC' AND t.active = true")
    List<String> findAllPublicTags();
    
    @Query("SELECT t FROM FlowTemplate t WHERE :tag MEMBER OF t.tags " +
           "AND t.visibility = 'PUBLIC' AND t.active = true")
    Page<FlowTemplate> findByTag(@Param("tag") String tag, Pageable pageable);
    
    @Query("SELECT COUNT(t) FROM FlowTemplate t WHERE t.author.id = :authorId")
    long countByAuthorId(@Param("authorId") UUID authorId);
    
    @Query("SELECT COUNT(t) FROM FlowTemplate t WHERE t.organization.id = :orgId")
    long countByOrganizationId(@Param("orgId") UUID orgId);
    
    @Modifying
    @Query("UPDATE FlowTemplate t SET t.downloadCount = t.downloadCount + 1 WHERE t.id = :id")
    void incrementDownloadCount(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE FlowTemplate t SET t.installCount = t.installCount + 1 WHERE t.id = :id")
    void incrementInstallCount(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE FlowTemplate t SET " +
           "t.averageRating = :averageRating, " +
           "t.ratingCount = :ratingCount " +
           "WHERE t.id = :id")
    void updateRatingStats(
        @Param("id") UUID id,
        @Param("averageRating") Double averageRating,
        @Param("ratingCount") Long ratingCount
    );
    
    @Query("SELECT t FROM FlowTemplate t LEFT JOIN FETCH t.dependencies " +
           "WHERE t.id = :id")
    Optional<FlowTemplate> findByIdWithDependencies(@Param("id") UUID id);
    
    @Query("SELECT DISTINCT t FROM FlowTemplate t " +
           "LEFT JOIN FETCH t.tags " +
           "LEFT JOIN FETCH t.requirements " +
           "LEFT JOIN FETCH t.screenshots " +
           "WHERE t.slug = :slug")
    Optional<FlowTemplate> findBySlugWithDetails(@Param("slug") String slug);
}