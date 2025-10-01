package com.integrixs.backend.domain.service;

import com.integrixs.data.model.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Domain service for certificate management
 * Contains core business logic for certificate operations
 */
@Service
public class CertificateManagementService {

    private static final Logger log = LoggerFactory.getLogger(CertificateManagementService.class);

    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("PEM", "DER", "P12", "PKCS12", "CER", "CRT");
    private static final List<String> SUPPORTED_TYPES = Arrays.asList("X.509", "PKCS12", "JKS");
    private static final long MAX_CERTIFICATE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Validate certificate data
     */
    public void validateCertificate(Certificate certificate) {
        if(certificate.getName() == null || certificate.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Certificate name is required");
        }

        if(certificate.getName().length() > 255) {
            throw new IllegalArgumentException("Certificate name cannot exceed 255 characters");
        }

        if(certificate.getFormat() == null || certificate.getFormat().trim().isEmpty()) {
            throw new IllegalArgumentException("Certificate format is required");
        }

        if(!SUPPORTED_FORMATS.contains(certificate.getFormat().toUpperCase())) {
            throw new IllegalArgumentException("Unsupported certificate format: " + certificate.getFormat() +
                ". Supported formats: " + String.join(", ", SUPPORTED_FORMATS));
        }

        if(certificate.getType() == null || certificate.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Certificate type is required");
        }

        if(!SUPPORTED_TYPES.contains(certificate.getType())) {
            throw new IllegalArgumentException("Unsupported certificate type: " + certificate.getType() +
                ". Supported types: " + String.join(", ", SUPPORTED_TYPES));
        }

        if(certificate.getFileName() == null || certificate.getFileName().trim().isEmpty()) {
            throw new IllegalArgumentException("Certificate file name is required");
        }
    }

    /**
     * Validate certificate file
     */
    public void validateCertificateFile(String fileName, byte[] content, String format) {
        if(content == null || content.length == 0) {
            throw new IllegalArgumentException("Certificate file content is empty");
        }

        if(content.length > MAX_CERTIFICATE_SIZE) {
            throw new IllegalArgumentException("Certificate file size exceeds maximum allowed size of " +
                (MAX_CERTIFICATE_SIZE / 1024 / 1024) + "MB");
        }

        // Validate file extension matches format
        String extension = getFileExtension(fileName);
        if(!isValidExtensionForFormat(extension, format)) {
            throw new IllegalArgumentException("File extension '" + extension +
                "' does not match certificate format '" + format + "'");
        }

        // For X.509 certificates, try to parse and validate
        if("X.509".equals(format) || "PEM".equals(format) || "DER".equals(format)) {
            validateX509Certificate(content, format);
        }
    }

    /**
     * Validate X.509 certificate
     */
    private void validateX509Certificate(byte[] content, String format) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            if("PEM".equals(format)) {
                // PEM format needs to be decoded from base64
                String pemContent = new String(content);
                if(!pemContent.contains("-----BEGIN CERTIFICATE-----")) {
                    throw new IllegalArgumentException("Invalid PEM certificate format");
                }
            }

            X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(content));

            // Check if certificate is expired
            try {
                cert.checkValidity();
            } catch(Exception e) {
                log.warn("Certificate is expired or not yet valid: {}", e.getMessage());
                // Don't throw exception, just warn - user might want to upload expired certs
            }

            log.debug("Certificate validated successfully. Subject: {}, Issuer: {}",
                cert.getSubjectDN(), cert.getIssuerDN());

        } catch(Exception e) {
            throw new IllegalArgumentException("Invalid certificate file: " + e.getMessage(), e);
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        if(fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Check if file extension is valid for the format
     */
    private boolean isValidExtensionForFormat(String extension, String format) {
        if(format == null) return false;

        return switch(format.toUpperCase()) {
            case "PEM" -> "pem".equals(extension) || "crt".equals(extension) || "cer".equals(extension);
            case "DER" -> "der".equals(extension) || "cer".equals(extension);
            case "P12", "PKCS12" -> "p12".equals(extension) || "pfx".equals(extension) || "pkcs12".equals(extension);
            case "JKS" -> "jks".equals(extension);
            case "X.509" -> "crt".equals(extension) || "cer".equals(extension) || "pem".equals(extension) || "der".equals(extension);
            default -> false;
        };
    }

    /**
     * Generate storage file name for certificate
     */
    public String generateStorageFileName(String certificateId, String originalFileName) {
        String extension = getFileExtension(originalFileName);
        if(extension.isEmpty()) {
            extension = "crt"; // Default extension
        }
        return certificateId + "." + extension;
    }

    /**
     * Check if certificate needs password
     */
    public boolean requiresPassword(String format) {
        return "P12".equals(format) || "PKCS12".equals(format) || "JKS".equals(format);
    }

    /**
     * Create certificate metadata
     */
    public Certificate createCertificate(String name, String format, String type, String fileName,
                                       String password, String uploadedBy, byte[] content) {
        Certificate certificate = new Certificate();
        certificate.setName(name);
        certificate.setFormat(format.toUpperCase());
        certificate.setType(type);
        certificate.setFileName(fileName);
        certificate.setPassword(password);
        certificate.setUploadedBy(uploadedBy);
        certificate.setContent(content);
        certificate.setUploadedAt(LocalDateTime.now());

        return certificate;
    }
}
