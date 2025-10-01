package com.integrixs.backend.service;

import com.integrixs.shared.services.TokenRefreshService;
import com.integrixs.shared.services.OAuth2TokenRefreshService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Simple implementation of TokenRefreshService that delegates to OAuth2TokenRefreshService
 */
@Service
public class SimpleTokenRefreshService implements TokenRefreshService {

    @Autowired
    private OAuth2TokenRefreshService oauth2TokenRefreshService;

    @Override
    public String refreshToken(String clientId, String clientSecret, String refreshToken, String tokenEndpointUrl) throws Exception {
        OAuth2TokenRefreshService.OAuth2TokenResponse response =
            oauth2TokenRefreshService.refreshToken(clientId, clientSecret, refreshToken, tokenEndpointUrl);
        return response.getAccessToken();
    }

    @Override
    public boolean shouldRefreshToken(long tokenExpiryTime) {
        // Refresh if token expires within next 10 minutes
        return oauth2TokenRefreshService.isTokenExpired(tokenExpiryTime, 600);
    }

    @Override
    public void storeRefreshedToken(String adapterId, String accessToken, long expiresIn) {
        // For now, just log the operation
        // This could be enhanced to store the token using the OAuth2TokenRefreshService
        // or delegate to another service
    }
}