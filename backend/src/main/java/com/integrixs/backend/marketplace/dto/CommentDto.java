package com.integrixs.backend.marketplace.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CommentDto {
    private UUID id;
    private AuthorDto author;
    private String content;
    private boolean authorResponse;
    private boolean pinned;
    private LocalDateTime postedAt;
    private LocalDateTime editedAt;
    private Long likeCount;
    private List<CommentDto> replies;
    private UUID userId;
    private String userName;
    private LocalDateTime createdAt;
    private UUID parentCommentId;

    // Default constructor
    public CommentDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(UUID parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    // New getters and setters
    public AuthorDto getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDto author) {
        this.author = author;
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

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public List<CommentDto> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentDto> replies) {
        this.replies = replies;
    }

    // Builder
    public static CommentDtoBuilder builder() {
        return new CommentDtoBuilder();
    }

    public static class CommentDtoBuilder {
        private UUID id;
        private AuthorDto author;
        private String content;
        private boolean authorResponse;
        private boolean pinned;
        private LocalDateTime postedAt;
        private LocalDateTime editedAt;
        private Long likeCount;
        private List<CommentDto> replies;

        public CommentDtoBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public CommentDtoBuilder author(AuthorDto author) {
            this.author = author;
            return this;
        }

        public CommentDtoBuilder content(String content) {
            this.content = content;
            return this;
        }

        public CommentDtoBuilder authorResponse(boolean authorResponse) {
            this.authorResponse = authorResponse;
            return this;
        }

        public CommentDtoBuilder pinned(boolean pinned) {
            this.pinned = pinned;
            return this;
        }

        public CommentDtoBuilder postedAt(LocalDateTime postedAt) {
            this.postedAt = postedAt;
            return this;
        }

        public CommentDtoBuilder editedAt(LocalDateTime editedAt) {
            this.editedAt = editedAt;
            return this;
        }

        public CommentDtoBuilder likeCount(Long likeCount) {
            this.likeCount = likeCount;
            return this;
        }

        public CommentDtoBuilder replies(List<CommentDto> replies) {
            this.replies = replies;
            return this;
        }

        public CommentDto build() {
            CommentDto dto = new CommentDto();
            dto.setId(this.id);
            dto.setAuthor(this.author);
            dto.setContent(this.content);
            dto.setAuthorResponse(this.authorResponse);
            dto.setPinned(this.pinned);
            dto.setPostedAt(this.postedAt);
            dto.setEditedAt(this.editedAt);
            dto.setLikeCount(this.likeCount);
            dto.setReplies(this.replies);
            return dto;
        }
    }
}