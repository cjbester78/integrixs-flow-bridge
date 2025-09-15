package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.SystemLogRepository;
import com.integrixs.data.model.SystemLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of SystemLogRepository using JPA
 * Bridges between domain repository interface and JPA repository
 */
@Repository("domainSystemLogRepository")
@RequiredArgsConstructor
public class SystemLogRepositoryImpl implements SystemLogRepository {

    private final com.integrixs.data.repository.SystemLogRepository jpaRepository;

    @Override
    public SystemLog save(SystemLog log) {
        return jpaRepository.save(log);
    }

    @Override
    public Optional<SystemLog> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<SystemLog> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<SystemLog> findBySource(String source) {
        // This would need a custom query method in JPA repository
        return jpaRepository.findAll().stream()
            .filter(log -> source.equals(log.getSource()))
            .toList();
    }

    @Override
    public List<SystemLog> findByLevel(SystemLog.LogLevel level) {
        // This would need a custom query method in JPA repository
        return jpaRepository.findAll().stream()
            .filter(log -> level.equals(log.getLevel()))
            .toList();
    }

    @Override
    public List<SystemLog> findByUserId(UUID userId) {
        // This would need a custom query method in JPA repository
        return jpaRepository.findAll().stream()
            .filter(log -> userId != null && userId.equals(log.getUserId()))
            .toList();
    }

    @Override
    public List<SystemLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // This would need a custom query method in JPA repository
        return jpaRepository.findAll().stream()
            .filter(log -> {
                LocalDateTime timestamp = log.getTimestamp();
                return timestamp != null &&
                       timestamp.isAfter(startDate) &&
                       timestamp.isBefore(endDate);
            })
            .toList();
    }

    @Override
    public List<SystemLog> findByDomainTypeAndReferenceId(String domainType, String domainReferenceId) {
        // This would need a custom query method in JPA repository
        return jpaRepository.findAll().stream()
            .filter(log -> domainType.equals(log.getDomainType()) &&
                          domainReferenceId.equals(log.getDomainReferenceId()))
            .toList();
    }

    @Override
    public List<SystemLog> findByCorrelationId(String correlationId) {
        return jpaRepository.findByCorrelationId(correlationId);
    }

    @Override
    public long countByLevelAndDateRange(SystemLog.LogLevel level, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countByLevelAndTimestampAfter(level, startDate);
    }
}
