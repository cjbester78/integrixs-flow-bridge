-- Add LinkedIn adapter type to adapter_types table
INSERT INTO adapter_types (adapter_type, description, icon, created_at, updated_at)
VALUES 
    ('LINKEDIN', 'LinkedIn API for professional networking and content management', 'mdi-linkedin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);