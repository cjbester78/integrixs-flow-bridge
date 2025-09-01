package com.integrixs.data.repository;

import com.integrixs.data.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for Message entities
 */
@Repository
public interface JpaMessageRepository extends JpaRepository<Message, UUID>, JpaSpecificationExecutor<Message> {
    
    List<Message> findByStatus(Message.MessageStatus status);
    
    Page<Message> findByStatus(Message.MessageStatus status, Pageable pageable);
    
    List<Message> findByCorrelationId(String correlationId);
    
    @Query("SELECT m FROM Message m WHERE m.flow.id = :flowId")
    List<Message> findByFlowId(@Param("flowId") UUID flowId);
    
    long countByStatus(Message.MessageStatus status);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.flow.id = :flowId")
    long countByFlowId(@Param("flowId") UUID flowId);
    
    @Query("SELECT m FROM Message m WHERE m.status IN :statuses ORDER BY m.priority ASC, m.createdAt ASC")
    List<Message> findByStatusInOrderByPriorityAscCreatedAtAsc(@Param("statuses") List<Message.MessageStatus> statuses);
}