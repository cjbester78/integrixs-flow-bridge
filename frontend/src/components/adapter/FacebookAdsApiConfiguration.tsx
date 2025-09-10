import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { 
  Facebook, 
  AlertCircle, 
  CheckCircle, 
  Settings, 
  DollarSign,
  Target,
  BarChart,
  Image,
  Video,
  Users,
  TrendingUp,
  FileText,
  Download
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface FacebookAdsApiConfigurationProps {
  config: any;
  onChange: (config: any) => void;
  onTest?: () => void;
  mode?: 'create' | 'edit' | 'view';
}

export const FacebookAdsApiConfiguration: React.FC<FacebookAdsApiConfigurationProps> = ({
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
    enableCampaignManagement: true,
    enableAudienceTargeting: true,
    enableBudgetOptimization: true,
    enableCreativeAssets: true,
    enablePerformanceTracking: true,
    enableAutomatedRules: false,
    enableA_BTesting: true,
    enableCustomConversions: true,
    enablePixelTracking: true,
    enableLeadForms: true
  });

  // Limits state
  const [limits, setLimits] = useState(config.limits || {
    maxCampaignsPerAccount: 5000,
    maxAdSetsPerCampaign: 5000,
    maxAdsPerAdSet: 5000,
    maxCustomAudiences: 500,
    minDailyBudgetCents: 100,
    maxDailyBudgetCents: 100000000,
    maxBatchSize: 50
  });

  // OAuth handler - reuses Facebook login but with ads_management scope
  const handleOAuthConnect = () => {
    setIsConnecting(true);
    
    const clientId = config.clientId || config.appId;
    const redirectUri = encodeURIComponent(window.location.origin + '/oauth/facebook/callback');
    const scope = [
      'ads_management',
      'ads_read',
      'business_management',
      'pages_read_engagement',
      'pages_manage_ads',
      'leads_retrieval',
      'catalog_management'
    ].join(',');
    
    const state = btoa(JSON.stringify({
      adapterId: config.id,
      adapterType: 'facebook_ads',
      timestamp: Date.now()
    }));
    
    const authUrl = `https://www.facebook.com/v18.0/dialog/oauth?` +
      `client_id=${clientId}` +
      `&redirect_uri=${redirectUri}` +
      `&scope=${scope}` +
      `&state=${state}` +
      `&response_type=code`;
    
    const authWindow = window.open(authUrl, 'FacebookAdsAuth', 'width=600,height=700');
    
    const checkInterval = setInterval(() => {
      if (authWindow?.closed) {
        clearInterval(checkInterval);
        setIsConnecting(false);
      }
    }, 1000);
  };

  // Update features and limits in config
  useEffect(() => {
    onChange({
      ...config,
      features,
      limits
    });
  }, [features, limits, config, onChange]);

  const isViewMode = mode === 'view';

  // Campaign objectives for dropdown
  const campaignObjectives = [
    'BRAND_AWARENESS',
    'REACH',
    'TRAFFIC',
    'ENGAGEMENT',
    'APP_INSTALLS',
    'VIDEO_VIEWS',
    'LEAD_GENERATION',
    'MESSAGES',
    'CONVERSIONS',
    'CATALOG_SALES',
    'STORE_TRAFFIC'
  ];

  return (
    <div className="space-y-6">
      {/* Connection Status */}
      {config.accessToken && (
        <Alert className="border-green-200 bg-green-50 dark:bg-green-900/20">
          <CheckCircle className="h-4 w-4 text-green-600" />
          <AlertDescription className="text-green-800 dark:text-green-200">
            Connected to Facebook Ads. Account ID: {config.adAccountId}
          </AlertDescription>
        </Alert>
      )}

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="basic">Basic Setup</TabsTrigger>
          <TabsTrigger value="features">Features</TabsTrigger>
          <TabsTrigger value="targeting">Targeting</TabsTrigger>
          <TabsTrigger value="budget">Budget & Limits</TabsTrigger>
          <TabsTrigger value="advanced">Advanced</TabsTrigger>
        </TabsList>

        {/* Basic Setup Tab */}
        <TabsContent value="basic" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Facebook className="h-5 w-5 text-blue-600" />
                Facebook Ads Configuration
              </CardTitle>
              <CardDescription>
                Configure your Facebook Ads account and authentication
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

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="adAccountId">Ad Account ID</Label>
                  <Input
                    id="adAccountId"
                    value={config.adAccountId || ''}
                    onChange={(e) => onChange({ ...config, adAccountId: e.target.value })}
                    placeholder="123456789012345"
                    disabled={isViewMode}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    Without the 'act_' prefix
                  </p>
                </div>
                <div>
                  <Label htmlFor="businessId">Business ID (Optional)</Label>
                  <Input
                    id="businessId"
                    value={config.businessId || ''}
                    onChange={(e) => onChange({ ...config, businessId: e.target.value })}
                    placeholder="Your Business Manager ID"
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
                    {isConnecting ? 'Connecting...' : 'Connect Facebook Ads Account'}
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
                Choose which Facebook Ads features to enable
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <TrendingUp className="h-4 w-4" />
                    <Label htmlFor="campaignManagement">Campaign Management</Label>
                  </div>
                  <Switch
                    id="campaignManagement"
                    checked={features.enableCampaignManagement}
                    onCheckedChange={(checked) => setFeatures({...features, enableCampaignManagement: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Target className="h-4 w-4" />
                    <Label htmlFor="audienceTargeting">Audience Targeting</Label>
                  </div>
                  <Switch
                    id="audienceTargeting"
                    checked={features.enableAudienceTargeting}
                    onCheckedChange={(checked) => setFeatures({...features, enableAudienceTargeting: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <DollarSign className="h-4 w-4" />
                    <Label htmlFor="budgetOptimization">Budget Optimization</Label>
                  </div>
                  <Switch
                    id="budgetOptimization"
                    checked={features.enableBudgetOptimization}
                    onCheckedChange={(checked) => setFeatures({...features, enableBudgetOptimization: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Image className="h-4 w-4" />
                    <Label htmlFor="creativeAssets">Creative Assets</Label>
                  </div>
                  <Switch
                    id="creativeAssets"
                    checked={features.enableCreativeAssets}
                    onCheckedChange={(checked) => setFeatures({...features, enableCreativeAssets: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <BarChart className="h-4 w-4" />
                    <Label htmlFor="performanceTracking">Performance Tracking</Label>
                  </div>
                  <Switch
                    id="performanceTracking"
                    checked={features.enablePerformanceTracking}
                    onCheckedChange={(checked) => setFeatures({...features, enablePerformanceTracking: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Settings className="h-4 w-4" />
                    <Label htmlFor="automatedRules">Automated Rules</Label>
                  </div>
                  <Switch
                    id="automatedRules"
                    checked={features.enableAutomatedRules}
                    onCheckedChange={(checked) => setFeatures({...features, enableAutomatedRules: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <BarChart className="h-4 w-4" />
                    <Label htmlFor="abTesting">A/B Testing</Label>
                  </div>
                  <Switch
                    id="abTesting"
                    checked={features.enableA_BTesting}
                    onCheckedChange={(checked) => setFeatures({...features, enableA_BTesting: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Target className="h-4 w-4" />
                    <Label htmlFor="customConversions">Custom Conversions</Label>
                  </div>
                  <Switch
                    id="customConversions"
                    checked={features.enableCustomConversions}
                    onCheckedChange={(checked) => setFeatures({...features, enableCustomConversions: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Settings className="h-4 w-4" />
                    <Label htmlFor="pixelTracking">Pixel Tracking</Label>
                  </div>
                  <Switch
                    id="pixelTracking"
                    checked={features.enablePixelTracking}
                    onCheckedChange={(checked) => setFeatures({...features, enablePixelTracking: checked})}
                    disabled={isViewMode}
                  />
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <FileText className="h-4 w-4" />
                    <Label htmlFor="leadForms">Lead Forms</Label>
                  </div>
                  <Switch
                    id="leadForms"
                    checked={features.enableLeadForms}
                    onCheckedChange={(checked) => setFeatures({...features, enableLeadForms: checked})}
                    disabled={isViewMode}
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Targeting Tab */}
        <TabsContent value="targeting" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Target className="h-5 w-5" />
                Default Targeting Options
              </CardTitle>
              <CardDescription>
                Configure default targeting settings for new campaigns
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <Label htmlFor="defaultObjective">Default Campaign Objective</Label>
                <Select
                  value={config.defaultObjective || 'CONVERSIONS'}
                  onValueChange={(value) => onChange({ ...config, defaultObjective: value })}
                  disabled={isViewMode}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {campaignObjectives.map(obj => (
                      <SelectItem key={obj} value={obj}>
                        {obj.replace(/_/g, ' ')}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="defaultAgeMin">Default Age Range</Label>
                  <div className="flex gap-2 items-center mt-1">
                    <Input
                      id="defaultAgeMin"
                      type="number"
                      value={config.defaultAgeMin || 18}
                      onChange={(e) => onChange({ ...config, defaultAgeMin: parseInt(e.target.value) })}
                      placeholder="18"
                      className="w-20"
                      disabled={isViewMode}
                    />
                    <span>to</span>
                    <Input
                      type="number"
                      value={config.defaultAgeMax || 65}
                      onChange={(e) => onChange({ ...config, defaultAgeMax: parseInt(e.target.value) })}
                      placeholder="65+"
                      className="w-20"
                      disabled={isViewMode}
                    />
                  </div>
                </div>

                <div>
                  <Label htmlFor="defaultGenders">Default Genders</Label>
                  <Select
                    value={config.defaultGenders || 'all'}
                    onValueChange={(value) => onChange({ ...config, defaultGenders: value })}
                    disabled={isViewMode}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All</SelectItem>
                      <SelectItem value="male">Male</SelectItem>
                      <SelectItem value="female">Female</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div>
                <Label htmlFor="defaultLocations">Default Locations (comma-separated)</Label>
                <Input
                  id="defaultLocations"
                  value={config.defaultLocations || ''}
                  onChange={(e) => onChange({ ...config, defaultLocations: e.target.value })}
                  placeholder="United States, Canada, United Kingdom"
                  disabled={isViewMode}
                />
              </div>

              <div>
                <Label htmlFor="defaultInterests">Default Interests (comma-separated)</Label>
                <Input
                  id="defaultInterests"
                  value={config.defaultInterests || ''}
                  onChange={(e) => onChange({ ...config, defaultInterests: e.target.value })}
                  placeholder="Technology, Business, Marketing"
                  disabled={isViewMode}
                />
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Budget & Limits Tab */}
        <TabsContent value="budget" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <DollarSign className="h-5 w-5" />
                Budget & Limits Configuration
              </CardTitle>
              <CardDescription>
                Configure budget limits and campaign restrictions
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="minDailyBudget">Min Daily Budget ($)</Label>
                  <Input
                    id="minDailyBudget"
                    type="number"
                    value={(limits.minDailyBudgetCents / 100).toFixed(2)}
                    onChange={(e) => setLimits({
                      ...limits,
                      minDailyBudgetCents: Math.round(parseFloat(e.target.value) * 100)
                    })}
                    step="0.01"
                    disabled={isViewMode}
                  />
                </div>
                <div>
                  <Label htmlFor="maxDailyBudget">Max Daily Budget ($)</Label>
                  <Input
                    id="maxDailyBudget"
                    type="number"
                    value={(limits.maxDailyBudgetCents / 100).toFixed(2)}
                    onChange={(e) => setLimits({
                      ...limits,
                      maxDailyBudgetCents: Math.round(parseFloat(e.target.value) * 100)
                    })}
                    step="0.01"
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="maxCampaigns">Max Campaigns per Account</Label>
                  <Input
                    id="maxCampaigns"
                    type="number"
                    value={limits.maxCampaignsPerAccount}
                    onChange={(e) => setLimits({
                      ...limits,
                      maxCampaignsPerAccount: parseInt(e.target.value)
                    })}
                    disabled={isViewMode}
                  />
                </div>
                <div>
                  <Label htmlFor="maxAdSets">Max Ad Sets per Campaign</Label>
                  <Input
                    id="maxAdSets"
                    type="number"
                    value={limits.maxAdSetsPerCampaign}
                    onChange={(e) => setLimits({
                      ...limits,
                      maxAdSetsPerCampaign: parseInt(e.target.value)
                    })}
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="maxAds">Max Ads per Ad Set</Label>
                  <Input
                    id="maxAds"
                    type="number"
                    value={limits.maxAdsPerAdSet}
                    onChange={(e) => setLimits({
                      ...limits,
                      maxAdsPerAdSet: parseInt(e.target.value)
                    })}
                    disabled={isViewMode}
                  />
                </div>
                <div>
                  <Label htmlFor="maxAudiences">Max Custom Audiences</Label>
                  <Input
                    id="maxAudiences"
                    type="number"
                    value={limits.maxCustomAudiences}
                    onChange={(e) => setLimits({
                      ...limits,
                      maxCustomAudiences: parseInt(e.target.value)
                    })}
                    disabled={isViewMode}
                  />
                </div>
              </div>

              <div>
                <Label htmlFor="maxBatchSize">Max Batch Size</Label>
                <Input
                  id="maxBatchSize"
                  type="number"
                  value={limits.maxBatchSize}
                  onChange={(e) => setLimits({
                    ...limits,
                    maxBatchSize: parseInt(e.target.value)
                  })}
                  disabled={isViewMode}
                />
                <p className="text-xs text-muted-foreground mt-1">
                  Maximum number of items to process in a single batch operation
                </p>
              </div>

              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  These limits help prevent accidental overspending and API rate limit violations.
                  Adjust based on your account tier and requirements.
                </AlertDescription>
              </Alert>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Advanced Tab */}
        <TabsContent value="advanced" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Advanced Settings</CardTitle>
              <CardDescription>
                Configure advanced options for the Facebook Ads adapter
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
                  <Label htmlFor="pollingInterval">Polling Interval (ms)</Label>
                  <Input
                    id="pollingInterval"
                    type="number"
                    value={config.pollingInterval || 300000}
                    onChange={(e) => onChange({ ...config, pollingInterval: parseInt(e.target.value) })}
                    disabled={isViewMode}
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    How often to poll for performance data (default: 5 minutes)
                  </p>
                </div>
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
              </div>

              <div className="grid grid-cols-2 gap-4">
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
                <div>
                  <Label htmlFor="retryDelay">Retry Delay (ms)</Label>
                  <Input
                    id="retryDelay"
                    type="number"
                    value={config.retryConfig?.retryDelay || 1000}
                    onChange={(e) => onChange({
                      ...config,
                      retryConfig: {
                        ...config.retryConfig,
                        retryDelay: parseInt(e.target.value)
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
                  <Download className="h-4 w-4" />
                  <Label htmlFor="enableBulkOperations">Enable Bulk Operations</Label>
                </div>
                <Switch
                  id="enableBulkOperations"
                  checked={config.enableBulkOperations !== false}
                  onCheckedChange={(checked) => onChange({ ...config, enableBulkOperations: checked })}
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