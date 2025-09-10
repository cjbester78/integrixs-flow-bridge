-- Add Facebook Ads adapter type
INSERT INTO adapter_type (code, name, category, icon, description, is_active) 
VALUES ('FACEBOOK_ADS', 'Facebook Ads', 'Social Media', 'credit-card', 'Facebook Ads API for advertising campaign management', true)
ON CONFLICT (code) DO NOTHING;

-- Add Facebook Ads specific configurations
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
    ('adAccountId', '{"type": "text", "label": "Ad Account ID", "placeholder": "Your Ad Account ID (without act_ prefix)"}', true),
    ('businessId', '{"type": "text", "label": "Business Manager ID", "placeholder": "Your Business Manager ID"}', false),
    ('accessToken', '{"type": "password", "label": "Access Token", "placeholder": "Will be obtained via OAuth"}', false),
    ('apiVersion', '{"type": "text", "label": "API Version", "placeholder": "v18.0", "default": "v18.0"}', false),
    ('pollingInterval', '{"type": "number", "label": "Polling Interval (ms)", "placeholder": "300000", "default": "300000"}', false),
    ('enableCampaignManagement', '{"type": "boolean", "label": "Enable Campaign Management", "default": "true"}', false),
    ('enableAudienceTargeting', '{"type": "boolean", "label": "Enable Audience Targeting", "default": "true"}', false),
    ('enableBudgetOptimization', '{"type": "boolean", "label": "Enable Budget Optimization", "default": "true"}', false),
    ('enableCreativeAssets', '{"type": "boolean", "label": "Enable Creative Assets", "default": "true"}', false),
    ('enablePerformanceTracking', '{"type": "boolean", "label": "Enable Performance Tracking", "default": "true"}', false),
    ('enableAutomatedRules', '{"type": "boolean", "label": "Enable Automated Rules", "default": "false"}', false),
    ('enableA_BTesting', '{"type": "boolean", "label": "Enable A/B Testing", "default": "true"}', false),
    ('enableCustomConversions', '{"type": "boolean", "label": "Enable Custom Conversions", "default": "true"}', false),
    ('enablePixelTracking', '{"type": "boolean", "label": "Enable Pixel Tracking", "default": "true"}', false),
    ('enableLeadForms', '{"type": "boolean", "label": "Enable Lead Forms", "default": "true"}', false)
) AS config(key, value, required)
WHERE at.code = 'FACEBOOK_ADS'
ON CONFLICT (adapter_type_id, configuration_key) DO NOTHING;

-- Add default rate limit configuration for Facebook Ads
INSERT INTO rate_limit_configuration (adapter_type_id, requests_per_minute, requests_per_hour, burst_capacity)
SELECT id, 60, 200, 100
FROM adapter_type
WHERE code = 'FACEBOOK_ADS'
ON CONFLICT (adapter_type_id) DO NOTHING;