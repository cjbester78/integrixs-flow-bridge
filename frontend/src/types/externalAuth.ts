// External Authentication Types

export enum AuthType {
 BASIC = 'BASIC',
 OAUTH1 = 'OAUTH1',
 OAUTH2 = 'OAUTH2',
 API_KEY = 'API_KEY'
}

export enum OAuth2GrantType {
 AUTHORIZATION_CODE = 'AUTHORIZATION_CODE',
 CLIENT_CREDENTIALS = 'CLIENT_CREDENTIALS',
 PASSWORD = 'PASSWORD',
 REFRESH_TOKEN = 'REFRESH_TOKEN'
}

export interface ExternalAuthConfig {
 id: string;
 name: string;
 description?: string;
 authType: AuthType;
 isActive: boolean;
 configuration?: string;
 createdAt: string;
 updatedAt: string;
 basicAuth?: BasicAuthDetails;
 oauth2?: OAuth2Details;
 apiKey?: ApiKeyDetails;
}

export interface BasicAuthDetails {
 username: string;
 realm?: string;
 isActive: boolean;
 lastUsedAt?: string;
 failedAttempts: number;
 lockedUntil?: string;
}

export interface OAuth2Details {
 clientId: string;
 authorizationUrl?: string;
 tokenUrl?: string;
 redirectUri?: string;
 scope?: string;
 grantType: OAuth2GrantType;
 usePkce: boolean;
}

export interface ApiKeyDetails {
 keyPrefix?: string;
 headerName: string;
 queryParamName?: string;
 isActive: boolean;
 expiresAt?: string;
 rateLimitPerHour?: number;
 allowedIps?: string[];
 usageCount: number;
 lastUsedAt?: string;
 lastUsedIp?: string;
}

export interface CreateExternalAuthRequest {
 name: string;
 description?: string;
 authType: AuthType;
 configuration?: string;
 basicAuth?: BasicAuthRequest;
 oauth2?: OAuth2Request;
 apiKey?: ApiKeyRequest;
}

export interface BasicAuthRequest {
 username: string;
 password: string;
 confirmPassword?: string;
 realm?: string;
}

export interface OAuth2Request {
 clientId: string;
 clientSecret: string;
 authorizationUrl?: string;
 tokenUrl?: string;
 redirectUri?: string;
 scope?: string;
 grantType: OAuth2GrantType;
 usePkce?: boolean;
}

export interface ApiKeyRequest {
 apiKey: string;
 keyPrefix?: string;
 headerName: string;
 queryParamName?: string;
 expiresAt?: string;
 rateLimitPerHour?: number;
 allowedIps?: string[];
}

export interface UpdateExternalAuthRequest {
 name?: string;
 description?: string;
 isActive?: boolean;
 configuration?: string;
 basicAuth?: Partial<BasicAuthRequest>;
 oauth2?: Partial<OAuth2Request>;
 apiKey?: Partial<ApiKeyRequest>;
}

export interface AuthAttemptLog {
 id: string;
 authConfigId: string;
 authType: AuthType;
 username?: string;
 apiKeyPrefix?: string;
 ipAddress?: string;
 userAgent?: string;
 success: boolean;
 failureReason?: string;
 flowId?: string;
 createdAt: string;
}