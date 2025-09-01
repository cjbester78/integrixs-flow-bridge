package com.integrixs.data.repository;

import com.integrixs.data.model.AdapterHealthRecord;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdapterHealthRecordRepository extends JpaRepository<AdapterHealthRecord, UUID> {
    
    Page<AdapterHealthRecord> findByAdapter(CommunicationAdapter adapter, Pageable pageable);
    
    @Query("SELECT ahr FROM AdapterHealthRecord ahr WHERE ahr.adapter.id = :adapterId ORDER BY ahr.checkedAt DESC")
    Optional<AdapterHealthRecord> findLatestByAdapterId(@Param("adapterId") Long adapterId);
    
    @Query("SELECT ahr FROM AdapterHealthRecord ahr WHERE ahr.checkedAt BETWEEN :startDate AND :endDate")
    Page<AdapterHealthRecord> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate, 
                                             Pageable pageable);
    
    @Query("SELECT ahr FROM AdapterHealthRecord ahr WHERE ahr.healthStatus = :status AND ahr.checkedAt > :since")
    List<AdapterHealthRecord> findByStatusSince(@Param("status") AdapterHealthRecord.HealthStatus status,
                                                @Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(ahr.responseTimeMs) FROM AdapterHealthRecord ahr WHERE ahr.adapter.id = :adapterId AND ahr.checkedAt > :since")
    Double getAverageResponseTime(@Param("adapterId") Long adapterId, @Param("since") LocalDateTime since);
}