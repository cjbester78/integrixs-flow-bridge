package com.integrixs.data.repository;

import com.integrixs.data.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entities
 */
public interface UserRepository {

    User save(User user);

    User update(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAll();

    Page<User> findAll(Pageable pageable);

    List<User> findByStatus(String status);

    List<User> findByRole(String role);

    Page<User> findByStatusAndRole(String status, String role, Pageable pageable);

    void deleteById(UUID id);

    long count();

    long countByStatus(String status);

    long countByRole(String role);
}