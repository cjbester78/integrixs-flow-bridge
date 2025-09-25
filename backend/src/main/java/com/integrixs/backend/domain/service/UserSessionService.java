package com.integrixs.backend.domain.service;

import com.integrixs.shared.exceptions.SessionException;
import com.integrixs.data.model.User;
import com.integrixs.data.model.UserSession;
import com.integrixs.data.sql.repository.UserSessionSqlRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for user session management
 */
@Service
public class UserSessionService {

    private final UserSessionSqlRepository sessionRepository;

    public UserSessionService(UserSessionSqlRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Create a new user session
     */
    public UserSession createSession(User user, String refreshToken, String ipAddress, String userAgent) {
        // Invalidate any existing sessions for this user
        sessionRepository.findByUserAndExpiresAtAfter(user, LocalDateTime.now())
                .forEach(session -> {
                    session.setInvalidated(true);
                    sessionRepository.save(session);
                });

        // Create new session
        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setCreatedAt(LocalDateTime.now());
        session.setLastUsedAt(LocalDateTime.now());
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);

        return sessionRepository.save(session);
    }

    /**
     * Validate and update session last used time
     */
    public UserSession validateAndRefreshSession(String refreshToken) {
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new SessionException("Invalid refresh token"));

        if(session.isInvalidated()) {
            throw new SessionException("Session has been invalidated");
        }

        if(session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new SessionException("Session has expired");
        }

        // Update last used time
        session.setLastUsedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    /**
     * Invalidate a session
     */
    public void invalidateSession(String refreshToken) {
        sessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(session -> {
                    session.setInvalidated(true);
                    sessionRepository.save(session);
                });
    }

    /**
     * Clean up expired sessions
     */
    public void cleanupExpiredSessions() {
        sessionRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
