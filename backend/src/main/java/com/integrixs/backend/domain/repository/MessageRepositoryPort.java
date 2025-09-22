package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Message repository port - domain layer
 * Acts as a port in hexagonal architecture for message persistence operations
 */
public interface MessageRepositoryPort {

    Optional<Message> findById(UUID id);

    Message save(Message message);

    void deleteById(UUID id);

    Page<Message> findAll(Specification<Message> spec, Pageable pageable);

    List<Message> findByStatus(Message.MessageStatus status);

    List<Message> findPendingMessages(int limit);

    List<Message> findByCorrelationId(String correlationId);

    Optional<Message> findByCorrelationIdAndType(String correlationId, String type);

    long countByStatus(Message.MessageStatus status);

    long countByFlowId(UUID flowId);

    void updateStatus(UUID id, Message.MessageStatus status);
}
