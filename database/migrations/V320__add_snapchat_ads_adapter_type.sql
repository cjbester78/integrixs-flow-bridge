-- Add Snapchat Ads adapter type
DO $$
BEGIN
    -- Check if SNAPCHAT_ADS already exists in adapter_type enum
    IF NOT EXISTS (
        SELECT 1
        FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'adapter_type'
        AND e.enumlabel = 'SNAPCHAT_ADS'
    ) THEN
        -- Add the new value to the enum
        ALTER TYPE adapter_type ADD VALUE 'SNAPCHAT_ADS';
    END IF;
END $$;