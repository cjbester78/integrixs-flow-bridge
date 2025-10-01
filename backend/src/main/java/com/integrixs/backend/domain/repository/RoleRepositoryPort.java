package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for roles
 */
public interface RoleRepositoryPort {

    Role save(Role role);

    Optional<Role> findById(UUID id);

    Optional<Role> findByName(String name);

    Page<Role> findAll(Pageable pageable);

    List<Role> findAll();

    void deleteById(UUID id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    long count();
}
