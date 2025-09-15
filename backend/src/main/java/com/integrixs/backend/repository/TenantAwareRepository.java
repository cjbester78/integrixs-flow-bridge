package com.integrixs.backend.repository;

import com.integrixs.backend.security.TenantContext;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.UUID;

/**
 * Base interface for tenant - aware repositories
 */
public interface TenantAwareRepository {

    /**
     * Get specification for current tenant
     */
    static <T> Specification<T> currentTenantSpec() {
        return(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            UUID tenantId = TenantContext.getCurrentTenant();
            if(tenantId != null) {
                return cb.equal(root.get("tenantId"), tenantId);
            }
            // If no tenant context, return all(for admin operations)
            return cb.conjunction();
        };
    }

    /**
     * Get specification for specific tenant
     */
    static <T> Specification<T> forTenant(UUID tenantId) {
        return(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if(tenantId != null) {
                return cb.equal(root.get("tenantId"), tenantId);
            }
            return cb.conjunction();
        };
    }

    /**
     * Combine tenant specification with another specification
     */
    static <T> Specification<T> withTenant(Specification<T> spec) {
        Specification<T> tenantSpec = currentTenantSpec();
        if(spec == null) {
            return tenantSpec;
        }
        return tenantSpec.and(spec);
    }
}
