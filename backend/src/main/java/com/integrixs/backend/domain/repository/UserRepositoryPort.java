package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.User;
import java.util.UUID;
import java.util.Optional;

/**
 * User repository port - domain layer
 * Acts as a port in hexagonal architecture for user persistence operations
 */
public interface UserRepositoryPort {

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User save(User user);

    void delete(User user);

    long countByRole(String role);
}
