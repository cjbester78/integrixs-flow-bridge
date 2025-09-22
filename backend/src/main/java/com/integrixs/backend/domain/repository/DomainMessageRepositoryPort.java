package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.Message;
import com.integrixs.data.model.Message.MessageStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain message repository port - domain layer
 * Acts as a port in hexagonal architecture for domain-specific message persistence operations
 */
public interface DomainMessageRepositoryPort {

    List<Message> findAll();

    Optional<Message> findById(UUID id);

    Message save(Message message);

    long countByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    long countByBusinessComponentAndDateRange(UUID businessComponentId, LocalDateTime startDate, LocalDateTime endDate);

    long countByStatusAndDateRange(MessageStatus status, LocalDateTime startDate, LocalDateTime endDate);

    long countByBusinessComponentStatusAndDateRange(UUID businessComponentId, MessageStatus status,
                                                   LocalDateTime startDate, LocalDateTime endDate);
}
