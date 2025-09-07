-- ERP & Finance Adapters

-- SAP S/4HANA
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'sap-s4hana',
    'SAP S/4HANA',
    (SELECT id FROM adapter_categories WHERE code = 'sap'),
    'SAP',
    '2023',
    'Connect to SAP S/4HANA for enterprise resource planning',
    'building',
    true,
    true,
    false,
    -- Common configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'SAP Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'host',
                        'type', 'text',
                        'label', 'Application Server Host',
                        'required', true,
                        'placeholder', 'sap.company.com',
                        'help', 'SAP application server hostname or IP address'
                    ),
                    jsonb_build_object(
                        'name', 'systemNumber',
                        'type', 'text',
                        'label', 'System Number',
                        'required', true,
                        'placeholder', '00',
                        'validation', jsonb_build_object(
                            'pattern', '^[0-9]{2}$',
                            'message', 'Must be a 2-digit number'
                        )
                    ),
                    jsonb_build_object(
                        'name', 'client',
                        'type', 'text',
                        'label', 'Client',
                        'required', true,
                        'placeholder', '100',
                        'validation', jsonb_build_object(
                            'pattern', '^[0-9]{3}$',
                            'message', 'Must be a 3-digit number'
                        )
                    ),
                    jsonb_build_object(
                        'name', 'language',
                        'type', 'select',
                        'label', 'Language',
                        'default', 'EN',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'EN', 'label', 'English'),
                            jsonb_build_object('value', 'DE', 'label', 'German'),
                            jsonb_build_object('value', 'FR', 'label', 'French'),
                            jsonb_build_object('value', 'ES', 'label', 'Spanish')
                        )
                    )
                )
            ),
            jsonb_build_object(
                'id', 'authentication',
                'title', 'Authentication',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Method',
                        'required', true,
                        'default', 'basic',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'basic', 'label', 'Basic (User/Password)'),
                            jsonb_build_object('value', 'sso', 'label', 'Single Sign-On'),
                            jsonb_build_object('value', 'certificate', 'label', 'X.509 Certificate')
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
                    )
                )
            )
        )
    ),
    -- Inbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'interface',
                'title', 'Interface Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'interfaceType',
                        'type', 'select',
                        'label', 'Interface Type',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'odata', 'label', 'OData Service'),
                            jsonb_build_object('value', 'rfc', 'label', 'RFC/BAPI'),
                            jsonb_build_object('value', 'idoc', 'label', 'IDoc'),
                            jsonb_build_object('value', 'rest', 'label', 'REST API')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'servicePath',
                        'type', 'text',
                        'label', 'Service Path',
                        'placeholder', '/sap/opu/odata/sap/API_SALES_ORDER_SRV',
                        'condition', jsonb_build_object('field', 'interfaceType', 'value', 'odata'),
                        'help', 'OData service path'
                    ),
                    jsonb_build_object(
                        'name', 'functionModule',
                        'type', 'text',
                        'label', 'Function Module',
                        'placeholder', 'BAPI_SALESORDER_GETLIST',
                        'condition', jsonb_build_object('field', 'interfaceType', 'value', 'rfc'),
                        'help', 'RFC function module name'
                    ),
                    jsonb_build_object(
                        'name', 'idocType',
                        'type', 'text',
                        'label', 'IDoc Type',
                        'placeholder', 'ORDERS05',
                        'condition', jsonb_build_object('field', 'interfaceType', 'value', 'idoc'),
                        'help', 'IDoc message type'
                    )
                )
            )
        )
    ),
    -- Outbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'interface',
                'title', 'Interface Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'interfaceType',
                        'type', 'select',
                        'label', 'Interface Type',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'odata', 'label', 'OData Service'),
                            jsonb_build_object('value', 'rfc', 'label', 'RFC/BAPI'),
                            jsonb_build_object('value', 'idoc', 'label', 'IDoc')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'transactional',
                        'type', 'boolean',
                        'label', 'Use Transactional Processing',
                        'default', true,
                        'help', 'Wrap operations in SAP LUW (Logical Unit of Work)'
                    ),
                    jsonb_build_object(
                        'name', 'commitWork',
                        'type', 'boolean',
                        'label', 'Auto-commit',
                        'default', true,
                        'condition', jsonb_build_object('field', 'transactional', 'value', true)
                    )
                )
            )
        )
    ),
    -- Capabilities
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', false,
        'webhooks', false,
        'pagination', true,
        'filtering', true,
        'sorting', true,
        'customFields', true,
        'attachments', true,
        'transactions', true,
        'changePointers', true
    ),
    ARRAY['OData', 'RFC', 'IDoc', 'REST'],
    ARRAY['JSON', 'XML'],
    ARRAY['Basic', 'SSO', 'Certificate'],
    'https://api.sap.com/',
    'enterprise',
    'active',
    true
);

-- Oracle ERP Cloud
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'oracle-erp-cloud',
    'Oracle ERP Cloud',
    (SELECT id FROM adapter_categories WHERE code = 'oracle'),
    'Oracle',
    '23.1',
    'Connect to Oracle ERP Cloud for financials, procurement, and supply chain',
    'database',
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
                        'placeholder', 'https://your-instance.oraclecloud.com',
                        'validation', jsonb_build_object(
                            'pattern', '^https://.*\\.oraclecloud\\.com$',
                            'message', 'Must be a valid Oracle Cloud URL'
                        )
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
        'biReporting', true
    ),
    ARRAY['REST', 'SOAP'],
    ARRAY['JSON', 'XML'],
    ARRAY['Basic', 'OAuth2'],
    'https://docs.oracle.com/en/cloud/saas/applications-common/23a/farca/',
    'enterprise',
    'active',
    true
);

-- Microsoft Dynamics 365 Finance
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'dynamics365-finance',
    'Microsoft Dynamics 365 Finance',
    (SELECT id FROM adapter_categories WHERE code = 'microsoft'),
    'Microsoft',
    '10.0',
    'Connect to Dynamics 365 Finance for financial management',
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
                        'placeholder', 'https://your-instance.operations.dynamics.com'
                    ),
                    jsonb_build_object(
                        'name', 'tenantId',
                        'type', 'text',
                        'label', 'Azure AD Tenant ID',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'clientId',
                        'type', 'text',
                        'label', 'Application ID',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'clientSecret',
                        'type', 'password',
                        'label', 'Client Secret',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'resource',
                        'type', 'text',
                        'label', 'Resource',
                        'default', 'https://your-instance.operations.dynamics.com',
                        'help', 'Usually same as instance URL'
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
        'dataEntities', true,
        'batchProcessing', true
    ),
    ARRAY['OData', 'REST'],
    ARRAY['JSON'],
    ARRAY['OAuth2'],
    'https://docs.microsoft.com/en-us/dynamics365/fin-ops-core/dev-itpro/',
    'enterprise',
    'active',
    true
);

-- NetSuite
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'netsuite',
    'NetSuite',
    (SELECT id FROM adapter_categories WHERE code = 'erp'),
    'Oracle',
    '2023.2',
    'Connect to NetSuite for cloud-based ERP, CRM, and e-commerce',
    'cloud',
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
                        'name', 'accountId',
                        'type', 'text',
                        'label', 'Account ID',
                        'required', true,
                        'placeholder', '1234567',
                        'help', 'Your NetSuite account ID (without _SB or _RT suffix)'
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'token', 'label', 'Token-Based Authentication'),
                            jsonb_build_object('value', 'oauth2', 'label', 'OAuth 2.0')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'consumerKey',
                        'type', 'text',
                        'label', 'Consumer Key',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'token')
                    ),
                    jsonb_build_object(
                        'name', 'consumerSecret',
                        'type', 'password',
                        'label', 'Consumer Secret',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'token')
                    ),
                    jsonb_build_object(
                        'name', 'tokenId',
                        'type', 'text',
                        'label', 'Token ID',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'token')
                    ),
                    jsonb_build_object(
                        'name', 'tokenSecret',
                        'type', 'password',
                        'label', 'Token Secret',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'token')
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
        'savedSearches', true,
        'suiteScript', true
    ),
    ARRAY['REST', 'SOAP'],
    ARRAY['JSON', 'XML'],
    ARRAY['Token', 'OAuth2'],
    'https://docs.oracle.com/en/cloud/saas/netsuite/ns-online-help/',
    'standard',
    'active',
    true
);

-- QuickBooks Online
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'quickbooks-online',
    'QuickBooks Online',
    (SELECT id FROM adapter_categories WHERE code = 'erp'),
    'Intuit',
    '3.0',
    'Connect to QuickBooks Online for small business accounting',
    'credit-card',
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
                        'name', 'environment',
                        'type', 'select',
                        'label', 'Environment',
                        'required', true,
                        'default', 'production',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'sandbox', 'label', 'Sandbox'),
                            jsonb_build_object('value', 'production', 'label', 'Production')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'clientId',
                        'type', 'text',
                        'label', 'Client ID',
                        'required', true,
                        'help', 'OAuth 2.0 Client ID from your Intuit app'
                    ),
                    jsonb_build_object(
                        'name', 'clientSecret',
                        'type', 'password',
                        'label', 'Client Secret',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'realmId',
                        'type', 'text',
                        'label', 'Company ID (Realm ID)',
                        'required', true,
                        'help', 'Your QuickBooks Company ID'
                    ),
                    jsonb_build_object(
                        'name', 'minorVersion',
                        'type', 'number',
                        'label', 'API Minor Version',
                        'default', 65,
                        'help': 'QuickBooks API minor version to use'
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
        'changeDataCapture', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['OAuth2'],
    'https://developer.intuit.com/app/developer/qbo/docs/get-started',
    'standard',
    'active',
    true
);

-- Sage X3
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'sage-x3',
    'Sage X3',
    (SELECT id FROM adapter_categories WHERE code = 'erp'),
    'Sage',
    '12',
    'Connect to Sage X3 for enterprise management',
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
                        'name', 'serverUrl',
                        'type', 'text',
                        'label', 'Server URL',
                        'required', true,
                        'placeholder', 'https://x3server.company.com:8124'
                    ),
                    jsonb_build_object(
                        'name', 'folder',
                        'type', 'text',
                        'label', 'X3 Folder',
                        'required', true,
                        'placeholder', 'PROD',
                        'help', 'X3 folder/company code'
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
                        'name', 'language',
                        'type', 'select',
                        'label', 'Language',
                        'default', 'ENG',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'ENG', 'label', 'English'),
                            jsonb_build_object('value', 'FRA', 'label', 'French'),
                            jsonb_build_object('value', 'GER', 'label', 'German')
                        )
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
        'webServices', true
    ),
    ARRAY['SOAP', 'REST'],
    ARRAY['XML', 'JSON'],
    ARRAY['Basic'],
    'https://help.sageX3.com/erp/public/',
    'enterprise',
    'active',
    false
);