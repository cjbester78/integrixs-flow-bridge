package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.UserRepository;
import com.integrixs.data.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserRepository using JPA
 * Bridges between domain repository interface and JPA repository
 */
@Repository("backendUserRepository")
public class UserRepositoryImpl implements UserRepository {

    private final com.integrixs.data.repository.UserRepository jpaRepository;

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        User user = jpaRepository.findByUsername(username);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        User user = jpaRepository.findByEmail(email);
        return Optional.ofNullable(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public void delete(User user) {
        jpaRepository.delete(user);
    }

    @Override
    public long countByRole(String role) {
        return jpaRepository.countByRole(role);
    }
}
