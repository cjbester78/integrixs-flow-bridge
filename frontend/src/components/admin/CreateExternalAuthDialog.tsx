import { useState } from 'react';
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
 CreateExternalAuthRequest,
 BasicAuthRequest,
 OAuth2Request,
 ApiKeyRequest
} from '@/types/externalAuth';
import { Shield, Key, UserCheck, ShieldAlert } from 'lucide-react';
import { isApiResponse } from '@/lib/api-response-utils';
import { logger, LogCategory } from '@/lib/logger';

interface CreateExternalAuthDialogProps {
 open: boolean;
 onOpenChange: (open: boolean) => void;
 onCreated: () => void;
}

export function CreateExternalAuthDialog({ open, onOpenChange, onCreated }: CreateExternalAuthDialogProps) {
 const { toast } = useToast();
 const [isLoading, setIsLoading] = useState(false);
 const [authType, setAuthType] = useState<AuthType>(AuthType.BASIC);

 const [formData, setFormData] = useState<CreateExternalAuthRequest>({
 name: '',
 description: '',
 authType: AuthType.BASIC,
 basicAuth: {
 username: '',
 password: '',
 confirmPassword: '',
 realm: ''
 },
 oauth2: {
 clientId: '',
 clientSecret: '',
 grantType: OAuth2GrantType.CLIENT_CREDENTIALS
 },
 apiKey: {
 apiKey: '',
 headerName: 'X-API-Key',
 queryParamName: '',
 keyPrefix: '',
 rateLimitPerHour: undefined,
 allowedIps: []
 }
 });


 const [allowedIpsInput, setAllowedIpsInput] = useState('');

 const handleSubmit = async (e: React.FormEvent) => {
 e.preventDefault();
 setIsLoading(true);

 try {
 // Prepare the request based on auth type
 const request: CreateExternalAuthRequest = {
 name: formData.name,
 description: formData.description,
 authType: authType
 };

 // Add type-specific data
 if (authType === AuthType.BASIC && formData.basicAuth) {
 if (formData.basicAuth.password !== formData.basicAuth.confirmPassword) {
 toast({
 title: "Error",
 description: "Passwords do not match",
 variant: "destructive"
 });
 setIsLoading(false);
 return;
 }
 request.basicAuth = {
 username: formData.basicAuth.username,
 password: formData.basicAuth.password,
 realm: formData.basicAuth.realm
 }
} else if (authType === AuthType.OAUTH2 && formData.oauth2) {
 request.oauth2 = formData.oauth2;
 } else if (authType === AuthType.API_KEY && formData.apiKey) {
 // Convert allowed IPs from string to array
 const allowedIps = allowedIpsInput
 .split('\n')
 .map(ip => ip.trim())
 .filter(ip => ip.length > 0);

 request.apiKey = {
 ...formData.apiKey,
 allowedIps: allowedIps.length > 0 ? allowedIps : undefined
 }
}

 const response = await externalAuthService.createAuthConfig(request);
 if (isApiResponse(response)) {
 if (response.success) {
 toast({
 title: "Success",
 description: "Authentication configuration created successfully"
 });
 onCreated();
 onOpenChange(false);
 resetForm();
 } else {
 throw new Error(response.message || 'Failed to create configuration');
 }
 } else {
 // Handle direct response
 toast({
 title: "Success",
 description: "Authentication configuration created successfully"
 });
 onCreated();
 onOpenChange(false);
 resetForm();}
} catch (error) {
 logger.error(LogCategory.AUTH, 'Error creating auth config', { error: error });
 toast({
 title: "Error",
 description: error instanceof Error ? error.message : "Failed to create authentication configuration",
 variant: "destructive"
 });
 } finally {
 setIsLoading(false);
 }
 };

 const resetForm = () => {
 setAuthType(AuthType.BASIC);
 setFormData({
 name: '',
 description: '',
 authType: AuthType.BASIC,
 basicAuth: {
 username: '',
 password: '',
 confirmPassword: '',
 realm: ''
 },
 oauth2: {
 clientId: '',
 clientSecret: '',
 grantType: OAuth2GrantType.CLIENT_CREDENTIALS
 },
 apiKey: {
 apiKey: '',
 headerName: 'X-API-Key',
 queryParamName: '',
 keyPrefix: '',
 rateLimitPerHour: undefined,
 allowedIps: []
 }
 });
 setAllowedIpsInput('');
 };


 return (
 <Dialog open={open} onOpenChange={onOpenChange}>
 <DialogContent className="max-w-2xl">
 <form onSubmit={handleSubmit}>
 <DialogHeader>
 <DialogTitle>Create External Authentication</DialogTitle>
 <DialogDescription>
 Configure external authentication for HTTP/S adapters
 </DialogDescription>
 </DialogHeader>

 <div className="space-y-4 py-4">
 {/* Basic Information */}
 <div className="space-y-2">
 <Label htmlFor="name">Name</Label>
 <Input
 id="name"
 placeholder="e.g., Production API Key"
 value={formData.name}
 onChange={(e) => setFormData({ ...formData, name: e.target.value })}
 required
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="description">Description</Label>
 <Textarea
 id="description"
 placeholder="Optional description of this authentication configuration"
 value={formData.description}
 onChange={(e) => setFormData({ ...formData, description: e.target.value })}
 rows={2}
 />
 </div>

 <div className="space-y-2">
 <Label htmlFor="authType">Authentication Type</Label>
 <Select
 value={authType}
 onValueChange={(value: AuthType) => setAuthType(value)}
 >
 <SelectTrigger>
 <SelectValue />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value={AuthType.BASIC}>
 <div className="flex items-center gap-2">
 <UserCheck className="h-4 w-4" />
 Basic Authentication
 </div>
 </SelectItem>
 <SelectItem value={AuthType.API_KEY}>
 <div className="flex items-center gap-2">
 <Key className="h-4 w-4" />
 API Key
 </div>
 </SelectItem>
 <SelectItem value={AuthType.OAUTH2}>
 <div className="flex items-center gap-2">
 <Shield className="h-4 w-4" />
 OAuth 2.0
 </div>
 </SelectItem>
 </SelectContent>
 </Select>
 </div>

 {/* Type-specific configuration */}
 <div className="pt-4">
 {authType === AuthType.BASIC && formData.basicAuth && (
 <div className="space-y-4">
 <h3 className="text-sm font-medium">Basic Authentication Details</h3>
 <div className="space-y-2">
 <Label htmlFor="username">Username</Label>
 <Input
 id="username"
 value={formData.basicAuth.username}
 onChange={(e) => setFormData({
 ...formData,
 basicAuth: { ...formData.basicAuth!, username: e.target.value }
 })}
 required
 />
 </div>
 <PasswordConfirmation
 name="password"
 label="Password"
 value={formData.basicAuth.password}
 onValueChange={(value) => setFormData({
 ...formData,
 basicAuth: { ...formData.basicAuth!, password: value }
 })}
 required
 />
 <div className="space-y-2">
 <Label htmlFor="realm">Realm (Optional)</Label>
 <Input
 id="realm"
 placeholder="Authentication realm"
 value={formData.basicAuth.realm}
 onChange={(e) => setFormData({
 ...formData,
 basicAuth: { ...formData.basicAuth!, realm: e.target.value }
 })}
 />
 </div>
 </div>
 )}

 {authType === AuthType.OAUTH2 && formData.oauth2 && (
 <div className="space-y-4">
 <h3 className="text-sm font-medium">OAuth 2.0 Details</h3>
 <div className="space-y-2">
 <Label htmlFor="clientId">Client ID</Label>
 <Input
 id="clientId"
 value={formData.oauth2.clientId}
 onChange={(e) => setFormData({
 ...formData,
 oauth2: { ...formData.oauth2!, clientId: e.target.value }
 })}
 required
 />
 </div>
 <PasswordConfirmation
 name="clientSecret"
 label="Client Secret"
 value={formData.oauth2.clientSecret}
 onValueChange={(value) => setFormData({
 ...formData,
 oauth2: { ...formData.oauth2!, clientSecret: value }
 })}
 showConfirmation={false}
 required
 />
 <div className="space-y-2">
 <Label htmlFor="grantType">Grant Type</Label>
 <Select
 value={formData.oauth2.grantType}
 onValueChange={(value: OAuth2GrantType) => setFormData({
 ...formData,
 oauth2: { ...formData.oauth2!, grantType: value }
 })}
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
 {formData.oauth2.grantType === OAuth2GrantType.AUTHORIZATION_CODE && (
 <>
 <div className="space-y-2">
 <Label htmlFor="authorizationUrl">Authorization URL</Label>
 <Input
 id="authorizationUrl"
 type="url"
 placeholder="https://auth.example.com/authorize"
 value={formData.oauth2.authorizationUrl || ''}
 onChange={(e) => setFormData({
 ...formData,
 oauth2: { ...formData.oauth2!, authorizationUrl: e.target.value }
 })}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="redirectUri">Redirect URI</Label>
 <Input
 id="redirectUri"
 type="url"
 placeholder="https://your-app.com/callback"
 value={formData.oauth2.redirectUri || ''}
 onChange={(e) => setFormData({
 ...formData,
 oauth2: { ...formData.oauth2!, redirectUri: e.target.value }
 })}
 />
 </div>
 </>
 )}
 <div className="space-y-2">
 <Label htmlFor="tokenUrl">Token URL</Label>
 <Input
 id="tokenUrl"
 type="url"
 placeholder="https://auth.example.com/token"
 value={formData.oauth2.tokenUrl || ''}
 onChange={(e) => setFormData({
 ...formData,
 oauth2: { ...formData.oauth2!, tokenUrl: e.target.value }
 })}
 required
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="scope">Scope (Optional)</Label>
 <Input
 id="scope"
 placeholder="read write admin"
 value={formData.oauth2.scope || ''}
 onChange={(e) => setFormData({
 ...formData,
 oauth2: { ...formData.oauth2!, scope: e.target.value }
 })}
 />
 </div>
 <div className="flex items-center space-x-2">
 <Switch
 id="usePkce"
 checked={formData.oauth2.usePkce || false}
 onCheckedChange={(checked) => setFormData({
 ...formData,
 oauth2: { ...formData.oauth2!, usePkce: checked }
 })}
 />
 <Label htmlFor="usePkce">Use PKCE (Proof Key for Code Exchange)</Label>
 </div>
 </div>
 )}

 {authType === AuthType.API_KEY && formData.apiKey && (
 <div className="space-y-4">
 <h3 className="text-sm font-medium">API Key Details</h3>
 <PasswordConfirmation
 name="apiKey"
 label="API Key"
 value={formData.apiKey.apiKey}
 onValueChange={(value) => setFormData({
 ...formData,
 apiKey: { ...formData.apiKey!, apiKey: value }
 })}
 showConfirmation={false}
 required
 />
 <div className="space-y-2">
 <Label htmlFor="headerName">Header Name</Label>
 <Input
 id="headerName"
 placeholder="X-API-Key"
 value={formData.apiKey.headerName}
 onChange={(e) => setFormData({
 ...formData,
 apiKey: { ...formData.apiKey!, headerName: e.target.value }
 })}
 required
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="keyPrefix">Key Prefix (Optional)</Label>
 <Input
 id="keyPrefix"
 placeholder="Bearer"
 value={formData.apiKey.keyPrefix || ''}
 onChange={(e) => setFormData({
 ...formData,
 apiKey: { ...formData.apiKey!, keyPrefix: e.target.value }
 })}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="queryParamName">Query Parameter Name (Optional)</Label>
 <Input
 id="queryParamName"
 placeholder="api_key"
 value={formData.apiKey.queryParamName || ''}
 onChange={(e) => setFormData({
 ...formData,
 apiKey: { ...formData.apiKey!, queryParamName: e.target.value }
 })}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="rateLimitPerHour">Rate Limit per Hour (Optional)</Label>
 <Input
 id="rateLimitPerHour"
 type="number"
 placeholder="1000"
 value={formData.apiKey.rateLimitPerHour || ''}
 onChange={(e) => setFormData({
 ...formData,
 apiKey: {
 ...formData.apiKey!,
 rateLimitPerHour: e.target.value ? parseInt(e.target.value) : undefined
 }
 })}
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="allowedIps">Allowed IPs (Optional)</Label>
 <Textarea
 id="allowedIps"
 placeholder="One IP address per line&#10;192.168.1.0/24&#10;10.0.0.1"
 value={allowedIpsInput}
 onChange={(e) => setAllowedIpsInput(e.target.value)}
 rows={3}
 />
 <p className="text-xs text-muted-foreground">
 Enter IP addresses or CIDR ranges, one per line
 </p>
 </div>
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
 {isLoading ? 'Creating...' : 'Create Configuration'}
 </Button>
 </DialogFooter>
 </form>
 </DialogContent>
 </Dialog>
 );
}
