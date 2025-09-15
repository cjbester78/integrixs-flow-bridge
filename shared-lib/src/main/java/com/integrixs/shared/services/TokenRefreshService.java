package com.integrixs.shared.services;

/**
 * Service interface for handling OAuth token refresh operations
 */
public interface TokenRefreshService {

    /**
     * Refresh an OAuth access token using a refresh token
     * @param clientId The OAuth client ID
     * @param clientSecret The OAuth client secret
     * @param refreshToken The refresh token
     * @param tokenEndpointUrl The OAuth token endpoint URL
     * @return The new access token
     * @throws Exception if token refresh fails
     */
    String refreshToken(String clientId, String clientSecret, String refreshToken, String tokenEndpointUrl) throws Exception;

    /**
     * Check if a token needs to be refreshed
     * @param tokenExpiryTime The token expiry time in milliseconds
     * @return true if the token should be refreshed
     */
    boolean shouldRefreshToken(long tokenExpiryTime);

    /**
     * Store a refreshed token
     * @param adapterId The adapter ID
     * @param accessToken The new access token
     * @param expiresIn The token expiry time in seconds
     */
    void storeRefreshedToken(String adapterId, String accessToken, long expiresIn);
}
