import { useState, useEffect } from 'react';
import {
 Dialog,
 DialogContent,
 DialogDescription,
 DialogFooter,
 DialogHeader,
 DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { PasswordConfirmation } from '@/components/ui/password-confirmation';
import { Switch } from '@/components/ui/switch';
import { useToast } from '@/hooks/use-toast';
import { externalAuthService } from '@/services/externalAuthService';
import {
 AuthType,
 OAuth2GrantType,
 ExternalAuthConfig,
 UpdateExternalAuthRequest,
 BasicAuthRequest,
 OAuth2Request,
 ApiKeyRequest
} from '@/types/externalAuth';
import { isApiResponse } from '@/lib/api-response-utils';
import { logger, LogCategory } from '@/lib/logger';

interface EditExternalAuthDialogProps {
 config: ExternalAuthConfig;
 open: boolean;
 onOpenChange: (open: boolean) => void;
 onUpdated: () => void;
}

export function EditExternalAuthDialog({ config, open, onOpenChange, onUpdated }: EditExternalAuthDialogProps) {
 const { toast } = useToast();
 const [isLoading, setIsLoading] = useState(false);

 const [formData, setFormData] = useState<UpdateExternalAuthRequest>({
 name: config.name,
 description: config.description,
 isActive: config.isActive
 });


 const [basicAuth, setBasicAuth] = useState<Partial<BasicAuthRequest>>({
 username: config.basicAuth?.username || '',
 password: '',
 realm: config.basicAuth?.realm || ''
 });


 const [oauth2, setOauth2] = useState<Partial<OAuth2Request>>({
 clientId: config.oauth2?.clientId || '',
 clientSecret: '',
 authorizationUrl: config.oauth2?.authorizationUrl || '',
 tokenUrl: config.oauth2?.tokenUrl || '',
 redirectUri: config.oauth2?.redirectUri || '',
 scope: config.oauth2?.scope || '',
 grantType: config.oauth2?.grantType || OAuth2GrantType.CLIENT_CREDENTIALS,
 usePkce: config.oauth2?.usePkce || false
 });


 const [apiKey, setApiKey] = useState<Partial<ApiKeyRequest>>({
 apiKey: '',
 keyPrefix: config.apiKey?.keyPrefix || '',
 headerName: config.apiKey?.headerName || 'X-API-Key',
 queryParamName: config.apiKey?.queryParamName || '',
 rateLimitPerHour: config.apiKey?.rateLimitPerHour,
 allowedIps: config.apiKey?.allowedIps || []
 });


 const [allowedIpsInput, setAllowedIpsInput] = useState(
 config.apiKey?.allowedIps?.join('\n') || ''
 );

 useEffect(() => {
 // Reset form when config changes
 setFormData({
 name: config.name,
 description: config.description,
 isActive: config.isActive
 });

 if (config.authType === AuthType.BASIC && config.basicAuth) {
 setBasicAuth({
 username: config.basicAuth.username,
 password: '',
 realm: config.basicAuth.realm || ''
 });
 }

 if (config.authType === AuthType.OAUTH2 && config.oauth2) {
 setOauth2({
 clientId: config.oauth2.clientId,
 clientSecret: '',
 authorizationUrl: config.oauth2.authorizationUrl || '',
 tokenUrl: config.oauth2.tokenUrl || '',
 redirectUri: config.oauth2.redirectUri || '',
 scope: config.oauth2.scope || '',
 grantType: config.oauth2.grantType,
 usePkce: config.oauth2.usePkce
 });
 }

 if (config.authType === AuthType.API_KEY && config.apiKey) {
 setApiKey({
 apiKey: '',
 keyPrefix: config.apiKey.keyPrefix || '',
 headerName: config.apiKey.headerName,
 queryParamName: config.apiKey.queryParamName || '',
 rateLimitPerHour: config.apiKey.rateLimitPerHour,
 allowedIps: config.apiKey.allowedIps || []
 });
 setAllowedIpsInput(config.apiKey.allowedIps?.join('\n') || '');
 }
 }, [config]);

 const handleSubmit = async (e: React.FormEvent) => {
 e.preventDefault();
 setIsLoading(true);

 try {
 const request: UpdateExternalAuthRequest = {
 ...formData
 };

 // Add type-specific data only if fields have been filled
 if (config.authType === AuthType.BASIC) {
 const basicAuthUpdate: any = {};
 if (basicAuth.username) basicAuthUpdate.username = basicAuth.username;
 if (basicAuth.password) basicAuthUpdate.password = basicAuth.password;
 if (basicAuth.realm !== undefined) basicAuthUpdate.realm = basicAuth.realm;

 if (Object.keys(basicAuthUpdate).length > 0) {
 request.basicAuth = basicAuthUpdate;
 }
 } else if (config.authType === AuthType.OAUTH2) {
 const oauth2Update: any = {};
 if (oauth2.clientId) oauth2Update.clientId = oauth2.clientId;
 if (oauth2.clientSecret) oauth2Update.clientSecret = oauth2.clientSecret;
 if (oauth2.authorizationUrl !== undefined) oauth2Update.authorizationUrl = oauth2.authorizationUrl;
 if (oauth2.tokenUrl !== undefined) oauth2Update.tokenUrl = oauth2.tokenUrl;
 if (oauth2.redirectUri !== undefined) oauth2Update.redirectUri = oauth2.redirectUri;
 if (oauth2.scope !== undefined) oauth2Update.scope = oauth2.scope;
 if (oauth2.grantType) oauth2Update.grantType = oauth2.grantType;
 if (oauth2.usePkce !== undefined) oauth2Update.usePkce = oauth2.usePkce;

 if (Object.keys(oauth2Update).length > 0) {
 request.oauth2 = oauth2Update;
 }
 } else if (config.authType === AuthType.API_KEY) {
 const apiKeyUpdate: any = {};
 if (apiKey.apiKey) apiKeyUpdate.apiKey = apiKey.apiKey;
 if (apiKey.keyPrefix !== undefined) apiKeyUpdate.keyPrefix = apiKey.keyPrefix;
 if (apiKey.headerName) apiKeyUpdate.headerName = apiKey.headerName;
 if (apiKey.queryParamName !== undefined) apiKeyUpdate.queryParamName = apiKey.queryParamName;
 if (apiKey.rateLimitPerHour !== undefined) apiKeyUpdate.rateLimitPerHour = apiKey.rateLimitPerHour;

 // Convert allowed IPs from string to array
 const allowedIps = allowedIpsInput
 .split('\n')
 .map(ip => ip.trim())
 .filter(ip => ip.length > 0);

 if (allowedIps.length > 0 || allowedIpsInput === '') {
 apiKeyUpdate.allowedIps = allowedIps;
 }

 if (Object.keys(apiKeyUpdate).length > 0) {
 request.apiKey = apiKeyUpdate;
 }
 }

 const response = await externalAuthService.updateAuthConfig(config.id, request);
;
 if (isApiResponse(response)) {
 if (response.success) {
 toast({
 title: "Success",
 description: "Authentication configuration updated successfully"
 });
 onUpdated();
 onOpenChange(false);
 } else {
 throw new Error(response.message || 'Failed to update configuration');
 }
 } else {
 // Handle direct response
 toast({
 title: "Success",
 description: "Authentication configuration updated successfully"
 });
 onUpdated();
 onOpenChange(false);}
} catch (error) {
 logger.error(LogCategory.AUTH, 'Error updating auth config', { error: error });
 toast({
 title: "Error",
 description: error instanceof Error ? error.message : "Failed to update authentication configuration",
 variant: "destructive"
 });
 } finally {
 setIsLoading(false);
 }
 };

 return (
 <Dialog open={open} onOpenChange={onOpenChange}>
 <DialogContent className="max-w-2xl">
 <form onSubmit={handleSubmit}>
 <DialogHeader>
 <DialogTitle>Edit External Authentication</DialogTitle>
 <DialogDescription>
 Update authentication configuration for {config.name}
 </DialogDescription>
 </DialogHeader>

 <div className="space-y-4 py-4">
 {/* Basic Information */}
 <div className="space-y-2">
 <Label htmlFor="name">Name</Label>
 <Input
 id="name"
 value={formData.name}
 onChange={(e) => setFormData({ ...formData, name: e.target.value })}
 required
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="description">Description</Label>
 <Textarea
 id="description"
 value={formData.description || ''}
 onChange={(e) => setFormData({ ...formData, description: e.target.value })}
 rows={2}
 />
 </div>

 <div className="flex items-center space-x-2">
 <Switch
 id="isActive"
 checked={formData.isActive}
 onCheckedChange={(checked) => setFormData({ ...formData, isActive: checked })}
 />
 <Label htmlFor="isActive">Active</Label>
 </div>

 {/* Type-specific configuration */}
 <div className="pt-4">
 <h3 className="text-sm font-medium mb-4">
 {config.authType} Authentication Details
 </h3>

 {config.authType === AuthType.BASIC && (
 <div className="space-y-4">
 <div className="space-y-2">
 <Label htmlFor="username">Username</Label>
 <Input
 id="username"
 value={basicAuth.username}
 onChange={(e) => setBasicAuth({ ...basicAuth, username: e.target.value })}
 />
 </div>
 <PasswordConfirmation
 name="password"
 label="New Password (leave empty to keep current)"
 value={basicAuth.password || ''}
 onValueChange={(value) => setBasicAuth({ ...basicAuth, password: value })}
 showConfirmation={false}
 />
 <div className="space-y-2">
 <Label htmlFor="realm">Realm</Label>
 <Input
 id="realm"
 value={basicAuth.realm || ''}
 onChange={(e) => setBasicAuth({ ...basicAuth, realm: e.target.value })}
 />
 </div>
 </div>
 )}

 {config.authType === AuthType.OAUTH2 && (
 <div className="space-y-4">
 <div className="space-y-2">
 <Label htmlFor="clientId">Client ID</Label>
 <Input
 id="clientId"
 value={oauth2.clientId}
 onChange={(e) => setOauth2({ ...oauth2, clientId: e.target.value })}
 />
 </div>
 <PasswordConfirmation
 name="clientSecret"
 label="Client Secret (leave empty to keep current)"
 value={oauth2.clientSecret || ''}
 onValueChange={(value) => setOauth2({ ...oauth2, clientSecret: value })}
 showConfirmation={false}
 />
 <div className="space-y-2">
 <Label htmlFor="grantType">Grant Type</Label>
 <Select
 value={oauth2.grantType}
 onValueChange={(value: OAuth2GrantType) => setOauth2({ ...oauth2, grantType: value })}
 >
 <SelectTrigger>
 <SelectValue />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value={OAuth2GrantType.CLIENT_CREDENTIALS}>Client Credentials</SelectItem>
 <SelectItem value={OAuth2GrantType.AUTHORIZATION_CODE}>Authorization Code</SelectItem>
 <SelectItem value={OAuth2GrantType.PASSWORD}>Password</SelectItem>
 <SelectItem value={OAuth2GrantType.REFRESH_TOKEN}>Refresh Token</SelectItem>
 </SelectContent>
 </Select>
 </div>
 {oauth2.grantType === OAuth2GrantType.AUTHORIZATION_CODE && (
 <>
 <div className="space-y-2">
 <Label htmlFor="authorizationUrl">Authorization URL</Label>
 <Input
 id="authorizationUrl"
 type="url"
 value={oauth2.authorizationUrl || ''}
 onChange={(e) => setOauth2({ ...oauth2, authorizationUrl: e.target.value })}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="redirectUri">Redirect URI</Label>
 <Input
 id="redirectUri"
 type="url"
 value={oauth2.redirectUri || ''}
 onChange={(e) => setOauth2({ ...oauth2, redirectUri: e.target.value })}
 />
 </div>
 </>
 )}
 <div className="space-y-2">
 <Label htmlFor="tokenUrl">Token URL</Label>
 <Input
 id="tokenUrl"
 type="url"
 value={oauth2.tokenUrl || ''}
 onChange={(e) => setOauth2({ ...oauth2, tokenUrl: e.target.value })}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="scope">Scope</Label>
 <Input
 id="scope"
 value={oauth2.scope || ''}
 onChange={(e) => setOauth2({ ...oauth2, scope: e.target.value })}
 />
 </div>
 <div className="flex items-center space-x-2">
 <Switch
 id="usePkce"
 checked={oauth2.usePkce || false}
 onCheckedChange={(checked) => setOauth2({ ...oauth2, usePkce: checked })}
 />
 <Label htmlFor="usePkce">Use PKCE</Label>
 </div>
 </div>
 )}

 {config.authType === AuthType.API_KEY && (
 <div className="space-y-4">
 <PasswordConfirmation
 name="apiKey"
 label="API Key (leave empty to keep current)"
 value={apiKey.apiKey || ''}
 onValueChange={(value) => setApiKey({ ...apiKey, apiKey: value })}
 showConfirmation={false}
 />
 <div className="space-y-2">
 <Label htmlFor="headerName">Header Name</Label>
 <Input
 id="headerName"
 value={apiKey.headerName}
 onChange={(e) => setApiKey({ ...apiKey, headerName: e.target.value })}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="keyPrefix">Key Prefix</Label>
 <Input
 id="keyPrefix"
 value={apiKey.keyPrefix || ''}
 onChange={(e) => setApiKey({ ...apiKey, keyPrefix: e.target.value })}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="queryParamName">Query Parameter Name</Label>
 <Input
 id="queryParamName"
 value={apiKey.queryParamName || ''}
 onChange={(e) => setApiKey({ ...apiKey, queryParamName: e.target.value })}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="rateLimitPerHour">Rate Limit per Hour</Label>
 <Input
 id="rateLimitPerHour"
 type="number"
 value={apiKey.rateLimitPerHour || ''}
 onChange={(e) => setApiKey({
 ...apiKey,
 rateLimitPerHour: e.target.value ? parseInt(e.target.value) : undefined
 })}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="allowedIps">Allowed IPs</Label>
 <Textarea
 id="allowedIps"
 value={allowedIpsInput}
 onChange={(e) => setAllowedIpsInput(e.target.value)}
 rows={3}
 />
 <p className="text-xs text-muted-foreground">
 Enter IP addresses or CIDR ranges, one per line
 </p>
 </div>
 {config.apiKey && (
 <div className="space-y-2 pt-2 border-t">
 <p className="text-sm text-muted-foreground">
 Usage: {config.apiKey.usageCount} requests;
 </p>
 {config.apiKey.lastUsedAt && (
 <p className="text-sm text-muted-foreground">
 Last used: {new Date(config.apiKey.lastUsedAt).toLocaleString()}
 {config.apiKey.lastUsedIp && ` from ${config.apiKey.lastUsedIp}`}
 </p>
 )}
 </div>
 )}
 </div>
 )}
 </div>
 </div>

 <DialogFooter>
 <Button
 type="button"
 variant="outline"
 onClick={() => onOpenChange(false)}
 disabled={isLoading}
 >
 Cancel
 </Button>
 <Button type="submit" disabled={isLoading}>
 {isLoading ? 'Updating...' : 'Update Configuration'}
 </Button>
 </DialogFooter>
 </form>
 </DialogContent>
 </Dialog>
 );
}