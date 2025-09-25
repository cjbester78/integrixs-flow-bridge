package com.integrixs.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IP whitelisting filter for enhanced security.
 * Restricts access to configured IP addresses or CIDR ranges.
 */
@Component
@ConditionalOnProperty(name = "security.ip.whitelist.enabled", havingValue = "true", matchIfMissing = false)
public class IpWhitelistFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IpWhitelistFilter.class);


    @Value("${security.ip.whitelist.addresses:127.0.0.1,::1}")
    private String whitelistedIps;

    @Value("${security.ip.whitelist.bypass-paths:/api/auth/login,/api/health,/actuator/health}")
    private String bypassPaths;

    @Value("${security.ip.whitelist.enforcement-mode:STRICT}")
    private EnforcementMode enforcementMode;

    private Set<String> whitelistedIpSet;
    private Set<IpRange> whitelistedRanges;
    private Set<String> bypassPathSet;

    public enum EnforcementMode {
        STRICT,   // Block all non-whitelisted IPs
        PERMISSIVE // Log warnings but allow access
    }

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();

        // Parse whitelisted IPs
        whitelistedIpSet = new HashSet<>();
        whitelistedRanges = new HashSet<>();

        Arrays.stream(whitelistedIps.split(","))
            .map(String::trim)
            .filter(ip -> !ip.isEmpty())
            .forEach(ip -> {
                if(ip.contains("/")) {
                    // CIDR notation
                    whitelistedRanges.add(IpRange.fromCidr(ip));
                } else {
                    whitelistedIpSet.add(ip);
                }
            });

        // Parse bypass paths
        bypassPathSet = new HashSet<>(Arrays.asList(bypassPaths.split(",")));

        log.info("IP Whitelist filter initialized with {} IPs and {} ranges in {} mode",
            whitelistedIpSet.size(), whitelistedRanges.size(), enforcementMode);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Check if path should bypass IP filtering
        if(shouldBypassPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIpAddress(request);

        if(isIpWhitelisted(clientIp)) {
            log.debug("Allowed access from whitelisted IP: {}", clientIp);
            filterChain.doFilter(request, response);
        } else {
            handleUnauthorizedIp(clientIp, request, response);

            if(enforcementMode == EnforcementMode.PERMISSIVE) {
                // In permissive mode, continue processing
                filterChain.doFilter(request, response);
            }
        }
    }

    /**
     * Check if the request path should bypass IP filtering.
     */
    private boolean shouldBypassPath(String path) {
        return bypassPathSet.stream()
            .anyMatch(bypassPath -> path.startsWith(bypassPath.trim()));
    }

    /**
     * Check if an IP address is whitelisted.
     */
    private boolean isIpWhitelisted(String ip) {
        // Check exact match
        if(whitelistedIpSet.contains(ip)) {
            return true;
        }

        // Check CIDR ranges
        return whitelistedRanges.stream()
            .anyMatch(range -> range.contains(ip));
    }

    /**
     * Handle access attempt from unauthorized IP.
     */
    private void handleUnauthorizedIp(String clientIp, HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        String message = String.format("Access denied from IP: %s to path: %s",
            clientIp, request.getRequestURI());

        if(enforcementMode == EnforcementMode.STRICT) {
            log.warn("BLOCKED-{}", message);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(" {\"error\":\"Access denied from your IP address\"}");
            response.getWriter().flush();
        } else {
            log.warn("ALLOWED(Permissive mode)-{}", message);
        }
    }

    /**
     * Extract client IP address from request, considering proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for(String header : headerNames) {
            String ip = request.getHeader(header);
            if(ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle multiple IPs in X-Forwarded-For
                if(ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip.trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Inner class to handle IP ranges in CIDR notation.
     */
    private static class IpRange {
        private final long start;
        private final long end;

        private IpRange(long start, long end) {
            this.start = start;
            this.end = end;
        }

        /**
         * Create an IP range from CIDR notation(e.g., "192.168.1.0/24").
         */
        public static IpRange fromCidr(String cidr) {
            String[] parts = cidr.split("/");
            String ip = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            long ipLong = ipToLong(ip);
            long mask = -1L << (32-prefixLength);

            long start = ipLong & mask;
            long end = start + (~mask);

            return new IpRange(start, end);
        }

        /**
         * Check if an IP address is within this range.
         */
        public boolean contains(String ip) {
            // Handle IPv6 localhost
            if("::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                return "127.0.0.1".equals(ipFromLong(start)) || "::1".equals(ipFromLong(start));
            }

            try {
                long ipLong = ipToLong(ip);
                return ipLong >= start && ipLong <= end;
            } catch(Exception e) {
                // If we can't parse the IP, it's not in the range
                return false;
            }
        }

        /**
         * Convert IP address string to long.
         */
        private static long ipToLong(String ip) {
            String[] octets = ip.split("\\.");
            if(octets.length != 4) {
                throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
            }

            long result = 0;
            for(int i = 0; i < 4; i++) {
                result = (result << 8) | Integer.parseInt(octets[i]);
            }
            return result;
        }

        /**
         * Convert long to IP address string.
         */
        private static String ipFromLong(long ip) {
            return String.format("%d.%d.%d.%d",
                (ip >> 24) & 0xFF,
                (ip >> 16) & 0xFF,
                (ip >> 8) & 0xFF,
                ip & 0xFF);
        }
    }
}
