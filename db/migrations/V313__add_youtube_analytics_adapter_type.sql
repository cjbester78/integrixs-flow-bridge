-- Add YouTube Analytics adapter type
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM adapter_type WHERE name = 'YOUTUBE_ANALYTICS'
    ) THEN
        INSERT INTO adapter_type (name, description, category, icon, is_active)
        VALUES (
            'YOUTUBE_ANALYTICS',
            'YouTube Analytics API - Comprehensive analytics and reporting for YouTube channels and videos',
            'SOCIAL_MEDIA',
            'youtube',
            true
        );
    END IF;
END $$;