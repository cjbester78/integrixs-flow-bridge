package com.integrixs.backend.marketplace.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.integrixs.data.model.BaseEntity;
import com.integrixs.backend.auth.entity.User;

import java.time.LocalDateTime;

/**
 * Entity representing a user rating for a template
 */
@Entity
@Table(name = "template_ratings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"template_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TemplateRating extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private FlowTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(length = 1000)
    private String review;

    @Column(name = "is_verified_purchase")
    private boolean verifiedPurchase = false;

    @Column(name = "rated_at")
    private LocalDateTime ratedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "helpful_count")
    private Long helpfulCount = 0L;

    @Column(name = "not_helpful_count")
    private Long notHelpfulCount = 0L;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        ratedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        validateRating();
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
        updatedAt = LocalDateTime.now();
        validateRating();
    }

    private void validateRating() {
        if(rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}
