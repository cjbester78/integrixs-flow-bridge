package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for certificates
 */
public interface CertificateRepositoryPort {

    Certificate save(Certificate certificate);

    Optional<Certificate> findById(UUID id);

    Optional<Certificate> findByName(String name);

    List<Certificate> findAll();

    Page<Certificate> findAll(Pageable pageable);

    List<Certificate> findByUploadedBy(String uploadedBy);

    void deleteById(UUID id);

    boolean existsByName(String name);

    boolean existsById(UUID id);

    long count();
}
