import React, { useState, useEffect, useCallback } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import { 
  Smartphone, 
  Settings, 
  AlertCircle,
  CheckCircle,
  Info,
  Shield,
  Zap,
  Phone,
  MessageSquare,
  Clock,
  MapPin,
  Ban,
  DollarSign,
  Key,
  AlertTriangle
} from 'lucide-react';

interface SMSConfigurationProps {
  configuration: Record<string, any>;
  onConfigurationChange: (config: Record<string, any>) => void;
  mode?: 'create' | 'edit';
}

const providerDefaults: Record<string, any> = {
  'Twilio': {
    apiEndpoint: 'https://api.twilio.com/2010-04-01',
    webhookPath: '/sms/twilio/webhook',
    requiresApiSecret: false,
  },
  'Vonage/Nexmo': {
    apiEndpoint: 'https://rest.nexmo.com',
    webhookPath: '/sms/vonage/webhook',
    requiresAccountId: false,
  },
  'AWS SNS': {
    apiEndpoint: 'sns.amazonaws.com',
    webhookPath: '/sms/aws/webhook',
    requiresApiKey: false,
  },
  'MessageBird': {
    apiEndpoint: 'https://rest.messagebird.com',
    webhookPath: '/sms/messagebird/webhook',
    requiresAccountId: false,
    requiresApiSecret: false,
  },
  'Infobip': {
    apiEndpoint: 'https://api.infobip.com',
    webhookPath: '/sms/infobip/webhook',
    requiresAccountId: false,
    requiresAuthToken: false,
  }
};

export const SMSConfiguration: React.FC<SMSConfigurationProps> = ({
  configuration,
  onConfigurationChange,
  mode = 'create'
}) => {
  const [localConfig, setLocalConfig] = useState<Record<string, any>>({
    // Provider configuration
    provider: 'Twilio',
    
    // Common authentication
    accountId: '',
    authToken: '',
    apiKey: '',
    apiSecret: '',
    
    // Sender configuration
    defaultSenderNumber: '',
    senderName: '',
    senderNumbers: '',
    
    // Message settings
    defaultMessageType: 'sms',
    maxMessageLength: 160,
    enableUnicode: true,
    enableConcatenation: true,
    maxConcatenatedParts: 10,
    
    // Delivery settings
    requestDeliveryReceipt: true,
    callbackUrl: '',
    messageValidityPeriod: 48,
    priority: 'NORMAL',
    
    // Rate limiting
    messagesPerSecond: 10,
    messagesPerMinute: 100,
    messagesPerHour: 1000,
    messagesPerDay: 10000,
    
    // Retry configuration
    maxRetries: 3,
    retryDelay: 5000,
    exponentialBackoff: true,
    
    // Provider-specific endpoints
    apiEndpoint: '',
    
    // Regional settings
    defaultCountryCode: '+1',
    blockedCountries: '',
    allowedCountries: '',
    
    // Content filtering
    enableContentFiltering: true,
    blockedKeywords: '',
    enableSpamDetection: true,
    
    // Number validation
    validateNumbers: true,
    enableNumberLookup: false,
    rejectLandlines: false,
    rejectVoip: false,
    
    // Opt-out management
    enableOptOutManagement: true,
    optOutKeywords: 'STOP,UNSUBSCRIBE,CANCEL,END,QUIT',
    optOutConfirmationMessage: 'You have been unsubscribed. Reply START to resubscribe.',
    
    // Analytics
    enableAnalytics: true,
    trackLinks: false,
    linkTrackingDomain: '',
    
    // Features
    enable2WayMessaging: false,
    enableKeywordProcessing: false,
    enableAutoResponse: false,
    enableScheduling: false,
    enableBulkMessaging: true,
    enablePersonalization: true,
    
    // Compliance settings
    enableTCPA: true,
    enableGDPR: true,
    requireExplicitConsent: true,
    honorQuietHours: true,
    quietHoursStart: '21:00',
    quietHoursEnd: '09:00',
    
    // Webhook settings
    webhookUrl: '',
    enableDeliveryReports: true,
    enableIncomingMessages: true,
    
    ...configuration
  });

  useEffect(() => {
    setLocalConfig(prev => ({ ...prev, ...configuration }));
  }, [configuration]);

  useEffect(() => {
    // Update provider-specific defaults when provider changes
    const provider = localConfig.provider;
    if (providerDefaults[provider]) {
      const defaults = providerDefaults[provider];
      handleChange('apiEndpoint', defaults.apiEndpoint);
      
      // Generate webhook URL if not set
      if (!localConfig.webhookUrl && defaults.webhookPath) {
        const baseUrl = window.location.origin;
        handleChange('webhookUrl', `${baseUrl}${defaults.webhookPath}`);
      }
    }
  }, [localConfig.provider, localConfig.webhookUrl, handleChange]);

  const handleChange = useCallback((field: string, value: any) => {
    const updatedConfig = { ...localConfig, [field]: value };
    setLocalConfig(updatedConfig);
    onConfigurationChange(updatedConfig);
  }, [localConfig, onConfigurationChange]);

  const handleNumberChange = (field: string, value: string) => {
    const numValue = parseInt(value) || 0;
    handleChange(field, numValue);
  };

  const validateConfiguration = () => {
    const errors: string[] = [];
    const provider = localConfig.provider;
    
    // Provider-specific validation
    switch (provider) {
      case 'Twilio':
        if (!localConfig.accountId) errors.push('Account SID is required for Twilio');
        if (!localConfig.authToken) errors.push('Auth Token is required for Twilio');
        break;
      case 'Vonage/Nexmo':
        if (!localConfig.apiKey) errors.push('API Key is required for Vonage/Nexmo');
        if (!localConfig.apiSecret) errors.push('API Secret is required for Vonage/Nexmo');
        break;
      case 'AWS SNS':
        if (!localConfig.apiKey) errors.push('Access Key ID is required for AWS SNS');
        if (!localConfig.apiSecret) errors.push('Secret Access Key is required for AWS SNS');
        break;
      case 'MessageBird':
        if (!localConfig.apiKey) errors.push('API Key is required for MessageBird');
        break;
      case 'Infobip':
        if (!localConfig.apiKey) errors.push('API Key is required for Infobip');
        break;
    }
    
    // Common validation
    if (!localConfig.defaultSenderNumber) {
      errors.push('Default sender number is required');
    }
    
    // Validate phone number format
    const phoneRegex = /^\+?[1-9]\d{6,14}$/;
    if (localConfig.defaultSenderNumber && !phoneRegex.test(localConfig.defaultSenderNumber)) {
      errors.push('Invalid sender number format. Use E.164 format (e.g., +1234567890)');
    }
    
    // Webhook URL validation
    if ((localConfig.enableDeliveryReports || localConfig.enableIncomingMessages) && !localConfig.webhookUrl) {
      errors.push('Webhook URL is required when delivery reports or incoming messages are enabled');
    }
    
    // Quiet hours validation
    if (localConfig.honorQuietHours) {
      if (!localConfig.quietHoursStart || !localConfig.quietHoursEnd) {
        errors.push('Quiet hours times are required when Honor Quiet Hours is enabled');
      }
    }
    
    return errors;
  };

  const errors = validateConfiguration();

  const getProviderInfo = (provider: string) => {
    const info: Record<string, any> = {
      'Twilio': {
        color: 'text-red-500',
        features: ['Two-way messaging', 'MMS support', 'WhatsApp integration', 'Programmable voice'],
        pricing: 'Pay-per-message',
        regions: 'Global coverage'
      },
      'Vonage/Nexmo': {
        color: 'text-purple-500',
        features: ['Two-way messaging', 'Number insights', 'Voice API', 'Verify API'],
        pricing: 'Pay-per-message',
        regions: 'Global coverage'
      },
      'AWS SNS': {
        color: 'text-orange-500',
        features: ['SMS & Push notifications', 'Topic-based messaging', 'Direct publish'],
        pricing: 'Pay-per-message + AWS fees',
        regions: 'Limited countries'
      },
      'MessageBird': {
        color: 'text-blue-500',
        features: ['Two-way messaging', 'Voice API', 'WhatsApp Business', 'Verify API'],
        pricing: 'Pay-per-message',
        regions: 'Europe-focused, global'
      },
      'Infobip': {
        color: 'text-green-500',
        features: ['Omnichannel messaging', 'Two-way messaging', 'Number lookup', 'Voice'],
        pricing: 'Pay-per-message',
        regions: 'Global coverage'
      }
    };
    
    return info[provider] || {};
  };

  const providerInfo = getProviderInfo(localConfig.provider);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Smartphone className="h-5 w-5" />
          SMS Configuration
        </CardTitle>
        <CardDescription>
          Configure SMS messaging provider and settings
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="provider" className="space-y-4">
          <TabsList className="grid w-full grid-cols-6">
            <TabsTrigger value="provider">Provider</TabsTrigger>
            <TabsTrigger value="messaging">Messaging</TabsTrigger>
            <TabsTrigger value="delivery">Delivery</TabsTrigger>
            <TabsTrigger value="compliance">Compliance</TabsTrigger>
            <TabsTrigger value="features">Features</TabsTrigger>
            <TabsTrigger value="webhooks">Webhooks</TabsTrigger>
          </TabsList>

          <TabsContent value="provider" className="space-y-4">
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="provider">SMS Provider *</Label>
                <Select
                  value={localConfig.provider}
                  onValueChange={(value) => handleChange('provider', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Twilio">Twilio</SelectItem>
                    <SelectItem value="Vonage/Nexmo">Vonage/Nexmo</SelectItem>
                    <SelectItem value="AWS SNS">AWS SNS</SelectItem>
                    <SelectItem value="MessageBird">MessageBird</SelectItem>
                    <SelectItem value="Infobip">Infobip</SelectItem>
                    <SelectItem value="Clickatell">Clickatell</SelectItem>
                    <SelectItem value="Plivo">Plivo</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {providerInfo.features && (
                <Alert>
                  <Info className="h-4 w-4" />
                  <AlertTitle className={providerInfo.color}>
                    {localConfig.provider} Features
                  </AlertTitle>
                  <AlertDescription>
                    <div className="mt-2 space-y-1">
                      <div className="flex flex-wrap gap-2 mb-2">
                        {providerInfo.features.map((feature: string) => (
                          <Badge key={feature} variant="secondary" className="text-xs">
                            {feature}
                          </Badge>
                        ))}
                      </div>
                      <div className="text-sm">
                        <strong>Pricing:</strong> {providerInfo.pricing}<br />
                        <strong>Coverage:</strong> {providerInfo.regions}
                      </div>
                    </div>
                  </AlertDescription>
                </Alert>
              )}

              <Separator />

              {/* Provider-specific authentication fields */}
              {localConfig.provider === 'Twilio' && (
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="accountId">Account SID *</Label>
                    <Input
                      id="accountId"
                      value={localConfig.accountId}
                      onChange={(e) => handleChange('accountId', e.target.value)}
                      placeholder="ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="authToken">Auth Token *</Label>
                    <Input
                      id="authToken"
                      type="password"
                      value={localConfig.authToken}
                      onChange={(e) => handleChange('authToken', e.target.value)}
                      placeholder="Your auth token"
                    />
                  </div>
                </div>
              )}

              {(localConfig.provider === 'Vonage/Nexmo' || 
                localConfig.provider === 'AWS SNS') && (
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="apiKey">
                      {localConfig.provider === 'AWS SNS' ? 'Access Key ID' : 'API Key'} *
                    </Label>
                    <Input
                      id="apiKey"
                      value={localConfig.apiKey}
                      onChange={(e) => handleChange('apiKey', e.target.value)}
                      placeholder="Your API key"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="apiSecret">
                      {localConfig.provider === 'AWS SNS' ? 'Secret Access Key' : 'API Secret'} *
                    </Label>
                    <Input
                      id="apiSecret"
                      type="password"
                      value={localConfig.apiSecret}
                      onChange={(e) => handleChange('apiSecret', e.target.value)}
                      placeholder="Your API secret"
                    />
                  </div>
                </div>
              )}

              {(localConfig.provider === 'MessageBird' || 
                localConfig.provider === 'Infobip') && (
                <div className="space-y-2">
                  <Label htmlFor="apiKey">API Key *</Label>
                  <Input
                    id="apiKey"
                    type="password"
                    value={localConfig.apiKey}
                    onChange={(e) => handleChange('apiKey', e.target.value)}
                    placeholder="Your API key"
                  />
                </div>
              )}

              <Separator />

              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="defaultSenderNumber">Default Sender Number *</Label>
                  <Input
                    id="defaultSenderNumber"
                    value={localConfig.defaultSenderNumber}
                    onChange={(e) => handleChange('defaultSenderNumber', e.target.value)}
                    placeholder="+1234567890"
                  />
                  <p className="text-sm text-muted-foreground">
                    Use E.164 format with country code
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="senderName">Sender Name (Alphanumeric)</Label>
                  <Input
                    id="senderName"
                    value={localConfig.senderName}
                    onChange={(e) => handleChange('senderName', e.target.value)}
                    placeholder="COMPANY"
                    maxLength={11}
                  />
                  <p className="text-sm text-muted-foreground">
                    11 characters max. Not supported in all countries.
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="senderNumbers">Additional Sender Numbers</Label>
                  <Textarea
                    id="senderNumbers"
                    value={localConfig.senderNumbers}
                    onChange={(e) => handleChange('senderNumbers', e.target.value)}
                    placeholder="+1234567890&#10;+0987654321"
                    rows={3}
                  />
                  <p className="text-sm text-muted-foreground">
                    One number per line for round-robin sending
                  </p>
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="messaging" className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="defaultMessageType">Default Message Type</Label>
                <Select
                  value={localConfig.defaultMessageType}
                  onValueChange={(value) => handleChange('defaultMessageType', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="sms">SMS</SelectItem>
                    <SelectItem value="mms">MMS</SelectItem>
                    <SelectItem value="flash">Flash SMS</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="maxMessageLength">Max Message Length</Label>
                <Input
                  id="maxMessageLength"
                  type="number"
                  value={localConfig.maxMessageLength}
                  onChange={(e) => handleNumberChange('maxMessageLength', e.target.value)}
                  placeholder="160"
                />
              </div>
            </div>

            <div className="space-y-4">
              <div className="flex items-center space-x-2">
                <Switch
                  id="enableUnicode"
                  checked={localConfig.enableUnicode}
                  onCheckedChange={(checked) => handleChange('enableUnicode', checked)}
                />
                <Label htmlFor="enableUnicode">Enable Unicode (70 chars per segment)</Label>
              </div>

              <div className="flex items-center space-x-2">
                <Switch
                  id="enableConcatenation"
                  checked={localConfig.enableConcatenation}
                  onCheckedChange={(checked) => handleChange('enableConcatenation', checked)}
                />
                <Label htmlFor="enableConcatenation">Enable Message Concatenation</Label>
              </div>

              {localConfig.enableConcatenation && (
                <div className="space-y-2 pl-6">
                  <Label htmlFor="maxConcatenatedParts">Max Concatenated Parts</Label>
                  <Input
                    id="maxConcatenatedParts"
                    type="number"
                    value={localConfig.maxConcatenatedParts}
                    onChange={(e) => handleNumberChange('maxConcatenatedParts', e.target.value)}
                    placeholder="10"
                  />
                </div>
              )}
            </div>

            <Separator />

            <div>
              <h4 className="text-sm font-medium mb-3">Rate Limiting</h4>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="messagesPerSecond">Messages per Second</Label>
                  <Input
                    id="messagesPerSecond"
                    type="number"
                    value={localConfig.messagesPerSecond}
                    onChange={(e) => handleNumberChange('messagesPerSecond', e.target.value)}
                    placeholder="10"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="messagesPerMinute">Messages per Minute</Label>
                  <Input
                    id="messagesPerMinute"
                    type="number"
                    value={localConfig.messagesPerMinute}
                    onChange={(e) => handleNumberChange('messagesPerMinute', e.target.value)}
                    placeholder="100"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="messagesPerHour">Messages per Hour</Label>
                  <Input
                    id="messagesPerHour"
                    type="number"
                    value={localConfig.messagesPerHour}
                    onChange={(e) => handleNumberChange('messagesPerHour', e.target.value)}
                    placeholder="1000"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="messagesPerDay">Messages per Day</Label>
                  <Input
                    id="messagesPerDay"
                    type="number"
                    value={localConfig.messagesPerDay}
                    onChange={(e) => handleNumberChange('messagesPerDay', e.target.value)}
                    placeholder="10000"
                  />
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="delivery" className="space-y-4">
            <div className="space-y-4">
              <div className="flex items-center space-x-2">
                <Switch
                  id="requestDeliveryReceipt"
                  checked={localConfig.requestDeliveryReceipt}
                  onCheckedChange={(checked) => handleChange('requestDeliveryReceipt', checked)}
                />
                <Label htmlFor="requestDeliveryReceipt">Request Delivery Receipts</Label>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="messageValidityPeriod">Message Validity Period (hours)</Label>
                  <Input
                    id="messageValidityPeriod"
                    type="number"
                    value={localConfig.messageValidityPeriod}
                    onChange={(e) => handleNumberChange('messageValidityPeriod', e.target.value)}
                    placeholder="48"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="priority">Delivery Priority</Label>
                  <Select
                    value={localConfig.priority}
                    onValueChange={(value) => handleChange('priority', value)}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="LOW">Low</SelectItem>
                      <SelectItem value="NORMAL">Normal</SelectItem>
                      <SelectItem value="HIGH">High</SelectItem>
                      <SelectItem value="URGENT">Urgent</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <Separator />

              <div>
                <h4 className="text-sm font-medium mb-3">Retry Configuration</h4>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="maxRetries">Max Retries</Label>
                    <Input
                      id="maxRetries"
                      type="number"
                      value={localConfig.maxRetries}
                      onChange={(e) => handleNumberChange('maxRetries', e.target.value)}
                      placeholder="3"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="retryDelay">Retry Delay (ms)</Label>
                    <Input
                      id="retryDelay"
                      type="number"
                      value={localConfig.retryDelay}
                      onChange={(e) => handleNumberChange('retryDelay', e.target.value)}
                      placeholder="5000"
                    />
                  </div>
                </div>

                <div className="flex items-center space-x-2 mt-4">
                  <Switch
                    id="exponentialBackoff"
                    checked={localConfig.exponentialBackoff}
                    onCheckedChange={(checked) => handleChange('exponentialBackoff', checked)}
                  />
                  <Label htmlFor="exponentialBackoff">Use Exponential Backoff</Label>
                </div>
              </div>

              <Separator />

              <div>
                <h4 className="text-sm font-medium mb-3">Number Validation</h4>
                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <Switch
                      id="validateNumbers"
                      checked={localConfig.validateNumbers}
                      onCheckedChange={(checked) => handleChange('validateNumbers', checked)}
                    />
                    <Label htmlFor="validateNumbers">Validate Phone Numbers</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableNumberLookup"
                      checked={localConfig.enableNumberLookup}
                      onCheckedChange={(checked) => handleChange('enableNumberLookup', checked)}
                    />
                    <Label htmlFor="enableNumberLookup">Enable Number Lookup (Additional cost)</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="rejectLandlines"
                      checked={localConfig.rejectLandlines}
                      onCheckedChange={(checked) => handleChange('rejectLandlines', checked)}
                    />
                    <Label htmlFor="rejectLandlines">Reject Landline Numbers</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="rejectVoip"
                      checked={localConfig.rejectVoip}
                      onCheckedChange={(checked) => handleChange('rejectVoip', checked)}
                    />
                    <Label htmlFor="rejectVoip">Reject VoIP Numbers</Label>
                  </div>
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="compliance" className="space-y-4">
            <Alert variant="default" className="mb-4">
              <AlertTriangle className="h-4 w-4" />
              <AlertTitle>Compliance Notice</AlertTitle>
              <AlertDescription>
                Ensure your SMS practices comply with local regulations. Non-compliance can result in 
                fines and service termination.
              </AlertDescription>
            </Alert>

            <div className="space-y-4">
              <h4 className="text-sm font-medium">Regulatory Compliance</h4>
              <div className="grid grid-cols-2 gap-4">
                <div className="flex items-center space-x-2">
                  <Switch
                    id="enableTCPA"
                    checked={localConfig.enableTCPA}
                    onCheckedChange={(checked) => handleChange('enableTCPA', checked)}
                  />
                  <Label htmlFor="enableTCPA">TCPA Compliance (US)</Label>
                </div>

                <div className="flex items-center space-x-2">
                  <Switch
                    id="enableGDPR"
                    checked={localConfig.enableGDPR}
                    onCheckedChange={(checked) => handleChange('enableGDPR', checked)}
                  />
                  <Label htmlFor="enableGDPR">GDPR Compliance (EU)</Label>
                </div>

                <div className="flex items-center space-x-2">
                  <Switch
                    id="requireExplicitConsent"
                    checked={localConfig.requireExplicitConsent}
                    onCheckedChange={(checked) => handleChange('requireExplicitConsent', checked)}
                  />
                  <Label htmlFor="requireExplicitConsent">Require Explicit Consent</Label>
                </div>

                <div className="flex items-center space-x-2">
                  <Switch
                    id="honorQuietHours"
                    checked={localConfig.honorQuietHours}
                    onCheckedChange={(checked) => handleChange('honorQuietHours', checked)}
                  />
                  <Label htmlFor="honorQuietHours">Honor Quiet Hours</Label>
                </div>
              </div>

              {localConfig.honorQuietHours && (
                <div className="grid grid-cols-2 gap-4 pl-6">
                  <div className="space-y-2">
                    <Label htmlFor="quietHoursStart">Quiet Hours Start</Label>
                    <Input
                      id="quietHoursStart"
                      type="time"
                      value={localConfig.quietHoursStart}
                      onChange={(e) => handleChange('quietHoursStart', e.target.value)}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="quietHoursEnd">Quiet Hours End</Label>
                    <Input
                      id="quietHoursEnd"
                      type="time"
                      value={localConfig.quietHoursEnd}
                      onChange={(e) => handleChange('quietHoursEnd', e.target.value)}
                    />
                  </div>
                </div>
              )}
            </div>

            <Separator />

            <div>
              <h4 className="text-sm font-medium mb-3">Opt-Out Management</h4>
              <div className="space-y-4">
                <div className="flex items-center space-x-2">
                  <Switch
                    id="enableOptOutManagement"
                    checked={localConfig.enableOptOutManagement}
                    onCheckedChange={(checked) => handleChange('enableOptOutManagement', checked)}
                  />
                  <Label htmlFor="enableOptOutManagement">Enable Opt-Out Management</Label>
                </div>

                {localConfig.enableOptOutManagement && (
                  <div className="space-y-4 pl-6">
                    <div className="space-y-2">
                      <Label htmlFor="optOutKeywords">Opt-Out Keywords</Label>
                      <Input
                        id="optOutKeywords"
                        value={localConfig.optOutKeywords}
                        onChange={(e) => handleChange('optOutKeywords', e.target.value)}
                        placeholder="STOP,UNSUBSCRIBE,CANCEL"
                      />
                      <p className="text-sm text-muted-foreground">
                        Comma-separated list of keywords
                      </p>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="optOutConfirmationMessage">Opt-Out Confirmation Message</Label>
                      <Textarea
                        id="optOutConfirmationMessage"
                        value={localConfig.optOutConfirmationMessage}
                        onChange={(e) => handleChange('optOutConfirmationMessage', e.target.value)}
                        rows={2}
                      />
                    </div>
                  </div>
                )}
              </div>
            </div>

            <Separator />

            <div>
              <h4 className="text-sm font-medium mb-3">Geographic Restrictions</h4>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="defaultCountryCode">Default Country Code</Label>
                  <Input
                    id="defaultCountryCode"
                    value={localConfig.defaultCountryCode}
                    onChange={(e) => handleChange('defaultCountryCode', e.target.value)}
                    placeholder="+1"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="allowedCountries">Allowed Countries</Label>
                  <Input
                    id="allowedCountries"
                    value={localConfig.allowedCountries}
                    onChange={(e) => handleChange('allowedCountries', e.target.value)}
                    placeholder="+1,+44,+61 (leave empty for all)"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="blockedCountries">Blocked Countries</Label>
                  <Input
                    id="blockedCountries"
                    value={localConfig.blockedCountries}
                    onChange={(e) => handleChange('blockedCountries', e.target.value)}
                    placeholder="+7,+86"
                  />
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="features" className="space-y-4">
            <div className="grid grid-cols-2 gap-6">
              <div>
                <h4 className="text-sm font-medium mb-3">Messaging Features</h4>
                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enable2WayMessaging"
                      checked={localConfig.enable2WayMessaging}
                      onCheckedChange={(checked) => handleChange('enable2WayMessaging', checked)}
                    />
                    <Label htmlFor="enable2WayMessaging">Enable 2-Way Messaging</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableKeywordProcessing"
                      checked={localConfig.enableKeywordProcessing}
                      onCheckedChange={(checked) => handleChange('enableKeywordProcessing', checked)}
                    />
                    <Label htmlFor="enableKeywordProcessing">Enable Keyword Processing</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableAutoResponse"
                      checked={localConfig.enableAutoResponse}
                      onCheckedChange={(checked) => handleChange('enableAutoResponse', checked)}
                    />
                    <Label htmlFor="enableAutoResponse">Enable Auto Response</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableScheduling"
                      checked={localConfig.enableScheduling}
                      onCheckedChange={(checked) => handleChange('enableScheduling', checked)}
                    />
                    <Label htmlFor="enableScheduling">Enable Message Scheduling</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableBulkMessaging"
                      checked={localConfig.enableBulkMessaging}
                      onCheckedChange={(checked) => handleChange('enableBulkMessaging', checked)}
                    />
                    <Label htmlFor="enableBulkMessaging">Enable Bulk Messaging</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enablePersonalization"
                      checked={localConfig.enablePersonalization}
                      onCheckedChange={(checked) => handleChange('enablePersonalization', checked)}
                    />
                    <Label htmlFor="enablePersonalization">Enable Personalization</Label>
                  </div>
                </div>
              </div>

              <div>
                <h4 className="text-sm font-medium mb-3">Content & Analytics</h4>
                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableContentFiltering"
                      checked={localConfig.enableContentFiltering}
                      onCheckedChange={(checked) => handleChange('enableContentFiltering', checked)}
                    />
                    <Label htmlFor="enableContentFiltering">Enable Content Filtering</Label>
                  </div>

                  {localConfig.enableContentFiltering && (
                    <div className="space-y-2 pl-6">
                      <Label htmlFor="blockedKeywords">Blocked Keywords</Label>
                      <Textarea
                        id="blockedKeywords"
                        value={localConfig.blockedKeywords}
                        onChange={(e) => handleChange('blockedKeywords', e.target.value)}
                        placeholder="spam&#10;inappropriate&#10;blocked"
                        rows={3}
                      />
                      <p className="text-sm text-muted-foreground">
                        One keyword per line
                      </p>
                    </div>
                  )}

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableSpamDetection"
                      checked={localConfig.enableSpamDetection}
                      onCheckedChange={(checked) => handleChange('enableSpamDetection', checked)}
                    />
                    <Label htmlFor="enableSpamDetection">Enable Spam Detection</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableAnalytics"
                      checked={localConfig.enableAnalytics}
                      onCheckedChange={(checked) => handleChange('enableAnalytics', checked)}
                    />
                    <Label htmlFor="enableAnalytics">Enable Analytics</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="trackLinks"
                      checked={localConfig.trackLinks}
                      onCheckedChange={(checked) => handleChange('trackLinks', checked)}
                    />
                    <Label htmlFor="trackLinks">Track Links</Label>
                  </div>

                  {localConfig.trackLinks && (
                    <div className="space-y-2 pl-6">
                      <Label htmlFor="linkTrackingDomain">Link Tracking Domain</Label>
                      <Input
                        id="linkTrackingDomain"
                        value={localConfig.linkTrackingDomain}
                        onChange={(e) => handleChange('linkTrackingDomain', e.target.value)}
                        placeholder="track.yourdomain.com"
                      />
                    </div>
                  )}
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="webhooks" className="space-y-4">
            <Alert>
              <Info className="h-4 w-4" />
              <AlertDescription>
                Configure webhooks to receive delivery reports and incoming messages in real-time.
              </AlertDescription>
            </Alert>

            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="webhookUrl">Webhook URL</Label>
                <Input
                  id="webhookUrl"
                  value={localConfig.webhookUrl}
                  onChange={(e) => handleChange('webhookUrl', e.target.value)}
                  placeholder="https://your-domain.com/sms/webhook"
                />
                <p className="text-sm text-muted-foreground">
                  HTTPS endpoint to receive SMS events
                </p>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="flex items-center space-x-2">
                  <Switch
                    id="enableDeliveryReports"
                    checked={localConfig.enableDeliveryReports}
                    onCheckedChange={(checked) => handleChange('enableDeliveryReports', checked)}
                  />
                  <Label htmlFor="enableDeliveryReports">Enable Delivery Reports</Label>
                </div>

                <div className="flex items-center space-x-2">
                  <Switch
                    id="enableIncomingMessages"
                    checked={localConfig.enableIncomingMessages}
                    onCheckedChange={(checked) => handleChange('enableIncomingMessages', checked)}
                  />
                  <Label htmlFor="enableIncomingMessages">Enable Incoming Messages</Label>
                </div>
              </div>

              {localConfig.webhookUrl && (
                <Alert>
                  <Shield className="h-4 w-4" />
                  <AlertTitle>Webhook Security</AlertTitle>
                  <AlertDescription>
                    <div className="mt-2 space-y-2 text-sm">
                      <div>Your webhook endpoint should:</div>
                      <ul className="list-disc list-inside space-y-1">
                        <li>Use HTTPS for secure communication</li>
                        <li>Validate webhook signatures (provider-specific)</li>
                        <li>Respond quickly (&lt;5 seconds)</li>
                        <li>Handle duplicate messages idempotently</li>
                      </ul>
                    </div>
                  </AlertDescription>
                </Alert>
              )}

              <div className="space-y-2">
                <Label>Webhook Event Types</Label>
                <div className="grid grid-cols-2 gap-4 p-4 border rounded-lg">
                  <div className="space-y-2">
                    <h5 className="text-sm font-medium">Delivery Reports</h5>
                    <div className="space-y-1 text-sm text-muted-foreground">
                      <div className="flex items-center gap-2">
                        <CheckCircle className="h-3 w-3" /> Delivered
                      </div>
                      <div className="flex items-center gap-2">
                        <AlertCircle className="h-3 w-3" /> Failed
                      </div>
                      <div className="flex items-center gap-2">
                        <Clock className="h-3 w-3" /> Pending
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <h5 className="text-sm font-medium">Message Events</h5>
                    <div className="space-y-1 text-sm text-muted-foreground">
                      <div className="flex items-center gap-2">
                        <MessageSquare className="h-3 w-3" /> Incoming SMS
                      </div>
                      <div className="flex items-center gap-2">
                        <Ban className="h-3 w-3" /> Opt-out
                      </div>
                      <div className="flex items-center gap-2">
                        <Key className="h-3 w-3" /> Keywords
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </TabsContent>
        </Tabs>

        {errors.length > 0 && (
          <Alert variant="destructive" className="mt-6">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              <ul className="list-disc list-inside">
                {errors.map((error, index) => (
                  <li key={index}>{error}</li>
                ))}
              </ul>
            </AlertDescription>
          </Alert>
        )}

        <div className="mt-6 pt-6 border-t">
          <div className="flex justify-between items-center">
            <div className="text-sm text-muted-foreground">
              SMS adapter configuration for {localConfig.provider}
            </div>
            <div className="flex gap-2">
              <Badge variant="outline">
                <Phone className="h-3 w-3 mr-1" />
                {localConfig.defaultSenderNumber || 'Not configured'}
              </Badge>
              <Badge variant="outline" className={providerInfo.color}>
                {localConfig.provider}
              </Badge>
              {localConfig.enable2WayMessaging && (
                <Badge variant="outline">
                  <ArrowLeftRight className="h-3 w-3 mr-1" />
                  2-Way
                </Badge>
              )}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};