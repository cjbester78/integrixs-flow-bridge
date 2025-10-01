package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.SystemLogRepositoryPort;
import com.integrixs.data.model.SystemLog;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of SystemLogRepository using SQL
 * Bridges between domain repository interface and SQL repository
 */
@Repository("domainSystemLogRepository")
public class SystemLogRepositoryImpl implements SystemLogRepositoryPort {

    private final com.integrixs.data.sql.repository.SystemLogSqlRepository sqlRepository;

    public SystemLogRepositoryImpl(com.integrixs.data.sql.repository.SystemLogSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public SystemLog save(SystemLog log) {
        return sqlRepository.save(log);
    }

    @Override
    public Optional<SystemLog> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public List<SystemLog> findAll() {
        return sqlRepository.findAll();
    }

    @Override
    public List<SystemLog> findBySource(String source) {
        // Custom implementation using in-memory filtering
        return sqlRepository.findAll().stream()
            .filter(log -> source.equals(log.getSource()))
            .toList();
    }

    @Override
    public List<SystemLog> findByLevel(SystemLog.LogLevel level) {
        // Custom implementation using in-memory filtering
        return sqlRepository.findAll().stream()
            .filter(log -> level.equals(log.getLevel()))
            .toList();
    }

    @Override
    public List<SystemLog> findByUserId(UUID userId) {
        // Custom implementation using in-memory filtering
        return sqlRepository.findAll().stream()
            .filter(log -> userId != null && userId.equals(log.getUserId()))
            .toList();
    }

    @Override
    public List<SystemLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // Custom implementation using in-memory filtering
        return sqlRepository.findAll().stream()
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
        // Custom implementation using in-memory filtering
        return sqlRepository.findAll().stream()
            .filter(log -> domainType.equals(log.getDomainType()) &&
                          domainReferenceId.equals(log.getDomainReferenceId()))
            .toList();
    }

    @Override
    public List<SystemLog> findByCorrelationId(String correlationId) {
        return sqlRepository.findByCorrelationId(correlationId);
    }

    @Override
    public long countByLevelAndDateRange(SystemLog.LogLevel level, LocalDateTime startDate, LocalDateTime endDate) {
        return sqlRepository.countByLevelAndTimestampAfter(level, startDate);
    }
}
