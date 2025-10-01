package com.integrixs.backend.service;

import com.integrixs.backend.exception.BusinessException;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import com.integrixs.shared.dto.AdapterStatusDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AdapterLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(AdapterLifecycleService.class);


    private final CommunicationAdapterSqlRepository adapterRepository;
    private final SystemLogSqlRepository systemLogRepository;

    // In - memory status tracking(in production, this would be in a cache or database)
    private final ConcurrentHashMap<String, AdapterStatusDTO> adapterStatuses = new ConcurrentHashMap<>();

    public AdapterLifecycleService(CommunicationAdapterSqlRepository adapterRepository,
                                   SystemLogSqlRepository systemLogRepository) {
        this.adapterRepository = adapterRepository;
        this.systemLogRepository = systemLogRepository;
    }

    public List<AdapterStatusDTO> getAdapterStatuses(String businessComponentId) {
        List<CommunicationAdapter> adapters;

        if(businessComponentId != null && !businessComponentId.isEmpty()) {
            // Since we don't have a direct findByBusinessComponentId method, we'll filter in memory
            adapters = adapterRepository.findAll().stream()
                    .filter(adapter -> adapter.getBusinessComponent() != null && businessComponentId.equals(adapter.getBusinessComponent().getId().toString()))
                    .collect(Collectors.toList());
        } else {
            adapters = adapterRepository.findAll();
        }

        return adapters.stream()
                .map(this::getOrCreateAdapterStatus)
                .collect(Collectors.toList());
    }

    public AdapterStatusDTO getAdapterStatus(String adapterId) {
        CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                .orElseThrow(() -> new BusinessException("Adapter not found: " + adapterId));

        return getOrCreateAdapterStatus(adapter);
    }

    public AdapterStatusDTO startAdapter(String adapterId) {
        CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                .orElseThrow(() -> new BusinessException("Adapter not found: " + adapterId));

        AdapterStatusDTO status = getOrCreateAdapterStatus(adapter);

        // Simulate starting the adapter
        log.info("Starting adapter: {} ( {})", adapter.getName(), adapter.getType());
        status.setStatus("running");
        status.setLoad(10); // Initial load

        adapterStatuses.put(adapterId, status);
        return status;
    }

    public AdapterStatusDTO stopAdapter(String adapterId) {
        CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                .orElseThrow(() -> new BusinessException("Adapter not found: " + adapterId));

        AdapterStatusDTO status = getOrCreateAdapterStatus(adapter);

        // Simulate stopping the adapter
        log.info("Stopping adapter: {} ( {})", adapter.getName(), adapter.getType());
        status.setStatus("stopped");
        status.setLoad(0);

        adapterStatuses.put(adapterId, status);
        return status;
    }

    public AdapterStatusDTO restartAdapter(String adapterId) {
        // Stop first
        stopAdapter(adapterId);

        // Small delay simulation
        try {
            Thread.sleep(100);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then start
        return startAdapter(adapterId);
    }

    private AdapterStatusDTO getOrCreateAdapterStatus(CommunicationAdapter adapter) {
        // Always recalculate statistics to get fresh data
        AdapterStatusDTO status = new AdapterStatusDTO();
        status.setId(adapter.getId().toString());
        status.setName(adapter.getName());
        status.setType(adapter.getType() != null ? adapter.getType().toString() : "UNKNOWN");
        status.setMode(adapter.getMode() != null ? adapter.getMode().toString() : "UNKNOWN");

        // Initially set status based on adapter configuration
        String adapterStatus;
        if(adapter.isActive()) {
            adapterStatus = "active";
            status.setLoad(25); // Default load for active adapters
        } else {
            adapterStatus = "inactive";
            status.setLoad(0);
        }

        status.setBusinessComponentId(adapter.getBusinessComponent() != null ? adapter.getBusinessComponent().getId().toString() : null);
        // Get business component name from the relation if available
        if(adapter.getBusinessComponent() != null) {
            status.setBusinessComponentName(adapter.getBusinessComponent().getName());
        }

        // Calculate real metrics from system logs
        try {
            // Count flow executions for this adapter
            String searchPattern = String.format("adapter: %s", adapter.getName());
            List<SystemLog> adapterLogs = systemLogRepository.findByMessageContainingAndSourceOrderByTimestampDesc(
                searchPattern, "FlowExecutionAsyncService"
           );

            // Count successful messages
            long successCount = adapterLogs.stream()
                .filter(log -> log.getMessage().contains("SUCCESS") || log.getMessage().contains("success"))
                .count();

            // Count errors
            long errorCount = adapterLogs.stream()
                .filter(log -> log.getLevel() == SystemLog.LogLevel.ERROR)
                .count();

            status.setMessagesProcessed(successCount);
            status.setErrorsCount(errorCount);

            // Get last activity time and check for recent errors
            LocalDateTime lastActivity = null;
            boolean hasRecentError = false;

            if(!adapterLogs.isEmpty()) {
                SystemLog mostRecentLog = adapterLogs.get(0);
                lastActivity = mostRecentLog.getTimestamp();
                status.setLastActivity(lastActivity);

                // Check if the most recent log is an error
                if(mostRecentLog.getLevel() == SystemLog.LogLevel.ERROR) {
                    hasRecentError = true;
                }
            } else {
                // Try to find any log mentioning this adapter
                List<SystemLog> anyAdapterLogs = systemLogRepository.findByMessageContainingOrderByTimestampDesc(
                    adapter.getName()
               );
                if(!anyAdapterLogs.isEmpty()) {
                    SystemLog mostRecentLog = anyAdapterLogs.get(0);
                    lastActivity = mostRecentLog.getTimestamp();
                    status.setLastActivity(lastActivity);

                    // Check if the most recent log is an error
                    if(mostRecentLog.getLevel() == SystemLog.LogLevel.ERROR) {
                        hasRecentError = true;
                    }
                }
            }

            // Check for recent errors(within last 5 minutes)
            if(!hasRecentError && lastActivity != null) {
                LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
                if(lastActivity.isAfter(fiveMinutesAgo)) {
                    // Check if there are any errors in the last 5 minutes
                    hasRecentError = adapterLogs.stream()
                        .filter(log -> log.getTimestamp().isAfter(fiveMinutesAgo))
                        .anyMatch(log -> log.getLevel() == SystemLog.LogLevel.ERROR);
                }
            }

            // Set status to error if adapter is active and has recent errors
            if(adapter.isActive() && hasRecentError) {
                adapterStatus = "error";
                status.setLoad(0); // Set load to 0 for error state
            }

        } catch(Exception e) {
            log.error("Error calculating adapter statistics for {}: {}", adapter.getName(), e.getMessage());
            // Set default values on error
            status.setMessagesProcessed(0L);
            status.setErrorsCount(0L);
        }

        // Set the final calculated status
        status.setStatus(adapterStatus);

        // Cache the calculated status
        adapterStatuses.put(adapter.getId().toString(), status);

        return status;
    }
}
