-- Add FACEBOOK_MESSENGER adapter type if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum 
        WHERE enumlabel = 'FACEBOOK_MESSENGER' 
        AND enumtypid = (
            SELECT oid FROM pg_type WHERE typname = 'adapter_type'
        )
    ) THEN
        ALTER TYPE adapter_type ADD VALUE 'FACEBOOK_MESSENGER' AFTER 'TIKTOK_CONTENT';
    END IF;
END$$;