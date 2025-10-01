package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.SystemLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * System log repository port - domain layer
 * Acts as a port in hexagonal architecture for system log persistence operations
 */
public interface SystemLogRepositoryPort {

    SystemLog save(SystemLog log);

    Optional<SystemLog> findById(UUID id);

    List<SystemLog> findAll();

    List<SystemLog> findBySource(String source);

    List<SystemLog> findByLevel(SystemLog.LogLevel level);

    List<SystemLog> findByUserId(UUID userId);

    List<SystemLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<SystemLog> findByDomainTypeAndReferenceId(String domainType, String domainReferenceId);

    List<SystemLog> findByCorrelationId(String correlationId);

    long countByLevelAndDateRange(SystemLog.LogLevel level, LocalDateTime startDate, LocalDateTime endDate);
}
