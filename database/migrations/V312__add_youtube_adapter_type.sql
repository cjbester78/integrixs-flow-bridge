-- Add YouTube adapter type
ALTER TYPE adapter_type ADD VALUE IF NOT EXISTS 'YOUTUBE' AFTER 'LINKEDIN_ADS';

-- Add YouTube to communication adapter types
UPDATE adapter_registry
SET supported_types = array_append(supported_types, 'YOUTUBE'::adapter_type)
WHERE adapter_category = 'SOCIAL_MEDIA'
  AND NOT ('YOUTUBE'::adapter_type = ANY(supported_types));