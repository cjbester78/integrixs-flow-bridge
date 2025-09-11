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
  Facebook, 
  AlertCircle, 
  CheckCircle, 
  Settings, 
  Shield,
  Webhook,
  Image,
  Video,
  MessageSquare,
  BarChart
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface FacebookGraphApiConfigurationProps {
  config: any;
  onChange: (config: any) => void;
  onTest?: () => void;
  mode?: 'create' | 'edit' | 'view';
}

export const FacebookGraphApiConfiguration: React.FC<FacebookGraphApiConfigurationProps> = ({
  config,
  onChange,
  onTest,
  mode = 'create'
}) => {
  const { toast } = useToast();
  const [isConnecting, setIsConnecting] = useState(false);
  const [activeTab, setActiveTab] = useState('basic');
  const [showAdvanced, setShowAdvanced] = useState(false);

  // Features state
  const [features, setFeatures] = useState(config.features || {
    enablePageManagement: true,
    enableInsights: true,
    enableComments: true,
    enableMessaging: false,
    enableLiveVideo: false,
    enableStories: true,
    enableReels: true,
    enableScheduling: true,
    enableAudienceTargeting: true
  });

  // OAuth handler
  const handleOAuthConnect = () => {
    setIsConnecting(true);
    
    // Construct OAuth URL
    const clientId = config.clientId || config.appId;
    const redirectUri = encodeURIComponent(window.location.origin + '/oauth/facebook/callback');
    const scope = [
      'pages_show_list',
      'pages_read_engagement',
      'pages_manage_posts',
      'pages_read_user_content',
      'pages_manage_engagement',
      'read_insights',
      'ads_management'
    ].join(',');
    
    const state = btoa(JSON.stringify({
      adapterId: config.id,
      timestamp: Date.now()
    }));
    
    const authUrl = `https://www.facebook.com/v18.0/dialog/oauth?` +
      `client_id=${clientId}` +
      `&redirect_uri=${redirectUri}` +
      `&scope=${scope}` +
      `&state=${state}` +
      `&response_type=code`;
    
    // Open OAuth window
    const authWindow = window.open(authUrl, 'FacebookAuth', 'width=600,height=700');
    
    // Listen for OAuth completion
    const checkInterval = setInterval(() => {
      if (authWindow?.closed) {
        clearInterval(checkInterval);
        setIsConnecting(false);
      }
    }, 1000);
  };

  // Update features in config
  useEffect(() => {
    onChange({
      ...config,
      features
    });
  }, [features, config, onChange]);

  const isViewMode = mode === 'view';

  return (
    <div className="space-y-6">
      {/* Connection Status */}
      {config.accessToken && (
        <Alert className="border-green-200 bg-green-50 dark:bg-green-900/20">
          <CheckCircle className="h-4 w-4 text-green-600" />
          <AlertDescription className="text-green-800 dark:text-green-200">
            Connected to Facebook. Page ID: {config.pageId}
          </AlertDescription>
        </Alert>
      )}

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="basic">Basic Setup</TabsTrigger>
          <TabsTrigger value="features">Features</TabsTrigger>
          <TabsTrigger value="webhook">Webhooks</TabsTrigger>
          <TabsTrigger value="advanced">Advanced</TabsTrigger>
        </TabsList>

        {/* Basic Setup Tab */}
        <TabsContent value="basic" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Facebook className="h-5 w-5 text-blue-600" />
                Facebook App Configuration
              </CardTitle>
              <CardDescription>
                Configure your Facebook app credentials and page settings
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="appId">App ID</Label>
                  <Input
                    id="appId"
                    value={config.appId || ''}
                    onChange={(e) => onChange({ ...config, appId: e.target.value })}
                    placeholder="Your Facebook App ID"
                    disabled={isViewMode}
                  />
                </div>
                <div>
                  <Label htmlFor="appSecret">App Secret</Label>
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

              <div>
                <Label htmlFor="pageId">Facebook Page ID</Label>
                <Input
                  id="pageId"
                  value={config.pageId || ''}
                  onChange={(e) => onChange({ ...config, pageId: e.target.value })}
                  placeholder="Your Facebook Page ID"
                  disabled={isViewMode}
                />
                <p className="text-xs text-muted-foreground mt-1">
                  You can find your Page ID in Page Settings → Page Info
                </p>
              </div>

              {/* OAuth Connection */}
              <div className="pt-4 border-t">
                <Label>Authentication</Label>
                {config.accessToken ? (
                  <div className="flex items-center justify-between mt-2">
                    <div className="flex items-center gap-2">
                      <Badge variant="success">Connected</Badge>
                      <span className="text-sm text-muted-foreground">
                        Token expires: {config.tokenExpiresAt ? new Date(config.tokenExpiresAt).toLocaleDateString() : 'Never'}
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
                    {isConnecting ? 'Connecting...' : 'Connect Facebook Account'}
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
                Choose which Facebook features to enable for this adapter
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Settings className="h-4 w-4" />
                    <Label htmlFor="pageManagement">Page Management</Label>
                  </div>
                  <Switch
                    id="pageManagement"
                    checked={features.enablePageManagement}
                    onCheckedChange={(checked) => setFeatures({...features, enablePageManagement: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <BarChart className="h-4 w-4" />
                    <Label htmlFor="insights">Insights & Analytics</Label>
                  </div>
                  <Switch
                    id="insights"
                    checked={features.enableInsights}
                    onCheckedChange={(checked) => setFeatures({...features, enableInsights: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <MessageSquare className="h-4 w-4" />
                    <Label htmlFor="comments">Comments</Label>
                  </div>
                  <Switch
                    id="comments"
                    checked={features.enableComments}
                    onCheckedChange={(checked) => setFeatures({...features, enableComments: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <MessageSquare className="h-4 w-4" />
                    <Label htmlFor="messaging">Messaging</Label>
                  </div>
                  <Switch
                    id="messaging"
                    checked={features.enableMessaging}
                    onCheckedChange={(checked) => setFeatures({...features, enableMessaging: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Video className="h-4 w-4" />
                    <Label htmlFor="liveVideo">Live Video</Label>
                  </div>
                  <Switch
                    id="liveVideo"
                    checked={features.enableLiveVideo}
                    onCheckedChange={(checked) => setFeatures({...features, enableLiveVideo: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Image className="h-4 w-4" />
                    <Label htmlFor="stories">Stories</Label>
                  </div>
                  <Switch
                    id="stories"
                    checked={features.enableStories}
                    onCheckedChange={(checked) => setFeatures({...features, enableStories: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Video className="h-4 w-4" />
                    <Label htmlFor="reels">Reels</Label>
                  </div>
                  <Switch
                    id="reels"
                    checked={features.enableReels}
                    onCheckedChange={(checked) => setFeatures({...features, enableReels: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Settings className="h-4 w-4" />
                    <Label htmlFor="scheduling">Post Scheduling</Label>
                  </div>
                  <Switch
                    id="scheduling"
                    checked={features.enableScheduling}
                    onCheckedChange={(checked) => setFeatures({...features, enableScheduling: checked})}
                    disabled={isViewMode}
                  />
                </div>
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
                Configure webhooks to receive real-time updates from Facebook
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
                        value={`${window.location.origin}/webhooks/facebook`}
                        readOnly
                        className="font-mono text-sm"
                      />
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => {
                          navigator.clipboard.writeText(`${window.location.origin}/webhooks/facebook`);
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
                      Add this URL in your Facebook App webhook settings
                    </p>
                  </div>

                  <div>
                    <Label htmlFor="webhookVerifyToken">Verify Token</Label>
                    <Input
                      id="webhookVerifyToken"
                      value={config.webhookVerifyToken || ''}
                      onChange={(e) => onChange({ ...config, webhookVerifyToken: e.target.value })}
                      placeholder="Create a secure verify token"
                      disabled={isViewMode}
                    />
                    <p className="text-xs text-muted-foreground mt-1">
                      Use this same token in your Facebook App webhook configuration
                    </p>
                  </div>

                  <Alert>
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>
                      Configure these webhook subscriptions in your Facebook App:
                      <ul className="list-disc ml-6 mt-2">
                        <li>Page: feed, comments, messages</li>
                        <li>Instagram: comments, messages</li>
                        <li>WhatsApp Business: messages</li>
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
                Configure advanced options for the Facebook adapter
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
                    value={config.rateLimitConfig?.requestsPerHour || 200}
                    onChange={(e) => onChange({
                      ...config,
                      rateLimitConfig: {
                        ...config.rateLimitConfig,
                        requestsPerHour: parseInt(e.target.value)
                      }
                    })}
                    disabled={isViewMode}
                  />
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
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Test Connection Button */}
      {onTest && !isViewMode && config.accessToken && (
        <div className="flex justify-end">
          <Button onClick={onTest} variant="outline">
            Test Connection
          </Button>
        </div>
      )}
    </div>
  );
};