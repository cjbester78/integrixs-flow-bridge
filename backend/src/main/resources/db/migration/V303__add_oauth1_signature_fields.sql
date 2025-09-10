-- Add OAuth1 signature method and realm fields to external_authentications table

-- Add OAuth1 signature method field
ALTER TABLE external_authentications 
ADD COLUMN IF NOT EXISTS oauth1_signature_method VARCHAR(50) DEFAULT 'HMAC-SHA1';

-- Add OAuth1 realm field
ALTER TABLE external_authentications 
ADD COLUMN IF NOT EXISTS oauth1_realm VARCHAR(255);

-- Add comments
COMMENT ON COLUMN external_authentications.oauth1_signature_method IS 'OAuth 1.0 signature method: HMAC-SHA1, HMAC-SHA256, or PLAINTEXT';
COMMENT ON COLUMN external_authentications.oauth1_realm IS 'OAuth 1.0 realm parameter for the Authorization header';

-- Add check constraint for valid signature methods
ALTER TABLE external_authentications 
ADD CONSTRAINT chk_oauth1_signature_method 
CHECK (oauth1_signature_method IN ('HMAC-SHA1', 'HMAC-SHA256', 'PLAINTEXT') OR oauth1_signature_method IS NULL);