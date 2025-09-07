package com.integrixs.backend.security.jwt;

import com.integrixs.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider wrapper for consistent naming.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    private final JwtUtil jwtUtil;
    
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
    
    public String getUserId(String token) {
        return jwtUtil.getUserId(token);
    }
    
    public String getClaimFromToken(String token, String claimName) {
        return jwtUtil.getClaimFromToken(token, claimName);
    }
}