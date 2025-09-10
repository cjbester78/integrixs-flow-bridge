-- Add Instagram adapter type
INSERT INTO adapter_type (code, name, category, icon, description, is_active) 
VALUES ('INSTAGRAM', 'Instagram', 'Social Media', 'camera', 'Instagram Graph API for social media content and analytics', true)
ON CONFLICT (code) DO NOTHING;

-- Add Instagram specific configurations
INSERT INTO adapter_type_configuration (adapter_type_id, configuration_key, configuration_value, is_required)
SELECT 
    at.id,
    key,
    value,
    required
FROM adapter_type at
CROSS JOIN (VALUES
    ('appId', '{"type": "text", "label": "Facebook App ID", "placeholder": "Your Facebook App ID"}', true),
    ('appSecret', '{"type": "password", "label": "Facebook App Secret", "placeholder": "Your Facebook App Secret"}', true),
    ('instagramBusinessAccountId', '{"type": "text", "label": "Instagram Business Account ID", "placeholder": "Your Instagram Business Account ID"}', true),
    ('facebookPageId', '{"type": "text", "label": "Facebook Page ID", "placeholder": "Connected Facebook Page ID"}', true),
    ('accessToken', '{"type": "password", "label": "Access Token", "placeholder": "Will be obtained via OAuth"}', false),
    ('apiVersion', '{"type": "text", "label": "API Version", "placeholder": "v18.0", "default": "v18.0"}', false),
    ('pollingInterval', '{"type": "number", "label": "Content Polling Interval (ms)", "placeholder": "300000", "default": "300000"}', false),
    ('insightsInterval', '{"type": "number", "label": "Insights Polling Interval (ms)", "placeholder": "3600000", "default": "3600000"}', false),
    ('enablePostPublishing', '{"type": "boolean", "label": "Enable Post Publishing", "default": "true"}', false),
    ('enableStories', '{"type": "boolean", "label": "Enable Stories", "default": "true"}', false),
    ('enableReels', '{"type": "boolean", "label": "Enable Reels", "default": "true"}', false),
    ('enableCommentManagement', '{"type": "boolean", "label": "Enable Comment Management", "default": "true"}', false),
    ('enableHashtagAnalytics', '{"type": "boolean", "label": "Enable Hashtag Analytics", "default": "true"}', false),
    ('enableUserInsights', '{"type": "boolean", "label": "Enable User Insights", "default": "true"}', false),
    ('enableShoppingTags', '{"type": "boolean", "label": "Enable Shopping Tags", "default": "true"}', false),
    ('enableIGTV', '{"type": "boolean", "label": "Enable IGTV", "default": "true"}', false),
    ('enableContentScheduling', '{"type": "boolean", "label": "Enable Content Scheduling", "default": "true"}', false),
    ('enableMentionMonitoring', '{"type": "boolean", "label": "Enable Mention Monitoring", "default": "true"}', false),
    ('enableCarouselPosts', '{"type": "boolean", "label": "Enable Carousel Posts", "default": "true"}', false),
    ('enableProductTagging', '{"type": "boolean", "label": "Enable Product Tagging", "default": "true"}', false)
) AS config(key, value, required)
WHERE at.code = 'INSTAGRAM'
ON CONFLICT (adapter_type_id, configuration_key) DO NOTHING;

-- Add default rate limit configuration for Instagram
INSERT INTO rate_limit_configuration (adapter_type_id, requests_per_minute, requests_per_hour, burst_capacity)
SELECT id, 60, 200, 100
FROM adapter_type
WHERE code = 'INSTAGRAM'
ON CONFLICT (adapter_type_id) DO NOTHING;