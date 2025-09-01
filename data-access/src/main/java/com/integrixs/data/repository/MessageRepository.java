package com.integrixs.data.repository;

import com.integrixs.data.model.Message;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.FlowExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    
    Optional<Message> findByMessageId(String messageId);
    
    Page<Message> findByFlow(IntegrationFlow flow, Pageable pageable);
    
    Page<Message> findByFlowExecution(FlowExecution flowExecution, Pageable pageable);
    
    List<Message> findByStatus(Message.MessageStatus status);
    
    @Query("SELECT m FROM Message m WHERE m.status = :status ORDER BY m.priority DESC, m.receivedAt ASC")
    List<Message> findByStatusOrderByPriorityAndReceivedAt(@Param("status") Message.MessageStatus status);
    
    @Query("SELECT m FROM Message m WHERE m.status IN :statuses AND m.retryCount < :maxRetries")
    List<Message> findRetryableMessages(@Param("statuses") List<Message.MessageStatus> statuses, 
                                       @Param("maxRetries") Integer maxRetries);
    
    @Query("SELECT m FROM Message m WHERE m.correlationId = :correlationId ORDER BY m.receivedAt")
    List<Message> findByCorrelationId(@Param("correlationId") String correlationId);
    
    @Query("SELECT m FROM Message m WHERE m.receivedAt BETWEEN :startDate AND :endDate")
    Page<Message> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate, 
                                  Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.flow.id = :flowId AND m.status = :status")
    Long countByFlowIdAndStatus(@Param("flowId") UUID flowId, @Param("status") Message.MessageStatus status);
    
    @Query("SELECT m FROM Message m WHERE m.status IN :statuses ORDER BY m.priority DESC, m.createdAt ASC")
    List<Message> findByStatusInOrderByPriorityDescCreatedAtAsc(@Param("statuses") List<Message.MessageStatus> statuses);
    
    @Modifying
    @Query("UPDATE Message m SET m.messageContent = :content WHERE m.id = :id")
    void updateMessageContent(@Param("id") UUID id, @Param("content") String content);
    
    @Query("SELECT m.messageContent FROM Message m WHERE m.id = :id")
    String findMessageContentById(@Param("id") UUID id);
}