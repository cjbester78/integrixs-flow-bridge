-- E-Commerce & Marketplace Adapters

-- Shopify
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'shopify',
    'Shopify',
    (SELECT id FROM adapter_categories WHERE code = 'ecommerce'),
    'Shopify',
    '2023-10',
    'Connect to Shopify for e-commerce operations, products, and orders',
    'shopping-cart',
    true,
    true,
    false,
    -- Common configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'store',
                'title', 'Store Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'storeName',
                        'type', 'text',
                        'label', 'Store Name',
                        'required', true,
                        'placeholder', 'your-store',
                        'help', 'Your Shopify store name (without .myshopify.com)'
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'private_app',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'private_app', 'label', 'Private App'),
                            jsonb_build_object('value', 'custom_app', 'label', 'Custom App'),
                            jsonb_build_object('value', 'oauth', 'label', 'OAuth 2.0')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'accessToken',
                        'type', 'password',
                        'label', 'Access Token',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'operator', 'in', 'value', jsonb_build_array('private_app', 'custom_app')),
                        'help', 'Admin API access token'
                    ),
                    jsonb_build_object(
                        'name', 'apiVersion',
                        'type', 'select',
                        'label', 'API Version',
                        'default', '2023-10',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', '2023-10', 'label', '2023-10 (Latest)'),
                            jsonb_build_object('value', '2023-07', 'label', '2023-07'),
                            jsonb_build_object('value', '2023-04', 'label', '2023-04')
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
                'id', 'webhooks',
                'title', 'Webhook Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'webhookTopics',
                        'type', 'multiselect',
                        'label', 'Webhook Topics',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'orders/create', 'label', 'Order Created'),
                            jsonb_build_object('value', 'orders/updated', 'label', 'Order Updated'),
                            jsonb_build_object('value', 'orders/cancelled', 'label', 'Order Cancelled'),
                            jsonb_build_object('value', 'orders/fulfilled', 'label', 'Order Fulfilled'),
                            jsonb_build_object('value', 'products/create', 'label', 'Product Created'),
                            jsonb_build_object('value', 'products/update', 'label', 'Product Updated'),
                            jsonb_build_object('value', 'customers/create', 'label', 'Customer Created'),
                            jsonb_build_object('value', 'inventory_levels/update', 'label', 'Inventory Updated')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'webhookVerification',
                        'type', 'boolean',
                        'label', 'Verify Webhook Signatures',
                        'default', true,
                        'help', 'Validate webhook requests are from Shopify'
                    )
                )
            ),
            jsonb_build_object(
                'id', 'polling',
                'title', 'Polling Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'enablePolling',
                        'type', 'boolean',
                        'label', 'Enable Polling',
                        'default', false,
                        'help', 'Poll for data instead of using webhooks'
                    ),
                    jsonb_build_object(
                        'name', 'pollingInterval',
                        'type', 'number',
                        'label', 'Polling Interval (seconds)',
                        'default', 300,
                        'min', 60,
                        'max', 3600,
                        'condition', jsonb_build_object('field', 'enablePolling', 'value', true)
                    )
                )
            )
        )
    ),
    -- Outbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'operations',
                'title', 'Operation Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'defaultOperation',
                        'type', 'select',
                        'label', 'Default Operation',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'create', 'label', 'Create'),
                            jsonb_build_object('value', 'update', 'label', 'Update'),
                            jsonb_build_object('value', 'upsert', 'label', 'Upsert'),
                            jsonb_build_object('value', 'delete', 'label', 'Delete'),
                            jsonb_build_object('value', 'query', 'label', 'Query')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'resource',
                        'type', 'select',
                        'label', 'Resource Type',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'products', 'label', 'Products'),
                            jsonb_build_object('value', 'orders', 'label', 'Orders'),
                            jsonb_build_object('value', 'customers', 'label', 'Customers'),
                            jsonb_build_object('value', 'inventory', 'label', 'Inventory'),
                            jsonb_build_object('value', 'fulfillments', 'label', 'Fulfillments'),
                            jsonb_build_object('value', 'collections', 'label', 'Collections')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'bulkOperations',
                        'type', 'boolean',
                        'label', 'Enable Bulk Operations',
                        'default', true,
                        'help', 'Use bulk API for large data sets'
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
        'inventory', true,
        'multiCurrency', true,
        'graphQL', true
    ),
    ARRAY['REST', 'GraphQL'],
    ARRAY['JSON'],
    ARRAY['PrivateApp', 'OAuth2'],
    'https://shopify.dev/api',
    'standard',
    'active',
    true
);

-- Amazon Seller Central
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'amazon-seller',
    'Amazon Seller Central',
    (SELECT id FROM adapter_categories WHERE code = 'ecommerce'),
    'Amazon',
    '2023-11-01',
    'Connect to Amazon Seller Central for marketplace operations',
    'package',
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
                        'name', 'marketplace',
                        'type', 'select',
                        'label', 'Marketplace',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'ATVPDKIKX0DER', 'label', 'United States'),
                            jsonb_build_object('value', 'A2EUQ1WTGCTBG2', 'label', 'Canada'),
                            jsonb_build_object('value', 'A1AM78C64UM0Y8', 'label', 'Mexico'),
                            jsonb_build_object('value', 'A1F83G8C2ARO7P', 'label', 'United Kingdom'),
                            jsonb_build_object('value', 'A1PA6795UKMFR9', 'label', 'Germany'),
                            jsonb_build_object('value', 'A13V1IB3VIYZZH', 'label', 'France'),
                            jsonb_build_object('value', 'APJ6JRA9NG5V4', 'label', 'Italy'),
                            jsonb_build_object('value', 'A1RKKUPIHCS9HS', 'label', 'Spain'),
                            jsonb_build_object('value', 'A21TJRUUN4KGV', 'label', 'India'),
                            jsonb_build_object('value', 'A39IBJ37TRP1C6', 'label', 'Australia'),
                            jsonb_build_object('value', 'A1VC38T7YXB528', 'label', 'Japan')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'sellerId',
                        'type', 'text',
                        'label', 'Seller ID',
                        'required', true,
                        'help', 'Your Amazon Seller ID'
                    ),
                    jsonb_build_object(
                        'name', 'mwsAuthToken',
                        'type', 'password',
                        'label', 'MWS Auth Token',
                        'required', true,
                        'help', 'Authorization token for accessing SP-API'
                    ),
                    jsonb_build_object(
                        'name', 'region',
                        'type', 'select',
                        'label', 'Region',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'us-east-1', 'label', 'North America'),
                            jsonb_build_object('value', 'eu-west-1', 'label', 'Europe'),
                            jsonb_build_object('value', 'us-west-2', 'label', 'Far East')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'clientId',
                        'type', 'text',
                        'label', 'Client ID',
                        'required', true,
                        'help', 'LWA application client ID'
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
                        'required', true
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
        'customFields', false,
        'attachments', true,
        'fba', true,
        'reports', true
    ),
    ARRAY['REST'],
    ARRAY['JSON', 'XML'],
    ARRAY['OAuth2'],
    'https://developer-docs.amazon.com/sp-api/',
    'standard',
    'active',
    true
);

-- WooCommerce
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'woocommerce',
    'WooCommerce',
    (SELECT id FROM adapter_categories WHERE code = 'ecommerce'),
    'Automattic',
    '3.0',
    'Connect to WooCommerce for WordPress e-commerce integration',
    'shopping-cart',
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
                        'name', 'storeUrl',
                        'type', 'text',
                        'label', 'Store URL',
                        'required', true,
                        'placeholder', 'https://your-store.com',
                        'validation', jsonb_build_object(
                            'pattern', '^https?://.*',
                            'message', 'Must be a valid URL'
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
                            jsonb_build_object('value', 'oauth1', 'label', 'OAuth 1.0a'),
                            jsonb_build_object('value', 'jwt', 'label', 'JWT Authentication')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'consumerKey',
                        'type', 'text',
                        'label', 'Consumer Key',
                        'required', true,
                        'help', 'WooCommerce REST API consumer key'
                    ),
                    jsonb_build_object(
                        'name', 'consumerSecret',
                        'type', 'password',
                        'label', 'Consumer Secret',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'apiVersion',
                        'type', 'select',
                        'label', 'API Version',
                        'default', 'wc/v3',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'wc/v3', 'label', 'v3 (Latest)'),
                            jsonb_build_object('value', 'wc/v2', 'label', 'v2'),
                            jsonb_build_object('value', 'wc/v1', 'label', 'v1')
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
        'sorting', true,
        'customFields', true,
        'attachments', true,
        'variations', true,
        'coupons', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['Basic', 'OAuth1', 'JWT'],
    'https://woocommerce.github.io/woocommerce-rest-api-docs/',
    'free',
    'active',
    true
);

-- Magento
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'magento',
    'Adobe Commerce (Magento)',
    (SELECT id FROM adapter_categories WHERE code = 'ecommerce'),
    'Adobe',
    '2.4',
    'Connect to Magento/Adobe Commerce for enterprise e-commerce',
    'shopping-bag',
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
                        'name', 'baseUrl',
                        'type', 'text',
                        'label', 'Base URL',
                        'required', true,
                        'placeholder', 'https://your-store.com',
                        'help', 'Your Magento store base URL'
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'token',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'token', 'label', 'Integration Token'),
                            jsonb_build_object('value', 'oauth', 'label', 'OAuth 1.0a'),
                            jsonb_build_object('value', 'admin', 'label', 'Admin Token')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'accessToken',
                        'type', 'password',
                        'label', 'Access Token',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'token'),
                        'help', 'Integration access token from Magento admin'
                    ),
                    jsonb_build_object(
                        'name', 'storeCode',
                        'type', 'text',
                        'label', 'Store Code',
                        'default', 'default',
                        'help', 'Store view code (leave as default for single store)'
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
        'multiStore', true,
        'b2b', true
    ),
    ARRAY['REST', 'GraphQL'],
    ARRAY['JSON', 'XML'],
    ARRAY['Token', 'OAuth1', 'AdminToken'],
    'https://developer.adobe.com/commerce/webapi/get-started/',
    'standard',
    'active',
    true
);

-- eBay
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'ebay',
    'eBay',
    (SELECT id FROM adapter_categories WHERE code = 'ecommerce'),
    'eBay',
    'v1',
    'Connect to eBay for marketplace selling and buying',
    'tag',
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
                        'name', 'marketplaceId',
                        'type', 'select',
                        'label', 'Marketplace',
                        'required', true,
                        'default', 'EBAY_US',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'EBAY_US', 'label', 'United States'),
                            jsonb_build_object('value', 'EBAY_UK', 'label', 'United Kingdom'),
                            jsonb_build_object('value', 'EBAY_DE', 'label', 'Germany'),
                            jsonb_build_object('value', 'EBAY_AU', 'label', 'Australia'),
                            jsonb_build_object('value', 'EBAY_CA', 'label', 'Canada')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'appId',
                        'type', 'text',
                        'label', 'App ID (Client ID)',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'certId',
                        'type', 'password',
                        'label', 'Cert ID (Client Secret)',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'devId',
                        'type', 'text',
                        'label', 'Dev ID',
                        'required', false,
                        'help', 'Required for Trading API only'
                    ),
                    jsonb_build_object(
                        'name', 'authToken',
                        'type', 'password',
                        'label', 'User Auth Token',
                        'required', true,
                        'help': 'OAuth token for user account access'
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
        'customFields', false,
        'attachments', true,
        'auctions', true,
        'fulfillment', true
    ),
    ARRAY['REST', 'SOAP'],
    ARRAY['JSON', 'XML'],
    ARRAY['OAuth2'],
    'https://developer.ebay.com/docs',
    'standard',
    'active',
    true
);

-- BigCommerce
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'bigcommerce',
    'BigCommerce',
    (SELECT id FROM adapter_categories WHERE code = 'ecommerce'),
    'BigCommerce',
    'v3',
    'Connect to BigCommerce for SaaS e-commerce platform',
    'shopping-cart',
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
                        'name', 'storeHash',
                        'type', 'text',
                        'label', 'Store Hash',
                        'required', true,
                        'placeholder', 'store_hash',
                        'help', 'Your unique store hash from BigCommerce'
                    ),
                    jsonb_build_object(
                        'name', 'clientId',
                        'type', 'text',
                        'label', 'Client ID',
                        'required', true,
                        'help', 'OAuth client ID from your app'
                    ),
                    jsonb_build_object(
                        'name', 'accessToken',
                        'type', 'password',
                        'label', 'Access Token',
                        'required', true,
                        'help', 'OAuth access token'
                    ),
                    jsonb_build_object(
                        'name', 'apiVersion',
                        'type', 'select',
                        'label', 'API Version',
                        'default', 'v3',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'v3', 'label', 'v3 (Current)'),
                            jsonb_build_object('value', 'v2', 'label', 'v2 (Legacy)')
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
        'sorting', true,
        'customFields', true,
        'attachments', true,
        'themes', true,
        'scripts', true
    ),
    ARRAY['REST', 'GraphQL'],
    ARRAY['JSON'],
    ARRAY['OAuth2'],
    'https://developer.bigcommerce.com/',
    'standard',
    'active',
    true
);

-- Etsy
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'etsy',
    'Etsy',
    (SELECT id FROM adapter_categories WHERE code = 'ecommerce'),
    'Etsy',
    'v3',
    'Connect to Etsy marketplace for handmade and vintage items',
    'gift',
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
                        'name', 'apiKey',
                        'type', 'text',
                        'label', 'API Key',
                        'required', true,
                        'help', 'Your Etsy app API key'
                    ),
                    jsonb_build_object(
                        'name', 'apiSecret',
                        'type', 'password',
                        'label', 'API Secret',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'shopId',
                        'type', 'text',
                        'label', 'Shop ID',
                        'required', false,
                        'help', 'Your Etsy shop ID (optional)'
                    ),
                    jsonb_build_object(
                        'name', 'accessToken',
                        'type', 'password',
                        'label', 'OAuth Access Token',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'refreshToken',
                        'type', 'password',
                        'label', 'OAuth Refresh Token',
                        'required', true
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', false,
        'streaming', false,
        'webhooks', false,
        'pagination', true,
        'filtering', true,
        'sorting', true,
        'customFields', false,
        'attachments', true,
        'variations', true,
        'shipping', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['OAuth2'],
    'https://developers.etsy.com/documentation/',
    'standard',
    'active',
    false
);

-- Square
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'square',
    'Square',
    (SELECT id FROM adapter_categories WHERE code = 'ecommerce'),
    'Square',
    '2023-10-18',
    'Connect to Square for payment processing and POS integration',
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
                        'name', 'accessToken',
                        'type', 'password',
                        'label', 'Access Token',
                        'required', true,
                        'help', 'Your Square access token'
                    ),
                    jsonb_build_object(
                        'name', 'locationId',
                        'type', 'text',
                        'label', 'Default Location ID',
                        'required', false,
                        'help', 'Default location for transactions'
                    ),
                    jsonb_build_object(
                        'name', 'applicationId',
                        'type', 'text',
                        'label', 'Application ID',
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
        'sorting', false,
        'customFields', true,
        'attachments', false,
        'payments', true,
        'pos', true,
        'loyalty', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['OAuth2', 'PersonalAccessToken'],
    'https://developer.squareup.com/docs',
    'standard',
    'active',
    true
);