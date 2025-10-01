package com.integrixs.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.integrixs.data.sql.repository.UserSqlRepository;
import com.integrixs.data.model.User;
import com.integrixs.backend.logging.EnhancedAuthenticationLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);


    private final JwtUtil jwtUtil;
    private UserSqlRepository userRepository;
    private EnhancedAuthenticationLogger authLogger;

    @Autowired
    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setUserRepository(UserSqlRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired(required = false)
    public void setAuthLogger(EnhancedAuthenticationLogger authLogger) {
        this.authLogger = authLogger;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Log authentication attempt
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User - Agent");

            if(jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                log.debug("JWT Auth - Username: {}, Role: {}", username, role);

                if(authLogger != null) {
                    authLogger.logAuthenticationAttempt(username, "JWT", ipAddress, userAgent);
                }

                if(role == null) {
                    // For old tokens without role, invalidate them
                    log.warn("JWT Auth - Token missing role information, rejecting");
                    if(authLogger != null) {
                        authLogger.logAuthenticationFailure(username, "Token missing required role information", "JWT", ipAddress);
                    }
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write(" {\"message\":\"Token missing required role information. Please login again.\"}");
                    return;
                }

                // Load the User entity from the database
                User user = null;
                if(userRepository != null) {
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    if(userOpt.isPresent()) {
                        user = userOpt.get();
                    }
                }

                // Create authorities list with ROLE_ prefix for Spring Security
                // Convert role to uppercase to match Spring Security expectations
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
               );

                log.debug("JWT Auth - Authorities: {}", authorities);

                // Set either the User entity or username as principal
                var auth = new UsernamePasswordAuthenticationToken(
                        user != null ? user : username,
                        null,
                        authorities
               );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("JWT Auth - Authentication set for: {} with principal: {}",
                    request.getRequestURI(), (user != null ? "User entity" : "username"));

                if(authLogger != null) {
                    authLogger.logAuthenticationSuccess(username, auth,
                        request.getSession(false) != null ? request.getSession().getId() : "NO_SESSION",
                        jwtUtil.getExpirationDateFromToken(token).toInstant());
                }
            } else {
                log.warn("JWT Auth - Invalid token for: {}", request.getRequestURI());
                if(authLogger != null) {
                    String username = null;
                    try {
                        username = jwtUtil.extractUsername(token);
                    } catch(Exception e) {
                        // Ignore
                    }
                    authLogger.logAuthenticationFailure(username, "Invalid or expired token", "JWT", ipAddress);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.trace("JWT Filter - Checking path: {}", path);

        // Skip JWT filter for WebSocket upgrade requests
        String upgradeHeader = request.getHeader("Upgrade");
        if("websocket".equalsIgnoreCase(upgradeHeader)) {
            return true;
        }

        boolean shouldSkip = path.contains("/auth/login") ||
               path.contains("/auth/register") ||
               path.contains("/auth/refresh") ||
               path.equals("/health") ||
               path.startsWith("/ws/") ||
               path.equals("/flow - execution") ||
               path.equals("/echo") ||
               path.equals("/test - ws") ||
               path.equals("/wstest") ||
               path.equals("/direct - ws") ||
               path.equals("/minimal - echo") ||
               path.equals("/basic");

        log.trace("JWT Filter - Should skip: {}", shouldSkip);
        return shouldSkip;
    }
}
