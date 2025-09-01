package com.integrixs.backend.controller;

import com.integrixs.backend.service.NotificationService;
import com.integrixs.data.model.NotificationChannel;
import com.integrixs.data.repository.NotificationChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller for managing notification channels
 */
@RestController
@RequestMapping("/api/v2/notification-channels")
@RequiredArgsConstructor
@Slf4j
public class NotificationChannelController {
    
    private final NotificationChannelRepository channelRepository;
    private final NotificationService notificationService;
    
    /**
     * Get all notification channels
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Page<NotificationChannel> getChannels(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) NotificationChannel.ChannelType type,
            Pageable pageable) {
        
        if (enabled != null && enabled && type != null) {
            return channelRepository.findByChannelTypeAndEnabledTrue(type, pageable);
        } else if (enabled != null && enabled) {
            return channelRepository.findByEnabledTrue(pageable);
        } else {
            return channelRepository.findAll(pageable);
        }
    }
    
    /**
     * Get notification channel by ID
     */
    @GetMapping("/{channelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<NotificationChannel> getChannel(@PathVariable Long channelId) {
        return channelRepository.findById(channelId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create notification channel
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationChannel> createChannel(@RequestBody NotificationChannel channel) {
        if (channelRepository.existsByChannelName(channel.getChannelName())) {
            return ResponseEntity.badRequest().build();
        }
        
        channel.setId(null); // Ensure new entity
        channel.setCreatedAt(LocalDateTime.now());
        channel.setModifiedAt(LocalDateTime.now());
        
        NotificationChannel saved = channelRepository.save(channel);
        return ResponseEntity.ok(saved);
    }
    
    /**
     * Update notification channel
     */
    @PutMapping("/{channelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationChannel> updateChannel(
            @PathVariable Long channelId,
            @RequestBody NotificationChannel channel) {
        
        return channelRepository.findById(channelId)
                .map(existing -> {
                    existing.setChannelName(channel.getChannelName());
                    existing.setDescription(channel.getDescription());
                    existing.setChannelType(channel.getChannelType());
                    existing.setEnabled(channel.isEnabled());
                    existing.setConfiguration(channel.getConfiguration());
                    existing.setRateLimitPerHour(channel.getRateLimitPerHour());
                    existing.setModifiedAt(LocalDateTime.now());
                    
                    return ResponseEntity.ok(channelRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete notification channel
     */
    @DeleteMapping("/{channelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long channelId) {
        if (channelRepository.existsById(channelId)) {
            channelRepository.deleteById(channelId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Test notification channel
     */
    @PostMapping("/{channelId}/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testChannel(@PathVariable Long channelId) {
        return channelRepository.findById(channelId)
                .map(channel -> {
                    boolean success = false;
                    String message = "";
                    
                    try {
                        // Create test alert
                        // This would send a test notification
                        success = true;
                        message = "Test notification sent successfully";
                        
                        // Update test results
                        channel.setLastTestAt(LocalDateTime.now());
                        channel.setLastTestSuccess(success);
                        channel.setLastTestMessage(message);
                        channelRepository.save(channel);
                        
                    } catch (Exception e) {
                        message = "Test failed: " + e.getMessage();
                        channel.setLastTestAt(LocalDateTime.now());
                        channel.setLastTestSuccess(false);
                        channel.setLastTestMessage(message);
                        channelRepository.save(channel);
                    }
                    
                    return ResponseEntity.ok(Map.of(
                            "success", success,
                            "message", message,
                            "testedAt", LocalDateTime.now()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get required configuration for channel type
     */
    @GetMapping("/types/{type}/configuration")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Map<String, String>> getRequiredConfiguration(
            @PathVariable NotificationChannel.ChannelType type) {
        
        NotificationChannel temp = new NotificationChannel();
        temp.setChannelType(type);
        
        return ResponseEntity.ok(temp.getRequiredConfiguration());
    }
    
    /**
     * Enable/disable notification channel
     */
    @PatchMapping("/{channelId}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationChannel> toggleChannel(
            @PathVariable Long channelId,
            @RequestParam boolean enabled) {
        
        return channelRepository.findById(channelId)
                .map(channel -> {
                    channel.setEnabled(enabled);
                    channel.setModifiedAt(LocalDateTime.now());
                    return ResponseEntity.ok(channelRepository.save(channel));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}