# Facebook Graph API Adapter - Detailed Implementation Guide

## Overview
This document provides a step-by-step implementation guide for the Facebook Graph API adapter, serving as a template for other social media adapters.

## Prerequisites
- Facebook Developer Account
- App Registration on Facebook
- Understanding of OAuth 2.0 flow
- Java/Spring Boot knowledge

## Implementation Steps

### Step 1: Create Base Social Media Adapter Classes

#### 1.1 Create Base Configuration
```java
package com.integrixs.adapters.social;

import com.integrixs.adapters.base.AdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class SocialMediaAdapterConfig extends AdapterConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String[] scopes;
    private String apiVersion;
    private RateLimitConfig rateLimitConfig;
    private boolean webhookEnabled;
    
    @Data
    public static class RateLimitConfig {
        private int requestsPerHour = 200;
        private int burstSize = 100;
        private int retryAttempts = 3;
        private long retryDelayMs = 1000;
    }
}
```

#### 1.2 Create OAuth2 Handler
```java
package com.integrixs.adapters.social.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OAuth2Handler {
    private final WebClient webClient;
    
    public OAuth2Handler(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    
    public String generateAuthorizationUrl(String clientId, String redirectUri, String[] scopes, String state) {
        // Implementation for generating OAuth2 authorization URL
    }
    
    public OAuth2Token exchangeCodeForToken(String code, String clientId, String clientSecret, String redirectUri) {
        // Implementation for exchanging authorization code for access token
    }
    
    public OAuth2Token refreshAccessToken(String refreshToken, String clientId, String clientSecret) {
        // Implementation for refreshing access token
    }
}
```

### Step 2: Implement Facebook-Specific Classes

#### 2.1 Facebook Configuration
```java
package com.integrixs.adapters.social.facebook;

import com.integrixs.adapters.social.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FacebookAdapterConfig extends SocialMediaAdapterConfig {
    private String pageAccessToken;
    private String pageId;
    private String webhookVerifyToken;
    private FacebookPermissions permissions;
    
    @Data
    public static class FacebookPermissions {
        private boolean managePages = true;
        private boolean publishPages = true;
        private boolean readInsights = true;
        private boolean readPageMailboxes = false;
        private boolean adsManagement = false;
    }
    
    public FacebookAdapterConfig() {
        setApiVersion("v18.0"); // Current Facebook Graph API version
        setScopes(new String[]{
            "pages_show_list",
            "pages_read_engagement", 
            "pages_manage_posts",
            "pages_read_user_content",
            "pages_manage_engagement"
        });
    }
}
```

#### 2.2 Facebook Inbound Adapter
```java
package com.integrixs.adapters.social.facebook;

import com.integrixs.adapters.base.InboundAdapter;
import com.integrixs.shared.dto.FlowMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FacebookInboundAdapter extends InboundAdapter<FacebookAdapterConfig> {
    
    private final FacebookApiClient apiClient;
    private final WebhookProcessor webhookProcessor;
    
    public FacebookInboundAdapter(FacebookApiClient apiClient, WebhookProcessor webhookProcessor) {
        this.apiClient = apiClient;
        this.webhookProcessor = webhookProcessor;
    }
    
    @Override
    public FlowMessage receive(String flowId, FacebookAdapterConfig config) {
        // Poll for new posts, comments, or messages
        try {
            if (config.isWebhookEnabled()) {
                // Process webhook events
                return webhookProcessor.getNextEvent(flowId);
            } else {
                // Polling approach
                return pollFacebookData(config);
            }
        } catch (Exception e) {
            log.error("Error receiving from Facebook: ", e);
            throw new AdapterException("Failed to receive Facebook data", e);
        }
    }
    
    private FlowMessage pollFacebookData(FacebookAdapterConfig config) {
        // Implementation for polling Facebook data
        // This could fetch new posts, comments, messages, etc.
    }
    
    @Override
    public void validateConfiguration(FacebookAdapterConfig config) {
        if (config.getClientId() == null || config.getClientId().isEmpty()) {
            throw new ConfigurationException("Facebook Client ID is required");
        }
        if (config.getPageId() == null || config.getPageId().isEmpty()) {
            throw new ConfigurationException("Facebook Page ID is required");
        }
    }
}
```

#### 2.3 Facebook Outbound Adapter
```java
package com.integrixs.adapters.social.facebook;

import com.integrixs.adapters.base.OutboundAdapter;
import com.integrixs.shared.dto.FlowMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FacebookOutboundAdapter extends OutboundAdapter<FacebookAdapterConfig> {
    
    private final FacebookApiClient apiClient;
    private final MediaUploader mediaUploader;
    
    public FacebookOutboundAdapter(FacebookApiClient apiClient, MediaUploader mediaUploader) {
        this.apiClient = apiClient;
        this.mediaUploader = mediaUploader;
    }
    
    @Override
    public void send(FlowMessage message, String flowId, FacebookAdapterConfig config) {
        try {
            FacebookPost post = convertToFacebookPost(message);
            
            // Handle media uploads if present
            if (post.hasMedia()) {
                post.setMediaIds(mediaUploader.uploadMedia(post.getMediaFiles(), config));
            }
            
            // Publish the post
            String postId = apiClient.publishPost(post, config);
            
            // Update message with Facebook post ID
            message.getHeaders().put("facebook_post_id", postId);
            
            log.info("Successfully published to Facebook: {}", postId);
            
        } catch (Exception e) {
            log.error("Error sending to Facebook: ", e);
            throw new AdapterException("Failed to send to Facebook", e);
        }
    }
    
    private FacebookPost convertToFacebookPost(FlowMessage message) {
        // Convert FlowMessage to Facebook-specific post format
    }
}
```

#### 2.4 Facebook API Client
```java
package com.integrixs.adapters.social.facebook;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;
import java.time.Duration;

@Component
public class FacebookApiClient {
    
    private static final String BASE_URL = "https://graph.facebook.com";
    private final WebClient webClient;
    private final RateLimiter rateLimiter;
    
    public FacebookApiClient(WebClient.Builder webClientBuilder, RateLimiter rateLimiter) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.rateLimiter = rateLimiter;
    }
    
    public String publishPost(FacebookPost post, FacebookAdapterConfig config) {
        rateLimiter.acquire();
        
        return webClient.post()
            .uri("/{version}/{pageId}/feed", config.getApiVersion(), config.getPageId())
            .header("Authorization", "Bearer " + config.getPageAccessToken())
            .bodyValue(post)
            .retrieve()
            .bodyToMono(FacebookResponse.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
            .map(FacebookResponse::getId)
            .block();
    }
    
    public FacebookPageInsights getPageInsights(String pageId, FacebookAdapterConfig config) {
        // Implementation for fetching page insights
    }
    
    public List<FacebookComment> getPostComments(String postId, FacebookAdapterConfig config) {
        // Implementation for fetching post comments
    }
}
```

### Step 3: Implement Data Models

#### 3.1 Facebook Post Model
```java
package com.integrixs.adapters.social.facebook.model;

import lombok.Data;
import java.util.List;

@Data
public class FacebookPost {
    private String message;
    private String link;
    private List<String> mediaIds;
    private boolean published = true;
    private String scheduledPublishTime;
    private FacebookTargeting targeting;
    private List<String> tags;
    
    @Data
    public static class FacebookTargeting {
        private List<String> countries;
        private List<String> cities;
        private Integer ageMin;
        private Integer ageMax;
        private List<String> interests;
    }
}
```

### Step 4: Implement Webhook Support

#### 4.1 Webhook Controller
```java
package com.integrixs.adapters.social.facebook;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/facebook")
public class FacebookWebhookController {
    
    private final WebhookProcessor processor;
    
    @GetMapping
    public String verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        
        // Verify the webhook subscription
        if ("subscribe".equals(mode) && processor.verifyToken(token)) {
            return challenge;
        }
        return "Invalid verification token";
    }
    
    @PostMapping
    public void handleWebhook(@RequestBody FacebookWebhookEvent event) {
        processor.processEvent(event);
    }
}
```

### Step 5: Add Rate Limiting

#### 5.1 Rate Limiter Implementation
```java
package com.integrixs.adapters.social.ratelimit;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;

@Component
public class AdaptiveRateLimiter {
    
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    public void acquire(String key, int requestsPerHour) {
        RateLimiter limiter = limiters.computeIfAbsent(key, 
            k -> RateLimiter.create(requestsPerHour / 3600.0));
        limiter.acquire();
    }
    
    public boolean tryAcquire(String key, int requestsPerHour, long timeout, TimeUnit unit) {
        RateLimiter limiter = limiters.computeIfAbsent(key, 
            k -> RateLimiter.create(requestsPerHour / 3600.0));
        return limiter.tryAcquire(timeout, unit);
    }
}
```

### Step 6: Add Configuration UI Components

#### 6.1 Facebook Configuration Component (React)
```typescript
// FacebookAdapterConfiguration.tsx
import React from 'react';
import { Input, Button, Switch } from '@/components/ui';

interface FacebookAdapterConfigProps {
  config: FacebookConfig;
  onChange: (config: FacebookConfig) => void;
}

export const FacebookAdapterConfiguration: React.FC<FacebookAdapterConfigProps> = ({
  config,
  onChange
}) => {
  const handleOAuth = async () => {
    // Initiate OAuth flow
    const authUrl = `/api/oauth/facebook/authorize?redirect_uri=${encodeURIComponent(window.location.origin + '/oauth/callback')}`;
    window.location.href = authUrl;
  };
  
  return (
    <div className="space-y-4">
      <div>
        <Label>Facebook Page ID</Label>
        <Input
          value={config.pageId || ''}
          onChange={(e) => onChange({ ...config, pageId: e.target.value })}
          placeholder="Enter your Facebook Page ID"
        />
      </div>
      
      <div>
        <Label>Authentication</Label>
        {config.pageAccessToken ? (
          <div className="flex items-center space-x-2">
            <span className="text-green-600">✓ Connected</span>
            <Button variant="outline" size="sm" onClick={handleOAuth}>
              Reconnect
            </Button>
          </div>
        ) : (
          <Button onClick={handleOAuth}>
            Connect Facebook Account
          </Button>
        )}
      </div>
      
      <div>
        <Label>Webhook Integration</Label>
        <Switch
          checked={config.webhookEnabled || false}
          onCheckedChange={(checked) => 
            onChange({ ...config, webhookEnabled: checked })
          }
        />
        <p className="text-sm text-gray-600 mt-1">
          Enable real-time updates via webhooks
        </p>
      </div>
      
      <div>
        <Label>Permissions</Label>
        <div className="space-y-2">
          {Object.entries(config.permissions || {}).map(([key, value]) => (
            <label key={key} className="flex items-center space-x-2">
              <Switch
                checked={value as boolean}
                onCheckedChange={(checked) =>
                  onChange({
                    ...config,
                    permissions: { ...config.permissions, [key]: checked }
                  })
                }
              />
              <span className="text-sm">{formatPermissionName(key)}</span>
            </label>
          ))}
        </div>
      </div>
    </div>
  );
};
```

### Step 7: Testing Strategy

#### 7.1 Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class FacebookOutboundAdapterTest {
    
    @Mock
    private FacebookApiClient apiClient;
    
    @Mock
    private MediaUploader mediaUploader;
    
    @InjectMocks
    private FacebookOutboundAdapter adapter;
    
    @Test
    void testPublishSimpleTextPost() {
        // Test implementation
    }
    
    @Test
    void testPublishPostWithMedia() {
        // Test implementation
    }
    
    @Test
    void testRateLimitingBehavior() {
        // Test implementation
    }
}
```

### Step 8: Documentation

#### 8.1 Quick Start Guide
```markdown
# Facebook Graph API Adapter - Quick Start

## Prerequisites
1. Create a Facebook App at https://developers.facebook.com
2. Add your app's redirect URI
3. Request necessary permissions

## Configuration
1. In Integrixs Flow Bridge, create a new adapter
2. Select "Facebook" as the adapter type
3. Click "Connect Facebook Account"
4. Authorize the requested permissions
5. Select the Facebook Page to connect

## Usage Examples

### Publishing a Post
```json
{
  "content": {
    "message": "Hello from Integrixs Flow Bridge!",
    "link": "https://example.com",
    "media": [
      {
        "type": "image",
        "url": "https://example.com/image.jpg"
      }
    ]
  }
}
```

### Reading Page Insights
Configure the inbound adapter to poll for:
- New posts
- Comments
- Messages
- Page insights
```

## Next Steps

This detailed implementation for Facebook serves as a template for implementing other social media adapters. Each platform will have its specific:

1. Authentication requirements
2. API endpoints
3. Rate limits
4. Data models
5. Webhook patterns

The key is maintaining consistency in the adapter interface while accommodating platform-specific features.