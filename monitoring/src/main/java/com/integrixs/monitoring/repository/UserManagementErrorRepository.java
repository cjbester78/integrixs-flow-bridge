package com.integrixs.monitoring.repository;

import com.integrixs.monitoring.model.UserManagementError;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserManagementError entities.
 */
public interface UserManagementErrorRepository {

    UserManagementError save(UserManagementError error);

    Optional<UserManagementError> findById(UUID id);

    List<UserManagementError> findAll();

    List<UserManagementError> findByAction(String action);

    void deleteById(UUID id);

    long count();
}