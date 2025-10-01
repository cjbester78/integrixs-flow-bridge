package com.integrixs.shared.services;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for OAuth2 token refresh operations
 */
public interface OAuth2TokenRefreshService {

    /**
     * Refresh an OAuth2 access token
     * @param clientId The OAuth2 client ID
     * @param clientSecret The OAuth2 client secret
     * @param refreshToken The refresh token
     * @param tokenUrl The token endpoint URL
     * @return OAuth2TokenResponse containing the new tokens
     */
    OAuth2TokenResponse refreshToken(String clientId, String clientSecret, String refreshToken, String tokenUrl);

    /**
     * Refresh an OAuth2 access token with additional parameters
     * @param clientId The OAuth2 client ID
     * @param clientSecret The OAuth2 client secret
     * @param refreshToken The refresh token
     * @param tokenUrl The token endpoint URL
     * @param additionalParams Additional parameters for the token request
     * @return OAuth2TokenResponse containing the new tokens
     */
    OAuth2TokenResponse refreshToken(String clientId, String clientSecret, String refreshToken,
                                   String tokenUrl, Map<String, String> additionalParams);

    /**
     * Refresh an OAuth2 access token asynchronously
     * @param clientId The OAuth2 client ID
     * @param clientSecret The OAuth2 client secret
     * @param refreshToken The refresh token
     * @param tokenUrl The token endpoint URL
     * @return CompletableFuture with OAuth2TokenResponse
     */
    CompletableFuture<OAuth2TokenResponse> refreshTokenAsync(String clientId, String clientSecret,
                                                            String refreshToken, String tokenUrl);

    /**
     * Check if a token needs refreshing based on expiry time
     * @param expiresAt The token expiry time in milliseconds
     * @param bufferTimeSeconds Buffer time before actual expiry to refresh
     * @return true if the token needs refreshing
     */
    boolean isTokenExpired(long expiresAt, int bufferTimeSeconds);

    /**
     * OAuth2 token response
     */
    class OAuth2TokenResponse {
        private final String accessToken;
        private final String refreshToken;
        private final String tokenType;
        private final Long expiresIn;
        private final String scope;

        public OAuth2TokenResponse(String accessToken, String refreshToken, String tokenType,
                                 Long expiresIn, String scope) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.scope = scope;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public Long getExpiresIn() {
            return expiresIn;
        }

        public String getScope() {
            return scope;
        }

        public long getExpiryTime() {
            if(expiresIn == null) {
                return Long.MAX_VALUE;
            }
            return System.currentTimeMillis() + (expiresIn * 1000);
        }
    }
}
