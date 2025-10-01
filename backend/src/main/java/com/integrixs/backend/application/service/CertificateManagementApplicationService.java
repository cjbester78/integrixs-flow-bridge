package com.integrixs.backend.application.service;

import com.integrixs.data.sql.repository.CertificateSqlRepository;
import com.integrixs.backend.domain.service.CertificateManagementService;
import com.integrixs.backend.exception.ConflictException;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.backend.infrastructure.storage.CertificateStorageService;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.Certificate;
import com.integrixs.shared.dto.certificate.CertificateDTO;
import com.integrixs.shared.dto.certificate.CertificateUploadRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for certificate management
 * Orchestrates certificate operations across domain services
 */
@Service
public class CertificateManagementApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CertificateManagementApplicationService.class);
    private final CertificateSqlRepository certificateRepository;
    private final CertificateManagementService certificateManagementService;
    private final CertificateStorageService certificateStorageService;
    private final AuditTrailService auditTrailService;

    public CertificateManagementApplicationService(CertificateSqlRepository certificateRepository,
                                                   CertificateManagementService certificateManagementService,
                                                   CertificateStorageService certificateStorageService,
                                                   AuditTrailService auditTrailService) {
        this.certificateRepository = certificateRepository;
        this.certificateManagementService = certificateManagementService;
        this.certificateStorageService = certificateStorageService;
        this.auditTrailService = auditTrailService;
    }

    /**
     * Upload and save a new certificate
     */
    public CertificateDTO saveCertificate(CertificateUploadRequestDTO dto, MultipartFile file) {
        log.info("Saving certificate: {}", dto.getName());

        try {
            // Check for duplicate name
            if(certificateRepository.existsByName(dto.getName())) {
                throw new ConflictException("Certificate already exists with name: " + dto.getName());
            }

            // Read file content
            byte[] content = file.getBytes();

            // Validate certificate file
            certificateManagementService.validateCertificateFile(
                file.getOriginalFilename(),
                content,
                dto.getFormat()
           );

            // Check if password is required
            if(certificateManagementService.requiresPassword(dto.getFormat()) &&
                (dto.getPassword() == null || dto.getPassword().trim().isEmpty())) {
                throw new IllegalArgumentException("Password is required for " + dto.getFormat() + " format");
            }

            // Create certificate entity
            Certificate certificate = certificateManagementService.createCertificate(
                dto.getName(),
                dto.getFormat(),
                dto.getType(),
                file.getOriginalFilename(),
                dto.getPassword(),
                dto.getUploadedBy(),
                content
           );

            // Validate certificate
            certificateManagementService.validateCertificate(certificate);

            // Save to database
            Certificate savedCertificate = certificateRepository.save(certificate);

            // Save file to disk
            String storageFileName = certificateManagementService.generateStorageFileName(
                savedCertificate.getId().toString(),
                file.getOriginalFilename()
           );
            certificateStorageService.saveCertificateFile(storageFileName, content);

            // Audit trail
            auditTrailService.logCreate(
                "Certificate",
                savedCertificate.getId().toString(),
                savedCertificate
           );

            log.info("Certificate saved successfully: {}", savedCertificate.getId());

            return toDTO(savedCertificate);

        } catch(IOException e) {
            log.error("Failed to save certificate file", e);
            throw new RuntimeException("Failed to save certificate file", e);
        }
    }

    /**
     * Get all certificates
     */
    public List<CertificateDTO> getAllCertificates() {
        log.debug("Fetching all certificates");

        return certificateRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get certificates with pagination
     */
    public Page<CertificateDTO> getCertificates(int page, int limit) {
        log.debug("Fetching certificates - page: {}, limit: {}", page, limit);

        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<Certificate> certificatePage = certificateRepository.findAll(pageRequest);

        return certificatePage.map(this::toDTO);
    }

    /**
     * Get certificate by ID
     */
    public CertificateDTO getCertificateById(String id) {
        log.debug("Fetching certificate by id: {}", id);

        UUID certificateId = UUID.fromString(id);
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));

        return toDTO(certificate);
    }

    /**
     * Get certificate entity by ID(for internal use)
     */
    public Certificate getCertificate(String id) {
        UUID certificateId = UUID.fromString(id);
        return certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
    }

    /**
     * Get certificate file content
     */
    public byte[] getCertificateContent(String id) {
        log.debug("Fetching certificate content for id: {}", id);

        Certificate certificate = getCertificate(id);

        // Try to read from file system first
        String storageFileName = certificateManagementService.generateStorageFileName(
            certificate.getId().toString(),
            certificate.getFileName()
       );

        try {
            if(certificateStorageService.certificateFileExists(storageFileName)) {
                return certificateStorageService.readCertificateFile(storageFileName);
            }
        } catch(IOException e) {
            log.warn("Failed to read certificate from file system, returning from database", e);
        }

        // Return from database if file not found
        return certificate.getContent();
    }

    /**
     * Delete certificate
     */
    public void deleteCertificate(String id, String deletedBy) {
        log.info("Deleting certificate: {}", id);

        UUID certificateId = UUID.fromString(id);
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));

        // Delete from database
        certificateRepository.deleteById(certificateId);

        // Delete file from disk
        String storageFileName = certificateManagementService.generateStorageFileName(
            certificate.getId().toString(),
            certificate.getFileName()
       );

        try {
            certificateStorageService.deleteCertificateFile(storageFileName);
        } catch(IOException e) {
            log.error("Failed to delete certificate file from disk", e);
            // Don't fail the transaction if file deletion fails
        }

        // Audit trail
        auditTrailService.logDelete(
            "Certificate",
            certificateId.toString(),
            certificate
       );

        log.info("Certificate deleted successfully: {}", certificateId);
    }

    /**
     * Get certificates uploaded by a specific user
     */
    public List<CertificateDTO> getCertificatesByUploader(String uploadedBy) {
        log.debug("Fetching certificates uploaded by: {}", uploadedBy);

        return certificateRepository.findByUploadedBy(uploadedBy).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Certificate entity to DTO
     */
    private CertificateDTO toDTO(Certificate certificate) {
        CertificateDTO dto = new CertificateDTO();
        dto.setId(certificate.getId().toString());
        dto.setName(certificate.getName());
        dto.setFormat(certificate.getFormat());
        dto.setType(certificate.getType());
        dto.setFileName(certificate.getFileName());
        dto.setUploadedBy(certificate.getUploadedBy());
        dto.setUploadedAt(certificate.getUploadedAt());
        // Don't include password in DTO for security
        dto.setHasPassword(certificate.getPassword() != null && !certificate.getPassword().isEmpty());
        return dto;
    }
}
