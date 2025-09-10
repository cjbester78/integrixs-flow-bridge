package com.integrixs.backend.marketplace.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.integrixs.data.model.BaseEntity;
import com.integrixs.backend.auth.entity.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a comment on a template
 */
@Entity
@Table(name = "template_comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TemplateComment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private FlowTemplate template;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private TemplateComment parentComment;
    
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TemplateComment> replies = new HashSet<>();
    
    @Column(nullable = false, length = 5000)
    private String content;
    
    @Column(name = "is_author_response")
    private boolean authorResponse = false;
    
    @Column(name = "is_pinned")
    private boolean pinned = false;
    
    @Column(name = "posted_at")
    private LocalDateTime postedAt;
    
    @Column(name = "edited_at")
    private LocalDateTime editedAt;
    
    @Column(name = "is_deleted")
    private boolean deleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;
    
    @Column(name = "like_count")
    private Long likeCount = 0L;
    
    @ElementCollection
    @CollectionTable(name = "comment_likes", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "user_id")
    private Set<UUID> likedByUsers = new HashSet<>();
    
    @PrePersist
    public void prePersist() {
        super.prePersist();
        postedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
        if (!deleted && content != null) {
            editedAt = LocalDateTime.now();
        }
    }
}