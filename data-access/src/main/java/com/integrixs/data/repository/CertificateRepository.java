package com.integrixs.data.repository;

import com.integrixs.data.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
/**
 * Repository interface for CertificateRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    
    Optional<Certificate> findByName(String name);
    
    List<Certificate> findByUploadedBy(String uploadedBy);
    
    boolean existsByName(String name);
}