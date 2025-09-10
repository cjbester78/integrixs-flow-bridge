-- Add WhatsApp Business adapter type
INSERT INTO adapter_type (code, name, category, icon, description, is_active) 
VALUES ('WHATSAPP', 'WhatsApp Business', 'Social Media', 'message-square', 'WhatsApp Business API for messaging and customer communication', true)
ON CONFLICT (code) DO NOTHING;

-- Add WhatsApp Business specific configurations
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
    ('phoneNumberId', '{"type": "text", "label": "Phone Number ID", "placeholder": "WhatsApp Phone Number ID"}', true),
    ('businessAccountId', '{"type": "text", "label": "Business Account ID", "placeholder": "WhatsApp Business Account ID"}', true),
    ('verifyToken', '{"type": "text", "label": "Webhook Verify Token", "placeholder": "Secure token for webhook verification"}', false),
    ('accessToken', '{"type": "password", "label": "Access Token", "placeholder": "Will be obtained via OAuth"}', false),
    ('systemUserAccessToken', '{"type": "password", "label": "System User Access Token", "placeholder": "For production use"}', false),
    ('apiVersion', '{"type": "text", "label": "API Version", "placeholder": "v18.0", "default": "v18.0"}', false),
    ('statusPollingInterval', '{"type": "number", "label": "Status Polling Interval (ms)", "placeholder": "60000", "default": "60000"}', false),
    ('messageRetentionHours', '{"type": "number", "label": "Message Retention Hours", "placeholder": "24", "default": "24"}', false),
    ('enableTextMessaging', '{"type": "boolean", "label": "Enable Text Messaging", "default": "true"}', false),
    ('enableMediaMessaging', '{"type": "boolean", "label": "Enable Media Messaging", "default": "true"}', false),
    ('enableTemplateMessaging', '{"type": "boolean", "label": "Enable Template Messaging", "default": "true"}', false),
    ('enableInteractiveMessages', '{"type": "boolean", "label": "Enable Interactive Messages", "default": "true"}', false),
    ('enableStatusUpdates', '{"type": "boolean", "label": "Enable Status Updates", "default": "true"}', false),
    ('enableGroupMessaging', '{"type": "boolean", "label": "Enable Group Messaging", "default": "true"}', false),
    ('enableContactManagement', '{"type": "boolean", "label": "Enable Contact Management", "default": "true"}', false),
    ('enableBusinessProfile', '{"type": "boolean", "label": "Enable Business Profile", "default": "true"}', false),
    ('enableCatalogs', '{"type": "boolean", "label": "Enable Catalogs", "default": "true"}', false),
    ('enableQRCodes', '{"type": "boolean", "label": "Enable QR Codes", "default": "true"}', false),
    ('enableLabels', '{"type": "boolean", "label": "Enable Labels", "default": "true"}', false),
    ('enableFlows', '{"type": "boolean", "label": "Enable Flows", "default": "true"}', false)
) AS config(key, value, required)
WHERE at.code = 'WHATSAPP'
ON CONFLICT (adapter_type_id, configuration_key) DO NOTHING;

-- Add default rate limit configuration for WhatsApp (higher limits)
INSERT INTO rate_limit_configuration (adapter_type_id, requests_per_minute, requests_per_hour, burst_capacity)
SELECT id, 100, 1000, 200
FROM adapter_type
WHERE code = 'WHATSAPP'
ON CONFLICT (adapter_type_id) DO NOTHING;