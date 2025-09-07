-- CRM & Sales Adapters
-- Common schema templates for authentication and connection

-- Helper function to create JSON schema
CREATE OR REPLACE FUNCTION create_field_schema(
    p_name TEXT,
    p_type TEXT,
    p_label TEXT,
    p_required BOOLEAN DEFAULT false,
    p_placeholder TEXT DEFAULT NULL,
    p_help TEXT DEFAULT NULL,
    p_default TEXT DEFAULT NULL
) RETURNS JSONB AS $$
BEGIN
    RETURN jsonb_build_object(
        'name', p_name,
        'type', p_type,
        'label', p_label,
        'required', p_required,
        'placeholder', p_placeholder,
        'help', p_help,
        'default', p_default
    ) - 'placeholder' - 'help' - 'default' 
    WHERE p_placeholder IS NULL OR p_help IS NULL OR p_default IS NULL;
END;
$$ LANGUAGE plpgsql;

-- Salesforce
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'salesforce',
    'Salesforce',
    (SELECT id FROM adapter_categories WHERE code = 'crm'),
    'Salesforce',
    '59.0',
    'Connect to Salesforce CRM for customer data, opportunities, and more',
    'cloud',
    true,
    true,
    false,
    -- Common configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Connection Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'instanceUrl',
                        'type', 'text',
                        'label', 'Instance URL',
                        'required', true,
                        'placeholder', 'https://mycompany.salesforce.com',
                        'help', 'Your Salesforce instance URL',
                        'validation', jsonb_build_object(
                            'pattern', '^https://.*\\.salesforce\\.com$',
                            'message', 'Must be a valid Salesforce URL'
                        )
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'oauth2',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'oauth2', 'label', 'OAuth 2.0 (Recommended)'),
                            jsonb_build_object('value', 'username_password', 'label', 'Username/Password'),
                            jsonb_build_object('value', 'jwt', 'label', 'JWT Bearer Token')
                        )
                    )
                )
            ),
            jsonb_build_object(
                'id', 'auth',
                'title', 'Authentication',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'oauth2Config',
                        'type', 'group',
                        'label', 'OAuth 2.0 Configuration',
                        'condition', jsonb_build_object('field', 'authType', 'value', 'oauth2'),
                        'fields', jsonb_build_array(
                            jsonb_build_object(
                                'name', 'clientId',
                                'type', 'text',
                                'label', 'Client ID',
                                'required', true,
                                'help', 'OAuth 2.0 Client ID from your Connected App'
                            ),
                            jsonb_build_object(
                                'name', 'clientSecret',
                                'type', 'password',
                                'label', 'Client Secret',
                                'required', true,
                                'help', 'OAuth 2.0 Client Secret from your Connected App'
                            ),
                            jsonb_build_object(
                                'name', 'scope',
                                'type', 'text',
                                'label', 'OAuth Scopes',
                                'default', 'api refresh_token',
                                'help', 'Space-separated list of OAuth scopes'
                            )
                        )
                    ),
                    jsonb_build_object(
                        'name', 'passwordConfig',
                        'type', 'group',
                        'label', 'Password Authentication',
                        'condition', jsonb_build_object('field', 'authType', 'value', 'username_password'),
                        'fields', jsonb_build_array(
                            jsonb_build_object(
                                'name', 'username',
                                'type', 'text',
                                'label', 'Username',
                                'required', true,
                                'placeholder', 'user@example.com'
                            ),
                            jsonb_build_object(
                                'name', 'password',
                                'type', 'password',
                                'label', 'Password',
                                'required', true
                            ),
                            jsonb_build_object(
                                'name', 'securityToken',
                                'type', 'password',
                                'label', 'Security Token',
                                'required', true,
                                'help', 'Your Salesforce security token (reset from Salesforce settings)'
                            )
                        )
                    )
                )
            )
        )
    ),
    -- Inbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'polling',
                'title', 'Data Retrieval',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'mode',
                        'type', 'select',
                        'label', 'Retrieval Mode',
                        'required', true,
                        'default', 'polling',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'polling', 'label', 'Polling (Query at intervals)'),
                            jsonb_build_object('value', 'streaming', 'label', 'Streaming API (Real-time)'),
                            jsonb_build_object('value', 'platform_events', 'label', 'Platform Events')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'pollingInterval',
                        'type', 'number',
                        'label', 'Polling Interval (seconds)',
                        'default', 300,
                        'min', 60,
                        'max', 3600,
                        'condition', jsonb_build_object('field', 'mode', 'value', 'polling'),
                        'help', 'How often to check for new records (60-3600 seconds)'
                    ),
                    jsonb_build_object(
                        'name', 'objects',
                        'type', 'multiselect',
                        'label', 'Salesforce Objects',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'Account', 'label', 'Account'),
                            jsonb_build_object('value', 'Contact', 'label', 'Contact'),
                            jsonb_build_object('value', 'Lead', 'label', 'Lead'),
                            jsonb_build_object('value', 'Opportunity', 'label', 'Opportunity'),
                            jsonb_build_object('value', 'Case', 'label', 'Case'),
                            jsonb_build_object('value', 'Custom', 'label', 'Custom Objects')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'query',
                        'type', 'textarea',
                        'label', 'SOQL Query',
                        'placeholder', 'SELECT Id, Name FROM Account WHERE CreatedDate > YESTERDAY',
                        'help', 'Salesforce Object Query Language (SOQL) for data retrieval',
                        'condition', jsonb_build_object('field', 'mode', 'value', 'polling')
                    )
                )
            )
        )
    ),
    -- Outbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'operation',
                'title', 'Operation Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'operation',
                        'type', 'select',
                        'label', 'Operation Type',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'create', 'label', 'Create Records'),
                            jsonb_build_object('value', 'update', 'label', 'Update Records'),
                            jsonb_build_object('value', 'upsert', 'label', 'Upsert Records'),
                            jsonb_build_object('value', 'delete', 'label', 'Delete Records'),
                            jsonb_build_object('value', 'query', 'label', 'Query Records')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'object',
                        'type', 'text',
                        'label', 'Target Object',
                        'required', true,
                        'placeholder', 'Account',
                        'help', 'The Salesforce object to perform operations on'
                    ),
                    jsonb_build_object(
                        'name', 'externalIdField',
                        'type', 'text',
                        'label', 'External ID Field',
                        'placeholder', 'External_ID__c',
                        'condition', jsonb_build_object('field', 'operation', 'value', 'upsert'),
                        'help', 'Field to use for upsert operations'
                    ),
                    jsonb_build_object(
                        'name', 'batchSize',
                        'type', 'number',
                        'label', 'Batch Size',
                        'default', 200,
                        'min', 1,
                        'max', 2000,
                        'help', 'Number of records to process in each batch'
                    )
                )
            )
        )
    ),
    -- Capabilities
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', true,
        'webhooks', false,
        'pagination', true,
        'filtering', true,
        'sorting', true,
        'customFields', true,
        'attachments', true,
        'metadata', true
    ),
    -- Supported protocols
    ARRAY['REST', 'SOAP'],
    -- Supported formats
    ARRAY['JSON', 'XML'],
    -- Authentication methods
    ARRAY['OAuth2', 'UsernamePassword', 'JWT'],
    -- Documentation URL
    'https://developer.salesforce.com/docs/apis',
    -- Pricing tier
    'standard',
    -- Status
    'active',
    -- Is certified
    true
);

-- HubSpot
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'hubspot',
    'HubSpot',
    (SELECT id FROM adapter_categories WHERE code = 'crm'),
    'HubSpot',
    '3.0',
    'Connect to HubSpot CRM for contacts, companies, deals, and marketing data',
    'users',
    true,
    true,
    false,
    -- Common configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Connection Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'oauth2',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'oauth2', 'label', 'OAuth 2.0 (Recommended)'),
                            jsonb_build_object('value', 'apikey', 'label', 'API Key'),
                            jsonb_build_object('value', 'private_app', 'label', 'Private App Token')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'apiKey',
                        'type', 'password',
                        'label', 'API Key',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'apikey'),
                        'help', 'Your HubSpot API key (deprecated, use OAuth 2.0 instead)'
                    ),
                    jsonb_build_object(
                        'name', 'privateAppToken',
                        'type', 'password',
                        'label', 'Private App Access Token',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'private_app'),
                        'help', 'Access token from your HubSpot private app'
                    )
                )
            )
        )
    ),
    -- Inbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'data',
                'title', 'Data Selection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'objects',
                        'type', 'multiselect',
                        'label', 'HubSpot Objects',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'contacts', 'label', 'Contacts'),
                            jsonb_build_object('value', 'companies', 'label', 'Companies'),
                            jsonb_build_object('value', 'deals', 'label', 'Deals'),
                            jsonb_build_object('value', 'tickets', 'label', 'Tickets'),
                            jsonb_build_object('value', 'products', 'label', 'Products'),
                            jsonb_build_object('value', 'line_items', 'label', 'Line Items')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'properties',
                        'type', 'text',
                        'label', 'Properties to Retrieve',
                        'placeholder', 'firstname,lastname,email,company',
                        'help', 'Comma-separated list of properties (leave empty for all)'
                    ),
                    jsonb_build_object(
                        'name', 'associations',
                        'type', 'multiselect',
                        'label', 'Include Associations',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'contacts', 'label', 'Contacts'),
                            jsonb_build_object('value', 'companies', 'label', 'Companies'),
                            jsonb_build_object('value', 'deals', 'label', 'Deals'),
                            jsonb_build_object('value', 'tickets', 'label', 'Tickets')
                        )
                    )
                )
            )
        )
    ),
    -- Outbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'operation',
                'title', 'Operation Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'object',
                        'type', 'select',
                        'label', 'Target Object',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'contacts', 'label', 'Contacts'),
                            jsonb_build_object('value', 'companies', 'label', 'Companies'),
                            jsonb_build_object('value', 'deals', 'label', 'Deals'),
                            jsonb_build_object('value', 'tickets', 'label', 'Tickets')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'operation',
                        'type', 'select',
                        'label', 'Operation',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'create', 'label', 'Create'),
                            jsonb_build_object('value', 'update', 'label', 'Update'),
                            jsonb_build_object('value', 'delete', 'label', 'Delete'),
                            jsonb_build_object('value', 'search', 'label', 'Search')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'idProperty',
                        'type', 'text',
                        'label', 'ID Property',
                        'default', 'email',
                        'condition', jsonb_build_object('field', 'operation', 'operator', 'in', 'value', jsonb_build_array('update', 'delete')),
                        'help', 'Property to use for identifying records (e.g., email, hs_object_id)'
                    )
                )
            )
        )
    ),
    -- Capabilities
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', false,
        'webhooks', true,
        'pagination', true,
        'filtering', true,
        'sorting', true,
        'customFields', true,
        'attachments', true
    ),
    -- Supported protocols
    ARRAY['REST'],
    -- Supported formats
    ARRAY['JSON'],
    -- Authentication methods
    ARRAY['OAuth2', 'APIKey', 'PrivateApp'],
    -- Documentation URL
    'https://developers.hubspot.com/docs/api/overview',
    -- Pricing tier
    'free',
    -- Status
    'active',
    -- Is certified
    true
);

-- Microsoft Dynamics 365 CRM
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats, 
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'dynamics365-crm',
    'Microsoft Dynamics 365 CRM',
    (SELECT id FROM adapter_categories WHERE code = 'crm'),
    'Microsoft',
    '9.2',
    'Connect to Microsoft Dynamics 365 CRM for customer engagement',
    'building',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Connection Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'instanceUrl',
                        'type', 'text',
                        'label', 'Instance URL',
                        'required', true,
                        'placeholder', 'https://yourorg.crm.dynamics.com',
                        'validation', jsonb_build_object(
                            'pattern', '^https://.*\\.dynamics\\.com$',
                            'message', 'Must be a valid Dynamics 365 URL'
                        )
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'oauth2',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'oauth2', 'label', 'OAuth 2.0'),
                            jsonb_build_object('value', 'client_credentials', 'label', 'Client Credentials')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'tenantId',
                        'type', 'text',
                        'label', 'Tenant ID',
                        'required', true,
                        'help', 'Your Azure AD tenant ID'
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
        'changeTracking', true
    ),
    ARRAY['REST', 'OData'],
    ARRAY['JSON', 'XML'],
    ARRAY['OAuth2', 'ClientCredentials'],
    'https://docs.microsoft.com/en-us/dynamics365/customerengagement/on-premises/developer/overview',
    'standard',
    'active',
    true
);

-- Zoho CRM
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'zoho-crm',
    'Zoho CRM',
    (SELECT id FROM adapter_categories WHERE code = 'crm'),
    'Zoho',
    '5.0',
    'Connect to Zoho CRM for sales automation and customer management',
    'users',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Connection Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'dataCenter',
                        'type', 'select',
                        'label', 'Data Center',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'com', 'label', 'United States (.com)'),
                            jsonb_build_object('value', 'eu', 'label', 'Europe (.eu)'),
                            jsonb_build_object('value', 'in', 'label', 'India (.in)'),
                            jsonb_build_object('value', 'com.cn', 'label', 'China (.com.cn)'),
                            jsonb_build_object('value', 'com.au', 'label', 'Australia (.com.au)')
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
                        'name', 'refreshToken',
                        'type', 'password',
                        'label', 'Refresh Token',
                        'required', true,
                        'help', 'Long-lived refresh token for accessing Zoho APIs'
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
        'attachments', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['OAuth2'],
    'https://www.zoho.com/crm/developer/docs/api/v2/',
    'free',
    'active',
    true
);

-- Pipedrive
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'pipedrive',
    'Pipedrive',
    (SELECT id FROM adapter_categories WHERE code = 'crm'),
    'Pipedrive',
    '1.0',
    'Connect to Pipedrive CRM for sales pipeline management',
    'activity',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Connection Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'domain',
                        'type', 'text',
                        'label', 'Company Domain',
                        'required', true,
                        'placeholder', 'yourcompany',
                        'help', 'Your Pipedrive subdomain (without .pipedrive.com)'
                    ),
                    jsonb_build_object(
                        'name', 'apiToken',
                        'type', 'password',
                        'label', 'API Token',
                        'required', true,
                        'help', 'Your personal API token from Pipedrive settings'
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
        'attachments', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['APIKey'],
    'https://developers.pipedrive.com/docs/api/v1',
    'standard',
    'active',
    true
);

-- Clean up helper function
DROP FUNCTION IF EXISTS create_field_schema;