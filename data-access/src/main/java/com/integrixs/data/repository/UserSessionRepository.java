package com.integrixs.data.repository;

import com.integrixs.data.model.UserSession;
import com.integrixs.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
/**
 * Repository interface for UserSessionRepository.
 * Provides CRUD operations and query methods for the corresponding entity.
 */
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    void deleteByRefreshToken(String refreshToken);

    void deleteAllByUser_Id(UUID userId);

    List<UserSession> findByUserAndExpiresAtAfter(User user, LocalDateTime expiresAt);

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
