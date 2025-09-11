-- Add PINTEREST adapter type if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum 
        WHERE enumlabel = 'PINTEREST' 
        AND enumtypid = (
            SELECT oid FROM pg_type WHERE typname = 'adapter_type'
        )
    ) THEN
        ALTER TYPE adapter_type ADD VALUE 'PINTEREST' AFTER 'FACEBOOK_MESSENGER';
    END IF;
END$$;