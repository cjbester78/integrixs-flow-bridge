package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.UserRepositoryPort;
import com.integrixs.data.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserSqlRepository using native SQL
 * Bridges between domain repository interface and SQL repository
 */
@Repository("backendUserRepository")
public class UserRepositoryImpl implements UserRepositoryPort {

    private final com.integrixs.data.sql.repository.UserSqlRepository sqlRepository;

    public UserRepositoryImpl(com.integrixs.data.sql.repository.UserSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return sqlRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return sqlRepository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return sqlRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return sqlRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return sqlRepository.save(user);
    }

    @Override
    public void delete(User user) {
        sqlRepository.delete(user);
    }

    @Override
    public long countByRole(String role) {
        return sqlRepository.countByRole(role);
    }
}
