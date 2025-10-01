package com.integrixs.backend.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing IP whitelist configuration dynamically.
 * Supports runtime updates without application restart.
 */
@Service
public class IpWhitelistService {

    // In - memory storage for dynamic IP whitelist

    private static final Logger log = LoggerFactory.getLogger(IpWhitelistService.class);

    private final Set<String> whitelistedIps = ConcurrentHashMap.newKeySet();
    private final Set<String> whitelistedRanges = ConcurrentHashMap.newKeySet();
    private final Map<String, IpWhitelistEntry> ipEntries = new ConcurrentHashMap<>();

    /**
     * IP whitelist entry with metadata.
     */
    public static class IpWhitelistEntry {
        private final String ip;
        private final String description;
        private final String addedBy;
        private final LocalDateTime addedAt;
        private final LocalDateTime expiresAt;
        private final boolean isRange;

        public IpWhitelistEntry(String ip, String description, String addedBy,
                               LocalDateTime expiresAt, boolean isRange) {
            this.ip = ip;
            this.description = description;
            this.addedBy = addedBy;
            this.addedAt = LocalDateTime.now();
            this.expiresAt = expiresAt;
            this.isRange = isRange;
        }

        public boolean isExpired() {
            return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
        }

        // Getters
        public String getIp() { return ip; }
        public String getDescription() { return description; }
        public String getAddedBy() { return addedBy; }
        public LocalDateTime getAddedAt() { return addedAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public boolean isRange() { return isRange; }
    }

    /**
     * Add an IP address to the whitelist.
     */
    @CacheEvict(value = "ipWhitelist", allEntries = true)
    public void addIpAddress(String ip, String description, String addedBy, LocalDateTime expiresAt) {
        validateIpAddress(ip);

        boolean isRange = ip.contains("/");
        IpWhitelistEntry entry = new IpWhitelistEntry(ip, description, addedBy, expiresAt, isRange);

        ipEntries.put(ip, entry);

        if(isRange) {
            whitelistedRanges.add(ip);
        } else {
            whitelistedIps.add(ip);
        }

        log.info("Added IP {} to whitelist by user {}", ip, addedBy);
    }

    /**
     * Add multiple IP addresses to the whitelist.
     */
    @CacheEvict(value = "ipWhitelist", allEntries = true)
    public void addIpAddresses(List<String> ips, String description, String addedBy, LocalDateTime expiresAt) {
        for(String ip : ips) {
            addIpAddress(ip.trim(), description, addedBy, expiresAt);
        }
    }

    /**
     * Remove an IP address from the whitelist.
     */
    @CacheEvict(value = "ipWhitelist", allEntries = true)
    public boolean removeIpAddress(String ip) {
        IpWhitelistEntry entry = ipEntries.remove(ip);
        if(entry != null) {
            if(entry.isRange()) {
                whitelistedRanges.remove(ip);
            } else {
                whitelistedIps.remove(ip);
            }
            log.info("Removed IP {} from whitelist", ip);
            return true;
        }
        return false;
    }

    /**
     * Check if an IP address is whitelisted.
     */
    @Cacheable(value = "ipWhitelist", key = "#ip")
    public boolean isIpWhitelisted(String ip) {
        // Remove expired entries
        cleanupExpiredEntries();

        // Check exact match
        if(whitelistedIps.contains(ip)) {
            return true;
        }

        // Check CIDR ranges
        return whitelistedRanges.stream()
            .anyMatch(range -> isIpInRange(ip, range));
    }

    /**
     * Get all whitelisted IPs.
     */
    public List<IpWhitelistEntry> getAllWhitelistedIps() {
        cleanupExpiredEntries();
        return new ArrayList<>(ipEntries.values());
    }

    /**
     * Get whitelisted IPs added by a specific user.
     */
    public List<IpWhitelistEntry> getWhitelistedIpsByUser(String username) {
        return ipEntries.values().stream()
            .filter(entry -> username.equals(entry.getAddedBy()))
            .collect(Collectors.toList());
    }

    /**
     * Update IP whitelist description.
     */
    @CacheEvict(value = "ipWhitelist", allEntries = true)
    public boolean updateIpDescription(String ip, String newDescription) {
        IpWhitelistEntry oldEntry = ipEntries.get(ip);
        if(oldEntry != null) {
            IpWhitelistEntry newEntry = new IpWhitelistEntry(
                oldEntry.getIp(),
                newDescription,
                oldEntry.getAddedBy(),
                oldEntry.getExpiresAt(),
                oldEntry.isRange()
           );
            ipEntries.put(ip, newEntry);
            return true;
        }
        return false;
    }

    /**
     * Extend expiration time for an IP.
     */
    @CacheEvict(value = "ipWhitelist", allEntries = true)
    public boolean extendExpiration(String ip, LocalDateTime newExpiresAt) {
        IpWhitelistEntry oldEntry = ipEntries.get(ip);
        if(oldEntry != null) {
            IpWhitelistEntry newEntry = new IpWhitelistEntry(
                oldEntry.getIp(),
                oldEntry.getDescription(),
                oldEntry.getAddedBy(),
                newExpiresAt,
                oldEntry.isRange()
           );
            ipEntries.put(ip, newEntry);
            log.info("Extended expiration for IP {} to {}", ip, newExpiresAt);
            return true;
        }
        return false;
    }

    /**
     * Clean up expired entries.
     */
    private void cleanupExpiredEntries() {
        List<String> expiredIps = ipEntries.entrySet().stream()
            .filter(entry -> entry.getValue().isExpired())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        for(String ip : expiredIps) {
            removeIpAddress(ip);
            log.info("Removed expired IP from whitelist: {}", ip);
        }
    }

    /**
     * Validate IP address format.
     */
    private void validateIpAddress(String ip) {
        if(ip == null || ip.trim().isEmpty()) {
            throw new IllegalArgumentException("IP address cannot be empty");
        }

        String trimmedIp = ip.trim();

        // Allow IPv4, IPv6, and CIDR notation
        if(!isValidIpv4(trimmedIp) && !isValidIpv6(trimmedIp) && !isValidCidr(trimmedIp)) {
            throw new IllegalArgumentException("Invalid IP address format: " + ip);
        }
    }

    /**
     * Check if IP is valid IPv4.
     */
    private boolean isValidIpv4(String ip) {
        if(ip.contains("/")) {
            return false; // CIDR notation
        }

        String[] parts = ip.split("\\.");
        if(parts.length != 4) {
            return false;
        }

        try {
            for(String part : parts) {
                int num = Integer.parseInt(part);
                if(num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if IP is valid IPv6.
     */
    private boolean isValidIpv6(String ip) {
        // Simplified IPv6 validation
        return ip.contains(":") && !ip.contains("/");
    }

    /**
     * Check if IP is valid CIDR notation.
     */
    private boolean isValidCidr(String cidr) {
        if(!cidr.contains("/")) {
            return false;
        }

        String[] parts = cidr.split("/");
        if(parts.length != 2) {
            return false;
        }

        try {
            // Validate IP part
            if(!isValidIpv4(parts[0]) && !isValidIpv6(parts[0])) {
                return false;
            }

            // Validate prefix length
            int prefix = Integer.parseInt(parts[1]);
            if(isValidIpv4(parts[0])) {
                return prefix >= 0 && prefix <= 32;
            } else {
                return prefix >= 0 && prefix <= 128;
            }
        } catch(NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if an IP is within a CIDR range.
     */
    private boolean isIpInRange(String ip, String cidr) {
        try {
            // This is simplified - in production, use a proper IP library
            if(!cidr.contains("/")) {
                return false;
            }

            String[] cidrParts = cidr.split("/");
            String rangeIp = cidrParts[0];
            int prefixLength = Integer.parseInt(cidrParts[1]);

            // For simplicity, just check if IPs match for /32
            if(prefixLength == 32) {
                return ip.equals(rangeIp);
            }

            // More complex CIDR matching would go here
            // In production, use Apache Commons Net or similar
            return false;
        } catch(Exception e) {
            log.error("Error checking IP range: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get statistics about the whitelist.
     */
    public Map<String, Object> getWhitelistStatistics() {
        cleanupExpiredEntries();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIps", whitelistedIps.size());
        stats.put("totalRanges", whitelistedRanges.size());
        stats.put("totalEntries", ipEntries.size());

        long expiringCount = ipEntries.values().stream()
            .filter(entry -> entry.getExpiresAt() != null)
            .count();
        stats.put("expiringEntries", expiringCount);

        return stats;
    }
}
