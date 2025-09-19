package com.integrixs.shared.dto.certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO for certificate information.
 *
 * <p>Represents SSL/TLS certificates used for secure communications
 * in adapters and integrations.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class CertificateDTO {

    private String id;
    private String name;
    private String format;
    private String type;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private LocalDateTime expiresAt;
    private String issuer;
    private String subject;
    private String thumbprint;
    private Boolean isValid;
    private Long sizeInBytes;
    private String fileName;
    private Boolean hasPassword;

    // Default constructor
    public CertificateDTO() {
    }

    // All args constructor
    public CertificateDTO(String id, String name, String format, String type, String uploadedBy, LocalDateTime uploadedAt, LocalDateTime expiresAt, String issuer, String subject, String thumbprint, Boolean isValid, Long sizeInBytes, String fileName, Boolean hasPassword) {
        this.id = id;
        this.name = name;
        this.format = format;
        this.type = type;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
        this.expiresAt = expiresAt;
        this.issuer = issuer;
        this.subject = subject;
        this.thumbprint = thumbprint;
        this.isValid = isValid;
        this.sizeInBytes = sizeInBytes;
        this.fileName = fileName;
        this.hasPassword = hasPassword;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getFormat() { return format; }
    public String getType() { return type; }
    public String getUploadedBy() { return uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public String getIssuer() { return issuer; }
    public String getSubject() { return subject; }
    public String getThumbprint() { return thumbprint; }
    public Boolean isIsValid() { return isValid; }
    public Long getSizeInBytes() { return sizeInBytes; }
    public String getFileName() { return fileName; }
    public Boolean isHasPassword() { return hasPassword; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setFormat(String format) { this.format = format; }
    public void setType(String type) { this.type = type; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setThumbprint(String thumbprint) { this.thumbprint = thumbprint; }
    public void setIsValid(Boolean isValid) { this.isValid = isValid; }
    public void setSizeInBytes(Long sizeInBytes) { this.sizeInBytes = sizeInBytes; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setHasPassword(Boolean hasPassword) { this.hasPassword = hasPassword; }

    // Builder
    public static CertificateDTOBuilder builder() {
        return new CertificateDTOBuilder();
    }

    public static class CertificateDTOBuilder {
        private String id;
        private String name;
        private String format;
        private String type;
        private String uploadedBy;
        private LocalDateTime uploadedAt;
        private LocalDateTime expiresAt;
        private String issuer;
        private String subject;
        private String thumbprint;
        private Boolean isValid;
        private Long sizeInBytes;
        private String fileName;
        private Boolean hasPassword;

        public CertificateDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public CertificateDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CertificateDTOBuilder format(String format) {
            this.format = format;
            return this;
        }

        public CertificateDTOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public CertificateDTOBuilder uploadedBy(String uploadedBy) {
            this.uploadedBy = uploadedBy;
            return this;
        }

        public CertificateDTOBuilder uploadedAt(LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
            return this;
        }

        public CertificateDTOBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public CertificateDTOBuilder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public CertificateDTOBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public CertificateDTOBuilder thumbprint(String thumbprint) {
            this.thumbprint = thumbprint;
            return this;
        }

        public CertificateDTOBuilder isValid(Boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public CertificateDTOBuilder sizeInBytes(Long sizeInBytes) {
            this.sizeInBytes = sizeInBytes;
            return this;
        }

        public CertificateDTOBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public CertificateDTOBuilder hasPassword(Boolean hasPassword) {
            this.hasPassword = hasPassword;
            return this;
        }

        public CertificateDTO build() {
            return new CertificateDTO(id, name, format, type, uploadedBy, uploadedAt, expiresAt, issuer, subject, thumbprint, isValid, sizeInBytes, fileName, hasPassword);
        }
    }
}
