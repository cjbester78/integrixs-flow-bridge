-- Add Twitter adapter type to adapter_types table
INSERT INTO adapter_types (adapter_type, description, icon, created_at, updated_at)
VALUES 
    ('TWITTER', 'Twitter/X API v2 adapter for tweet management and analytics', 'mdi-twitter', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);