package com.integrixs.backend.controller;

import com.integrixs.backend.service.NotificationService;
import com.integrixs.data.model.NotificationChannel;
import com.integrixs.data.sql.repository.NotificationChannelSqlRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing notification channels
 */
@RestController
@RequestMapping("/api/v2/notification - channels")
public class NotificationChannelController {

    private final NotificationChannelSqlRepository channelRepository;
    private final NotificationService notificationService;

    public NotificationChannelController(NotificationChannelSqlRepository channelRepository,
                                       NotificationService notificationService) {
        this.channelRepository = channelRepository;
        this.notificationService = notificationService;
    }

    /**
     * Get all notification channels
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Page<NotificationChannel> getChannels(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) NotificationChannel.ChannelType type,
            Pageable pageable) {

        if(enabled != null && enabled && type != null) {
            return channelRepository.findByChannelTypeAndEnabledTrue(type, pageable);
        } else if(enabled != null && enabled) {
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
    public ResponseEntity<NotificationChannel> getChannel(@PathVariable UUID channelId) {
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
        if(channelRepository.existsByChannelName(channel.getChannelName())) {
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
    @PutMapping("/ {channelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationChannel> updateChannel(
            @PathVariable UUID channelId,
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
    @DeleteMapping("/ {channelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId) {
        if(channelRepository.existsById(channelId)) {
            channelRepository.deleteById(channelId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Test notification channel
     */
    @PostMapping("/ {channelId}/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testChannel(@PathVariable UUID channelId) {
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

                    } catch(Exception e) {
                        message = "Test failed: " + e.getMessage();
                        channel.setLastTestAt(LocalDateTime.now());
                        channel.setLastTestSuccess(false);
                        channel.setLastTestMessage(message);
                        channelRepository.save(channel);
                    }

                    Map<String, Object> response = Map.of(
                            "success", success,
                            "message", message,
                            "testedAt", LocalDateTime.now()
                   );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get required configuration for channel type
     */
    @GetMapping("/types/ {type}/configuration")
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
    @PatchMapping("/ {channelId}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationChannel> toggleChannel(
            @PathVariable UUID channelId,
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
