package com.integrixs.backend.infrastructure.repository;

import com.integrixs.backend.domain.repository.MessageRepository;
import com.integrixs.data.model.Message;
import com.integrixs.data.repository.JpaMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of MessageRepository using JPA
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class MessageRepositoryImpl implements MessageRepository {

    private final JpaMessageRepository jpaRepository;

    @Override
    public Optional<Message> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    @Transactional
    public Message save(Message message) {
        return jpaRepository.save(message);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Page<Message> findAll(Specification<Message> spec, Pageable pageable) {
        return jpaRepository.findAll(spec, pageable);
    }

    @Override
    public List<Message> findByStatus(Message.MessageStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<Message> findPendingMessages(int limit) {
        PageRequest pageRequest = PageRequest.of(
            0,
            limit,
            Sort.by(Sort.Direction.ASC, "priority").and(Sort.by(Sort.Direction.ASC, "createdAt"))
       );

        return jpaRepository.findByStatus(Message.MessageStatus.PENDING, pageRequest).getContent();
    }

    @Override
    public List<Message> findByCorrelationId(String correlationId) {
        return jpaRepository.findByCorrelationId(correlationId);
    }

    @Override
    public Optional<Message> findByCorrelationIdAndType(String correlationId, String type) {
        // Since we don't have a type field in Message, we'll use flow name as type
        List<Message> messages = jpaRepository.findByCorrelationId(correlationId);

        return messages.stream()
            .filter(msg -> msg.getFlow() != null && type.equals(msg.getFlow().getName()))
            .findFirst();
    }

    @Override
    public long countByStatus(Message.MessageStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public long countByFlowId(UUID flowId) {
        return jpaRepository.countByFlowId(flowId);
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, Message.MessageStatus status) {
        jpaRepository.findById(id).ifPresent(message -> {
            message.setStatus(status);
            jpaRepository.save(message);
        });
    }
}
