package com.integrixs.data.repository;

import com.integrixs.data.model.ExternalAuthentication;
import com.integrixs.data.model.ExternalAuthentication.AuthType;
import com.integrixs.data.model.BusinessComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for external authentication configurations
 */
@Repository
public interface ExternalAuthenticationRepository extends JpaRepository<ExternalAuthentication, UUID> {

    /**
     * Find all authentications for a business component
     */
    List<ExternalAuthentication> findByBusinessComponentAndIsActiveTrue(BusinessComponent businessComponent);

    /**
     * Find all authentications by type for a business component
     */
    List<ExternalAuthentication> findByBusinessComponentAndAuthTypeAndIsActiveTrue(
            BusinessComponent businessComponent, AuthType authType);

    /**
     * Find by name and business component
     */
    Optional<ExternalAuthentication> findByNameAndBusinessComponent(String name, BusinessComponent businessComponent);

    /**
     * Check if authentication with name exists for business component
     */
    boolean existsByNameAndBusinessComponent(String name, BusinessComponent businessComponent);

    /**
     * Find OAuth2 authentications that need token refresh
     */
    @Query("SELECT e FROM ExternalAuthentication e " +
           "WHERE e.authType = 'OAUTH2' " +
           "AND e.isActive = true " +
           "AND e.tokenExpiresAt IS NOT NULL " +
           "AND e.tokenExpiresAt <= :expiryTime " +
           "AND e.refreshToken IS NOT NULL")
    List<ExternalAuthentication> findOAuth2TokensNeedingRefresh(@Param("expiryTime") LocalDateTime expiryTime);

    /**
     * Find authentications not used recently
     */
    @Query("SELECT e FROM ExternalAuthentication e " +
           "WHERE e.lastUsedAt IS NULL " +
           "OR e.lastUsedAt < :cutoffTime")
    List<ExternalAuthentication> findUnusedAuthentications(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Update usage statistics
     */
    @Query("UPDATE ExternalAuthentication e " +
           "SET e.usageCount = e.usageCount + 1, " +
           "e.lastUsedAt = CURRENT_TIMESTAMP " +
           "WHERE e.id = :id")
    void incrementUsageCount(@Param("id") UUID id);

    /**
     * Update error statistics
     */
    @Query("UPDATE ExternalAuthentication e " +
           "SET e.errorCount = e.errorCount + 1, " +
           "e.lastUsedAt = CURRENT_TIMESTAMP " +
           "WHERE e.id = :id")
    void incrementErrorCount(@Param("id") UUID id);
}
