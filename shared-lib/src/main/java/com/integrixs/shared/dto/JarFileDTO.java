package com.integrixs.shared.dto;

import java.time.LocalDateTime;
public class JarFileDTO {

    private String id;
    private String name;
    private String version;
    private String description;
    private Long size;
    private LocalDateTime uploadedAt;
    private String uploadedBy;

    // Default constructor
    public JarFileDTO() {
    }

    // All args constructor
    public JarFileDTO(String id, String name, String version, String description, Long size, LocalDateTime uploadedAt, String uploadedBy) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.size = size;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public Long getSize() { return size; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public String getUploadedBy() { return uploadedBy; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setVersion(String version) { this.version = version; }
    public void setDescription(String description) { this.description = description; }
    public void setSize(Long size) { this.size = size; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    // Builder
    public static JarFileDTOBuilder builder() {
        return new JarFileDTOBuilder();
    }

    public static class JarFileDTOBuilder {
        private String id;
        private String name;
        private String version;
        private String description;
        private Long size;
        private LocalDateTime uploadedAt;
        private String uploadedBy;

        public JarFileDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public JarFileDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public JarFileDTOBuilder version(String version) {
            this.version = version;
            return this;
        }

        public JarFileDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public JarFileDTOBuilder size(Long size) {
            this.size = size;
            return this;
        }

        public JarFileDTOBuilder uploadedAt(LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
            return this;
        }

        public JarFileDTOBuilder uploadedBy(String uploadedBy) {
            this.uploadedBy = uploadedBy;
            return this;
        }

        public JarFileDTO build() {
            return new JarFileDTO(id, name, version, description, size, uploadedAt, uploadedBy);
        }
    }
}
