package com.integrixs.backend.controller;

import com.integrixs.backend.service.IpWhitelistService;
import com.integrixs.backend.service.IpWhitelistService.IpWhitelistEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for managing IP whitelist configuration.
 * Only accessible by administrators.
 */
@RestController
@RequestMapping("/api/admin/ip - whitelist")
@PreAuthorize("hasRole('ADMIN')")
public class IpWhitelistController {

    private static final Logger log = LoggerFactory.getLogger(IpWhitelistController.class);


    private final IpWhitelistService ipWhitelistService;

    public IpWhitelistController(IpWhitelistService ipWhitelistService) {
        this.ipWhitelistService = ipWhitelistService;
    }

    /**
     * Get all whitelisted IPs.
     */
    @GetMapping
    public ResponseEntity<List<IpWhitelistEntry>> getAllWhitelistedIps() {
        log.debug("Fetching all whitelisted IPs");
        List<IpWhitelistEntry> entries = ipWhitelistService.getAllWhitelistedIps();
        return ResponseEntity.ok(entries);
    }

    /**
     * Add a single IP to the whitelist.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> addIpToWhitelist(
            @RequestBody AddIpRequest request,
            Authentication authentication) {

        log.info("Adding IP {} to whitelist by user {}", request.getIp(), authentication.getName());

        try {
            ipWhitelistService.addIpAddress(
                request.getIp(),
                request.getDescription(),
                authentication.getName(),
                request.getExpiresAt()
           );

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "IP address added to whitelist"
           ));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
           ));
        }
    }

    /**
     * Add multiple IPs to the whitelist.
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> addMultipleIpsToWhitelist(
            @RequestBody AddMultipleIpsRequest request,
            Authentication authentication) {

        log.info("Adding {} IPs to whitelist by user {}", request.getIps().size(), authentication.getName());

        try {
            ipWhitelistService.addIpAddresses(
                request.getIps(),
                request.getDescription(),
                authentication.getName(),
                request.getExpiresAt()
           );

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "IP addresses added to whitelist",
                "count", request.getIps().size()
           ));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
           ));
        }
    }

    /**
     * Remove an IP from the whitelist.
     */
    @DeleteMapping("/ {ip}")
    public ResponseEntity<Map<String, String>> removeIpFromWhitelist(
            @PathVariable String ip,
            Authentication authentication) {

        log.info("Removing IP {} from whitelist by user {}", ip, authentication.getName());

        boolean removed = ipWhitelistService.removeIpAddress(ip);

        if(removed) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "IP address removed from whitelist"
           ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update IP description.
     */
    @PutMapping("/ {ip}/description")
    public ResponseEntity<Map<String, String>> updateIpDescription(
            @PathVariable String ip,
            @RequestBody UpdateDescriptionRequest request,
            Authentication authentication) {

        log.info("Updating description for IP {} by user {}", ip, authentication.getName());

        boolean updated = ipWhitelistService.updateIpDescription(ip, request.getDescription());

        if(updated) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "IP description updated"
           ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Extend IP expiration.
     */
    @PutMapping("/ {ip}/extend")
    public ResponseEntity<Map<String, String>> extendIpExpiration(
            @PathVariable String ip,
            @RequestBody ExtendExpirationRequest request,
            Authentication authentication) {

        log.info("Extending expiration for IP {} by user {}", ip, authentication.getName());

        boolean extended = ipWhitelistService.extendExpiration(ip, request.getNewExpiresAt());

        if(extended) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "IP expiration extended"
           ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if an IP is whitelisted.
     */
    @GetMapping("/check/ {ip}")
    public ResponseEntity<Map<String, Object>> checkIpWhitelist(@PathVariable String ip) {
        log.debug("Checking if IP {} is whitelisted", ip);

        boolean whitelisted = ipWhitelistService.isIpWhitelisted(ip);

        return ResponseEntity.ok(Map.of(
            "ip", ip,
            "whitelisted", whitelisted
       ));
    }

    /**
     * Get whitelist statistics.
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getWhitelistStatistics() {
        log.debug("Fetching IP whitelist statistics");
        Map<String, Object> stats = ipWhitelistService.getWhitelistStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get IPs added by current user.
     */
    @GetMapping("/my - ips")
    public ResponseEntity<List<IpWhitelistEntry>> getMyWhitelistedIps(Authentication authentication) {
        log.debug("Fetching IPs whitelisted by user {}", authentication.getName());
        List<IpWhitelistEntry> entries = ipWhitelistService.getWhitelistedIpsByUser(authentication.getName());
        return ResponseEntity.ok(entries);
    }

    // Request DTOs

        public static class AddIpRequest {
        private String ip;
        private String description;
        private LocalDateTime expiresAt;

        // Getters and setters
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }

        public static class AddMultipleIpsRequest {
        private List<String> ips;
        private String description;
        private LocalDateTime expiresAt;

        // Getters and setters
        public List<String> getIps() { return ips; }
        public void setIps(List<String> ips) { this.ips = ips; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }

        public static class UpdateDescriptionRequest {
        private String description;

        // Getters and setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

        public static class ExtendExpirationRequest {
        private LocalDateTime newExpiresAt;

        // Getters and setters
        public LocalDateTime getNewExpiresAt() { return newExpiresAt; }
        public void setNewExpiresAt(LocalDateTime newExpiresAt) { this.newExpiresAt = newExpiresAt; }
    }
}
