package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.performance.OptimizedRepositoryImpl;
import com.integrixs.data.model.FlowStatus;
import com.integrixs.data.model.IntegrationFlow;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Optimized repository implementation for IntegrationFlow entities.
 * Provides efficient query methods with proper fetching strategies.
 */
@Slf4j
@Repository
@Transactional(readOnly = true)
public class OptimizedIntegrationFlowRepository extends OptimizedRepositoryImpl<IntegrationFlow, UUID> {
    
    public OptimizedIntegrationFlowRepository() {
        super(IntegrationFlow.class);
    }
    
    /**
     * Find flow with all associations loaded efficiently.
     */
    public Optional<IntegrationFlow> findByIdWithFullDetails(UUID id) {
        // Create entity graph to load all associations in one query
        EntityGraph<IntegrationFlow> graph = entityManager.createEntityGraph(IntegrationFlow.class);
        graph.addAttributeNodes("transformations", "businessComponent", "sourceAdapter", "targetAdapter");
        graph.addSubgraph("transformations").addAttributeNodes("transformationRules");
        
        Map<String, Object> hints = new HashMap<>();
        hints.put("jakarta.persistence.loadgraph", graph);
        hints.put("org.hibernate.readOnly", true);
        
        IntegrationFlow flow = entityManager.find(IntegrationFlow.class, id, hints);
        return Optional.ofNullable(flow);
    }
    
    /**
     * Find active flows with pagination and minimal data.
     */
    public Page<IntegrationFlow> findActiveFlowsOptimized(Pageable pageable) {
        // Count query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<IntegrationFlow> countRoot = countQuery.from(IntegrationFlow.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.isTrue(countRoot.get("isActive")));
        
        Long total = entityManager.createQuery(countQuery)
            .setHint("org.hibernate.cacheable", true)
            .getSingleResult();
        
        // Data query with projection
        CriteriaQuery<IntegrationFlow> dataQuery = cb.createQuery(IntegrationFlow.class);
        Root<IntegrationFlow> root = dataQuery.from(IntegrationFlow.class);
        
        dataQuery.select(root);
        dataQuery.where(cb.isTrue(root.get("isActive")));
        dataQuery.orderBy(cb.desc(root.get("updatedAt")));
        
        TypedQuery<IntegrationFlow> query = entityManager.createQuery(dataQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        query.setHint("org.hibernate.readOnly", true);
        
        List<IntegrationFlow> content = query.getResultList();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    /**
     * Batch update flow statistics.
     */
    @Transactional
    public void updateFlowStatistics(UUID flowId, boolean success, LocalDateTime executionTime) {
        String jpql = """
            UPDATE IntegrationFlow f 
            SET f.executionCount = f.executionCount + 1,
                f.successCount = CASE WHEN :success = true THEN f.successCount + 1 ELSE f.successCount END,
                f.errorCount = CASE WHEN :success = false THEN f.errorCount + 1 ELSE f.errorCount END,
                f.lastExecutionAt = :executionTime,
                f.updatedAt = :executionTime
            WHERE f.id = :flowId
            """;
        
        int updated = entityManager.createQuery(jpql)
            .setParameter("flowId", flowId)
            .setParameter("success", success)
            .setParameter("executionTime", executionTime)
            .executeUpdate();
        
        if (updated > 0) {
            log.debug("Updated statistics for flow {}: success={}", flowId, success);
        }
    }
    
    /**
     * Find flows by status with minimal fetching.
     */
    public List<IntegrationFlow> findByStatusOptimized(FlowStatus status) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<IntegrationFlow> query = cb.createQuery(IntegrationFlow.class);
        Root<IntegrationFlow> root = query.from(IntegrationFlow.class);
        
        query.select(root);
        query.where(
            cb.and(
                cb.equal(root.get("status"), status),
                cb.isTrue(root.get("isActive"))
            )
        );
        query.orderBy(cb.asc(root.get("name")));
        
        return entityManager.createQuery(query)
            .setHint("org.hibernate.readOnly", true)
            .setHint("org.hibernate.cacheable", true)
            .getResultList();
    }
    
    /**
     * Get flow execution statistics projection.
     */
    public List<FlowStatisticsProjection> getFlowStatistics(UUID userId) {
        String jpql = """
            SELECT NEW com.integrixs.backend.infrastructure.persistence.OptimizedIntegrationFlowRepository$FlowStatisticsProjection(
                f.id, f.name, f.status, f.isActive,
                f.executionCount, f.successCount, f.errorCount,
                f.lastExecutionAt
            )
            FROM IntegrationFlow f
            WHERE f.createdBy.id = :userId
            ORDER BY f.name
            """;
        
        return entityManager.createQuery(jpql, FlowStatisticsProjection.class)
            .setParameter("userId", userId)
            .setHint("org.hibernate.readOnly", true)
            .getResultList();
    }
    
    /**
     * Find flows using specific adapters.
     */
    public List<IntegrationFlow> findFlowsByAdapterUsage(UUID adapterId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<IntegrationFlow> query = cb.createQuery(IntegrationFlow.class);
        Root<IntegrationFlow> root = query.from(IntegrationFlow.class);
        
        // Use OR condition for source or target adapter
        Predicate sourceMatch = cb.equal(root.get("sourceAdapter").get("id"), adapterId);
        Predicate targetMatch = cb.equal(root.get("targetAdapter").get("id"), adapterId);
        
        query.select(root);
        query.where(cb.or(sourceMatch, targetMatch));
        
        return entityManager.createQuery(query)
            .setHint("org.hibernate.readOnly", true)
            .getResultList();
    }
    
    /**
     * Check if flow name exists efficiently.
     */
    public boolean existsByNameOptimized(String name, UUID excludeId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<IntegrationFlow> root = query.from(IntegrationFlow.class);
        
        Predicate namePredicate = cb.equal(cb.lower(root.get("name")), name.toLowerCase());
        
        if (excludeId != null) {
            Predicate notSameId = cb.notEqual(root.get("id"), excludeId);
            query.where(cb.and(namePredicate, notSameId));
        } else {
            query.where(namePredicate);
        }
        
        query.select(cb.count(root));
        
        Long count = entityManager.createQuery(query)
            .setHint("org.hibernate.cacheable", true)
            .getSingleResult();
        
        return count > 0;
    }
    
    /**
     * Find deployed flow by endpoint path.
     */
    public Optional<IntegrationFlow> findDeployedFlowByPath(String path) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<IntegrationFlow> query = cb.createQuery(IntegrationFlow.class);
        Root<IntegrationFlow> root = query.from(IntegrationFlow.class);
        
        // Fetch transformations eagerly for deployed flows
        root.fetch("transformations", JoinType.LEFT);
        
        query.select(root);
        query.where(
            cb.and(
                cb.like(root.get("deploymentEndpoint"), "%" + path + "%"),
                cb.equal(root.get("status"), FlowStatus.DEPLOYED)
            )
        );
        
        List<IntegrationFlow> results = entityManager.createQuery(query)
            .setHint("org.hibernate.readOnly", true)
            .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * Batch update flow status.
     */
    @Transactional
    public int batchUpdateStatus(List<UUID> flowIds, FlowStatus newStatus) {
        if (flowIds == null || flowIds.isEmpty()) {
            return 0;
        }
        
        String jpql = """
            UPDATE IntegrationFlow f 
            SET f.status = :status, f.updatedAt = :updatedAt
            WHERE f.id IN :ids
            """;
        
        return entityManager.createQuery(jpql)
            .setParameter("status", newStatus)
            .setParameter("updatedAt", LocalDateTime.now())
            .setParameter("ids", flowIds)
            .executeUpdate();
    }
    
    /**
     * Statistics projection class.
     */
    public static class FlowStatisticsProjection {
        public final UUID id;
        public final String name;
        public final FlowStatus status;
        public final boolean active;
        public final long executionCount;
        public final long successCount;
        public final long errorCount;
        public final LocalDateTime lastExecutionAt;
        
        public FlowStatisticsProjection(UUID id, String name, FlowStatus status, boolean active,
                                       long executionCount, long successCount, long errorCount,
                                       LocalDateTime lastExecutionAt) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.active = active;
            this.executionCount = executionCount;
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.lastExecutionAt = lastExecutionAt;
        }
    }
}