package com.integrixs.data.repository;

import com.integrixs.data.model.AdapterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdapterTypeRepository extends JpaRepository<AdapterType, UUID> {
    
    Optional<AdapterType> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT at FROM AdapterType at WHERE " +
           "(:categoryId IS NULL OR at.category.id = :categoryId) AND " +
           "(:status IS NULL OR at.status = :status) AND " +
           "(:search IS NULL OR LOWER(at.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(at.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AdapterType> findWithFilters(@Param("categoryId") UUID categoryId,
                                     @Param("status") String status,
                                     @Param("search") String search,
                                     Pageable pageable);
    
    List<AdapterType> findByCategoryIdAndStatus(UUID categoryId, String status);
    
    List<AdapterType> findByStatus(String status);
    
    @Query("SELECT at FROM AdapterType at WHERE " +
           "(:inbound = false OR at.supportsInbound = true) AND " +
           "(:outbound = false OR at.supportsOutbound = true) AND " +
           "at.status = 'active'")
    List<AdapterType> findByDirectionSupport(@Param("inbound") boolean inbound, 
                                            @Param("outbound") boolean outbound);
}