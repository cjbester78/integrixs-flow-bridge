package com.integrixs.backend.marketplace.specification;

import com.integrixs.backend.marketplace.entity.FlowTemplate;
import org.springframework.data.jpa.domain.Specification;

public class TemplateSpecifications {
    
    public static Specification<FlowTemplate> hasQuery(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (query == null || query.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String likePattern = "%" + query.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern)
            );
        };
    }
    
    public static Specification<FlowTemplate> hasCategory(String category) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (category == null || category.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category"), category);
        };
    }
    
    public static Specification<FlowTemplate> isFeatured(Boolean featured) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (featured == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("featured"), featured);
        };
    }
    
    public static Specification<FlowTemplate> isCertified(Boolean certified) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (certified == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("certified"), certified);
        };
    }
    
    public static Specification<FlowTemplate> isPublished() {
        return (root, criteriaQuery, criteriaBuilder) -> 
            criteriaBuilder.isNotNull(root.get("publishedAt"));
    }
}