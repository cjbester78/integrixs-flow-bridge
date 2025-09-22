package com.integrixs.data.repository;

import com.integrixs.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    User findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByRole(String role);
    
    List<User> findByTenantId(UUID tenantId);
    
    long countByTenantId(UUID tenantId);
}
