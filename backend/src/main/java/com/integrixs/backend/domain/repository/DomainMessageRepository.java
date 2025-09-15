package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.Message;
import com.integrixs.data.model.Message.MessageStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for messages
 */
public interface DomainMessageRepository {

    List<Message> findAll();

    Optional<Message> findById(UUID id);

    Message save(Message message);

    long countByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    long countByBusinessComponentAndDateRange(UUID businessComponentId, LocalDateTime startDate, LocalDateTime endDate);

    long countByStatusAndDateRange(MessageStatus status, LocalDateTime startDate, LocalDateTime endDate);

    long countByBusinessComponentStatusAndDateRange(UUID businessComponentId, MessageStatus status,
                                                   LocalDateTime startDate, LocalDateTime endDate);
}
