package com.integrixs.backend.marketplace.specification;

import com.integrixs.backend.marketplace.entity.FlowTemplate;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

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
    
    public static Specification<FlowTemplate> isActive() {
        return (root, criteriaQuery, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("active"), true);
    }
    
    public static Specification<FlowTemplate> isPublic() {
        return (root, criteriaQuery, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("visibility"), FlowTemplate.TemplateVisibility.PUBLIC);
    }
    
    public static Specification<FlowTemplate> searchByQuery(String searchQuery) {
        return hasQuery(searchQuery); // Use existing hasQuery method
    }
    
    public static Specification<FlowTemplate> hasType(String type) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (type == null || type.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }
    
    public static Specification<FlowTemplate> hasTags(List<String> tags) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (tags == null || tags.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.join("tags").in(tags);
        };
    }
    
    public static Specification<FlowTemplate> hasMinRating(Double minRating) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (minRating == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), minRating);
        };
    }
    
    public static Specification<FlowTemplate> isCertified() {
        return isCertified(true); // Use existing isCertified method with true parameter
    }
    
    public static Specification<FlowTemplate> hasAuthor(String authorId) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (authorId == null || authorId.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("author").get("id"), authorId);
        };
    }
    
    public static Specification<FlowTemplate> hasOrganization(String organizationId) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (organizationId == null || organizationId.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("organization").get("id"), organizationId);
        };
    }
}