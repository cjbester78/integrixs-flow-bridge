package com.integrixs.backend.performance;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Base repository implementation with query optimization techniques.
 * Provides common patterns for efficient database access.
 */
@Transactional(readOnly = true)
public abstract class OptimizedRepositoryImpl<T, ID> {

    @PersistenceContext
    protected EntityManager entityManager;

    protected final Class<T> entityClass;

    protected OptimizedRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Find entities with eager loading of associations.
     */
    public List<T> findAllWithAssociations(String... associationPaths) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        // Create fetches for each association path
        for(String path : associationPaths) {
            createFetchPath(root, path);
        }

        query.select(root).distinct(true);

        return entityManager.createQuery(query)
            .setHint("org.hibernate.readOnly", true)
            .setHint("org.hibernate.cacheable", true)
            .getResultList();
    }

    /**
     * Paginated query with associations.
     */
    public Page<T> findPageWithAssociations(Pageable pageable, String... associationPaths) {
        // Count query without fetches
        Long total = count();

        // Data query with fetches
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        for(String path : associationPaths) {
            createFetchPath(root, path);
        }

        query.select(root).distinct(true);

        // Apply pagination
        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        typedQuery.setHint("org.hibernate.readOnly", true);

        List<T> content = typedQuery.getResultList();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Batch update operation.
     */
    @Transactional
    public int batchUpdate(String updateQuery, Map<String, Object> parameters) {
        Query query = entityManager.createQuery(updateQuery);
        parameters.forEach(query::setParameter);
        return query.executeUpdate();
    }

    /**
     * Batch delete operation.
     */
    @Transactional
    public int batchDelete(List<ID> ids) {
        if(ids == null || ids.isEmpty()) {
            return 0;
        }

        String jpql = "DELETE FROM " + entityClass.getSimpleName() + " e WHERE e.id IN :ids";
        return entityManager.createQuery(jpql)
            .setParameter("ids", ids)
            .executeUpdate();
    }

    /**
     * Stream results for large datasets.
     */
    public Stream<T> streamAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);
        query.select(root);

        return entityManager.createQuery(query)
            .setHint("org.hibernate.fetchSize", 1000)
            .setHint("org.hibernate.readOnly", true)
            .getResultStream();
    }

    /**
     * Find with specification and fetch joins.
     */
    public List<T> findAllWithSpecAndFetch(Specification<T> spec, String... fetchPaths) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        // Apply specification
        if(spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if(predicate != null) {
                query.where(predicate);
            }
        }

        // Apply fetches
        for(String path : fetchPaths) {
            createFetchPath(root, path);
        }

        query.select(root).distinct(true);

        return entityManager.createQuery(query)
            .setHint("org.hibernate.readOnly", true)
            .getResultList();
    }

    /**
     * Count with specification.
     */
    public Long count(Specification<T> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);

        if(spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            if(predicate != null) {
                query.where(predicate);
            }
        }

        query.select(cb.count(root));

        return entityManager.createQuery(query)
            .setHint("org.hibernate.cacheable", true)
            .getSingleResult();
    }

    /**
     * Check existence efficiently.
     */
    public boolean exists(ID id) {
        String jpql = "SELECT COUNT(e.id) FROM " + entityClass.getSimpleName() + " e WHERE e.id = :id";
        Long count = entityManager.createQuery(jpql, Long.class)
            .setParameter("id", id)
            .setHint("org.hibernate.cacheable", true)
            .getSingleResult();
        return count > 0;
    }

    /**
     * Projection query for specific fields.
     */
    public <R> List<R> findProjection(Class<R> projectionClass, String... fields) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<R> query = cb.createQuery(projectionClass);
        Root<T> root = query.from(entityClass);

        // Build selection list
        List<Selection<?>> selections = new ArrayList<>();
        for(String field : fields) {
            selections.add(root.get(field));
        }

        query.multiselect(selections);

        return entityManager.createQuery(query)
            .setHint("org.hibernate.readOnly", true)
            .setHint("org.hibernate.cacheable", true)
            .getResultList();
    }

    /**
     * Bulk insert operation.
     */
    @Transactional
    public void bulkInsert(List<T> entities) {
        if(entities == null || entities.isEmpty()) {
            return;
        }

        int batchSize = 50;
        for(int i = 0; i < entities.size(); i++) {
            entityManager.persist(entities.get(i));

            if(i % batchSize == 0 && i > 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Helper method to create fetch paths.
     */
    private void createFetchPath(Root<T> root, String path) {
        String[] parts = path.split("\\.");
        Fetch<?, ?> fetch = root.fetch(parts[0], JoinType.LEFT);

        for(int i = 1; i < parts.length; i++) {
            fetch = fetch.fetch(parts[i], JoinType.LEFT);
        }
    }

    /**
     * Get count of all entities.
     */
    private Long count() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);
        query.select(cb.count(root));

        return entityManager.createQuery(query)
            .setHint("org.hibernate.cacheable", true)
            .getSingleResult();
    }
}
