package com.integrixs.data.repository;

import com.integrixs.data.model.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Organization entities
 */
@Repository
public interface OrganizationRepository {

    Organization save(Organization organization);

    Optional<Organization> findById(UUID id);

    Page<Organization> findAll(Pageable pageable);

    Page<Organization> findByVerifiedTrue(Pageable pageable);

    Optional<Organization> findByName(String name);

    Optional<Organization> findByEmail(String email);

    List<Organization> findByVerifiedFalse();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    long count();
}