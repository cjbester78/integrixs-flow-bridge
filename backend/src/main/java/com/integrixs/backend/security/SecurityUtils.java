package com.integrixs.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

/**
 * Security utility class
 */
public class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    /**
     * Get current username
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if(principal instanceof UserDetails) {
                return((UserDetails) principal).getUsername();
            } else if(principal instanceof String) {
                return(String) principal;
            }
        }
        return null;
    }

    /**
     * Get current username (static version for backwards compatibility)
     */
    public static String getCurrentUsernameStatic() {
        return getCurrentUsername();
    }

    /**
     * Get current user ID
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if(principal instanceof UserPrincipal) {
                return((UserPrincipal) principal).getUserId();
            }
        }
        return null;
    }

    /**
     * Check if user has role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
        }
        return false;
    }

    /**
     * Get current user's role
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            // Get the first role (assuming single role per user)
            return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .orElse("USER");
        }
        return "ANONYMOUS";
    }

    /**
     * Custom user principal
     */
    public static class UserPrincipal implements UserDetails {
        private final UUID userId;
        private final String username;
        private final String password;
        private final boolean enabled;
        private final java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities;

        public UserPrincipal(UUID userId, String username, String password, boolean enabled,
                           java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.enabled = enabled;
            this.authorities = authorities;
        }

        public UUID getUserId() {
            return userId;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
