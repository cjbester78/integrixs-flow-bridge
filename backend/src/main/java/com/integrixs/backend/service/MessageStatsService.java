package com.integrixs.backend.service;

import com.integrixs.data.model.Message;
import com.integrixs.data.repository.MessageRepository;
import com.integrixs.shared.dto.MessageStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for message statistics
 */
@Service
@RequiredArgsConstructor
public class MessageStatsService {
    
    private final MessageRepository messageRepository;
    
    public MessageStatsDTO getMessageStats(Map<String, String> filters) {
        MessageStatsDTO stats = new MessageStatsDTO();
        
        long total = messageRepository.count();
        long processing = 0; // TODO: implement countByStatus
        long successful = 0; // TODO: implement countByStatus
        long failed = 0; // TODO: implement countByStatus
        
        stats.setTotal(total);
        stats.setProcessing(processing);
        stats.setSuccessful(successful);
        stats.setFailed(failed);
        stats.setSuccessRate(total > 0 ? (successful * 100.0 / total) : 0);
        stats.setAvgProcessingTime(0); // TODO: implement avg processing time
        
        return stats;
    }
}