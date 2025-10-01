package com.integrixs.backend.marketplace.dto;

import java.util.UUID;

/**
 * DTO for author information
 */
public class AuthorDto {
    private String id;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String organization;
    private boolean verified;

    // Default constructor
    public AuthorDto() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    // Builder
    public static AuthorDtoBuilder builder() {
        return new AuthorDtoBuilder();
    }

    public static class AuthorDtoBuilder {
        private String id;
        private String username;
        private String displayName;
        private String avatarUrl;
        private String organization;
        private boolean verified;

        public AuthorDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AuthorDtoBuilder id(UUID id) {
            this.id = id != null ? id.toString() : null;
            return this;
        }

        public AuthorDtoBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AuthorDtoBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public AuthorDtoBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public AuthorDtoBuilder organization(String organization) {
            this.organization = organization;
            return this;
        }

        public AuthorDtoBuilder verified(boolean verified) {
            this.verified = verified;
            return this;
        }

        public AuthorDto build() {
            AuthorDto dto = new AuthorDto();
            dto.setId(this.id);
            dto.setUsername(this.username);
            dto.setDisplayName(this.displayName);
            dto.setAvatarUrl(this.avatarUrl);
            dto.setOrganization(this.organization);
            dto.setVerified(this.verified);
            return dto;
        }
    }
}