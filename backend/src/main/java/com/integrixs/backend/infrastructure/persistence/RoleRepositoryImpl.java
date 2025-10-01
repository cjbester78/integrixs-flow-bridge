package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.RoleRepositoryPort;
import com.integrixs.data.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of RoleRepository using SQL
 * Bridges between domain repository interface and SQL repository
 */
@Repository("domainRoleRepository")
public class RoleRepositoryImpl implements RoleRepositoryPort {

    private final com.integrixs.data.sql.repository.RoleSqlRepository sqlRepository;

    public RoleRepositoryImpl(com.integrixs.data.sql.repository.RoleSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public Role save(Role role) {
        return sqlRepository.save(role);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return sqlRepository.findByName(name);
    }

    @Override
    public Page<Role> findAll(Pageable pageable) {
        return sqlRepository.findAll(pageable);
    }

    @Override
    public List<Role> findAll() {
        return sqlRepository.findAll();
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
    public boolean existsByNameAndIdNot(String name, UUID id) {
        // Custom implementation for this method
        Optional<Role> existing = sqlRepository.findByName(name);
        return existing.isPresent() && !existing.get().getId().equals(id);
    }

    @Override
    public long count() {
        return sqlRepository.count();
    }
}
