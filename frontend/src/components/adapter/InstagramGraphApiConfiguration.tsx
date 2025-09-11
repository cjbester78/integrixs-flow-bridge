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
  Instagram,
  Camera,
  AlertCircle, 
  CheckCircle, 
  Settings,
  Hash,
  MessageSquare,
  Image,
  Video,
  BarChart,
  ShoppingBag,
  Calendar,
  User,
  Film,
  Layers
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface InstagramGraphApiConfigurationProps {
  config: any;
  onChange: (config: any) => void;
  onTest?: () => void;
  mode?: 'create' | 'edit' | 'view';
}

export const InstagramGraphApiConfiguration: React.FC<InstagramGraphApiConfigurationProps> = ({
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
    enablePostPublishing: true,
    enableStories: true,
    enableReels: true,
    enableCommentManagement: true,
    enableHashtagAnalytics: true,
    enableUserInsights: true,
    enableShoppingTags: true,
    enableIGTV: true,
    enableContentScheduling: true,
    enableMentionMonitoring: true,
    enableCarouselPosts: true,
    enableProductTagging: true
  });

  // OAuth handler - uses Facebook login as Instagram uses Facebook's auth
  const handleOAuthConnect = () => {
    setIsConnecting(true);
    
    const clientId = config.clientId || config.appId;
    const redirectUri = encodeURIComponent(window.location.origin + '/oauth/facebook/callback');
    const scope = [
      'instagram_basic',
      'instagram_content_publish',
      'instagram_manage_comments',
      'instagram_manage_insights',
      'instagram_shopping_tag_products',
      'pages_show_list',
      'pages_read_engagement',
      'business_management'
    ].join(',');
    
    const state = btoa(JSON.stringify({
      adapterId: config.id,
      adapterType: 'instagram',
      timestamp: Date.now()
    }));
    
    const authUrl = `https://www.facebook.com/v18.0/dialog/oauth?` +
      `client_id=${clientId}` +
      `&redirect_uri=${redirectUri}` +
      `&scope=${scope}` +
      `&state=${state}` +
      `&response_type=code`;
    
    const authWindow = window.open(authUrl, 'InstagramAuth', 'width=600,height=700');
    
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
            Connected to Instagram Business Account: {config.instagramBusinessAccountId}
          </AlertDescription>
        </Alert>
      )}

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="basic">Basic Setup</TabsTrigger>
          <TabsTrigger value="features">Features</TabsTrigger>
          <TabsTrigger value="content">Content Settings</TabsTrigger>
          <TabsTrigger value="advanced">Advanced</TabsTrigger>
        </TabsList>

        {/* Basic Setup Tab */}
        <TabsContent value="basic" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Instagram className="h-5 w-5" />
                Instagram Configuration
              </CardTitle>
              <CardDescription>
                Configure your Instagram Business account credentials
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  Instagram API requires a Facebook App and an Instagram Business Account connected to a Facebook Page
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
                  <Label htmlFor="instagramBusinessAccountId">Instagram Business Account ID</Label>
                  <Input
                    id="instagramBusinessAccountId"
                    value={config.instagramBusinessAccountId || ''}
                    onChange={(e) => onChange({ ...config, instagramBusinessAccountId: e.target.value })}
                    placeholder="Instagram Business Account ID"
                    disabled={isViewMode}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    Available after connecting Facebook Page
                  </p>
                </div>
                <div>
                  <Label htmlFor="facebookPageId">Facebook Page ID</Label>
                  <Input
                    id="facebookPageId"
                    value={config.facebookPageId || ''}
                    onChange={(e) => onChange({ ...config, facebookPageId: e.target.value })}
                    placeholder="Connected Facebook Page ID"
                    disabled={isViewMode}
                  />
                </div>
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
                    {isConnecting ? 'Connecting...' : 'Connect Instagram Business Account'}
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
                Choose which Instagram features to enable
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Image className="h-4 w-4" />
                    <Label htmlFor="postPublishing">Post Publishing</Label>
                  </div>
                  <Switch
                    id="postPublishing"
                    checked={features.enablePostPublishing}
                    onCheckedChange={(checked) => setFeatures({...features, enablePostPublishing: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Camera className="h-4 w-4" />
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
                    <MessageSquare className="h-4 w-4" />
                    <Label htmlFor="commentManagement">Comment Management</Label>
                  </div>
                  <Switch
                    id="commentManagement"
                    checked={features.enableCommentManagement}
                    onCheckedChange={(checked) => setFeatures({...features, enableCommentManagement: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Hash className="h-4 w-4" />
                    <Label htmlFor="hashtagAnalytics">Hashtag Analytics</Label>
                  </div>
                  <Switch
                    id="hashtagAnalytics"
                    checked={features.enableHashtagAnalytics}
                    onCheckedChange={(checked) => setFeatures({...features, enableHashtagAnalytics: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <BarChart className="h-4 w-4" />
                    <Label htmlFor="userInsights">User Insights</Label>
                  </div>
                  <Switch
                    id="userInsights"
                    checked={features.enableUserInsights}
                    onCheckedChange={(checked) => setFeatures({...features, enableUserInsights: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <ShoppingBag className="h-4 w-4" />
                    <Label htmlFor="shoppingTags">Shopping Tags</Label>
                  </div>
                  <Switch
                    id="shoppingTags"
                    checked={features.enableShoppingTags}
                    onCheckedChange={(checked) => setFeatures({...features, enableShoppingTags: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Film className="h-4 w-4" />
                    <Label htmlFor="igtv">IGTV/Long Videos</Label>
                  </div>
                  <Switch
                    id="igtv"
                    checked={features.enableIGTV}
                    onCheckedChange={(checked) => setFeatures({...features, enableIGTV: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    <Label htmlFor="scheduling">Content Scheduling</Label>
                  </div>
                  <Switch
                    id="scheduling"
                    checked={features.enableContentScheduling}
                    onCheckedChange={(checked) => setFeatures({...features, enableContentScheduling: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <User className="h-4 w-4" />
                    <Label htmlFor="mentionMonitoring">Mention Monitoring</Label>
                  </div>
                  <Switch
                    id="mentionMonitoring"
                    checked={features.enableMentionMonitoring}
                    onCheckedChange={(checked) => setFeatures({...features, enableMentionMonitoring: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Layers className="h-4 w-4" />
                    <Label htmlFor="carouselPosts">Carousel Posts</Label>
                  </div>
                  <Switch
                    id="carouselPosts"
                    checked={features.enableCarouselPosts}
                    onCheckedChange={(checked) => setFeatures({...features, enableCarouselPosts: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <ShoppingBag className="h-4 w-4" />
                    <Label htmlFor="productTagging">Product Tagging</Label>
                  </div>
                  <Switch
                    id="productTagging"
                    checked={features.enableProductTagging}
                    onCheckedChange={(checked) => setFeatures({...features, enableProductTagging: checked})}
                    disabled={isViewMode}
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Content Settings Tab */}
        <TabsContent value="content" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Settings className="h-5 w-5" />
                Content Settings
              </CardTitle>
              <CardDescription>
                Configure content publishing preferences
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <Label htmlFor="defaultHashtags">Default Hashtags</Label>
                <Input
                  id="defaultHashtags"
                  value={config.defaultHashtags || ''}
                  onChange={(e) => onChange({ ...config, defaultHashtags: e.target.value })}
                  placeholder="#instagram #business #social"
                  disabled={isViewMode}
                />
                <p className="text-xs text-muted-foreground mt-1">
                  Comma-separated list of hashtags to include by default
                </p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="imageMinWidth">Min Image Width (px)</Label>
                  <Input
                    id="imageMinWidth"
                    type="number"
                    value={config.imageMinWidth || 320}
                    onChange={(e) => onChange({ ...config, imageMinWidth: parseInt(e.target.value) })}
                    disabled={isViewMode}
                  />
                </div>
                <div>
                  <Label htmlFor="imageMaxSize">Max Image Size (MB)</Label>
                  <Input
                    id="imageMaxSize"
                    type="number"
                    value={config.imageMaxSize || 8}
                    onChange={(e) => onChange({ ...config, imageMaxSize: parseInt(e.target.value) })}
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="videoMinDuration">Min Video Duration (s)</Label>
                  <Input
                    id="videoMinDuration"
                    type="number"
                    value={config.videoMinDuration || 3}
                    onChange={(e) => onChange({ ...config, videoMinDuration: parseInt(e.target.value) })}
                    disabled={isViewMode}
                  />
                </div>
                <div>
                  <Label htmlFor="videoMaxDuration">Max Video Duration (s)</Label>
                  <Input
                    id="videoMaxDuration"
                    type="number"
                    value={config.videoMaxDuration || 60}
                    onChange={(e) => onChange({ ...config, videoMaxDuration: parseInt(e.target.value) })}
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <div>
                <Label htmlFor="captionMaxLength">Caption Max Length</Label>
                <Input
                  id="captionMaxLength"
                  type="number"
                  value={config.captionMaxLength || 2200}
                  onChange={(e) => onChange({ ...config, captionMaxLength: parseInt(e.target.value) })}
                  disabled={isViewMode}
                />
                <p className="text-xs text-muted-foreground mt-1">
                  Instagram allows up to 2,200 characters in captions
                </p>
              </div>

              <div>
                <Label htmlFor="hashtagLimit">Hashtag Limit per Post</Label>
                <Input
                  id="hashtagLimit"
                  type="number"
                  value={config.hashtagLimit || 30}
                  onChange={(e) => onChange({ ...config, hashtagLimit: parseInt(e.target.value) })}
                  disabled={isViewMode}
                />
                <p className="text-xs text-muted-foreground mt-1">
                  Instagram allows up to 30 hashtags per post
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Advanced Tab */}
        <TabsContent value="advanced" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Advanced Settings</CardTitle>
              <CardDescription>
                Configure advanced options for the Instagram adapter
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
                  <Label htmlFor="pollingInterval">Content Polling Interval (ms)</Label>
                  <Input
                    id="pollingInterval"
                    type="number"
                    value={config.pollingInterval || 300000}
                    onChange={(e) => onChange({ ...config, pollingInterval: parseInt(e.target.value) })}
                    disabled={isViewMode}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    How often to check for new comments/mentions (default: 5 min)
                  </p>
                </div>
                <div>
                  <Label htmlFor="insightsInterval">Insights Polling Interval (ms)</Label>
                  <Input
                    id="insightsInterval"
                    type="number"
                    value={config.insightsInterval || 3600000}
                    onChange={(e) => onChange({ ...config, insightsInterval: parseInt(e.target.value) })}
                    disabled={isViewMode}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    How often to fetch analytics data (default: 1 hour)
                  </p>
                </div>
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

              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Settings className="h-4 w-4" />
                  <Label htmlFor="autoApproveComments">Auto-approve Comments</Label>
                </div>
                <Switch
                  id="autoApproveComments"
                  checked={config.autoApproveComments || false}
                  onCheckedChange={(checked) => onChange({ ...config, autoApproveComments: checked })}
                  disabled={isViewMode}
                />
              </div>

              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Settings className="h-4 w-4" />
                  <Label htmlFor="enableWebhooks">Enable Webhooks</Label>
                </div>
                <Switch
                  id="enableWebhooks"
                  checked={config.enableWebhooks || false}
                  onCheckedChange={(checked) => onChange({ ...config, enableWebhooks: checked })}
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