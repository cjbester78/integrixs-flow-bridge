-- Database & Storage Adapters

-- PostgreSQL
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'postgresql',
    'PostgreSQL',
    (SELECT id FROM adapter_categories WHERE code = 'database'),
    'PostgreSQL',
    '15',
    'Connect to PostgreSQL for relational database operations',
    'database',
    true,
    true,
    false,
    -- Common configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Database Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'host',
                        'type', 'text',
                        'label', 'Host',
                        'required', true,
                        'placeholder', 'localhost',
                        'help', 'PostgreSQL server hostname or IP address'
                    ),
                    jsonb_build_object(
                        'name', 'port',
                        'type', 'number',
                        'label', 'Port',
                        'required', true,
                        'default', 5432,
                        'min', 1,
                        'max', 65535
                    ),
                    jsonb_build_object(
                        'name', 'database',
                        'type', 'text',
                        'label', 'Database Name',
                        'required', true,
                        'placeholder', 'mydb'
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
                        'name', 'sslMode',
                        'type', 'select',
                        'label', 'SSL Mode',
                        'default', 'prefer',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'disable', 'label', 'Disable'),
                            jsonb_build_object('value', 'require', 'label', 'Require'),
                            jsonb_build_object('value', 'verify-ca', 'label', 'Verify CA'),
                            jsonb_build_object('value', 'verify-full', 'label', 'Verify Full'),
                            jsonb_build_object('value', 'prefer', 'label', 'Prefer (Default)')
                        )
                    )
                )
            ),
            jsonb_build_object(
                'id', 'pool',
                'title', 'Connection Pool',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'poolSize',
                        'type', 'number',
                        'label', 'Connection Pool Size',
                        'default', 10,
                        'min', 1,
                        'max', 100
                    ),
                    jsonb_build_object(
                        'name', 'connectionTimeout',
                        'type', 'number',
                        'label', 'Connection Timeout (ms)',
                        'default', 30000,
                        'min', 1000,
                        'max', 300000
                    )
                )
            )
        )
    ),
    -- Inbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'query',
                'title', 'Query Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'mode',
                        'type', 'select',
                        'label', 'Query Mode',
                        'required', true,
                        'default', 'polling',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'polling', 'label', 'Polling'),
                            jsonb_build_object('value', 'cdc', 'label', 'Change Data Capture'),
                            jsonb_build_object('value', 'trigger', 'label', 'Database Triggers')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'query',
                        'type', 'sql',
                        'label', 'SQL Query',
                        'required', true,
                        'placeholder', 'SELECT * FROM users WHERE created_at > :last_sync',
                        'condition', jsonb_build_object('field', 'mode', 'value', 'polling')
                    ),
                    jsonb_build_object(
                        'name', 'pollingInterval',
                        'type', 'number',
                        'label', 'Polling Interval (seconds)',
                        'default', 60,
                        'min', 10,
                        'max', 3600,
                        'condition', jsonb_build_object('field', 'mode', 'value', 'polling')
                    ),
                    jsonb_build_object(
                        'name', 'timestampColumn',
                        'type', 'text',
                        'label', 'Timestamp Column',
                        'placeholder', 'updated_at',
                        'help': 'Column to track changes for incremental sync'
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
                            jsonb_build_object('value', 'insert', 'label', 'Insert'),
                            jsonb_build_object('value', 'update', 'label', 'Update'),
                            jsonb_build_object('value', 'upsert', 'label', 'Upsert'),
                            jsonb_build_object('value', 'delete', 'label', 'Delete'),
                            jsonb_build_object('value', 'stored_procedure', 'label', 'Stored Procedure'),
                            jsonb_build_object('value', 'custom_sql', 'label', 'Custom SQL')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'table',
                        'type', 'text',
                        'label', 'Target Table',
                        'required', true,
                        'placeholder', 'users',
                        'condition', jsonb_build_object('field', 'operation', 'operator', 'in', 'value', jsonb_build_array('insert', 'update', 'upsert', 'delete'))
                    ),
                    jsonb_build_object(
                        'name', 'conflictColumns',
                        'type', 'text',
                        'label', 'Conflict Columns',
                        'placeholder', 'email,username',
                        'condition', jsonb_build_object('field', 'operation', 'value', 'upsert'),
                        'help', 'Columns to check for conflicts in upsert operations'
                    ),
                    jsonb_build_object(
                        'name', 'batchSize',
                        'type', 'number',
                        'label', 'Batch Size',
                        'default', 1000,
                        'min', 1,
                        'max', 10000
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
        'storedProcedures', true,
        'cdc', true
    ),
    ARRAY['JDBC'],
    ARRAY['SQL'],
    ARRAY['UsernamePassword'],
    'https://www.postgresql.org/docs/',
    'free',
    'active',
    true
);

-- MySQL
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'mysql',
    'MySQL',
    (SELECT id FROM adapter_categories WHERE code = 'database'),
    'Oracle',
    '8.0',
    'Connect to MySQL for relational database operations',
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
                        'name', 'host',
                        'type', 'text',
                        'label', 'Host',
                        'required', true,
                        'placeholder', 'localhost'
                    ),
                    jsonb_build_object(
                        'name', 'port',
                        'type', 'number',
                        'label', 'Port',
                        'required', true,
                        'default', 3306
                    ),
                    jsonb_build_object(
                        'name', 'database',
                        'type', 'text',
                        'label', 'Database Name',
                        'required', true
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
                        'name', 'sslEnabled',
                        'type', 'boolean',
                        'label', 'Enable SSL',
                        'default', false
                    ),
                    jsonb_build_object(
                        'name', 'timezone',
                        'type', 'text',
                        'label', 'Timezone',
                        'default', 'UTC'
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
        'storedProcedures', true,
        'binlog', true
    ),
    ARRAY['JDBC'],
    ARRAY['SQL'],
    ARRAY['UsernamePassword'],
    'https://dev.mysql.com/doc/',
    'free',
    'active',
    true
);

-- MongoDB
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'mongodb',
    'MongoDB',
    (SELECT id FROM adapter_categories WHERE code = 'database'),
    'MongoDB',
    '6.0',
    'Connect to MongoDB for NoSQL document database operations',
    'database',
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
                        'name', 'connectionString',
                        'type', 'text',
                        'label', 'Connection String',
                        'required', true,
                        'placeholder', 'mongodb://localhost:27017',
                        'help', 'MongoDB connection URI'
                    ),
                    jsonb_build_object(
                        'name', 'database',
                        'type', 'text',
                        'label', 'Database Name',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'authSource',
                        'type', 'text',
                        'label', 'Authentication Database',
                        'default', 'admin',
                        'help', 'Database to authenticate against'
                    ),
                    jsonb_build_object(
                        'name', 'replicaSet',
                        'type', 'text',
                        'label', 'Replica Set',
                        'placeholder', 'rs0',
                        'help', 'Name of the replica set (if applicable)'
                    ),
                    jsonb_build_object(
                        'name', 'ssl',
                        'type', 'boolean',
                        'label', 'Use SSL/TLS',
                        'default', false
                    )
                )
            )
        )
    ),
    -- Inbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'query',
                'title', 'Query Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'collection',
                        'type', 'text',
                        'label', 'Collection',
                        'required', true,
                        'placeholder', 'users'
                    ),
                    jsonb_build_object(
                        'name', 'mode',
                        'type', 'select',
                        'label', 'Operation Mode',
                        'required', true,
                        'default', 'find',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'find', 'label', 'Find (Query)'),
                            jsonb_build_object('value', 'watch', 'label', 'Change Streams'),
                            jsonb_build_object('value', 'tail', 'label', 'Tailable Cursor')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'filter',
                        'type', 'json',
                        'label', 'Query Filter',
                        'placeholder', '{"active": true}',
                        'help': 'MongoDB query filter in JSON format'
                    ),
                    jsonb_build_object(
                        'name', 'projection',
                        'type', 'json',
                        'label', 'Projection',
                        'placeholder', '{"_id": 1, "name": 1, "email": 1}',
                        'help': 'Fields to include/exclude'
                    ),
                    jsonb_build_object(
                        'name', 'sort',
                        'type', 'json',
                        'label', 'Sort',
                        'placeholder', '{"createdAt": -1}'
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
                        'name', 'collection',
                        'type', 'text',
                        'label', 'Collection',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'operation',
                        'type', 'select',
                        'label', 'Operation',
                        'required', true,
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'insertOne', 'label', 'Insert One'),
                            jsonb_build_object('value', 'insertMany', 'label', 'Insert Many'),
                            jsonb_build_object('value', 'updateOne', 'label', 'Update One'),
                            jsonb_build_object('value', 'updateMany', 'label', 'Update Many'),
                            jsonb_build_object('value', 'replaceOne', 'label', 'Replace One'),
                            jsonb_build_object('value', 'deleteOne', 'label', 'Delete One'),
                            jsonb_build_object('value', 'deleteMany', 'label', 'Delete Many'),
                            jsonb_build_object('value', 'aggregate', 'label', 'Aggregation Pipeline')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'upsert',
                        'type', 'boolean',
                        'label', 'Upsert',
                        'default', false,
                        'condition', jsonb_build_object('field', 'operation', 'operator', 'in', 'value', jsonb_build_array('updateOne', 'updateMany', 'replaceOne'))
                    ),
                    jsonb_build_object(
                        'name', 'writeConcern',
                        'type', 'select',
                        'label', 'Write Concern',
                        'default', '1',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', '0', 'label', 'Unacknowledged'),
                            jsonb_build_object('value', '1', 'label', 'Acknowledged'),
                            jsonb_build_object('value', 'majority', 'label', 'Majority')
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
        'attachments', true,
        'changeStreams', true,
        'aggregation', true,
        'gridFS', true
    ),
    ARRAY['MongoDB'],
    ARRAY['BSON', 'JSON'],
    ARRAY['ConnectionString', 'X509'],
    'https://docs.mongodb.com/',
    'free',
    'active',
    true
);

-- Oracle Database
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'oracle-db',
    'Oracle Database',
    (SELECT id FROM adapter_categories WHERE code = 'oracle'),
    'Oracle',
    '19c',
    'Connect to Oracle Database for enterprise relational database operations',
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
                        'name', 'connectionType',
                        'type', 'select',
                        'label', 'Connection Type',
                        'required', true,
                        'default', 'service',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'service', 'label', 'Service Name'),
                            jsonb_build_object('value', 'sid', 'label', 'SID'),
                            jsonb_build_object('value', 'tns', 'label', 'TNS')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'host',
                        'type', 'text',
                        'label', 'Host',
                        'required', true,
                        'condition', jsonb_build_object('field', 'connectionType', 'operator', 'in', 'value', jsonb_build_array('service', 'sid'))
                    ),
                    jsonb_build_object(
                        'name', 'port',
                        'type', 'number',
                        'label', 'Port',
                        'default', 1521,
                        'condition', jsonb_build_object('field', 'connectionType', 'operator', 'in', 'value', jsonb_build_array('service', 'sid'))
                    ),
                    jsonb_build_object(
                        'name', 'serviceName',
                        'type', 'text',
                        'label', 'Service Name',
                        'required', true,
                        'condition', jsonb_build_object('field', 'connectionType', 'value', 'service')
                    ),
                    jsonb_build_object(
                        'name', 'sid',
                        'type', 'text',
                        'label', 'SID',
                        'required', true,
                        'condition', jsonb_build_object('field', 'connectionType', 'value', 'sid')
                    ),
                    jsonb_build_object(
                        'name', 'tnsName',
                        'type', 'text',
                        'label', 'TNS Name',
                        'required', true,
                        'condition', jsonb_build_object('field', 'connectionType', 'value', 'tns')
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
        'streaming', true,
        'webhooks', false,
        'pagination', true,
        'filtering', true,
        'sorting', true,
        'customFields', true,
        'attachments', false,
        'plsql', true,
        'partitioning', true,
        'goldenGate', true
    ),
    ARRAY['JDBC', 'OCI'],
    ARRAY['SQL'],
    ARRAY['UsernamePassword', 'Kerberos'],
    'https://docs.oracle.com/en/database/',
    'enterprise',
    'active',
    true
);

-- Microsoft SQL Server
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'sqlserver',
    'Microsoft SQL Server',
    (SELECT id FROM adapter_categories WHERE code = 'microsoft'),
    'Microsoft',
    '2022',
    'Connect to Microsoft SQL Server for enterprise database operations',
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
                        'name', 'server',
                        'type', 'text',
                        'label', 'Server',
                        'required', true,
                        'placeholder', 'localhost\\SQLEXPRESS',
                        'help', 'Server name or IP address'
                    ),
                    jsonb_build_object(
                        'name', 'port',
                        'type', 'number',
                        'label', 'Port',
                        'default', 1433
                    ),
                    jsonb_build_object(
                        'name', 'database',
                        'type', 'text',
                        'label', 'Database',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication',
                        'required', true,
                        'default', 'sql',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'sql', 'label', 'SQL Server Authentication'),
                            jsonb_build_object('value', 'windows', 'label', 'Windows Authentication'),
                            jsonb_build_object('value', 'azure', 'label', 'Azure AD')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'username',
                        'type', 'text',
                        'label', 'Username',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'operator', 'in', 'value', jsonb_build_array('sql', 'azure'))
                    ),
                    jsonb_build_object(
                        'name', 'password',
                        'type', 'password',
                        'label', 'Password',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'operator', 'in', 'value', jsonb_build_array('sql', 'azure'))
                    ),
                    jsonb_build_object(
                        'name', 'encrypt',
                        'type', 'boolean',
                        'label', 'Encrypt Connection',
                        'default', true
                    ),
                    jsonb_build_object(
                        'name', 'trustServerCertificate',
                        'type', 'boolean',
                        'label', 'Trust Server Certificate',
                        'default', false
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
        'storedProcedures', true,
        'changeDataCapture', true,
        'alwaysOn', true
    ),
    ARRAY['JDBC', 'ODBC'],
    ARRAY['SQL'],
    ARRAY['UsernamePassword', 'Windows', 'AzureAD'],
    'https://docs.microsoft.com/en-us/sql/',
    'standard',
    'active',
    true
);

-- Redis
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'redis',
    'Redis',
    (SELECT id FROM adapter_categories WHERE code = 'database'),
    'Redis',
    '7.0',
    'Connect to Redis for in-memory data store and cache',
    'cpu',
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
                        'name', 'host',
                        'type', 'text',
                        'label', 'Host',
                        'required', true,
                        'default', 'localhost'
                    ),
                    jsonb_build_object(
                        'name', 'port',
                        'type', 'number',
                        'label', 'Port',
                        'default', 6379
                    ),
                    jsonb_build_object(
                        'name', 'database',
                        'type', 'number',
                        'label', 'Database Index',
                        'default', 0,
                        'min', 0,
                        'max', 15
                    ),
                    jsonb_build_object(
                        'name', 'password',
                        'type', 'password',
                        'label', 'Password',
                        'help', 'Leave empty if not using authentication'
                    ),
                    jsonb_build_object(
                        'name', 'ssl',
                        'type', 'boolean',
                        'label', 'Use SSL/TLS',
                        'default', false
                    ),
                    jsonb_build_object(
                        'name', 'cluster',
                        'type', 'boolean',
                        'label', 'Redis Cluster Mode',
                        'default', false
                    ),
                    jsonb_build_object(
                        'name', 'sentinel',
                        'type', 'group',
                        'label', 'Sentinel Configuration',
                        'fields', jsonb_build_array(
                            jsonb_build_object(
                                'name', 'enabled',
                                'type', 'boolean',
                                'label', 'Use Sentinel',
                                'default', false
                            ),
                            jsonb_build_object(
                                'name', 'masterName',
                                'type', 'text',
                                'label', 'Master Name',
                                'condition', jsonb_build_object('field', 'sentinel.enabled', 'value', true)
                            ),
                            jsonb_build_object(
                                'name', 'sentinels',
                                'type', 'text',
                                'label', 'Sentinel Hosts',
                                'placeholder', 'host1:26379,host2:26379',
                                'condition', jsonb_build_object('field', 'sentinel.enabled', 'value', true)
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
        'pagination', false,
        'filtering', true,
        'sorting', false,
        'customFields', false,
        'attachments', false,
        'pubsub', true,
        'streams', true,
        'lua', true
    ),
    ARRAY['Redis'],
    ARRAY['String', 'Binary'],
    ARRAY['Password', 'None'],
    'https://redis.io/documentation',
    'free',
    'active',
    true
);

-- AWS S3
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, inbound_config_schema, outbound_config_schema,
    capabilities, supported_protocols, supported_formats, authentication_methods,
    documentation_url, pricing_tier, status, is_certified
) VALUES (
    'aws-s3',
    'Amazon S3',
    (SELECT id FROM adapter_categories WHERE code = 'aws'),
    'Amazon',
    '2006-03-01',
    'Connect to Amazon S3 for object storage in the cloud',
    'archive',
    true,
    true,
    false,
    -- Common configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'AWS Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'region',
                        'type', 'select',
                        'label', 'AWS Region',
                        'required', true,
                        'default', 'us-east-1',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'us-east-1', 'label', 'US East (N. Virginia)'),
                            jsonb_build_object('value', 'us-west-2', 'label', 'US West (Oregon)'),
                            jsonb_build_object('value', 'eu-west-1', 'label', 'EU (Ireland)'),
                            jsonb_build_object('value', 'eu-central-1', 'label', 'EU (Frankfurt)'),
                            jsonb_build_object('value', 'ap-northeast-1', 'label', 'Asia Pacific (Tokyo)'),
                            jsonb_build_object('value', 'ap-southeast-1', 'label', 'Asia Pacific (Singapore)'),
                            jsonb_build_object('value', 'ap-southeast-2', 'label', 'Asia Pacific (Sydney)')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'accessKey',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'accessKey', 'label', 'Access Key'),
                            jsonb_build_object('value', 'role', 'label', 'IAM Role'),
                            jsonb_build_object('value', 'temporary', 'label', 'Temporary Credentials')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'accessKeyId',
                        'type', 'text',
                        'label', 'Access Key ID',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'accessKey')
                    ),
                    jsonb_build_object(
                        'name', 'secretAccessKey',
                        'type', 'password',
                        'label', 'Secret Access Key',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'accessKey')
                    ),
                    jsonb_build_object(
                        'name', 'roleArn',
                        'type', 'text',
                        'label', 'Role ARN',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'role'),
                        'placeholder', 'arn:aws:iam::123456789012:role/MyRole'
                    )
                )
            )
        )
    ),
    -- Inbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'bucket',
                'title', 'Bucket Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'bucketName',
                        'type', 'text',
                        'label', 'Bucket Name',
                        'required', true,
                        'placeholder', 'my-bucket',
                        'validation', jsonb_build_object(
                            'pattern', '^[a-z0-9][a-z0-9-]*[a-z0-9]$',
                            'message', 'Must be valid S3 bucket name'
                        )
                    ),
                    jsonb_build_object(
                        'name', 'prefix',
                        'type', 'text',
                        'label', 'Key Prefix',
                        'placeholder', 'data/incoming/',
                        'help': 'Only process objects with this prefix'
                    ),
                    jsonb_build_object(
                        'name', 'suffix',
                        'type', 'text',
                        'label', 'Key Suffix',
                        'placeholder', '.csv',
                        'help': 'Only process objects with this suffix'
                    ),
                    jsonb_build_object(
                        'name', 'mode',
                        'type', 'select',
                        'label', 'Processing Mode',
                        'required', true,
                        'default', 'event',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'event', 'label', 'S3 Events (Real-time)'),
                            jsonb_build_object('value', 'poll', 'label', 'Polling'),
                            jsonb_build_object('value', 'batch', 'label', 'Batch Processing')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'deleteAfterRead',
                        'type', 'boolean',
                        'label', 'Delete After Processing',
                        'default', false
                    )
                )
            )
        )
    ),
    -- Outbound configuration
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'upload',
                'title', 'Upload Configuration',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'bucketName',
                        'type', 'text',
                        'label', 'Bucket Name',
                        'required', true
                    ),
                    jsonb_build_object(
                        'name', 'keyPattern',
                        'type', 'text',
                        'label', 'Object Key Pattern',
                        'placeholder', 'data/${date}/${filename}',
                        'help': 'Use variables: ${date}, ${time}, ${uuid}, ${filename}'
                    ),
                    jsonb_build_object(
                        'name', 'storageClass',
                        'type', 'select',
                        'label', 'Storage Class',
                        'default', 'STANDARD',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'STANDARD', 'label', 'Standard'),
                            jsonb_build_object('value', 'STANDARD_IA', 'label', 'Standard-IA'),
                            jsonb_build_object('value', 'INTELLIGENT_TIERING', 'label', 'Intelligent-Tiering'),
                            jsonb_build_object('value', 'GLACIER', 'label', 'Glacier'),
                            jsonb_build_object('value', 'DEEP_ARCHIVE', 'label', 'Deep Archive')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'encryption',
                        'type', 'select',
                        'label', 'Server-Side Encryption',
                        'default', 'AES256',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'none', 'label', 'None'),
                            jsonb_build_object('value', 'AES256', 'label', 'SSE-S3'),
                            jsonb_build_object('value', 'aws:kms', 'label', 'SSE-KMS')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'multipartThreshold',
                        'type', 'number',
                        'label', 'Multipart Threshold (MB)',
                        'default', 100,
                        'help': 'Use multipart upload for files larger than this'
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
        'sorting', false,
        'customFields', false,
        'attachments', true,
        'versioning', true,
        'lifecycle', true,
        'multipart', true,
        'presignedUrls', true
    ),
    ARRAY['S3', 'REST'],
    ARRAY['Binary', 'JSON', 'XML', 'CSV'],
    ARRAY['AWS_AccessKey', 'IAM_Role'],
    'https://docs.aws.amazon.com/s3/',
    'standard',
    'active',
    true
);

-- Azure Blob Storage
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'azure-blob',
    'Azure Blob Storage',
    (SELECT id FROM adapter_categories WHERE code = 'azure'),
    'Microsoft',
    '2023-11-03',
    'Connect to Azure Blob Storage for cloud object storage',
    'cloud',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'Azure Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'accountName',
                        'type', 'text',
                        'label', 'Storage Account Name',
                        'required', true,
                        'placeholder', 'mystorageaccount'
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'key',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'key', 'label', 'Account Key'),
                            jsonb_build_object('value', 'sas', 'label', 'SAS Token'),
                            jsonb_build_object('value', 'identity', 'label', 'Managed Identity'),
                            jsonb_build_object('value', 'connection', 'label', 'Connection String')
                        )
                    ),
                    jsonb_build_object(
                        'name', 'accountKey',
                        'type', 'password',
                        'label', 'Account Key',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'key')
                    ),
                    jsonb_build_object(
                        'name', 'sasToken',
                        'type', 'password',
                        'label', 'SAS Token',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'sas')
                    ),
                    jsonb_build_object(
                        'name', 'connectionString',
                        'type', 'password',
                        'label', 'Connection String',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'connection')
                    ),
                    jsonb_build_object(
                        'name', 'endpoint',
                        'type', 'text',
                        'label', 'Custom Endpoint',
                        'placeholder', 'https://mystorageaccount.blob.core.windows.net',
                        'help': 'Leave empty for default Azure endpoint'
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
        'sorting', false,
        'customFields', false,
        'attachments', true,
        'snapshots', true,
        'lifecycle', true,
        'versioning', true
    ),
    ARRAY['REST'],
    ARRAY['Binary', 'JSON', 'XML', 'CSV'],
    ARRAY['AccountKey', 'SAS', 'ManagedIdentity'],
    'https://docs.microsoft.com/en-us/azure/storage/blobs/',
    'standard',
    'active',
    true
);

-- Google Cloud Storage
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'gcs',
    'Google Cloud Storage',
    (SELECT id FROM adapter_categories WHERE code = 'google'),
    'Google',
    'v1',
    'Connect to Google Cloud Storage for object storage',
    'cloud',
    true,
    true,
    false,
    jsonb_build_object(
        'sections', jsonb_build_array(
            jsonb_build_object(
                'id', 'connection',
                'title', 'GCS Connection',
                'fields', jsonb_build_array(
                    jsonb_build_object(
                        'name', 'projectId',
                        'type', 'text',
                        'label', 'Project ID',
                        'required', true,
                        'placeholder', 'my-project-id'
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication Type',
                        'required', true,
                        'default', 'serviceAccount',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'serviceAccount', 'label', 'Service Account Key'),
                            jsonb_build_object('value', 'workloadIdentity', 'label', 'Workload Identity'),
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
                        'name', 'location',
                        'type', 'select',
                        'label', 'Default Location',
                        'default', 'US',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'US', 'label', 'Multi-region: United States'),
                            jsonb_build_object('value', 'EU', 'label', 'Multi-region: European Union'),
                            jsonb_build_object('value', 'ASIA', 'label', 'Multi-region: Asia'),
                            jsonb_build_object('value', 'us-central1', 'label', 'Region: US Central'),
                            jsonb_build_object('value', 'europe-west1', 'label', 'Region: Europe West')
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
        'sorting', false,
        'customFields', false,
        'attachments', true,
        'versioning', true,
        'lifecycle', true,
        'uniformAccess', true
    ),
    ARRAY['REST', 'gRPC'],
    ARRAY['Binary', 'JSON', 'XML', 'CSV'],
    ARRAY['ServiceAccount', 'OAuth2', 'WorkloadIdentity'],
    'https://cloud.google.com/storage/docs',
    'standard',
    'active',
    true
);

-- Elasticsearch
INSERT INTO adapter_types (
    code, name, category_id, vendor, version, description, icon,
    supports_inbound, supports_outbound, supports_bidirectional,
    common_config_schema, capabilities, supported_protocols, supported_formats,
    authentication_methods, documentation_url, pricing_tier, status, is_certified
) VALUES (
    'elasticsearch',
    'Elasticsearch',
    (SELECT id FROM adapter_categories WHERE code = 'database'),
    'Elastic',
    '8.11',
    'Connect to Elasticsearch for search and analytics',
    'search',
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
                        'name', 'nodes',
                        'type', 'text',
                        'label', 'Node URLs',
                        'required', true,
                        'placeholder', 'http://localhost:9200',
                        'help': 'Comma-separated list of Elasticsearch nodes'
                    ),
                    jsonb_build_object(
                        'name', 'authType',
                        'type', 'select',
                        'label', 'Authentication',
                        'default', 'none',
                        'options', jsonb_build_array(
                            jsonb_build_object('value', 'none', 'label', 'No Authentication'),
                            jsonb_build_object('value', 'basic', 'label', 'Basic Authentication'),
                            jsonb_build_object('value', 'apiKey', 'label', 'API Key'),
                            jsonb_build_object('value', 'cloud', 'label', 'Elastic Cloud')
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
                        'name', 'apiKey',
                        'type', 'password',
                        'label', 'API Key',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'apiKey')
                    ),
                    jsonb_build_object(
                        'name', 'cloudId',
                        'type', 'text',
                        'label', 'Cloud ID',
                        'required', true,
                        'condition', jsonb_build_object('field', 'authType', 'value', 'cloud')
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
        'attachments', false,
        'aggregations', true,
        'fullTextSearch', true,
        'geoSearch', true
    ),
    ARRAY['REST'],
    ARRAY['JSON'],
    ARRAY['None', 'Basic', 'APIKey', 'OAuth2'],
    'https://www.elastic.co/guide/en/elasticsearch/reference/current/',
    'standard',
    'active',
    true
);