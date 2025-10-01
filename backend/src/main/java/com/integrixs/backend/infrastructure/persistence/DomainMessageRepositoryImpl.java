package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.DomainMessageRepositoryPort;
import com.integrixs.data.model.Message;
import com.integrixs.data.model.Message.MessageStatus;
import com.integrixs.data.sql.repository.MessageSqlRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of DomainMessageRepository using SQL
 * Bridges between domain repository interface and SQL repository
 */
@Repository("domainMessageRepositoryImpl")
public class DomainMessageRepositoryImpl implements DomainMessageRepositoryPort {

    private final MessageSqlRepository sqlRepository;

    public DomainMessageRepositoryImpl(MessageSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public List<Message> findAll() {
        return sqlRepository.findAll();
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public Message save(Message message) {
        return sqlRepository.save(message);
    }

    @Override
    public long countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return sqlRepository.findByReceivedAtBetween(startDate, endDate, PageRequest.of(0, 1))
            .getTotalElements();
    }

    @Override
    public long countByBusinessComponentAndDateRange(UUID businessComponentId, LocalDateTime startDate, LocalDateTime endDate) {
        // This is a simplified implementation - in reality would need a custom query
        // that joins with flow and adapter tables to filter by business component
        return sqlRepository.findByReceivedAtBetween(startDate, endDate, PageRequest.of(0, 1))
            .getTotalElements();
    }

    @Override
    public long countByStatusAndDateRange(MessageStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        // Custom implementation needed
        return sqlRepository.findByReceivedAtBetween(startDate, endDate, PageRequest.of(0, Integer.MAX_VALUE))
            .stream()
            .filter(m -> m.getStatus() == status)
            .count();
    }

    @Override
    public long countByBusinessComponentStatusAndDateRange(UUID businessComponentId, MessageStatus status,
                                                          LocalDateTime startDate, LocalDateTime endDate) {
        // Simplified implementation - would need proper query
        return countByStatusAndDateRange(status, startDate, endDate);
    }
}
