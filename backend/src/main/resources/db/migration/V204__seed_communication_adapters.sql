-- Communication Adapters

-- Twilio
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'twilio',
    'Twilio',
    (SELECT id FROM adapter_categories WHERE code = 'communication'),
    'Twilio',
    '2.0',
    'Connect to Twilio for SMS, voice, and messaging communications',
    'message-circle',
    true,
    true,
    false,
    -- Common configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'account',
                'title', 'Account Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'accountSid',
                        'type', 'text',
                        'label', 'Account SID',
                        'required', true,
                        'placeholder', 'AC...',
                        'help', 'Your Twilio Account SID'
                    ),
                    jsonb_build_object(
                        'name', 'authToken',
                        'type', 'password',
                        'label', 'Auth Token',
                        'required', true,
                        'help', 'Your Twilio Auth Token'
                    ),
                    jsonb_build_object(
                        'name', 'region',
                        'type', 'select',
                        'label', 'Region',
                        'default', 'us1',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'us1', 'label', 'US (Default)'),
                            jsonb_build_object('value', 'ie1', 'label', 'Ireland'),
                            jsonb_build_object('value', 'au1', 'label', 'Australia'),
                            jsonb_build_object('value', 'jp1', 'label', 'Japan')
                        )
                    )
                )
            )
        )
    ),
    -- Inbound configuration (webhooks)
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'webhook',
                'title', 'Webhook Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'webhookType',
                        'type', 'multiselect',
                        'label', 'Event Types',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'message.received', 'label', 'SMS Received'),
                            jsonb_build_object('value', 'message.delivered', 'label', 'SMS Delivered'),
                            jsonb_build_object('value', 'message.failed', 'label', 'SMS Failed'),
                            jsonb_build_object('value', 'call.initiated', 'label', 'Call Started'),
                            jsonb_build_object('value', 'call.completed', 'label', 'Call Completed')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'validateSignature',
                        'type', 'boolean',
                        'label', 'Validate Webhook Signature',
                        'default', true,
                        'help', 'Verify webhook requests are from Twilio'
                    )
                )
            )
        )
    ),
    -- Outbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'messaging',
                'title', 'Messaging Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'fromPhoneNumber',
                        'type', 'text',
                        'label', 'From Phone Number',
                        'required', true,
                        'placeholder', '+1234567890',
                        'help', 'Your Twilio phone number'
                    ),
                    jsonb_build_object(
                        'name', 'messageService',
                        'type', 'select',
                        'label', 'Message Service',
                        'required', true,
                        'default', 'sms',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'sms', 'label', 'SMS'),
                            jsonb_build_object('value', 'whatsapp', 'label', 'WhatsApp'),
                            jsonb_build_object('value', 'voice', 'label', 'Voice Call')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'statusCallback',
                        'type', 'text',
                        'label', 'Status Callback URL',
                        'placeholder', 'https://your-app.com/twilio/status',
                        'help', 'URL to receive delivery status updates'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', false,
        'webhooks', true,
        'pagination', true,
        'filtering', true,
        'sorting', false,
        'customFields', false,
        'attachments', true,
        'mms', true,
        'voice', true,
        'video', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['APIKey'],
    'https://www.twilio.com/docs',
    'standard',
    'active',
    true
);

-- SendGrid
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'sendgrid',
    'SendGrid',
    (SELECT id FROM adapter_categories WHERE code = 'communication'),
    'Twilio',
    '3.0',
    'Connect to SendGrid for transactional and marketing emails',
    'mail',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'authentication',
                'title', 'Authentication',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'apiKey',
                        'type', 'password',
                        'label', 'API Key',
                        'required', true,
                        'help', 'Your SendGrid API key with appropriate permissions'
                    )
                )
            )
        )
    ),
    -- Inbound (webhooks)
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'events',
                'title', 'Event Webhooks',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'events',
                        'type', 'multiselect',
                        'label', 'Track Events',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'processed', 'label', 'Processed'),
                            jsonb_build_object('value', 'delivered', 'label', 'Delivered'),
                            jsonb_build_object('value', 'open', 'label', 'Opened'),
                            jsonb_build_object('value', 'click', 'label', 'Clicked'),
                            jsonb_build_object('value', 'bounce', 'label', 'Bounced'),
                            jsonb_build_object('value', 'spam_report', 'label', 'Spam Report'),
                            jsonb_build_object('value', 'unsubscribe', 'label', 'Unsubscribed')
                        )
                    )
                )
            )
        )
    ),
    -- Outbound
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'sending',
                'title', 'Email Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'fromEmail',
                        'type', 'text',
                        'label', 'Default From Email',
                        'required', true,
                        'placeholder', 'noreply@company.com',
                        'validation', jsonb_build_object(
                            'pattern', '^[^@]+@[^@]+\\.[^@]+$',
                            'message', 'Must be a valid email address'
                        )
                    ),
                    jsonb_build_object(
                        'name', 'fromName',
                        'type', 'text',
                        'label', 'Default From Name',
                        'placeholder', 'Your Company'
                    ),
                    jsonb_build_object(
                        'name', 'replyTo',
                        'type', 'text',
                        'label', 'Reply-To Email',
                        'placeholder', 'support@company.com'
                    ),
                    jsonb_build_object(
                        'name', 'sandboxMode',
                        'type', 'boolean',
                        'label', 'Sandbox Mode',
                        'default', false,
                        'help', 'Validate but don''t send emails'
                    ),
                    jsonb_build_object(
                        'name', 'trackingSettings',
                        'type', 'group',
                        'label', 'Tracking Settings',
                        'fields', jsonb_build_array(
                            jsonb_build_object(
                                'name', 'clickTracking',
                                'type', 'boolean',
                                'label', 'Enable Click Tracking',
                                'default', true
                            ),
                            jsonb_build_object(
                                'name', 'openTracking',
                                'type', 'boolean',
                                'label', 'Enable Open Tracking',
                                'default', true
                            )
                        )
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', false,
        'webhooks', true,
        'pagination', true,
        'filtering', true,
        'sorting', false,
        'customFields', true,
        'attachments', true,
        'templates', true,
        'dynamicContent', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['APIKey'],
    'https://docs.sendgrid.com/',
    'free',
    'active',
    true
);

-- Slack
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'slack',
    'Slack',
    (SELECT id FROM adapter_categories WHERE code = 'communication'),
    'Slack',
    '2.0',
    'Connect to Slack for team messaging and notifications',
    'message-square',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'app',
                'title', 'Slack App Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'oauth2',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'oauth2', 'label', 'OAuth 2.0 (Workspace)'),
                            jsonb_build_object('value', 'bot', 'label', 'Bot User Token'),
                            jsonb_build_object('value', 'webhook', 'label', 'Incoming Webhook')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'token',
                        'type', 'password',
                        'label', 'Access Token',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'operator', 'in', 'value', jsonb_build_array('oauth2', 'bot')),
                        'help', 'xoxb-... for bot tokens, xoxp-... for user tokens'
                    ),
                    jsonb_build_object(
                        'name', 'webhookUrl',
                        'type', 'text',
                        'label', 'Webhook URL',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'webhook'),
                        'placeholder', 'https://hooks.slack.com/services/...'
                    ),
                    jsonb_build_object(
                        'name', 'defaultChannel',
                        'type', 'text',
                        'label', 'Default Channel',
                        'placeholder', '#general',
                        'help': 'Default channel for messages (use # for channels, @ for users)'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', false,
        'streaming', true,
        'webhooks', true,
        'pagination', true,
        'filtering', true,
        'sorting', false,
        'customFields', false,
        'attachments', true,
        'interactiveMessages', true,
        'slashCommands', true,
        'threading', true
    ),
    ARRAY['REST', 'WebSocket'],
    ARRAY['JSON'],
    ARRAY['OAuth2', 'BotToken', 'Webhook'],
    'https://api.slack.com/',
    'free',
    'active',
    true
);

-- Microsoft Teams
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'microsoft-teams',
    'Microsoft Teams',
    (SELECT id FROM adapter_categories WHERE code = 'communication'),
    'Microsoft',
    '1.0',
    'Connect to Microsoft Teams for collaboration and messaging',
    'message-square',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'app',
                'title', 'Teams App Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'tenantId',
                        'type', 'text',
                        'label', 'Tenant ID',
                        'required', true,
                        'help', 'Your Microsoft 365 tenant ID'
                    ),
                    jsonb_build_object(
                        'name', 'clientId',
                        'type', 'text',
                        'label', 'Application (Client) ID',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'clientSecret',
                        'type', 'password',
                        'label', 'Client Secret',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'botId',
                        'type', 'text',
                        'label', 'Bot ID',
                        'help', 'Microsoft App ID for bot framework'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', false,
        'streaming', true,
        'webhooks', true,
        'pagination', true,
        'filtering', true,
        'sorting', false,
        'customFields', false,
        'attachments', true,
        'adaptiveCards', true,
        'meetings', true,
        'channels', true
    ),
    ARRAY['REST', 'WebSocket'],
    ARRAY['JSON'],
    ARRAY['OAuth2'],
    'https://docs.microsoft.com/en-us/microsoftteams/platform/',
    'standard',
    'active',
    true
);

-- WhatsApp Business
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'whatsapp-business',
    'WhatsApp Business',
    (SELECT id FROM adapter_categories WHERE code = 'communication'),
    'Meta',
    '2.0',
    'Connect to WhatsApp Business API for customer messaging',
    'message-circle',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'account',
                'title', 'WhatsApp Business Account',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'phoneNumberId',
                        'type', 'text',
                        'label', 'Phone Number ID',
                        'required', true,
                        'help', 'Your WhatsApp Business phone number ID'
                    ),
                    jsonb_build_object(
                        'name', 'businessAccountId',
                        'type', 'text',
                        'label', 'Business Account ID',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'accessToken',
                        'type', 'password',
                        'label', 'Access Token',
                        'required', true,
                        'help', 'Permanent or temporary access token'
                    ),
                    jsonb_build_object(
                        'name', 'apiVersion',
                        'type', 'select',
                        'label', 'API Version',
                        'default', 'v17.0',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'v17.0', 'label', 'v17.0 (Latest)'),
                            jsonb_build_object('value', 'v16.0', 'label', 'v16.0'),
                            jsonb_build_object('value', 'v15.0', 'label', 'v15.0')
                        )
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', false,
        'webhooks', true,
        'pagination', false,
        'filtering', false,
        'sorting', false,
        'customFields', false,
        'attachments', true,
        'templates', true,
        'interactiveMessages', true,
        'mediaMessages', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['BearerToken'],
    'https://developers.facebook.com/docs/whatsapp',
    'standard',
    'active',
    true
);

-- Zoom
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'zoom',
    'Zoom',
    (SELECT id FROM adapter_categories WHERE code = 'communication'),
    'Zoom',
    '2.0',
    'Connect to Zoom for video conferencing and webinars',
    'video',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'app',
                'title', 'Zoom App Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'App Type',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'oauth', 'label', 'OAuth App'),
                            jsonb_build_object('value', 'jwt', 'label', 'JWT App (Deprecated)'),
                            jsonb_build_object('value', 'server', 'label', 'Server-to-Server OAuth')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'clientId',
                        'type', 'text',
                        'label', 'Client ID',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'clientSecret',
                        'type', 'password',
                        'label', 'Client Secret',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'accountId',
                        'type', 'text',
                        'label', 'Account ID',
                        'condition', jsonb_build_object('field', 'authType', 'value', 'server'),
                        'required', true
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', false,
        'streaming', true,
        'webhooks', true,
        'pagination', true,
        'filtering', true,
        'sorting', true,
        'customFields', false,
        'attachments', false,
        'meetings', true,
        'webinars', true,
        'recordings', true,
        'chat', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['OAuth2', 'JWT', 'ServerOAuth'],
    'https://marketplace.zoom.us/docs/api-reference',
    'standard',
    'active',
    true
);