package com.integrixs.data.repository;

import com.integrixs.data.model.AdapterPayload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for AdapterPayload entities
 */
@Repository
public interface AdapterPayloadRepository extends JpaRepository<AdapterPayload, UUID> {

    List<AdapterPayload> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);

    List<AdapterPayload> findByCorrelationIdOrderByCreatedAt(String correlationId);

    List<AdapterPayload> findByAdapterIdOrderByCreatedAtDesc(UUID adapterId);

    void deleteByCorrelationId(String correlationId);

    @Modifying
    @Query("DELETE FROM AdapterPayload a WHERE a.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    default List<AdapterPayload> findByCorrelationId(String correlationId) {
        return findByCorrelationIdOrderByCreatedAtAsc(correlationId);
    }
}
