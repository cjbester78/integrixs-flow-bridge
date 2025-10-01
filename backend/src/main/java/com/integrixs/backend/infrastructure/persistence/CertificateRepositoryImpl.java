package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.CertificateRepositoryPort;
import com.integrixs.data.model.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of CertificateRepository using native SQL
 * Bridges between domain repository interface and SQL repository
 */
@Repository("domainCertificateRepository")
public class CertificateRepositoryImpl implements CertificateRepositoryPort {

    private final com.integrixs.data.sql.repository.CertificateSqlRepository sqlRepository;

    public CertificateRepositoryImpl(com.integrixs.data.sql.repository.CertificateSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public Certificate save(Certificate certificate) {
        return sqlRepository.save(certificate);
    }

    @Override
    public Optional<Certificate> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public Optional<Certificate> findByName(String name) {
        return sqlRepository.findByName(name);
    }

    @Override
    public List<Certificate> findAll() {
        return sqlRepository.findAll();
    }

    @Override
    public Page<Certificate> findAll(Pageable pageable) {
        return sqlRepository.findAll(pageable);
    }

    @Override
    public List<Certificate> findByUploadedBy(String uploadedBy) {
        return sqlRepository.findByUploadedBy(uploadedBy);
    }

    @Override
    public void deleteById(UUID id) {
        sqlRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return sqlRepository.existsByName(name);
    }

    @Override
    public boolean existsById(UUID id) {
        return sqlRepository.existsById(id);
    }

    @Override
    public long count() {
        return sqlRepository.count();
    }
}
