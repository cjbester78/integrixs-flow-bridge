package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.CertificateRepository;
import com.integrixs.data.model.Certificate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of CertificateRepository using JPA
 * Bridges between domain repository interface and JPA repository
 */
@Repository("domainCertificateRepository")
@RequiredArgsConstructor
public class CertificateRepositoryImpl implements CertificateRepository {
    
    private final com.integrixs.data.repository.CertificateRepository jpaRepository;
    
    @Override
    public Certificate save(Certificate certificate) {
        return jpaRepository.save(certificate);
    }
    
    @Override
    public Optional<Certificate> findById(UUID id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Optional<Certificate> findByName(String name) {
        return jpaRepository.findByName(name);
    }
    
    @Override
    public List<Certificate> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public Page<Certificate> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
    
    @Override
    public List<Certificate> findByUploadedBy(String uploadedBy) {
        return jpaRepository.findByUploadedBy(uploadedBy);
    }
    
    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
}