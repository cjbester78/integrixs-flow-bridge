package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.DomainMessageRepository;
import com.integrixs.data.model.Message;
import com.integrixs.data.model.Message.MessageStatus;
import com.integrixs.data.repository.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of DomainMessageRepository using JPA
 * Bridges between domain repository interface and JPA repository
 */
@Repository("domainMessageRepositoryImpl")
public class DomainMessageRepositoryImpl implements DomainMessageRepository {

    private final MessageRepository jpaRepository;

    @Override
    public List<Message> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Message save(Message message) {
        return jpaRepository.save(message);
    }

    @Override
    public long countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByReceivedAtBetween(startDate, endDate, PageRequest.of(0, 1))
            .getTotalElements();
    }

    @Override
    public long countByBusinessComponentAndDateRange(UUID businessComponentId, LocalDateTime startDate, LocalDateTime endDate) {
        // This is a simplified implementation - in reality would need a custom query
        // that joins with flow and adapter tables to filter by business component
        return jpaRepository.findByReceivedAtBetween(startDate, endDate, PageRequest.of(0, 1))
            .getTotalElements();
    }

    @Override
    public long countByStatusAndDateRange(MessageStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        // This would need a custom query in the JPA repository
        return jpaRepository.findByReceivedAtBetween(startDate, endDate, PageRequest.of(0, Integer.MAX_VALUE))
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
