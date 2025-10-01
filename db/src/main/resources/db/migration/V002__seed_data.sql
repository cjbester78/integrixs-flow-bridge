-- ========================================
-- Integrix Flow Bridge Seed Data
-- ========================================
-- This script inserts initial configuration and required data
-- Version: 2.0.0
-- ========================================

-- ========================================
-- Default Roles
-- ========================================

INSERT INTO roles (id, name, description, permissions) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ADMINISTRATOR', 'Full system access', 
     '["SYSTEM_ADMIN", "USER_MANAGEMENT", "FLOW_MANAGEMENT", "ADAPTER_MANAGEMENT", "CONFIGURATION_MANAGEMENT", "MONITORING", "AUDIT_VIEW"]'::jsonb),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'DEVELOPER', 'Flow and adapter development access',
     '["FLOW_MANAGEMENT", "ADAPTER_MANAGEMENT", "MONITORING", "AUDIT_VIEW"]'::jsonb),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'OPERATOR', 'Operational monitoring and management',
     '["FLOW_EXECUTE", "MONITORING", "AUDIT_VIEW"]'::jsonb),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'VIEWER', 'Read-only access',
     '["MONITORING", "AUDIT_VIEW"]'::jsonb)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- Default Admin User
-- ========================================
-- Password: Admin@123 (BCrypt hash)

INSERT INTO users (id, username, email, password_hash, first_name, last_name, role_id, is_active) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'admin', 'admin@integrix.com', 
     '$2a$10$Xg7z4rG8MzI6v9XQH8xLqOGKgRvHFp7Ll9WQ7DhX5Fg5SmPV3K5bO', 
     'System', 'Administrator', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', true)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- Default Business Component
-- ========================================

INSERT INTO business_components (id, name, description, contact_email, status) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', 'Default Component', 
     'Default business component for initial setup', 'admin@integrix.com', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- Adapter Types
-- ========================================

INSERT INTO adapter_types (id, code, name, description, category, supported_operations) VALUES
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'FILE', 'File Adapter', 'Read and write files from local or network file systems', 'FILE_BASED', ARRAY['READ', 'WRITE', 'MOVE', 'DELETE']),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'FTP', 'FTP Adapter', 'Transfer files using FTP/FTPS protocol', 'FILE_BASED', ARRAY['GET', 'PUT', 'LIST', 'DELETE', 'MOVE']),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'SFTP', 'SFTP Adapter', 'Secure file transfer using SSH', 'FILE_BASED', ARRAY['GET', 'PUT', 'LIST', 'DELETE', 'MOVE']),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'HTTP', 'HTTP Adapter', 'RESTful API integration', 'WEB_SERVICE', ARRAY['GET', 'POST', 'PUT', 'DELETE', 'PATCH']),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'SOAP', 'SOAP Adapter', 'SOAP web service integration', 'WEB_SERVICE', ARRAY['INVOKE']),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'DATABASE', 'Database Adapter', 'Database operations (PostgreSQL, MySQL, Oracle)', 'DATABASE', ARRAY['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'CALL']),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'KAFKA', 'Kafka Adapter', 'Apache Kafka messaging', 'MESSAGING', ARRAY['PRODUCE', 'CONSUME']),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'JMS', 'JMS Adapter', 'Java Message Service integration', 'MESSAGING', ARRAY['SEND', 'RECEIVE']),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'EMAIL', 'Email Adapter', 'Send and receive emails', 'COMMUNICATION', ARRAY['SEND', 'RECEIVE']),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 'SAP', 'SAP Adapter', 'SAP system integration', 'ERP', ARRAY['RFC', 'IDOC', 'BAPI'])
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- System Configuration
-- ========================================

INSERT INTO system_configurations (config_key, config_value, config_type, category, description, is_encrypted) VALUES
    -- Database Configuration
    ('db.connection.pool.size', '20', 'INTEGER', 'DATABASE', 'Maximum database connection pool size', false),
    ('db.connection.timeout', '30000', 'INTEGER', 'DATABASE', 'Database connection timeout in milliseconds', false),
    
    -- Security Configuration
    ('security.jwt.secret', 'IntegrixFlowBridge2024SecureKey', 'STRING', 'SECURITY', 'JWT secret key', true),
    ('security.jwt.expiration', '86400000', 'LONG', 'SECURITY', 'JWT token expiration time in milliseconds (24 hours)', false),
    ('security.password.min.length', '8', 'INTEGER', 'SECURITY', 'Minimum password length', false),
    ('security.password.require.uppercase', 'true', 'BOOLEAN', 'SECURITY', 'Require uppercase letters in password', false),
    ('security.password.require.lowercase', 'true', 'BOOLEAN', 'SECURITY', 'Require lowercase letters in password', false),
    ('security.password.require.numbers', 'true', 'BOOLEAN', 'SECURITY', 'Require numbers in password', false),
    ('security.password.require.special', 'true', 'BOOLEAN', 'SECURITY', 'Require special characters in password', false),
    ('security.login.max.attempts', '5', 'INTEGER', 'SECURITY', 'Maximum failed login attempts before account lock', false),
    ('security.login.lockout.duration', '1800000', 'LONG', 'SECURITY', 'Account lockout duration in milliseconds (30 minutes)', false),
    
    -- Email Configuration
    ('email.smtp.host', 'smtp.gmail.com', 'STRING', 'EMAIL', 'SMTP server host', false),
    ('email.smtp.port', '587', 'INTEGER', 'EMAIL', 'SMTP server port', false),
    ('email.smtp.auth', 'true', 'BOOLEAN', 'EMAIL', 'Enable SMTP authentication', false),
    ('email.smtp.starttls', 'true', 'BOOLEAN', 'EMAIL', 'Enable STARTTLS', false),
    ('email.from.address', 'noreply@integrix.com', 'STRING', 'EMAIL', 'Default from email address', false),
    
    -- Flow Configuration
    ('flow.execution.thread.pool.size', '10', 'INTEGER', 'FLOW', 'Flow execution thread pool size', false),
    ('flow.execution.timeout', '300000', 'LONG', 'FLOW', 'Default flow execution timeout in milliseconds (5 minutes)', false),
    ('flow.retry.max.attempts', '3', 'INTEGER', 'FLOW', 'Default maximum retry attempts', false),
    ('flow.retry.initial.delay', '1000', 'LONG', 'FLOW', 'Initial retry delay in milliseconds', false),
    ('flow.retry.multiplier', '2.0', 'DOUBLE', 'FLOW', 'Retry delay multiplier', false),
    
    -- Message Configuration
    ('message.payload.max.size', '52428800', 'LONG', 'MESSAGE', 'Maximum message payload size in bytes (50MB)', false),
    ('message.retention.days', '30', 'INTEGER', 'MESSAGE', 'Message retention period in days', false),
    ('message.compression.enabled', 'true', 'BOOLEAN', 'MESSAGE', 'Enable message compression for large payloads', false),
    ('message.compression.threshold', '1048576', 'LONG', 'MESSAGE', 'Compression threshold in bytes (1MB)', false),
    
    -- Monitoring Configuration
    ('monitoring.health.check.interval', '60000', 'LONG', 'MONITORING', 'Health check interval in milliseconds (1 minute)', false),
    ('monitoring.metrics.enabled', 'true', 'BOOLEAN', 'MONITORING', 'Enable metrics collection', false),
    ('monitoring.metrics.export.interval', '60000', 'LONG', 'MONITORING', 'Metrics export interval in milliseconds', false),
    
    -- Adapter Configuration
    ('adapter.connection.timeout', '30000', 'INTEGER', 'ADAPTER', 'Default adapter connection timeout in milliseconds', false),
    ('adapter.read.timeout', '60000', 'INTEGER', 'ADAPTER', 'Default adapter read timeout in milliseconds', false),
    ('adapter.pool.max.size', '10', 'INTEGER', 'ADAPTER', 'Maximum adapter connection pool size', false),
    
    -- File Adapter Configuration
    ('adapter.file.temp.directory', '/tmp/integrix/temp', 'STRING', 'ADAPTER', 'Temporary directory for file operations', false),
    ('adapter.file.archive.directory', '/tmp/integrix/archive', 'STRING', 'ADAPTER', 'Archive directory for processed files', false),
    ('adapter.file.error.directory', '/tmp/integrix/error', 'STRING', 'ADAPTER', 'Error directory for failed files', false),
    
    -- System Configuration
    ('system.timezone', 'UTC', 'STRING', 'SYSTEM', 'System timezone', false),
    ('system.locale', 'en_US', 'STRING', 'SYSTEM', 'System locale', false),
    ('system.environment', 'DEVELOPMENT', 'STRING', 'SYSTEM', 'System environment (DEVELOPMENT/STAGING/PRODUCTION)', false)
ON CONFLICT (config_key) DO NOTHING;

-- ========================================
-- System Settings (UI-configurable)
-- ========================================

INSERT INTO system_settings (setting_key, setting_value, setting_type, category, description, default_value, is_required, is_visible, display_order) VALUES
    -- General Settings
    ('app.name', 'Integrix Flow Bridge', 'STRING', 'GENERAL', 'Application name', 'Integrix Flow Bridge', true, true, 1),
    ('app.logo.url', '/assets/logo.png', 'STRING', 'GENERAL', 'Application logo URL', '/assets/logo.png', false, true, 2),
    ('app.theme', 'light', 'STRING', 'GENERAL', 'UI theme (light/dark)', 'light', true, true, 3),
    
    -- Dashboard Settings
    ('dashboard.refresh.interval', '30', 'INTEGER', 'DASHBOARD', 'Dashboard refresh interval in seconds', '30', true, true, 10),
    ('dashboard.show.alerts', 'true', 'BOOLEAN', 'DASHBOARD', 'Show alerts on dashboard', 'true', true, true, 11),
    ('dashboard.max.recent.messages', '10', 'INTEGER', 'DASHBOARD', 'Maximum recent messages to display', '10', true, true, 12),
    
    -- Notification Settings
    ('notifications.email.enabled', 'true', 'BOOLEAN', 'NOTIFICATIONS', 'Enable email notifications', 'true', true, true, 20),
    ('notifications.alert.threshold.critical', '10', 'INTEGER', 'NOTIFICATIONS', 'Critical alert threshold', '10', true, true, 21),
    ('notifications.alert.threshold.warning', '5', 'INTEGER', 'NOTIFICATIONS', 'Warning alert threshold', '5', true, true, 22)
ON CONFLICT (setting_key) DO NOTHING;

-- ========================================
-- Default Retry Policy
-- ========================================

INSERT INTO retry_policies (id, name, description, max_attempts, initial_delay_ms, max_delay_ms, multiplier) VALUES
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Default Retry Policy', 
     'Standard retry policy with exponential backoff', 3, 1000, 60000, 2.0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- Default Alert Rules
-- ========================================

INSERT INTO alert_rules (id, name, description, rule_type, entity_type, condition_expression, severity, is_active) VALUES
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Flow Execution Failed', 
     'Alert when flow execution fails', 'THRESHOLD', 'FLOW_EXECUTION', 
     'status = "FAILED"', 'HIGH', true),
    
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'Adapter Health Critical', 
     'Alert when adapter health is critical', 'THRESHOLD', 'ADAPTER_HEALTH', 
     'health_status = "CRITICAL"', 'CRITICAL', true),
    
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'High Message Error Rate', 
     'Alert when message error rate exceeds threshold', 'PERCENTAGE', 'MESSAGE', 
     'error_rate > 10', 'WARNING', true),
    
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'Database Connection Pool Exhausted', 
     'Alert when database connection pool is exhausted', 'THRESHOLD', 'SYSTEM', 
     'db_pool_available = 0', 'CRITICAL', true)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- Default Notification Channels
-- ========================================

INSERT INTO notification_channels (id, name, channel_type, configuration, is_active) VALUES
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Default Email Channel', 'EMAIL',
     '{"recipients": ["admin@integrix.com"], "cc": [], "bcc": []}'::jsonb, true),
    
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'System Log Channel', 'SYSTEM_LOG',
     '{"logLevel": "ERROR"}'::jsonb, true)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- Sample Message Structures
-- ========================================

INSERT INTO message_structures (id, name, description, structure_type, xsd_content, validation_enabled) VALUES
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Generic XML Message', 
     'Generic XML message structure for testing', 'XML',
     '<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xs:element name="Message">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="Header">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="MessageId" type="xs:string"/>
                            <xs:element name="Timestamp" type="xs:dateTime"/>
                            <xs:element name="Source" type="xs:string"/>
                            <xs:element name="Target" type="xs:string"/>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
                <xs:element name="Body">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:any minOccurs="0" maxOccurs="unbounded" processContents="lax"/>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
            </xs:sequence>
        </xs:complexType>
    </xs:element>
</xs:schema>', true),

    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'Order Message', 
     'Order message structure for e-commerce integration', 'XML',
     '<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xs:element name="Order">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="OrderNumber" type="xs:string"/>
                <xs:element name="OrderDate" type="xs:date"/>
                <xs:element name="Customer">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="CustomerId" type="xs:string"/>
                            <xs:element name="Name" type="xs:string"/>
                            <xs:element name="Email" type="xs:string"/>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
                <xs:element name="Items">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="Item" maxOccurs="unbounded">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name="SKU" type="xs:string"/>
                                        <xs:element name="Description" type="xs:string"/>
                                        <xs:element name="Quantity" type="xs:integer"/>
                                        <xs:element name="Price" type="xs:decimal"/>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
                <xs:element name="TotalAmount" type="xs:decimal"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>
</xs:schema>', true)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- Grant Permissions
-- ========================================

-- Grant necessary permissions to application user
-- Note: Replace 'integrix' with your actual database user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO integrix;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO integrix;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO integrix;