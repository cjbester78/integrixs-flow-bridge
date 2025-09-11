-- Add LinkedIn Ads adapter type
ALTER TYPE adapter_type ADD VALUE IF NOT EXISTS 'LINKEDIN_ADS' AFTER 'LINKEDIN';

-- Add LinkedIn Ads to communication adapter types
UPDATE adapter_registry
SET supported_types = array_append(supported_types, 'LINKEDIN_ADS'::adapter_type)
WHERE adapter_category = 'SOCIAL_MEDIA'
  AND NOT ('LINKEDIN_ADS'::adapter_type = ANY(supported_types));