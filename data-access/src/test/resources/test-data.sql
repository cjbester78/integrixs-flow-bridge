-- Test data for integration tests
-- This file provides minimal test data needed for repository tests

-- Insert test roles
INSERT INTO roles (id, name, description, permissions) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'ADMINISTRATOR', 'System administrator with full access', '["*"]'),
    ('550e8400-e29b-41d4-a716-446655440002', 'DEVELOPER', 'Developer with flow and adapter management', '["flow:*", "adapter:*"]'),
    ('550e8400-e29b-41d4-a716-446655440003', 'INTEGRATOR', 'Integration specialist', '["flow:read", "flow:execute", "adapter:read"]'),
    ('550e8400-e29b-41d4-a716-446655440004', 'VIEWER', 'Read-only access', '["*:read"]');

-- Insert test system configurations
INSERT INTO system_configurations (id, key, value, description, category, data_type) VALUES
    ('550e8400-e29b-41d4-a716-446655440010', 'system.timezone', 'UTC', 'System timezone', 'SYSTEM', 'STRING'),
    ('550e8400-e29b-41d4-a716-446655440011', 'log.retention.days', '90', 'Log retention period in days', 'LOGGING', 'INTEGER'),
    ('550e8400-e29b-41d4-a716-446655440012', 'adapter.health.check.interval', '300', 'Health check interval in seconds', 'ADAPTER', 'INTEGER');

-- Note: We don't insert users here as tests will create their own test users
-- This avoids conflicts and ensures each test has clean data