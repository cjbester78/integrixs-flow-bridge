package com.integrixs.backend.service;

import com.integrixs.data.model.Message;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.MessageRepository;
import com.integrixs.shared.dto.MessageStatsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageStatsService
 */
@ExtendWith(MockitoExtension.class)
class MessageStatsServiceTest {
    
    @Mock
    private MessageRepository messageRepository;
    
    @InjectMocks
    private OptimizedMessageStatsService messageStatsService;
    
    private List<Object[]> mockStatusCounts;
    
    @BeforeEach
    void setUp() {
        // Set up mock status counts
        mockStatusCounts = Arrays.asList(
            new Object[]{Message.MessageStatus.PROCESSING, 5L},
            new Object[]{Message.MessageStatus.PROCESSED, 80L},
            new Object[]{Message.MessageStatus.COMPLETED, 10L},
            new Object[]{Message.MessageStatus.FAILED, 3L},
            new Object[]{Message.MessageStatus.DEAD_LETTER, 2L}
        );
    }
    
    @Test
    void testGetMessageStats() {
        // Given
        when(messageRepository.countByStatusGrouped()).thenReturn(mockStatusCounts);
        when(messageRepository.calculateAverageProcessingTime()).thenReturn(1500.0); // 1.5 seconds
        
        // When
        MessageStatsDTO stats = messageStatsService.getMessageStats(null);
        
        // Then
        assertEquals(100, stats.getTotal()); // 5 + 80 + 10 + 3 + 2
        assertEquals(5, stats.getProcessing());
        assertEquals(90, stats.getSuccessful()); // 80 + 10
        assertEquals(5, stats.getFailed()); // 3 + 2
        assertEquals(94.74, stats.getSuccessRate(), 0.01); // 90/(90+5) * 100
        assertEquals(1500.0, stats.getAvgProcessingTime());
    }
    
    @Test
    void testGetMessageStatsWithFlowFilter() {
        // Given
        UUID flowId = UUID.randomUUID();
        Map<String, String> filters = Map.of("flowId", flowId.toString());
        
        when(messageRepository.countByStatusGrouped()).thenReturn(mockStatusCounts);
        when(messageRepository.calculateAverageProcessingTimeByFlowId(flowId)).thenReturn(2000.0);
        
        // When
        MessageStatsDTO stats = messageStatsService.getMessageStats(filters);
        
        // Then
        assertEquals(2000.0, stats.getAvgProcessingTime());
        verify(messageRepository).calculateAverageProcessingTimeByFlowId(flowId);
    }
    
    @Test
    void testGetMessageStatsWithNoCompletedMessages() {
        // Given
        List<Object[]> emptyStatusCounts = Arrays.asList(
            new Object[]{Message.MessageStatus.PROCESSING, 10L},
            new Object[]{Message.MessageStatus.QUEUED, 5L}
        );
        
        when(messageRepository.countByStatusGrouped()).thenReturn(emptyStatusCounts);
        when(messageRepository.calculateAverageProcessingTime()).thenReturn(null);
        
        // When
        MessageStatsDTO stats = messageStatsService.getMessageStats(null);
        
        // Then
        assertEquals(15, stats.getTotal());
        assertEquals(10, stats.getProcessing());
        assertEquals(0, stats.getSuccessful());
        assertEquals(0, stats.getFailed());
        assertEquals(0.0, stats.getSuccessRate()); // No completed messages
        assertEquals(0.0, stats.getAvgProcessingTime());
    }
    
    @Test
    void testGetStatsForPeriod() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<Message> messages = createMockMessages();
        org.springframework.data.domain.Page<Message> page = 
            new org.springframework.data.support.PageableExecutionUtils.Page<>(
                messages, 
                org.springframework.data.domain.Pageable.unpaged(), 
                () -> messages.size()
            );
        
        when(messageRepository.findByReceivedAtBetween(eq(startDate), eq(endDate), any()))
            .thenReturn(page);
        when(messageRepository.calculateAverageProcessingTimeForPeriod(startDate, endDate))
            .thenReturn(1200.0);
        
        // When
        MessageStatsDTO stats = messageStatsService.getStatsForPeriod(startDate, endDate);
        
        // Then
        assertEquals(5, stats.getTotal());
        assertEquals(1, stats.getProcessing());
        assertEquals(3, stats.getSuccessful());
        assertEquals(1, stats.getFailed());
        assertEquals(75.0, stats.getSuccessRate()); // 3/(3+1) * 100
        assertEquals(1200.0, stats.getAvgProcessingTime());
    }
    
    @Test
    void testGetStatusBreakdown() {
        // Given
        when(messageRepository.countByStatusGrouped()).thenReturn(mockStatusCounts);
        
        // When
        Map<String, Long> breakdown = messageStatsService.getStatusBreakdown();
        
        // Then
        assertEquals(5, breakdown.size());
        assertEquals(5L, breakdown.get("PROCESSING"));
        assertEquals(80L, breakdown.get("PROCESSED"));
        assertEquals(10L, breakdown.get("COMPLETED"));
        assertEquals(3L, breakdown.get("FAILED"));
        assertEquals(2L, breakdown.get("DEAD_LETTER"));
    }
    
    @Test
    void testErrorHandling() {
        // Given
        when(messageRepository.countByStatusGrouped()).thenThrow(new RuntimeException("Database error"));
        
        // When
        MessageStatsDTO stats = messageStatsService.getMessageStats(null);
        
        // Then
        assertNotNull(stats);
        assertEquals(0, stats.getTotal());
        assertEquals(0, stats.getProcessing());
        assertEquals(0, stats.getSuccessful());
        assertEquals(0, stats.getFailed());
        assertEquals(0.0, stats.getSuccessRate());
        assertEquals(0.0, stats.getAvgProcessingTime());
    }
    
    private List<Message> createMockMessages() {
        List<Message> messages = new ArrayList<>();
        
        Message m1 = new Message();
        m1.setStatus(Message.MessageStatus.PROCESSING);
        messages.add(m1);
        
        Message m2 = new Message();
        m2.setStatus(Message.MessageStatus.PROCESSED);
        messages.add(m2);
        
        Message m3 = new Message();
        m3.setStatus(Message.MessageStatus.COMPLETED);
        messages.add(m3);
        
        Message m4 = new Message();
        m4.setStatus(Message.MessageStatus.PROCESSED);
        messages.add(m4);
        
        Message m5 = new Message();
        m5.setStatus(Message.MessageStatus.FAILED);
        messages.add(m5);
        
        return messages;
    }
}