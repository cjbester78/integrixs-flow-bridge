package com.integrixs.backend.security;

import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.UserSqlRepository;
import com.integrixs.shared.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter to populate UserContext from Spring Security context.
 * This allows the audit entity listener to access the current user.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@Component
public class UserContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserContextFilter.class);


    private final UserSqlRepository userRepository;

    public UserContextFilter(UserSqlRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            // Get authentication from Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if(authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName())) {

                String username = authentication.getName();

                // Try to get user from database
                try {
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    if(userOpt.isPresent()) {
                        User user = userOpt.get();
                        // Set user context for audit
                        UserContext.setCurrentUser(user.getId(), user.getEmail());
                        log.debug("Set UserContext for user: {}", username);
                    }
                } catch(Exception e) {
                    log.warn("Failed to set user context for: {}", username, e);
                }
            }

            // Continue filter chain
            filterChain.doFilter(request, response);

        } finally {
            // Always clear context after request
            UserContext.clear();
        }
    }
}
