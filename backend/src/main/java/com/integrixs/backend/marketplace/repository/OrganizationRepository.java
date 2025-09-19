package com.integrixs.backend.marketplace.repository;

import com.integrixs.backend.marketplace.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for organizations
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}