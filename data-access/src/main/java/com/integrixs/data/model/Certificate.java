package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing Certificate.
 * This maps to the corresponding table in the database.
 */
public class Certificate {

        /** Unique identifier(UUID) for the entity */
    private UUID id;

    /** Name of the component */
    private String name;

    private String format; // e.g., PEM, DER, P12

    private String type;   // e.g., X.509, PKCS12

    private String fileName;

    private String password; // encrypted if present

    private String uploadedBy;

    private LocalDateTime uploadedAt;

    private byte[] content;

    /**
     * Automatically sets creation and update timestamps before persisting.
     */
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
