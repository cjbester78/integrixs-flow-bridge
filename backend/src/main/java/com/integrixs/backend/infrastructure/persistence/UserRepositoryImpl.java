package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.UserRepositoryPort;
import com.integrixs.data.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserRepository using JPA
 * Bridges between domain repository interface and JPA repository
 */
@Repository("backendUserRepository")
public class UserRepositoryImpl implements UserRepositoryPort {

    private final com.integrixs.data.repository.UserRepository jpaRepository;
    
    public UserRepositoryImpl(com.integrixs.data.repository.UserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(jpaRepository.findByEmail(email));
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
