-- Remaining Adapters for Various Categories

-- JIRA (Business Apps)
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'jira',
    'Jira',
    (SELECT id FROM adapter_categories WHERE code = 'business_apps'),
    'Atlassian',
    '3',
    'Connect to Jira for issue tracking and project management',
    'check-square',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Jira Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'instanceUrl',
                        'type', 'text',
                        'label', 'Instance URL',
                        'required', true,
                        'placeholder', 'https://your-domain.atlassian.net',
                        'help', 'Your Jira instance URL'
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'basic',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'basic', 'label', 'Basic Auth (Email + API Token)'),
                            jsonb_build_object('value', 'oauth2', 'label', 'OAuth 2.0'),
                            jsonb_build_object('value', 'pat', 'label', 'Personal Access Token')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'email',
                        'type', 'text',
                        'label', 'Email',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'basic'),
                        'placeholder', 'user@example.com'
                    ),
                    jsonb_build_object(
                        'name', 'apiToken',
                        'type', 'password',
                        'label', 'API Token',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'basic'),
                        'help': 'Generate from Atlassian account settings'
                    ),
                    jsonb_build_object(
                        'name', 'personalAccessToken',
                        'type', 'password',
                        'label', 'Personal Access Token',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'pat')
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
        'sorting', true,
        'customFields', true,
        'attachments', true,
        'jql', true,
        'agile', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['Basic', 'OAuth2', 'PAT'],
    'https://developer.atlassian.com/cloud/jira/platform/',
    'standard',
    'active',
    true
);

-- Stripe (Financial Services)
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'stripe',
    'Stripe',
    (SELECT id FROM adapter_categories WHERE code = 'financial'),
    'Stripe',
    '2023-10-16',
    'Connect to Stripe for payment processing and billing',
    'credit-card',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Stripe Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'environment',
                        'type', 'select',
                        'label', 'Environment',
                        'required', true,
                        'default', 'test',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'test', 'label', 'Test Mode'),
                            jsonb_build_object('value', 'live', 'label', 'Live Mode')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'secretKey',
                        'type', 'password',
                        'label', 'Secret Key',
                        'required', true,
                        'placeholder', 'sk_test_... or sk_live_...',
                        'help': 'Your Stripe secret API key'
                    ),
                    jsonb_build_object(
                        'name', 'webhookSecret',
                        'type', 'password',
                        'label', 'Webhook Signing Secret',
                        'placeholder', 'whsec_...',
                        'help': 'For validating webhook events'
                    ),
                    jsonb_build_object(
                        'name', 'apiVersion',
                        'type', 'text',
                        'label', 'API Version',
                        'default', '2023-10-16',
                        'help': 'Pin to specific API version (optional)'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', false,
        'streaming', false,
        'webhooks', true,
        'pagination', true,
        'filtering', true,
        'sorting', false,
        'customFields', true,
        'attachments', false,
        'subscriptions', true,
        'paymentIntents', true,
        'billing', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['APIKey'],
    'https://stripe.com/docs/api',
    'standard',
    'active',
    true
);

-- ServiceNow (ITSM)
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'servicenow',
    'ServiceNow',
    (SELECT id FROM adapter_categories WHERE code = 'itsm'),
    'ServiceNow',
    'Vancouver',
    'Connect to ServiceNow for IT service management',
    'settings',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'ServiceNow Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'instanceUrl',
                        'type', 'text',
                        'label', 'Instance URL',
                        'required', true,
                        'placeholder', 'https://your-instance.service-now.com',
                        'validation', jsonb_build_object(
                            'pattern', '^https://.*\\.service-now\\.com$',
                            'message', 'Must be a valid ServiceNow URL'
                        )
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'basic',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'basic', 'label', 'Basic Authentication'),
                            jsonb_build_object('value', 'oauth2', 'label', 'OAuth 2.0')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'username',
                        'type', 'text',
                        'label', 'Username',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'basic')
                    ),
                    jsonb_build_object(
                        'name', 'password',
                        'type', 'password',
                        'label', 'Password',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'basic')
                    ),
                    jsonb_build_object(
                        'name', 'clientId',
                        'type', 'text',
                        'label', 'Client ID',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'oauth2')
                    ),
                    jsonb_build_object(
                        'name', 'clientSecret',
                        'type', 'password',
                        'label', 'Client Secret',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'oauth2')
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
        'sorting', true,
        'customFields', true,
        'attachments', true,
        'cmdb', true,
        'workflows', true
    ),
    ARRAY['REST', 'SOAP'],
    ARRAY['JSON', 'XML'],
    ARRAY['Basic', 'OAuth2'],
    'https://developer.servicenow.com/',
    'enterprise',
    'active',
    true
);

-- Workday (HR)
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'workday',
    'Workday',
    (SELECT id FROM adapter_categories WHERE code = 'hr'),
    'Workday',
    '38.0',
    'Connect to Workday for HR and finance management',
    'users',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Workday Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'tenantUrl',
                        'type', 'text',
                        'label', 'Tenant URL',
                        'required', true,
                        'placeholder', 'https://wd2-impl-services1.workday.com',
                        'help': 'Your Workday tenant URL'
                    ),
                    jsonb_build_object(
                        'name', 'tenantId',
                        'type', 'text',
                        'label', 'Tenant ID',
                        'required', true,
                        'placeholder', 'your_tenant'
                    ),
                    jsonb_build_object(
                        'name', 'username',
                        'type', 'text',
                        'label', 'Integration System User',
                        'required', true,
                        'placeholder', 'ISU_Integration_User'
                    ),
                    jsonb_build_object(
                        'name', 'password',
                        'type', 'password',
                        'label', 'Password',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'apiVersion',
                        'type', 'text',
                        'label', 'API Version',
                        'default', 'v38.0'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', false,
        'webhooks', false,
        'pagination', true,
        'filtering', true,
        'sorting', true,
        'customFields', true,
        'attachments', true,
        'reports', true,
        'raas', true
    ),
    ARRAY['SOAP', 'REST'],
    ARRAY['XML', 'JSON'],
    ARRAY['Basic', 'OAuth2'],
    'https://community.workday.com/developers',
    'enterprise',
    'active',
    true
);

-- Google Sheets (Business Apps)
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'google-sheets',
    'Google Sheets',
    (SELECT id FROM adapter_categories WHERE code = 'google'),
    'Google',
    'v4',
    'Connect to Google Sheets for spreadsheet operations',
    'file-text',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Google Sheets Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'serviceAccount',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'serviceAccount', 'label', 'Service Account'),
                            jsonb_build_object('value', 'oauth2', 'label', 'OAuth 2.0')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'serviceAccountKey',
                        'type', 'textarea',
                        'label', 'Service Account Key (JSON)',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'serviceAccount'),
                        'placeholder': '{ "type": "service_account", ... }'
                    ),
                    jsonb_build_object(
                        'name', 'spreadsheetId',
                        'type', 'text',
                        'label', 'Default Spreadsheet ID',
                        'placeholder', '1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms',
                        'help': 'Can be overridden per operation'
                    ),
                    jsonb_build_object(
                        'name', 'shareWithServiceAccount',
                        'type', 'boolean',
                        'label', 'Auto-share with Service Account',
                        'default', true,
                        'help': 'Automatically share sheets with service account email'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', false,
        'webhooks', false,
        'pagination', true,
        'filtering', true,
        'sorting', false,
        'customFields', false,
        'attachments', false,
        'formulas', true,
        'formatting', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['ServiceAccount', 'OAuth2'],
    'https://developers.google.com/sheets/api',
    'free',
    'active',
    true
);

-- Tableau (Analytics)
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'tableau',
    'Tableau',
    (SELECT id FROM adapter_categories WHERE code = 'analytics'),
    'Salesforce',
    '2023.3',
    'Connect to Tableau for data visualization and analytics',
    'bar-chart',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Tableau Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'serverUrl',
                        'type', 'text',
                        'label', 'Server URL',
                        'required', true,
                        'placeholder', 'https://your-server.tableau.com',
                        'help': 'Tableau Server or Tableau Online URL'
                    ),
                    jsonb_build_object(
                        'name', 'siteName',
                        'type', 'text',
                        'label', 'Site Name',
                        'placeholder', 'Default',
                        'help': 'Leave empty for default site'
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'pat',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'pat', 'label', 'Personal Access Token'),
                            jsonb_build_object('value', 'userpass', 'label', 'Username/Password'),
                            jsonb_build_object('value', 'impersonate', 'label', 'Impersonation')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'tokenName',
                        'type', 'text',
                        'label', 'Token Name',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'pat')
                    ),
                    jsonb_build_object(
                        'name', 'tokenSecret',
                        'type', 'password',
                        'label', 'Token Secret',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'pat')
                    ),
                    jsonb_build_object(
                        'name', 'username',
                        'type', 'text',
                        'label', 'Username',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'operator', 'in', 'value', jsonb_build_array('userpass', 'impersonate'))
                    ),
                    jsonb_build_object(
                        'name', 'password',
                        'type', 'password',
                        'label', 'Password',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'userpass')
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
        'attachments', false,
        'extracts', true,
        'flows', true,
        'datasources', true
    ),
    ARRAY['REST'],
    ARRAY['JSON', 'XML'],
    ARRAY['PAT', 'Basic'],
    'https://help.tableau.com/current/api/rest_api/en-us/REST/rest_api.htm',
    'enterprise',
    'active',
    true
);

-- Snowflake (Data Warehouse)
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'snowflake',
    'Snowflake',
    (SELECT id FROM adapter_categories WHERE code = 'analytics'),
    'Snowflake',
    '2023',
    'Connect to Snowflake cloud data warehouse',
    'cloud',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Snowflake Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'account',
                        'type', 'text',
                        'label', 'Account Identifier',
                        'required', true,
                        'placeholder', 'xy12345.us-east-1',
                        'help': 'Your Snowflake account identifier'
                    ),
                    jsonb_build_object(
                        'name', 'warehouse',
                        'type', 'text',
                        'label', 'Warehouse',
                        'required', true,
                        'placeholder', 'COMPUTE_WH'
                    ),
                    jsonb_build_object(
                        'name', 'database',
                        'type', 'text',
                        'label', 'Database',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'schema',
                        'type', 'text',
                        'label', 'Schema',
                        'default', 'PUBLIC'
                    ),
                    jsonb_build_object(
                        'name', 'username',
                        'type', 'text',
                        'label', 'Username',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'password',
                        'type', 'password',
                        'label', 'Password',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'role',
                        'type', 'text',
                        'label', 'Role',
                        'placeholder': 'PUBLIC',
                        'help': 'Optional: Snowflake role to use'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', true,
        'webhooks', false,
        'pagination', true,
        'filtering', true,
        'sorting', true,
        'customFields', false,
        'attachments', false,
        'stages', true,
        'tasks', true,
        'pipes', true
    ),
    ARRAY['JDBC', 'REST'],
    ARRAY['SQL', 'JSON'],
    ARRAY['UsernamePassword', 'KeyPair'],
    'https://docs.snowflake.com/',
    'enterprise',
    'active',
    true
);

-- GitHub (DevOps)
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'github',
    'GitHub',
    (SELECT id FROM adapter_categories WHERE code = 'devops'),
    'GitHub',
    'v3',
    'Connect to GitHub for repository and project management',
    'github',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'GitHub Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'token',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'token', 'label', 'Personal Access Token'),
                            jsonb_build_object('value', 'app', 'label', 'GitHub App'),
                            jsonb_build_object('value', 'oauth', 'label', 'OAuth')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'token',
                        'type', 'password',
                        'label', 'Personal Access Token',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'token'),
                        'help': 'GitHub personal access token with required scopes'
                    ),
                    jsonb_build_object(
                        'name', 'appId',
                        'type', 'text',
                        'label', 'App ID',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'app')
                    ),
                    jsonb_build_object(
                        'name', 'privateKey',
                        'type', 'textarea',
                        'label', 'Private Key',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'app'),
                        'help': 'GitHub App private key in PEM format'
                    ),
                    jsonb_build_object(
                        'name', 'installationId',
                        'type', 'text',
                        'label', 'Installation ID',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'app')
                    ),
                    jsonb_build_object(
                        'name', 'enterprise',
                        'type', 'text',
                        'label', 'Enterprise URL',
                        'placeholder', 'https://github.enterprise.com',
                        'help': 'For GitHub Enterprise Server only'
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
        'attachments', true,
        'issues', true,
        'pullRequests', true,
        'actions', true
    ),
    ARRAY['REST', 'GraphQL'],
    ARRAY['JSON'],
    ARRAY['PAT', 'OAuth2', 'GitHubApp'],
    'https://docs.github.com/en/rest',
    'free',
    'active',
    true
);

-- DocuSign (Business Apps)
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'docusign',
    'DocuSign',
    (SELECT id FROM adapter_categories WHERE code = 'business_apps'),
    'DocuSign',
    'v2.1',
    'Connect to DocuSign for electronic signature workflows',
    'edit',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'DocuSign Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'environment',
                        'type', 'select',
                        'label', 'Environment',
                        'required', true,
                        'default', 'demo',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'demo', 'label', 'Demo (Sandbox)'),
                            jsonb_build_object('value', 'production', 'label', 'Production')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'integrationKey',
                        'type', 'text',
                        'label', 'Integration Key',
                        'required', true,
                        'help': 'Your DocuSign app integration key'
                    ),
                    jsonb_build_object(
                        'name', 'userId',
                        'type', 'text',
                        'label', 'User ID',
                        'required', true,
                        'help': 'Impersonated user ID (GUID)'
                    ),
                    jsonb_build_object(
                        'name', 'privateKey',
                        'type', 'textarea',
                        'label', 'RSA Private Key',
                        'required', true,
                        'help': 'RSA private key for JWT authentication'
                    ),
                    jsonb_build_object(
                        'name', 'accountId',
                        'type', 'text',
                        'label', 'Account ID',
                        'placeholder': 'Will be auto-discovered',
                        'help': 'DocuSign account ID (optional)'
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
        'powerForms', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['JWT', 'OAuth2'],
    'https://developers.docusign.com/',
    'standard',
    'active',
    true
);