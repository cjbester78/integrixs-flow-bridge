-- Add Twitter Ads adapter type to adapter_types table
INSERT INTO adapter_types (adapter_type, description, icon, created_at, updated_at)
VALUES 
    ('TWITTER_ADS', 'Twitter/X Ads API for advertising campaign management', 'mdi-twitter', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);