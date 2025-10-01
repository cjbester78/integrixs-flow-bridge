package com.integrixs.backend.api.controller;

import com.integrixs.backend.application.service.CertificateManagementApplicationService;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.shared.dto.certificate.CertificateDTO;
import com.integrixs.shared.dto.certificate.CertificateUploadRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for certificate management
 * Handles certificate CRUD operations
 */
@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "*")
public class CertificateManagementController {

    private static final Logger log = LoggerFactory.getLogger(CertificateManagementController.class);

    private final CertificateManagementApplicationService certificateManagementApplicationService;

    public CertificateManagementController(CertificateManagementApplicationService certificateManagementApplicationService) {
        this.certificateManagementApplicationService = certificateManagementApplicationService;
    }

    /**
     * Upload a new certificate
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<CertificateDTO> uploadCertificate(
            @RequestPart("certificate") @Valid CertificateUploadRequestDTO certificateData,
            @RequestPart("file") MultipartFile file) {

        log.info("Uploading certificate: {}", certificateData.getName());

        // Set uploaded by from current user
        String currentUser = SecurityUtils.getCurrentUsernameStatic();
        certificateData.setUploadedBy(currentUser);

        CertificateDTO savedCertificate = certificateManagementApplicationService.saveCertificate(certificateData, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedCertificate);
    }

    /**
     * Get all certificates
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<List<CertificateDTO>> getAllCertificates() {
        log.debug("Getting all certificates");
        List<CertificateDTO> certificates = certificateManagementApplicationService.getAllCertificates();
        return ResponseEntity.ok(certificates);
    }

    /**
     * Get certificates with pagination
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<Page<CertificateDTO>> getCertificates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {

        log.debug("Getting certificates - page: {}, limit: {}", page, limit);
        Page<CertificateDTO> certificates = certificateManagementApplicationService.getCertificates(page, limit);
        return ResponseEntity.ok(certificates);
    }

    /**
     * Get certificate by ID
     */
    @GetMapping("/ {id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<CertificateDTO> getCertificateById(@PathVariable String id) {
        log.debug("Getting certificate by id: {}", id);
        CertificateDTO certificate = certificateManagementApplicationService.getCertificateById(id);
        return ResponseEntity.ok(certificate);
    }

    /**
     * Download certificate file
     */
    @GetMapping("/ {id}/download")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable String id) {
        log.info("Downloading certificate: {}", id);

        CertificateDTO certificate = certificateManagementApplicationService.getCertificateById(id);
        byte[] content = certificateManagementApplicationService.getCertificateContent(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = \"" + certificate.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(content);
    }

    /**
     * Delete certificate
     */
    @DeleteMapping("/ {id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteCertificate(@PathVariable String id) {
        log.info("Deleting certificate: {}", id);

        String currentUser = SecurityUtils.getCurrentUsernameStatic();
        certificateManagementApplicationService.deleteCertificate(id, currentUser);

        return ResponseEntity.noContent().build();
    }

    /**
     * Get certificates uploaded by current user
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<List<CertificateDTO>> getMyCertificates() {
        String currentUser = SecurityUtils.getCurrentUsernameStatic();
        log.debug("Getting certificates uploaded by: {}", currentUser);

        List<CertificateDTO> certificates = certificateManagementApplicationService.getCertificatesByUploader(currentUser);
        return ResponseEntity.ok(certificates);
    }
}
