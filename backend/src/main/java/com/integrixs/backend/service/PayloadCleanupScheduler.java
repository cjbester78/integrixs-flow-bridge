package com.integrixs.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to clean up old external payload files.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayloadCleanupScheduler {

    private final MessageLazyLoadingService messageLazyLoadingService;

    @Value("$ {payload.cleanup.days - to - keep:30}")
    private int daysToKeep;

    @Value("$ {payload.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    /**
     * Run cleanup daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldPayloads() {
        if(!cleanupEnabled) {
            log.debug("Payload cleanup is disabled");
            return;
        }

        try {
            log.info("Starting scheduled payload cleanup - removing files older than {} days", daysToKeep);
            messageLazyLoadingService.cleanupOrphanedPayloads(daysToKeep);
            log.info("Completed scheduled payload cleanup");
        } catch(Exception e) {
            log.error("Failed to run scheduled payload cleanup", e);
        }
    }
}
