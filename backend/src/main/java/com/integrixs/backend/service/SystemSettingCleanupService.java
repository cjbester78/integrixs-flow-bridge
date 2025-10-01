package com.integrixs.backend.service;

import com.integrixs.data.sql.repository.SystemSettingSqlRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SystemSettingCleanupService {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingCleanupService.class);


    private final SystemSettingSqlRepository systemSettingRepository;

    public SystemSettingCleanupService(SystemSettingSqlRepository systemSettingRepository) {
        this.systemSettingRepository = systemSettingRepository;
    }

    @PostConstruct
    public void cleanupDuplicateSettings() {
        try {
            // Remove the old max_retry_attempts setting if it exists
            systemSettingRepository.findBySettingKey("max_retry_attempts").ifPresent(setting -> {
                log.info("Removing deprecated max_retry_attempts setting");
                systemSettingRepository.deleteById(setting.getId());
            });

            // Remove the old retry_delay setting if it exists
            systemSettingRepository.findBySettingKey("retry_delay").ifPresent(setting -> {
                log.info("Removing deprecated retry_delay setting");
                systemSettingRepository.deleteById(setting.getId());
            });

            // Remove the old max_retries setting if it exists(duplicate of max_retry_attempts)
            systemSettingRepository.findBySettingKey("max_retries").ifPresent(setting -> {
                log.info("Removing deprecated max_retries setting");
                systemSettingRepository.deleteById(setting.getId());
            });

            // Remove the old connection_pool_size setting(replaced by performance.connection.pool.size)
            systemSettingRepository.findBySettingKey("connection_pool_size").ifPresent(setting -> {
                log.info("Removing deprecated connection_pool_size setting");
                systemSettingRepository.deleteById(setting.getId());
            });
        } catch(Exception e) {
            log.error("Error cleaning up duplicate settings", e);
        }
    }
}
