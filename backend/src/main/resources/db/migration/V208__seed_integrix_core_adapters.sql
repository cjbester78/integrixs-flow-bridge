-- Integrix Flow Bridge Core Adapters
-- These are the pre-built adapters that come with the system

-- HTTP/HTTPS Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-http',
    'HTTP/HTTPS',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'HTTP/HTTPS adapter for RESTful API integration and webhooks',
    'globe',
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
                        'name', 'timeout',
                        'type', 'number',
                        'label', 'Connection Timeout (ms)',
                        'default', 30000,
                        'min', 1000,
                        'max', 300000
                    ),
                    jsonb_build_object(
                        'name', 'retryAttempts',
                        'type', 'number',
                        'label', 'Retry Attempts',
                        'default', 3,
                        'min', 0,
                        'max', 10
                    ),
                    jsonb_build_object(
                        'name', 'proxyEnabled',
                        'type', 'boolean',
                        'label', 'Use Proxy',
                        'default', false
                    ),
                    jsonb_build_object(
                        'name', 'proxyHost',
                        'type', 'text',
                        'label', 'Proxy Host',
                        'condition', jsonb_build_object('field', 'proxyEnabled', 'value', true)
                    ),
                    jsonb_build_object(
                        'name', 'proxyPort',
                        'type', 'number',
                        'label', 'Proxy Port',
                        'condition', jsonb_build_object('field', 'proxyEnabled', 'value', true)
                    )
                )
            )
        )
    ),
    -- Inbound configuration (receiving webhooks)
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'endpoint',
                'title', 'Endpoint Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'path',
                        'type', 'text',
                        'label', 'Endpoint Path',
                        'required', true,
                        'placeholder', '/webhook/receive',
                        'help', 'The path where webhooks will be received'
                    ),
                    jsonb_build_object(
                        'name', 'httpMethod',
                        'type', 'multiselect',
                        'label', 'Allowed HTTP Methods',
                        'default', jsonb_build_array('POST'),
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'GET', 'label', 'GET'),
                            jsonb_build_object('value', 'POST', 'label', 'POST'),
                            jsonb_build_object('value', 'PUT', 'label', 'PUT'),
                            jsonb_build_object('value', 'PATCH', 'label', 'PATCH'),
                            jsonb_build_object('value', 'DELETE', 'label', 'DELETE')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'authRequired',
                        'type', 'boolean',
                        'label', 'Require Authentication',
                        'default', true
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'condition', jsonb_build_object('field', 'authRequired', 'value', true),
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'basic', 'label', 'Basic Auth'),
                            jsonb_build_object('value', 'bearer', 'label', 'Bearer Token'),
                            jsonb_build_object('value', 'apikey', 'label', 'API Key'),
                            jsonb_build_object('value', 'hmac', 'label', 'HMAC Signature')
                        )
                    )
                )
            )
        )
    ),
    -- Outbound configuration (sending HTTP requests)
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'request',
                'title', 'Request Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'endpointUrl',
                        'type', 'text',
                        'label', 'Endpoint URL',
                        'required', true,
                        'placeholder', 'https://api.example.com/endpoint'
                    ),
                    jsonb_build_object(
                        'name', 'httpMethod',
                        'type', 'select',
                        'label', 'HTTP Method',
                        'required', true,
                        'default', 'POST',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'GET', 'label', 'GET'),
                            jsonb_build_object('value', 'POST', 'label', 'POST'),
                            jsonb_build_object('value', 'PUT', 'label', 'PUT'),
                            jsonb_build_object('value', 'PATCH', 'label', 'PATCH'),
                            jsonb_build_object('value', 'DELETE', 'label', 'DELETE')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'headers',
                        'type', 'keyvalue',
                        'label', 'Request Headers',
                        'help': 'Additional headers to send with the request'
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication',
                        'required', true,
                        'default', 'none',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'none', 'label', 'No Authentication'),
                            jsonb_build_object('value', 'basic', 'label', 'Basic Auth'),
                            jsonb_build_object('value', 'bearer', 'label', 'Bearer Token'),
                            jsonb_build_object('value', 'apikey', 'label', 'API Key'),
                            jsonb_build_object('value', 'oauth2', 'label', 'OAuth 2.0')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'contentType',
                        'type', 'select',
                        'label', 'Content Type',
                        'default', 'application/json',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'application/json', 'label', 'JSON'),
                            jsonb_build_object('value', 'application/xml', 'label', 'XML'),
                            jsonb_build_object('value', 'application/x-www-form-urlencoded', 'label', 'Form URL Encoded'),
                            jsonb_build_object('value', 'multipart/form-data', 'label', 'Multipart Form'),
                            jsonb_build_object('value', 'text/plain', 'label', 'Plain Text')
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
        'filtering', false,
        'sorting', false,
        'customFields', true,
        'attachments', true,
        'ssl', true,
        'compression', true
    ),
    ARRAY['HTTP', 'HTTPS'],
    ARRAY['JSON', 'XML', 'TEXT', 'BINARY'],
    ARRAY['None', 'Basic', 'Bearer', 'APIKey', 'OAuth2'],
    '/docs/adapters/http',
    'free',
    'active',
    true
);

-- FILE Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-file',
    'File System',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'File system adapter for reading and writing local or network files',
    'file',
    true,
    true,
    false,
    -- Common configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'general',
                'title', 'General Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'encoding',
                        'type', 'select',
                        'label', 'File Encoding',
                        'default', 'UTF-8',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'UTF-8', 'label', 'UTF-8'),
                            jsonb_build_object('value', 'UTF-16', 'label', 'UTF-16'),
                            jsonb_build_object('value', 'ISO-8859-1', 'label', 'ISO-8859-1'),
                            jsonb_build_object('value', 'US-ASCII', 'label', 'US-ASCII')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'lineEnding',
                        'type', 'select',
                        'label', 'Line Ending',
                        'default', 'AUTO',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'AUTO', 'label', 'Auto-detect'),
                            jsonb_build_object('value', 'LF', 'label', 'Unix (LF)'),
                            jsonb_build_object('value', 'CRLF', 'label', 'Windows (CRLF)')
                        )
                    )
                )
            )
        )
    ),
    -- Inbound configuration (reading files)
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'source',
                'title', 'Source Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'directoryPath',
                        'type', 'text',
                        'label', 'Directory Path',
                        'required', true,
                        'placeholder', '/path/to/input/directory'
                    ),
                    jsonb_build_object(
                        'name', 'filePattern',
                        'type', 'text',
                        'label', 'File Pattern',
                        'default', '*',
                        'placeholder', '*.csv',
                        'help': 'Use wildcards: * for any characters, ? for single character'
                    ),
                    jsonb_build_object(
                        'name', 'recursive',
                        'type', 'boolean',
                        'label', 'Include Subdirectories',
                        'default', false
                    ),
                    jsonb_build_object(
                        'name', 'pollingInterval',
                        'type', 'number',
                        'label', 'Polling Interval (seconds)',
                        'default', 60,
                        'min', 1
                    ),
                    jsonb_build_object(
                        'name', 'processedFileAction',
                        'type', 'select',
                        'label', 'After Processing',
                        'required', true,
                        'default', 'move',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'keep', 'label', 'Keep in place'),
                            jsonb_build_object('value', 'move', 'label', 'Move to archive'),
                            jsonb_build_object('value', 'delete', 'label', 'Delete'),
                            jsonb_build_object('value', 'rename', 'label', 'Rename')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'archivePath',
                        'type', 'text',
                        'label', 'Archive Directory',
                        'condition', jsonb_build_object('field', 'processedFileAction', 'value', 'move'),
                        'placeholder': '/path/to/archive'
                    )
                )
            )
        )
    ),
    -- Outbound configuration (writing files)
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'target',
                'title', 'Target Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'directoryPath',
                        'type', 'text',
                        'label', 'Output Directory',
                        'required', true,
                        'placeholder': '/path/to/output/directory'
                    ),
                    jsonb_build_object(
                        'name', 'fileNamePattern',
                        'type', 'text',
                        'label', 'File Name Pattern',
                        'required', true,
                        'default', '${filename}_${timestamp}.${extension}',
                        'help': 'Variables: ${filename}, ${timestamp}, ${date}, ${uuid}'
                    ),
                    jsonb_build_object(
                        'name', 'createDirectory',
                        'type', 'boolean',
                        'label', 'Create Directory if Missing',
                        'default', true
                    ),
                    jsonb_build_object(
                        'name', 'overwriteExisting',
                        'type', 'boolean',
                        'label', 'Overwrite Existing Files',
                        'default', false
                    ),
                    jsonb_build_object(
                        'name', 'tempFilePrefix',
                        'type', 'text',
                        'label', 'Temporary File Prefix',
                        'default', '.tmp_',
                        'help': 'Prefix for temporary files during write'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', true,
        'webhooks', false,
        'pagination', false,
        'filtering', true,
        'sorting', false,
        'customFields', false,
        'attachments', true,
        'compression', true,
        'encryption', false
    ),
    ARRAY['FILE'],
    ARRAY['TEXT', 'CSV', 'JSON', 'XML', 'BINARY'],
    ARRAY['FileSystem'],
    '/docs/adapters/file',
    'free',
    'active',
    true
);

-- FTP Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-ftp',
    'FTP',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'FTP adapter for file transfer protocol operations',
    'upload',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'FTP Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'host',
                        'type', 'text',
                        'label', 'FTP Server Host',
                        'required', true,
                        'placeholder', 'ftp.example.com'
                    ),
                    jsonb_build_object(
                        'name', 'port',
                        'type', 'number',
                        'label', 'Port',
                        'default', 21,
                        'min', 1,
                        'max', 65535
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
                        'name', 'passiveMode',
                        'type', 'boolean',
                        'label', 'Use Passive Mode',
                        'default', true
                    ),
                    jsonb_build_object(
                        'name', 'binaryTransfer',
                        'type', 'boolean',
                        'label', 'Binary Transfer Mode',
                        'default', true
                    ),
                    jsonb_build_object(
                        'name', 'connectionTimeout',
                        'type', 'number',
                        'label', 'Connection Timeout (ms)',
                        'default', 30000
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', true,
        'webhooks', false,
        'pagination', false,
        'filtering', true,
        'sorting', false,
        'customFields', false,
        'attachments', true
    ),
    ARRAY['FTP'],
    ARRAY['BINARY', 'TEXT'],
    ARRAY['UsernamePassword', 'Anonymous'],
    '/docs/adapters/ftp',
    'free',
    'active',
    true
);

-- SFTP Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-sftp',
    'SFTP',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'SFTP adapter for secure file transfer protocol operations',
    'lock',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'SFTP Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'host',
                        'type', 'text',
                        'label', 'SFTP Server Host',
                        'required', true,
                        'placeholder', 'sftp.example.com'
                    ),
                    jsonb_build_object(
                        'name', 'port',
                        'type', 'number',
                        'label', 'Port',
                        'default', 22,
                        'min', 1,
                        'max', 65535
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'password',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'password', 'label', 'Password'),
                            jsonb_build_object('value', 'key', 'label', 'SSH Key'),
                            jsonb_build_object('value', 'both', 'label', 'Password + Key')
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
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'operator', 'in', 'value', jsonb_build_array('password', 'both'))
                    ),
                    jsonb_build_object(
                        'name', 'privateKey',
                        'type', 'textarea',
                        'label', 'Private Key',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'operator', 'in', 'value', jsonb_build_array('key', 'both')),
                        'placeholder': '-----BEGIN RSA PRIVATE KEY-----\n...'
                    ),
                    jsonb_build_object(
                        'name', 'passphrase',
                        'type', 'password',
                        'label', 'Key Passphrase',
                        'condition', jsonb_build_object('field', 'authType', 'operator', 'in', 'value', jsonb_build_array('key', 'both'))
                    ),
                    jsonb_build_object(
                        'name', 'strictHostKeyChecking',
                        'type', 'boolean',
                        'label', 'Strict Host Key Checking',
                        'default', true
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', true,
        'webhooks', false,
        'pagination', false,
        'filtering', true,
        'sorting', false,
        'customFields', false,
        'attachments', true,
        'compression', true
    ),
    ARRAY['SFTP', 'SSH'],
    ARRAY['BINARY', 'TEXT'],
    ARRAY['UsernamePassword', 'SSHKey'],
    '/docs/adapters/sftp',
    'free',
    'active',
    true
);

-- JDBC Database Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-jdbc',
    'JDBC Database',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'Universal JDBC adapter for database connectivity',
    'database',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Database Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'databaseType',
                        'type', 'select',
                        'label', 'Database Type',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'postgresql', 'label', 'PostgreSQL'),
                            jsonb_build_object('value', 'mysql', 'label', 'MySQL'),
                            jsonb_build_object('value', 'oracle', 'label', 'Oracle'),
                            jsonb_build_object('value', 'sqlserver', 'label', 'SQL Server'),
                            jsonb_build_object('value', 'db2', 'label', 'IBM DB2'),
                            jsonb_build_object('value', 'custom', 'label', 'Custom JDBC')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'jdbcUrl',
                        'type', 'text',
                        'label', 'JDBC URL',
                        'required', true,
                        'condition', jsonb_build_object('field', 'databaseType', 'value', 'custom'),
                        'placeholder', 'jdbc:postgresql://localhost:5432/mydb'
                    ),
                    jsonb_build_object(
                        'name', 'host',
                        'type', 'text',
                        'label', 'Host',
                        'required', true,
                        'condition', jsonb_build_object('field', 'databaseType', 'operator', '!=', 'value', 'custom')
                    ),
                    jsonb_build_object(
                        'name', 'port',
                        'type', 'number',
                        'label', 'Port',
                        'required', true,
                        'condition', jsonb_build_object('field', 'databaseType', 'operator', '!=', 'value', 'custom')
                    ),
                    jsonb_build_object(
                        'name', 'database',
                        'type', 'text',
                        'label', 'Database Name',
                        'required', true,
                        'condition', jsonb_build_object('field', 'databaseType', 'operator', '!=', 'value', 'custom')
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
                        'name', 'connectionPool',
                        'type', 'group',
                        'label', 'Connection Pool',
                        'fields', jsonb_build_array(
                            jsonb_build_object(
                                'name', 'minConnections',
                                'type', 'number',
                                'label', 'Min Connections',
                                'default', 1
                            ),
                            jsonb_build_object(
                                'name', 'maxConnections',
                                'type', 'number',
                                'label', 'Max Connections',
                                'default', 10
                            )
                        )
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
        'customFields', true,
        'attachments', false,
        'transactions', true,
        'storedProcedures', true
    ),
    ARRAY['JDBC'],
    ARRAY['SQL', 'ResultSet'],
    ARRAY['UsernamePassword'],
    '/docs/adapters/jdbc',
    'free',
    'active',
    true
);

-- JMS Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-jms',
    'JMS',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'Java Message Service adapter for enterprise messaging',
    'message-square',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'JMS Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'providerType',
                        'type', 'select',
                        'label', 'JMS Provider',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'activemq', 'label', 'Apache ActiveMQ'),
                            jsonb_build_object('value', 'rabbitmq', 'label', 'RabbitMQ'),
                            jsonb_build_object('value', 'ibmmq', 'label', 'IBM MQ'),
                            jsonb_build_object('value', 'weblogic', 'label', 'WebLogic JMS'),
                            jsonb_build_object('value', 'generic', 'label', 'Generic JMS')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'brokerUrl',
                        'type', 'text',
                        'label', 'Broker URL',
                        'required', true,
                        'placeholder', 'tcp://localhost:61616'
                    ),
                    jsonb_build_object(
                        'name', 'username',
                        'type', 'text',
                        'label', 'Username'
                    ),
                    jsonb_build_object(
                        'name', 'password',
                        'type', 'password',
                        'label', 'Password'
                    ),
                    jsonb_build_object(
                        'name', 'destinationType',
                        'type', 'select',
                        'label', 'Destination Type',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'queue', 'label', 'Queue'),
                            jsonb_build_object('value', 'topic', 'label', 'Topic')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'destinationName',
                        'type', 'text',
                        'label', 'Destination Name',
                        'required', true,
                        'placeholder', 'myQueue'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', true,
        'webhooks', false,
        'pagination', false,
        'filtering', true,
        'sorting', false,
        'customFields', true,
        'attachments', false,
        'transactions', true,
        'durableSubscriptions', true
    ),
    ARRAY['JMS'],
    ARRAY['JMS', 'TEXT', 'BINARY'],
    ARRAY['UsernamePassword', 'None'],
    '/docs/adapters/jms',
    'free',
    'active',
    true
);

-- Kafka Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-kafka',
    'Apache Kafka',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'Apache Kafka adapter for distributed streaming',
    'activity',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Kafka Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'bootstrapServers',
                        'type', 'text',
                        'label', 'Bootstrap Servers',
                        'required', true,
                        'placeholder', 'localhost:9092,localhost:9093',
                        'help': 'Comma-separated list of Kafka brokers'
                    ),
                    jsonb_build_object(
                        'name', 'securityProtocol',
                        'type', 'select',
                        'label', 'Security Protocol',
                        'default', 'PLAINTEXT',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'PLAINTEXT', 'label', 'Plain Text'),
                            jsonb_build_object('value', 'SSL', 'label', 'SSL/TLS'),
                            jsonb_build_object('value', 'SASL_PLAINTEXT', 'label', 'SASL Plain Text'),
                            jsonb_build_object('value', 'SASL_SSL', 'label', 'SASL SSL')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'saslMechanism',
                        'type', 'select',
                        'label', 'SASL Mechanism',
                        'condition', jsonb_build_object('field', 'securityProtocol', 'operator', 'in', 'value', jsonb_build_array('SASL_PLAINTEXT', 'SASL_SSL')),
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'PLAIN', 'label', 'PLAIN'),
                            jsonb_build_object('value', 'SCRAM-SHA-256', 'label', 'SCRAM-SHA-256'),
                            jsonb_build_object('value', 'SCRAM-SHA-512', 'label', 'SCRAM-SHA-512')
                        )
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', true,
        'webhooks', false,
        'pagination', false,
        'filtering', true,
        'sorting', false,
        'customFields', true,
        'attachments', false,
        'partitioning', true,
        'exactly-once', true
    ),
    ARRAY['Kafka'],
    ARRAY['BINARY', 'JSON', 'AVRO'],
    ARRAY['None', 'SASL', 'SSL'],
    '/docs/adapters/kafka',
    'free',
    'active',
    true
);

-- Email (SMTP/IMAP) Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-mail',
    'Email (SMTP/IMAP)',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'Email adapter for sending and receiving emails',
    'mail',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'server',
                'title', 'Email Server Settings',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'smtpHost',
                        'type', 'text',
                        'label', 'SMTP Host',
                        'required', true,
                        'placeholder', 'smtp.gmail.com'
                    ),
                    jsonb_build_object(
                        'name', 'smtpPort',
                        'type', 'number',
                        'label', 'SMTP Port',
                        'default', 587
                    ),
                    jsonb_build_object(
                        'name', 'imapHost',
                        'type', 'text',
                        'label', 'IMAP Host',
                        'placeholder', 'imap.gmail.com'
                    ),
                    jsonb_build_object(
                        'name', 'imapPort',
                        'type', 'number',
                        'label', 'IMAP Port',
                        'default', 993
                    ),
                    jsonb_build_object(
                        'name', 'username',
                        'type', 'text',
                        'label', 'Username/Email',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'password',
                        'type', 'password',
                        'label', 'Password',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'enableSSL',
                        'type', 'boolean',
                        'label', 'Enable SSL/TLS',
                        'default', true
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
        'attachments', true
    ),
    ARRAY['SMTP', 'IMAP', 'POP3'],
    ARRAY['EMAIL', 'TEXT', 'HTML'],
    ARRAY['UsernamePassword', 'OAuth2'],
    '/docs/adapters/email',
    'free',
    'active',
    true
);

-- REST API Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-rest',
    'REST API',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'RESTful API adapter with OpenAPI support',
    'code',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'api',
                'title', 'API Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'openApiSpec',
                        'type', 'textarea',
                        'label', 'OpenAPI Specification',
                        'placeholder', 'Paste OpenAPI/Swagger spec or URL',
                        'help': 'Optional: Import API definition from OpenAPI spec'
                    ),
                    jsonb_build_object(
                        'name', 'baseUrl',
                        'type', 'text',
                        'label', 'Base URL',
                        'required', true,
                        'placeholder', 'https://api.example.com/v1'
                    ),
                    jsonb_build_object(
                        'name', 'defaultHeaders',
                        'type', 'keyvalue',
                        'label', 'Default Headers',
                        'help': 'Headers to include in all requests'
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
        'openapi', true,
        'graphql', false
    ),
    ARRAY['REST', 'HTTP', 'HTTPS'],
    ARRAY['JSON', 'XML', 'FORM'],
    ARRAY['None', 'Basic', 'Bearer', 'APIKey', 'OAuth2', 'Custom'],
    '/docs/adapters/rest',
    'free',
    'active',
    true
);

-- SOAP Web Service Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-soap',
    'SOAP Web Service',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'SOAP web service adapter with WSDL support',
    'globe',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'service',
                'title', 'Web Service Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'wsdlUrl',
                        'type', 'text',
                        'label', 'WSDL URL',
                        'required', true,
                        'placeholder', 'https://example.com/service?wsdl',
                        'help': 'URL to the WSDL file'
                    ),
                    jsonb_build_object(
                        'name', 'soapVersion',
                        'type', 'select',
                        'label', 'SOAP Version',
                        'default', '1.2',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', '1.1', 'label', 'SOAP 1.1'),
                            jsonb_build_object('value', '1.2', 'label', 'SOAP 1.2')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'wsAddressing',
                        'type', 'boolean',
                        'label', 'Enable WS-Addressing',
                        'default', false
                    ),
                    jsonb_build_object(
                        'name', 'mtom',
                        'type', 'boolean',
                        'label', 'Enable MTOM',
                        'default', false,
                        'help': 'Message Transmission Optimization Mechanism'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', false,
        'streaming', false,
        'webhooks', false,
        'pagination', false,
        'filtering', false,
        'sorting', false,
        'customFields', true,
        'attachments', true,
        'wsdl', true,
        'ws-security', true
    ),
    ARRAY['SOAP', 'HTTP', 'HTTPS'],
    ARRAY['SOAP', 'XML'],
    ARRAY['None', 'Basic', 'WSSecurity', 'Certificate'],
    '/docs/adapters/soap',
    'free',
    'active',
    true
);

-- SAP RFC Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-rfc',
    'SAP RFC',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'SAP Remote Function Call adapter for ABAP function modules',
    'cpu',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'sap',
                'title', 'SAP Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'ashost',
                        'type', 'text',
                        'label', 'Application Server Host',
                        'required', true,
                        'placeholder', 'sap.company.com'
                    ),
                    jsonb_build_object(
                        'name', 'sysnr',
                        'type', 'text',
                        'label', 'System Number',
                        'required', true,
                        'placeholder', '00'
                    ),
                    jsonb_build_object(
                        'name', 'client',
                        'type', 'text',
                        'label', 'Client',
                        'required', true,
                        'placeholder', '100'
                    ),
                    jsonb_build_object(
                        'name', 'user',
                        'type', 'text',
                        'label', 'Username',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'passwd',
                        'type', 'password',
                        'label', 'Password',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'lang',
                        'type', 'text',
                        'label', 'Language',
                        'default', 'EN'
                    ),
                    jsonb_build_object(
                        'name', 'poolCapacity',
                        'type', 'number',
                        'label', 'Connection Pool Size',
                        'default', 5
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', false,
        'webhooks', false,
        'pagination', false,
        'filtering', false,
        'sorting', false,
        'customFields', true,
        'attachments', false,
        'bapi', true,
        'tables', true
    ),
    ARRAY['RFC', 'JCo'],
    ARRAY['RFC', 'ABAP'],
    ARRAY['UsernamePassword', 'SNC'],
    '/docs/adapters/rfc',
    'free',
    'active',
    true
);

-- SAP IDoc Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-idoc',
    'SAP IDoc',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'SAP Intermediate Document adapter for EDI and ALE',
    'file-text',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'sap',
                'title', 'SAP Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'ashost',
                        'type', 'text',
                        'label', 'Application Server Host',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'sysnr',
                        'type', 'text',
                        'label', 'System Number',
                        'required', true,
                        'placeholder', '00'
                    ),
                    jsonb_build_object(
                        'name', 'client',
                        'type', 'text',
                        'label', 'Client',
                        'required', true,
                        'placeholder', '100'
                    ),
                    jsonb_build_object(
                        'name', 'user',
                        'type', 'text',
                        'label', 'Username',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'passwd',
                        'type', 'password',
                        'label', 'Password',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'progid',
                        'type', 'text',
                        'label', 'Program ID',
                        'required', true,
                        'help', 'RFC Program ID for IDoc server'
                    ),
                    jsonb_build_object(
                        'name', 'gwhost',
                        'type', 'text',
                        'label', 'Gateway Host',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'gwserv',
                        'type', 'text',
                        'label', 'Gateway Service',
                        'required', true,
                        'placeholder', 'sapgw00'
                    )
                )
            )
        )
    ),
    jsonb_build_object(
        'bulkOperations', true,
        'streaming', false,
        'webhooks', false,
        'pagination', false,
        'filtering', false,
        'sorting', false,
        'customFields', false,
        'attachments', false,
        'ale', true,
        'edi', true
    ),
    ARRAY['IDoc', 'RFC', 'tRFC'],
    ARRAY['IDoc', 'XML'],
    ARRAY['UsernamePassword', 'SNC'],
    '/docs/adapters/idoc',
    'free',
    'active',
    true
);

-- SAP OData Adapter
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'integrix-odata',
    'OData',
    (SELECT id FROM adapter_categories WHERE code = 'integrix'),
    'Integrix',
    '1.0',
    'OData protocol adapter for REST-based data services',
    'share-2',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'service',
                'title', 'OData Service',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'serviceUrl',
                        'type', 'text',
                        'label', 'Service URL',
                        'required', true,
                        'placeholder', 'https://services.odata.org/V4/Northwind/Northwind.svc'
                    ),
                    jsonb_build_object(
                        'name', 'odataVersion',
                        'type', 'select',
                        'label', 'OData Version',
                        'required', true,
                        'default', 'v4',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'v2', 'label', 'OData v2'),
                            jsonb_build_object('value', 'v3', 'label', 'OData v3'),
                            jsonb_build_object('value', 'v4', 'label', 'OData v4')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'format',
                        'type', 'select',
                        'label', 'Response Format',
                        'default', 'json',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'json', 'label', 'JSON'),
                            jsonb_build_object('value', 'xml', 'label', 'XML')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication',
                        'required', true,
                        'default', 'none',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'none', 'label', 'No Authentication'),
                            jsonb_build_object('value', 'basic', 'label', 'Basic Auth'),
                            jsonb_build_object('value', 'oauth2', 'label', 'OAuth 2.0'),
                            jsonb_build_object('value', 'saptoken', 'label', 'SAP Token')
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
        'customFields', false,
        'attachments', false,
        'metadata', true,
        'batch', true
    ),
    ARRAY['OData', 'REST'],
    ARRAY['JSON', 'XML', 'ATOM'],
    ARRAY['None', 'Basic', 'OAuth2', 'Token'],
    '/docs/adapters/odata',
    'free',
    'active',
    true
);