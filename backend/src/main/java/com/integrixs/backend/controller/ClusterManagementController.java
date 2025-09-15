package com.integrixs.backend.controller;

import com.integrixs.backend.cluster.ClusterCoordinationService;
import com.integrixs.backend.cluster.ClusterCoordinationService.ClusterHealth;
import com.integrixs.backend.cluster.ClusterCoordinationService.ClusterMember;
import com.integrixs.backend.security.RequiresPermission;
import com.integrixs.backend.security.ResourcePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Controller for cluster management and monitoring
 */
@RestController
@RequestMapping("/api/cluster")
@ConditionalOnBean(ClusterCoordinationService.class)
@Tag(name = "Cluster Management", description = "Cluster management and monitoring API")
public class ClusterManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ClusterManagementController.class);

    @Autowired(required = false)
    private ClusterCoordinationService clusterService;

    /**
     * Get cluster information
     */
    @GetMapping("/info")
    @Operation(summary = "Get cluster information",
               description = "Returns current cluster state and member information")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Cluster information retrieved"),
        @ApiResponse(responseCode = "503", description = "Clustering not available")
    })
    public ResponseEntity<?> getClusterInfo() {
        if(clusterService == null) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Clustering not enabled"));
        }

        Map<String, Object> info = new HashMap<>();
        info.put("clusterState", clusterService.getClusterState().toString());
        info.put("clusterSize", clusterService.getClusterSize());
        info.put("localMember", clusterService.getLocalMember());
        info.put("isOldestMember", clusterService.isOldestMember());
        info.put("members", clusterService.getClusterMembers());

        return ResponseEntity.ok(info);
    }

    /**
     * Get cluster health
     */
    @GetMapping("/health")
    @Operation(summary = "Get cluster health",
               description = "Performs comprehensive cluster health check")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Health check completed"),
        @ApiResponse(responseCode = "503", description = "Cluster unhealthy or unavailable")
    })
    public ResponseEntity<ClusterHealth> getClusterHealth() {
        if(clusterService == null) {
            ClusterHealth health = new ClusterHealth();
            health.setHealthy(false);
            health.setError("Clustering not enabled");
            return ResponseEntity.status(503).body(health);
        }

        ClusterHealth health = clusterService.checkClusterHealth();

        if(health.isHealthy()) {
            return ResponseEntity.ok(health);
        } else {
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Get cluster members
     */
    @GetMapping("/members")
    @Operation(summary = "Get cluster members",
               description = "Returns list of all cluster members")
    public ResponseEntity<?> getClusterMembers() {
        if(clusterService == null) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Clustering not enabled"));
        }

        Set<ClusterMember> members = clusterService.getClusterMembers();
        return ResponseEntity.ok(members);
    }

    /**
     * Test cluster coordination
     */
    @PostMapping("/test/coordination")
    @Operation(summary = "Test cluster coordination",
               description = "Tests distributed execution across cluster")
    @RequiresPermission(ResourcePermission.ADMIN_SYSTEM)
    public ResponseEntity<?> testClusterCoordination() {
        if(clusterService == null) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Clustering not enabled"));
        }

        try {
            // Test distributed execution
            Callable<Map<String, Object>> testTask = () -> {
                Map<String, Object> result = new HashMap<>();
                result.put("timestamp", System.currentTimeMillis());
                result.put("hostname", java.net.InetAddress.getLocalHost().getHostName());
                result.put("threadName", Thread.currentThread().getName());
                result.put("random", UUID.randomUUID().toString());
                return result;
            };

            var futures = clusterService.executeOnAllMembers(testTask);

            Map<String, Object> results = new HashMap<>();
            futures.forEach((member, future) -> {
                try {
                    results.put(member.getAddress().toString(), future.get(5, TimeUnit.SECONDS));
                } catch(Exception e) {
                    results.put(member.getAddress().toString(),
                        Map.of("error", e.getMessage()));
                }
            });

            return ResponseEntity.ok(Map.of(
                "test", "distributed - execution",
                "results", results
           ));

        } catch(Exception e) {
            logger.error("Cluster coordination test failed", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Test failed", "message", e.getMessage()));
        }
    }

    /**
     * Test distributed locking
     */
    @PostMapping("/test/locking")
    @Operation(summary = "Test distributed locking",
               description = "Tests distributed lock acquisition and release")
    @RequiresPermission(ResourcePermission.ADMIN_SYSTEM)
    public ResponseEntity<?> testDistributedLocking(
            @RequestParam(defaultValue = "test - lock") String lockName,
            @RequestParam(defaultValue = "5") int holdSeconds) {

        if(clusterService == null) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Clustering not enabled"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("lockName", lockName);

        try {
            boolean acquired = clusterService.tryLock(lockName, 2, TimeUnit.SECONDS);
            result.put("acquired", acquired);

            if(acquired) {
                result.put("holdingLock", true);
                result.put("holdDuration", holdSeconds + " seconds");

                // Hold the lock for specified duration
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(holdSeconds * 1000L);
                    } catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        clusterService.unlock(lockName);
                        logger.info("Released lock: {}", lockName);
                    }
                });
            } else {
                result.put("holdingLock", false);
                result.put("message", "Lock is held by another member");
            }

            return ResponseEntity.ok(result);

        } catch(Exception e) {
            logger.error("Distributed locking test failed", e);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Test leader election
     */
    @PostMapping("/test/leader - election")
    @Operation(summary = "Test leader election",
               description = "Attempts to become leader for a test service")
    @RequiresPermission(ResourcePermission.ADMIN_SYSTEM)
    public ResponseEntity<?> testLeaderElection(
            @RequestParam(defaultValue = "test - service") String serviceName) {

        if(clusterService == null) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Clustering not enabled"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("serviceName", serviceName);
        result.put("localMember", clusterService.getLocalMember().getAddress());

        try {
            CompletableFuture<Boolean> electionFuture = clusterService.electLeader(
                serviceName,
                () -> logger.info("Became leader for: {}", serviceName),
                () -> logger.info("Lost leadership for: {}", serviceName)
           );

            boolean becameLeader = electionFuture.get(5, TimeUnit.SECONDS);
            result.put("isLeader", becameLeader);
            result.put("message", becameLeader ?
                "Successfully became leader" : "Another member is already leader");

            return ResponseEntity.ok(result);

        } catch(Exception e) {
            logger.error("Leader election test failed", e);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Publish cluster event
     */
    @PostMapping("/event/publish")
    @Operation(summary = "Publish cluster event",
               description = "Publishes an event to all cluster members")
    @RequiresPermission(ResourcePermission.ADMIN_SYSTEM)
    public ResponseEntity<?> publishClusterEvent(
            @RequestParam(defaultValue = "test - topic") String topic,
            @RequestBody Map<String, Object> eventData) {

        if(clusterService == null) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Clustering not enabled"));
        }

        try {
            ClusterCoordinationService.ClusterEvent event =
                new ClusterCoordinationService.ClusterEvent(
                    clusterService.getLocalMember().getAddress()
               );
            event.getData().putAll(eventData);

            clusterService.publishEvent(topic, event);

            return ResponseEntity.ok(Map.of(
                "published", true,
                "topic", topic,
                "source", event.getSource(),
                "timestamp", event.getTimestamp()
           ));

        } catch(Exception e) {
            logger.error("Failed to publish cluster event", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to publish event", "message", e.getMessage()));
        }
    }

    /**
     * Get distributed counter value
     */
    @GetMapping("/counter/ {name}")
    @Operation(summary = "Get counter value",
               description = "Gets current value of a distributed counter")
    public ResponseEntity<?> getCounterValue(@PathVariable String name) {
        if(clusterService == null) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Clustering not enabled"));
        }

        long value = clusterService.getCounter(name).get();
        return ResponseEntity.ok(Map.of(
            "counter", name,
            "value", value
       ));
    }

    /**
     * Increment distributed counter
     */
    @PostMapping("/counter/ {name}/increment")
    @Operation(summary = "Increment counter",
               description = "Increments a distributed counter")
    public ResponseEntity<?> incrementCounter(@PathVariable String name) {
        if(clusterService == null) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Clustering not enabled"));
        }

        long newValue = clusterService.incrementCounter(name);
        return ResponseEntity.ok(Map.of(
            "counter", name,
            "value", newValue
       ));
    }

    /**
     * Step down from leadership
     */
    @PostMapping("/leader/step - down")
    @Operation(summary = "Step down from leadership",
               description = "Steps down from leadership role for a service")
    @RequiresPermission(ResourcePermission.ADMIN_SYSTEM)
    public ResponseEntity<?> stepDownFromLeadership(
            @RequestParam String serviceName) {

        if(clusterService == null) {
            return ResponseEntity.status(503)
                .body(Map.of("error", "Clustering not enabled"));
        }

        boolean wasLeader = clusterService.isLeader(serviceName);
        clusterService.stepDownFromLeadership(serviceName);

        return ResponseEntity.ok(Map.of(
            "serviceName", serviceName,
            "wasLeader", wasLeader,
            "message", wasLeader ?
                "Successfully stepped down from leadership" :
                "Was not the leader for this service"
       ));
    }
}
