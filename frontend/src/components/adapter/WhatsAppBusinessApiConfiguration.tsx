import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { 
  MessageCircle,
  Phone,
  AlertCircle, 
  CheckCircle, 
  Settings,
  FileText,
  Image,
  Users,
  ShoppingBag,
  QrCode,
  Tag,
  Zap,
  Database,
  Shield,
  Webhook
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface WhatsAppBusinessApiConfigurationProps {
  config: any;
  onChange: (config: any) => void;
  onTest?: () => void;
  mode?: 'create' | 'edit' | 'view';
}

export const WhatsAppBusinessApiConfiguration: React.FC<WhatsAppBusinessApiConfigurationProps> = ({
  config,
  onChange,
  onTest,
  mode = 'create'
}) => {
  const { toast } = useToast();
  const [isConnecting, setIsConnecting] = useState(false);
  const [activeTab, setActiveTab] = useState('basic');

  // Features state
  const [features, setFeatures] = useState(config.features || {
    enableTextMessaging: true,
    enableMediaMessaging: true,
    enableTemplateMessaging: true,
    enableInteractiveMessages: true,
    enableStatusUpdates: true,
    enableGroupMessaging: true,
    enableContactManagement: true,
    enableBusinessProfile: true,
    enableCatalogs: true,
    enableQRCodes: true,
    enableLabels: true,
    enableFlows: true
  });

  // Settings state
  const [settings, setSettings] = useState(config.settings || {
    maxTextLength: 4096,
    maxCaptionLength: 1024,
    maxButtonsPerMessage: 3,
    maxSectionsPerList: 10,
    maxItemsPerSection: 10,
    sessionTimeoutMinutes: 1440, // 24 hours
    autoDownloadMedia: true,
    saveMediaLocally: false,
    mediaStoragePath: '/tmp/whatsapp-media'
  });

  // OAuth handler - uses Facebook login for WhatsApp Business
  const handleOAuthConnect = () => {
    setIsConnecting(true);
    
    const clientId = config.clientId || config.appId;
    const redirectUri = encodeURIComponent(window.location.origin + '/oauth/facebook/callback');
    const scope = [
      'whatsapp_business_management',
      'whatsapp_business_messaging',
      'business_management',
      'pages_show_list',
      'pages_read_engagement',
      'pages_manage_metadata'
    ].join(',');
    
    const state = btoa(JSON.stringify({
      adapterId: config.id,
      adapterType: 'whatsapp',
      timestamp: Date.now()
    }));
    
    const authUrl = `https://www.facebook.com/v18.0/dialog/oauth?` +
      `client_id=${clientId}` +
      `&redirect_uri=${redirectUri}` +
      `&scope=${scope}` +
      `&state=${state}` +
      `&response_type=code`;
    
    const authWindow = window.open(authUrl, 'WhatsAppAuth', 'width=600,height=700');
    
    const checkInterval = setInterval(() => {
      if (authWindow?.closed) {
        clearInterval(checkInterval);
        setIsConnecting(false);
      }
    }, 1000);
  };

  // Update features and settings in config
  useEffect(() => {
    onChange({
      ...config,
      features,
      settings
    });
  }, [features, settings, config, onChange]);

  const isViewMode = mode === 'view';

  return (
    <div className="space-y-6">
      {/* Connection Status */}
      {config.accessToken && (
        <Alert className="border-green-200 bg-green-50 dark:bg-green-900/20">
          <CheckCircle className="h-4 w-4 text-green-600" />
          <AlertDescription className="text-green-800 dark:text-green-200">
            Connected to WhatsApp Business Account: {config.phoneNumberId}
          </AlertDescription>
        </Alert>
      )}

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="basic">Basic Setup</TabsTrigger>
          <TabsTrigger value="features">Features</TabsTrigger>
          <TabsTrigger value="messaging">Messaging</TabsTrigger>
          <TabsTrigger value="webhook">Webhooks</TabsTrigger>
          <TabsTrigger value="advanced">Advanced</TabsTrigger>
        </TabsList>

        {/* Basic Setup Tab */}
        <TabsContent value="basic" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MessageCircle className="h-5 w-5 text-green-600" />
                WhatsApp Business Configuration
              </CardTitle>
              <CardDescription>
                Configure your WhatsApp Business account credentials
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  WhatsApp Business API requires a Facebook App and WhatsApp Business Account
                </AlertDescription>
              </Alert>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="appId">Facebook App ID</Label>
                  <Input
                    id="appId"
                    value={config.appId || ''}
                    onChange={(e) => onChange({ ...config, appId: e.target.value })}
                    placeholder="Your Facebook App ID"
                    disabled={isViewMode}
                  />
                </div>
                <div>
                  <Label htmlFor="appSecret">Facebook App Secret</Label>
                  <Input
                    id="appSecret"
                    type="password"
                    value={config.appSecret || ''}
                    onChange={(e) => onChange({ ...config, appSecret: e.target.value })}
                    placeholder="Your Facebook App Secret"
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="phoneNumberId">Phone Number ID</Label>
                  <Input
                    id="phoneNumberId"
                    value={config.phoneNumberId || ''}
                    onChange={(e) => onChange({ ...config, phoneNumberId: e.target.value })}
                    placeholder="WhatsApp Phone Number ID"
                    disabled={isViewMode}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    From WhatsApp Business Manager
                  </p>
                </div>
                <div>
                  <Label htmlFor="businessAccountId">Business Account ID</Label>
                  <Input
                    id="businessAccountId"
                    value={config.businessAccountId || ''}
                    onChange={(e) => onChange({ ...config, businessAccountId: e.target.value })}
                    placeholder="WhatsApp Business Account ID"
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <div>
                <Label htmlFor="systemUserAccessToken">System User Access Token (Optional)</Label>
                <Input
                  id="systemUserAccessToken"
                  type="password"
                  value={config.systemUserAccessToken || ''}
                  onChange={(e) => onChange({ ...config, systemUserAccessToken: e.target.value })}
                  placeholder="For production use"
                  disabled={isViewMode}
                />
                <p className="text-xs text-muted-foreground mt-1">
                  Recommended for production environments
                </p>
              </div>

              {/* OAuth Connection */}
              <div className="pt-4 border-t">
                <Label>Authentication</Label>
                {config.accessToken || config.systemUserAccessToken ? (
                  <div className="flex items-center justify-between mt-2">
                    <div className="flex items-center gap-2">
                      <Badge variant="success">Connected</Badge>
                      <span className="text-sm text-muted-foreground">
                        {config.systemUserAccessToken ? 'Using System User Token' : 'Using OAuth Token'}
                      </span>
                    </div>
                    {!isViewMode && (
                      <Button variant="outline" size="sm" onClick={handleOAuthConnect}>
                        Reconnect
                      </Button>
                    )}
                  </div>
                ) : (
                  <Button 
                    onClick={handleOAuthConnect} 
                    className="w-full mt-2"
                    disabled={!config.appId || isConnecting || isViewMode}
                  >
                    {isConnecting ? 'Connecting...' : 'Connect WhatsApp Business Account'}
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Features Tab */}
        <TabsContent value="features" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Enabled Features</CardTitle>
              <CardDescription>
                Choose which WhatsApp Business features to enable
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <MessageCircle className="h-4 w-4" />
                    <Label htmlFor="textMessaging">Text Messaging</Label>
                  </div>
                  <Switch
                    id="textMessaging"
                    checked={features.enableTextMessaging}
                    onCheckedChange={(checked) => setFeatures({...features, enableTextMessaging: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Image className="h-4 w-4" />
                    <Label htmlFor="mediaMessaging">Media Messaging</Label>
                  </div>
                  <Switch
                    id="mediaMessaging"
                    checked={features.enableMediaMessaging}
                    onCheckedChange={(checked) => setFeatures({...features, enableMediaMessaging: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <FileText className="h-4 w-4" />
                    <Label htmlFor="templateMessaging">Template Messaging</Label>
                  </div>
                  <Switch
                    id="templateMessaging"
                    checked={features.enableTemplateMessaging}
                    onCheckedChange={(checked) => setFeatures({...features, enableTemplateMessaging: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Zap className="h-4 w-4" />
                    <Label htmlFor="interactiveMessages">Interactive Messages</Label>
                  </div>
                  <Switch
                    id="interactiveMessages"
                    checked={features.enableInteractiveMessages}
                    onCheckedChange={(checked) => setFeatures({...features, enableInteractiveMessages: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <CheckCircle className="h-4 w-4" />
                    <Label htmlFor="statusUpdates">Status Updates</Label>
                  </div>
                  <Switch
                    id="statusUpdates"
                    checked={features.enableStatusUpdates}
                    onCheckedChange={(checked) => setFeatures({...features, enableStatusUpdates: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Users className="h-4 w-4" />
                    <Label htmlFor="groupMessaging">Group Messaging</Label>
                  </div>
                  <Switch
                    id="groupMessaging"
                    checked={features.enableGroupMessaging}
                    onCheckedChange={(checked) => setFeatures({...features, enableGroupMessaging: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Phone className="h-4 w-4" />
                    <Label htmlFor="contactManagement">Contact Management</Label>
                  </div>
                  <Switch
                    id="contactManagement"
                    checked={features.enableContactManagement}
                    onCheckedChange={(checked) => setFeatures({...features, enableContactManagement: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Settings className="h-4 w-4" />
                    <Label htmlFor="businessProfile">Business Profile</Label>
                  </div>
                  <Switch
                    id="businessProfile"
                    checked={features.enableBusinessProfile}
                    onCheckedChange={(checked) => setFeatures({...features, enableBusinessProfile: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <ShoppingBag className="h-4 w-4" />
                    <Label htmlFor="catalogs">Catalogs</Label>
                  </div>
                  <Switch
                    id="catalogs"
                    checked={features.enableCatalogs}
                    onCheckedChange={(checked) => setFeatures({...features, enableCatalogs: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <QrCode className="h-4 w-4" />
                    <Label htmlFor="qrCodes">QR Codes</Label>
                  </div>
                  <Switch
                    id="qrCodes"
                    checked={features.enableQRCodes}
                    onCheckedChange={(checked) => setFeatures({...features, enableQRCodes: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Tag className="h-4 w-4" />
                    <Label htmlFor="labels">Labels</Label>
                  </div>
                  <Switch
                    id="labels"
                    checked={features.enableLabels}
                    onCheckedChange={(checked) => setFeatures({...features, enableLabels: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Zap className="h-4 w-4" />
                    <Label htmlFor="flows">Flows</Label>
                  </div>
                  <Switch
                    id="flows"
                    checked={features.enableFlows}
                    onCheckedChange={(checked) => setFeatures({...features, enableFlows: checked})}
                    disabled={isViewMode}
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Messaging Settings Tab */}
        <TabsContent value="messaging" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MessageCircle className="h-5 w-5" />
                Messaging Settings
              </CardTitle>
              <CardDescription>
                Configure message limits and behavior
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="maxTextLength">Max Text Length</Label>
                  <Input
                    id="maxTextLength"
                    type="number"
                    value={settings.maxTextLength}
                    onChange={(e) => setSettings({...settings, maxTextLength: parseInt(e.target.value)})}
                    disabled={isViewMode}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    Maximum characters per text message
                  </p>
                </div>
                <div>
                  <Label htmlFor="maxCaptionLength">Max Caption Length</Label>
                  <Input
                    id="maxCaptionLength"
                    type="number"
                    value={settings.maxCaptionLength}
                    onChange={(e) => setSettings({...settings, maxCaptionLength: parseInt(e.target.value)})}
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="maxButtonsPerMessage">Max Buttons per Message</Label>
                  <Input
                    id="maxButtonsPerMessage"
                    type="number"
                    value={settings.maxButtonsPerMessage}
                    onChange={(e) => setSettings({...settings, maxButtonsPerMessage: parseInt(e.target.value)})}
                    max="3"
                    disabled={isViewMode}
                  />
                </div>
                <div>
                  <Label htmlFor="maxSectionsPerList">Max Sections per List</Label>
                  <Input
                    id="maxSectionsPerList"
                    type="number"
                    value={settings.maxSectionsPerList}
                    onChange={(e) => setSettings({...settings, maxSectionsPerList: parseInt(e.target.value)})}
                    max="10"
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="maxItemsPerSection">Max Items per Section</Label>
                  <Input
                    id="maxItemsPerSection"
                    type="number"
                    value={settings.maxItemsPerSection}
                    onChange={(e) => setSettings({...settings, maxItemsPerSection: parseInt(e.target.value)})}
                    max="10"
                    disabled={isViewMode}
                  />
                </div>
                <div>
                  <Label htmlFor="sessionTimeout">Session Timeout (minutes)</Label>
                  <Input
                    id="sessionTimeout"
                    type="number"
                    value={settings.sessionTimeoutMinutes}
                    onChange={(e) => setSettings({...settings, sessionTimeoutMinutes: parseInt(e.target.value)})}
                    disabled={isViewMode}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    24-hour window for customer responses
                  </p>
                </div>
              </div>

              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Database className="h-4 w-4" />
                    <Label htmlFor="autoDownloadMedia">Auto-download Media</Label>
                  </div>
                  <Switch
                    id="autoDownloadMedia"
                    checked={settings.autoDownloadMedia}
                    onCheckedChange={(checked) => setSettings({...settings, autoDownloadMedia: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <HardDrive className="h-4 w-4" />
                    <Label htmlFor="saveMediaLocally">Save Media Locally</Label>
                  </div>
                  <Switch
                    id="saveMediaLocally"
                    checked={settings.saveMediaLocally}
                    onCheckedChange={(checked) => setSettings({...settings, saveMediaLocally: checked})}
                    disabled={isViewMode}
                  />
                </div>

                {settings.saveMediaLocally && (
                  <div>
                    <Label htmlFor="mediaStoragePath">Media Storage Path</Label>
                    <Input
                      id="mediaStoragePath"
                      value={settings.mediaStoragePath}
                      onChange={(e) => setSettings({...settings, mediaStoragePath: e.target.value})}
                      placeholder="/tmp/whatsapp-media"
                      disabled={isViewMode}
                    />
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Webhook Tab */}
        <TabsContent value="webhook" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Webhook className="h-5 w-5" />
                Webhook Configuration
              </CardTitle>
              <CardDescription>
                Configure webhooks to receive real-time updates
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <Label htmlFor="webhookEnabled">Enable Webhooks</Label>
                <Switch
                  id="webhookEnabled"
                  checked={config.webhookEnabled || false}
                  onCheckedChange={(checked) => onChange({ ...config, webhookEnabled: checked })}
                  disabled={isViewMode}
                />
              </div>

              {config.webhookEnabled && (
                <>
                  <div>
                    <Label>Webhook URL</Label>
                    <div className="flex gap-2 mt-1">
                      <Input
                        value={`${window.location.origin}/webhooks/whatsapp`}
                        readOnly
                        className="font-mono text-sm"
                      />
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => {
                          navigator.clipboard.writeText(`${window.location.origin}/webhooks/whatsapp`);
                          toast({
                            title: "Copied!",
                            description: "Webhook URL copied to clipboard"
                          });
                        }}
                      >
                        Copy
                      </Button>
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">
                      Add this URL in your WhatsApp Business webhook settings
                    </p>
                  </div>

                  <div>
                    <Label htmlFor="verifyToken">Verify Token</Label>
                    <Input
                      id="verifyToken"
                      value={config.verifyToken || ''}
                      onChange={(e) => onChange({ ...config, verifyToken: e.target.value })}
                      placeholder="Create a secure verify token"
                      disabled={isViewMode}
                    />
                    <p className="text-xs text-muted-foreground mt-1">
                      Use this same token in your WhatsApp webhook configuration
                    </p>
                  </div>

                  <Alert>
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>
                      Configure these webhook fields in WhatsApp Business:
                      <ul className="list-disc ml-6 mt-2">
                        <li>messages - For incoming messages</li>
                        <li>message_status - For delivery receipts</li>
                        <li>message_template_status_update - For template approvals</li>
                      </ul>
                    </AlertDescription>
                  </Alert>
                </>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Advanced Tab */}
        <TabsContent value="advanced" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Advanced Settings</CardTitle>
              <CardDescription>
                Configure advanced options for the WhatsApp Business adapter
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <Label htmlFor="apiVersion">API Version</Label>
                <Input
                  id="apiVersion"
                  value={config.apiVersion || 'v18.0'}
                  onChange={(e) => onChange({ ...config, apiVersion: e.target.value })}
                  placeholder="v18.0"
                  disabled={isViewMode}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="requestsPerHour">Rate Limit (requests/hour)</Label>
                  <Input
                    id="requestsPerHour"
                    type="number"
                    value={config.rateLimitConfig?.requestsPerHour || 1000}
                    onChange={(e) => onChange({
                      ...config,
                      rateLimitConfig: {
                        ...config.rateLimitConfig,
                        requestsPerHour: parseInt(e.target.value)
                      }
                    })}
                    disabled={isViewMode}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    WhatsApp has higher rate limits than other platforms
                  </p>
                </div>
                <div>
                  <Label htmlFor="retryAttempts">Max Retry Attempts</Label>
                  <Input
                    id="retryAttempts"
                    type="number"
                    value={config.retryConfig?.maxRetries || 3}
                    onChange={(e) => onChange({
                      ...config,
                      retryConfig: {
                        ...config.retryConfig,
                        maxRetries: parseInt(e.target.value)
                      }
                    })}
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <div>
                <Label htmlFor="baseUrl">API Base URL</Label>
                <Input
                  id="baseUrl"
                  value={config.baseUrl || 'https://graph.facebook.com'}
                  onChange={(e) => onChange({ ...config, baseUrl: e.target.value })}
                  placeholder="https://graph.facebook.com"
                  disabled={isViewMode}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="statusPollingInterval">Status Polling Interval (ms)</Label>
                  <Input
                    id="statusPollingInterval"
                    type="number"
                    value={config.statusPollingInterval || 60000}
                    onChange={(e) => onChange({ ...config, statusPollingInterval: parseInt(e.target.value) })}
                    disabled={isViewMode}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    How often to clean up processed messages
                  </p>
                </div>
                <div>
                  <Label htmlFor="messageRetentionHours">Message Retention (hours)</Label>
                  <Input
                    id="messageRetentionHours"
                    type="number"
                    value={config.messageRetentionHours || 24}
                    onChange={(e) => onChange({ ...config, messageRetentionHours: parseInt(e.target.value) })}
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <Alert>
                <Shield className="h-4 w-4" />
                <AlertDescription>
                  <strong>Security Note:</strong> Always use System User Access Tokens for production. 
                  User access tokens expire after 60 days.
                </AlertDescription>
              </Alert>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Test Connection Button */}
      {onTest && !isViewMode && (config.accessToken || config.systemUserAccessToken) && (
        <div className="flex justify-end">
          <Button onClick={onTest} variant="outline">
            Test Connection
          </Button>
        </div>
      )}
    </div>
  );
};