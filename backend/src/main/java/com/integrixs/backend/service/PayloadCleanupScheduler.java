package com.integrixs.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduled task to clean up old external payload files.
 */
@Component
public class PayloadCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(PayloadCleanupScheduler.class);


    private final MessageLazyLoadingService messageLazyLoadingService;

    public PayloadCleanupScheduler(MessageLazyLoadingService messageLazyLoadingService) {
        this.messageLazyLoadingService = messageLazyLoadingService;
    }

    @Value("${payload.cleanup.days-to-keep:30}")
    private int daysToKeep;

    @Value("${payload.cleanup.enabled:true}")
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
            log.info("Starting scheduled payload cleanup-removing files older than {} days", daysToKeep);
            messageLazyLoadingService.cleanupOrphanedPayloads(daysToKeep);
            log.info("Completed scheduled payload cleanup");
        } catch(Exception e) {
            log.error("Failed to run scheduled payload cleanup", e);
        }
    }
}
