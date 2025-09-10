-- Add TikTok Content adapter type
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM adapter_type WHERE name = 'TIKTOK_CONTENT'
    ) THEN
        INSERT INTO adapter_type (name, description, category, icon, is_active)
        VALUES (
            'TIKTOK_CONTENT',
            'TikTok Content API - Video publishing, engagement, trends, and creator tools',
            'SOCIAL_MEDIA',
            'tiktok',
            true
        );
    END IF;
END $$;