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
 * Implementation of RoleRepository using JPA
 * Bridges between domain repository interface and JPA repository
 */
@Repository("domainRoleRepository")
public class RoleRepositoryImpl implements RoleRepositoryPort {

    private final com.integrixs.data.repository.RoleRepository jpaRepository;
    
    public RoleRepositoryImpl(com.integrixs.data.repository.RoleRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Role save(Role role) {
        return jpaRepository.save(role);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRepository.findByName(name);
    }

    @Override
    public Page<Role> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll();
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
    public boolean existsByNameAndIdNot(String name, UUID id) {
        // This would need a custom query method in JPA repository
        Optional<Role> existing = jpaRepository.findByName(name);
        return existing.isPresent() && !existing.get().getId().equals(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
