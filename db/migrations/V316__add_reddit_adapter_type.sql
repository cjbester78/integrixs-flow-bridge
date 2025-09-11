-- Add REDDIT adapter type if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum 
        WHERE enumlabel = 'REDDIT' 
        AND enumtypid = (
            SELECT oid FROM pg_type WHERE typname = 'adapter_type'
        )
    ) THEN
        ALTER TYPE adapter_type ADD VALUE 'REDDIT' AFTER 'PINTEREST';
    END IF;
END$$;