-- Add TikTok Business adapter type
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM adapter_type WHERE name = 'TIKTOK_BUSINESS'
    ) THEN
        INSERT INTO adapter_type (name, description, category, icon, is_active)
        VALUES (
            'TIKTOK_BUSINESS',
            'TikTok Business API - Advertising campaigns, creative management, and analytics',
            'SOCIAL_MEDIA',
            'tiktok',
            true
        );
    END IF;
END $$;