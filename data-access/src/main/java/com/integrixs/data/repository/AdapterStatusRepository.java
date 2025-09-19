package com.integrixs.data.repository;

import com.integrixs.data.model.AdapterStatus;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AdapterStatus entities
 */
@Repository
public interface AdapterStatusRepository extends JpaRepository<AdapterStatus, UUID> {
    
    /**
     * Find status by adapter
     */
    Optional<AdapterStatus> findByAdapter(CommunicationAdapter adapter);
    
    /**
     * Find status by adapter ID
     */
    Optional<AdapterStatus> findByAdapterId(UUID adapterId);
    
    /**
     * Find all statuses with errors
     */
    List<AdapterStatus> findByStatus(String status);
    
    /**
     * Find adapters with health score below threshold
     */
    @Query("SELECT a FROM AdapterStatus a WHERE a.healthScore < :threshold")
    List<AdapterStatus> findUnhealthyAdapters(@Param("threshold") Integer threshold);
    
    /**
     * Find adapters that haven't been active since a certain time
     */
    @Query("SELECT a FROM AdapterStatus a WHERE a.lastActivity < :since")
    List<AdapterStatus> findInactiveSince(@Param("since") LocalDateTime since);
    
    /**
     * Find adapters with recent errors
     */
    @Query("SELECT a FROM AdapterStatus a WHERE a.lastError > :since")
    List<AdapterStatus> findWithErrorsSince(@Param("since") LocalDateTime since);
    
    /**
     * Count adapters by status
     */
    Long countByStatus(String status);
    
    /**
     * Find adapters needing health check
     */
    @Query("SELECT a FROM AdapterStatus a WHERE a.nextHealthCheck <= :now OR a.lastHealthCheck IS NULL")
    List<AdapterStatus> findAdaptersNeedingHealthCheck(@Param("now") LocalDateTime now);
    
    /**
     * Get average health score across all adapters
     */
    @Query("SELECT AVG(a.healthScore) FROM AdapterStatus a")
    Double getAverageHealthScore();
    
    /**
     * Find disconnected adapters
     */
    List<AdapterStatus> findByIsConnectedFalse();
    
    /**
     * Update last activity timestamp
     */
    @Query("UPDATE AdapterStatus a SET a.lastActivity = :timestamp WHERE a.adapter.id = :adapterId")
    void updateLastActivity(@Param("adapterId") UUID adapterId, @Param("timestamp") LocalDateTime timestamp);
}