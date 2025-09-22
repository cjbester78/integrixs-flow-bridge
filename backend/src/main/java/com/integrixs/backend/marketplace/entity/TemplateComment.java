package com.integrixs.backend.marketplace.entity;

import jakarta.persistence.*;
import com.integrixs.data.model.BaseEntity;
import com.integrixs.backend.auth.entity.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity representing a comment on a template
 */
@Entity
@Table(name = "template_comments")
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
        postedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        if(!deleted && content != null) {
            editedAt = LocalDateTime.now();
        }
    }

    // Default constructor
    public TemplateComment() {
    }

    public FlowTemplate getTemplate() {
        return template;
    }

    public void setTemplate(FlowTemplate template) {
        this.template = template;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TemplateComment getParentComment() {
        return parentComment;
    }

    public void setParentComment(TemplateComment parentComment) {
        this.parentComment = parentComment;
    }

    public Set<TemplateComment> getReplies() {
        return replies;
    }

    public void setReplies(Set<TemplateComment> replies) {
        this.replies = replies;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAuthorResponse() {
        return authorResponse;
    }

    public void setAuthorResponse(boolean authorResponse) {
        this.authorResponse = authorResponse;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public User getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(User deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Set<UUID> getLikedByUsers() {
        return likedByUsers;
    }

    public void setLikedByUsers(Set<UUID> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }
}
