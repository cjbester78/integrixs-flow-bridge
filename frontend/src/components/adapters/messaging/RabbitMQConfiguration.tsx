import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { Slider } from '@/components/ui/slider';
import { 
  Share2, 
  Server, 
  Lock, 
  Settings, 
  ArrowRightLeft, 
  AlertCircle,
  CheckCircle,
  Info,
  Shield,
  Zap,
  RefreshCw,
  Database,
  Network
} from 'lucide-react';

interface RabbitMQConfigurationProps {
  configuration: Record<string, any>;
  onConfigurationChange: (config: Record<string, any>) => void;
  mode?: 'create' | 'edit';
}

export const RabbitMQConfiguration: React.FC<RabbitMQConfigurationProps> = ({
  configuration,
  onConfigurationChange,
  mode = 'create'
}) => {
  const [localConfig, setLocalConfig] = useState<Record<string, any>>({
    // Connection settings
    host: 'localhost',
    port: 5672,
    virtualHost: '/',
    username: '',
    password: '',
    connectionTimeout: 60000,
    requestedHeartbeat: 60,
    
    // Exchange settings
    exchangeName: '',
    exchangeType: 'direct',
    exchangeDurable: true,
    exchangeAutoDelete: false,
    
    // Queue settings
    queueName: '',
    queueDurable: true,
    queueExclusive: false,
    queueAutoDelete: false,
    
    // Routing
    routingKey: '',
    bindingKey: '',
    
    // Consumer settings
    prefetchCount: 1,
    autoAck: false,
    exclusive: false,
    
    // Publisher settings
    publisherConfirms: true,
    publisherReturns: true,
    mandatory: false,
    deliveryMode: 'persistent',
    
    // Retry and error handling
    maxRetries: 3,
    retryDelay: 5000,
    enableDeadLetterExchange: true,
    deadLetterExchangeName: '',
    deadLetterRoutingKey: '',
    
    // SSL/TLS settings
    sslEnabled: false,
    trustStore: '',
    trustStorePassword: '',
    keyStore: '',
    keyStorePassword: '',
    sslProtocol: 'TLSv1.2',
    
    // Features
    enableMetrics: true,
    enableTracing: false,
    enableBatching: false,
    batchSize: 100,
    enableTransactions: false,
    
    // Cluster settings
    clusterEnabled: false,
    clusterAddresses: '',
    
    ...configuration
  });

  useEffect(() => {
    setLocalConfig(prev => ({ ...prev, ...configuration }));
  }, [configuration]);

  const handleChange = (field: string, value: any) => {
    const updatedConfig = { ...localConfig, [field]: value };
    setLocalConfig(updatedConfig);
    onConfigurationChange(updatedConfig);
  };

  const handleNumberChange = (field: string, value: string) => {
    const numValue = parseInt(value) || 0;
    handleChange(field, numValue);
  };

  const validateConfiguration = () => {
    const errors: string[] = [];
    
    if (!localConfig.host) errors.push('Host is required');
    if (!localConfig.username) errors.push('Username is required');
    if (!localConfig.password) errors.push('Password is required');
    
    if (!localConfig.exchangeName && !localConfig.queueName) {
      errors.push('Either Exchange Name or Queue Name must be specified');
    }
    
    if (localConfig.sslEnabled) {
      if (!localConfig.trustStore) errors.push('Trust Store is required when SSL is enabled');
    }
    
    return errors;
  };

  const errors = validateConfiguration();

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Share2 className="h-5 w-5" />
          RabbitMQ Configuration
        </CardTitle>
        <CardDescription>
          Configure RabbitMQ connection and messaging settings
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="connection" className="space-y-4">
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="connection">Connection</TabsTrigger>
            <TabsTrigger value="exchange">Exchange</TabsTrigger>
            <TabsTrigger value="queue">Queue</TabsTrigger>
            <TabsTrigger value="advanced">Advanced</TabsTrigger>
            <TabsTrigger value="security">Security</TabsTrigger>
          </TabsList>

          <TabsContent value="connection" className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="host">Host *</Label>
                <Input
                  id="host"
                  value={localConfig.host}
                  onChange={(e) => handleChange('host', e.target.value)}
                  placeholder="localhost or rabbitmq.example.com"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="port">Port *</Label>
                <Input
                  id="port"
                  type="number"
                  value={localConfig.port}
                  onChange={(e) => handleNumberChange('port', e.target.value)}
                  placeholder="5672"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="virtualHost">Virtual Host</Label>
                <Input
                  id="virtualHost"
                  value={localConfig.virtualHost}
                  onChange={(e) => handleChange('virtualHost', e.target.value)}
                  placeholder="/ (default)"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="username">Username *</Label>
                <Input
                  id="username"
                  value={localConfig.username}
                  onChange={(e) => handleChange('username', e.target.value)}
                  placeholder="guest"
                />
              </div>

              <div className="space-y-2 col-span-2">
                <Label htmlFor="password">Password *</Label>
                <Input
                  id="password"
                  type="password"
                  value={localConfig.password}
                  onChange={(e) => handleChange('password', e.target.value)}
                  placeholder="Enter password"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="connectionTimeout">Connection Timeout (ms)</Label>
                <Input
                  id="connectionTimeout"
                  type="number"
                  value={localConfig.connectionTimeout}
                  onChange={(e) => handleNumberChange('connectionTimeout', e.target.value)}
                  placeholder="60000"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="requestedHeartbeat">Heartbeat (seconds)</Label>
                <Input
                  id="requestedHeartbeat"
                  type="number"
                  value={localConfig.requestedHeartbeat}
                  onChange={(e) => handleNumberChange('requestedHeartbeat', e.target.value)}
                  placeholder="60"
                />
              </div>
            </div>

            {localConfig.clusterEnabled && (
              <div className="space-y-2">
                <Label htmlFor="clusterAddresses">Cluster Addresses</Label>
                <Input
                  id="clusterAddresses"
                  value={localConfig.clusterAddresses}
                  onChange={(e) => handleChange('clusterAddresses', e.target.value)}
                  placeholder="host1:5672,host2:5672,host3:5672"
                />
                <p className="text-sm text-muted-foreground">
                  Comma-separated list of cluster node addresses
                </p>
              </div>
            )}

            <div className="flex items-center space-x-2 pt-4">
              <Switch
                id="clusterEnabled"
                checked={localConfig.clusterEnabled}
                onCheckedChange={(checked) => handleChange('clusterEnabled', checked)}
              />
              <Label htmlFor="clusterEnabled">Enable Cluster Mode</Label>
            </div>
          </TabsContent>

          <TabsContent value="exchange" className="space-y-4">
            <Alert>
              <Info className="h-4 w-4" />
              <AlertDescription>
                Configure exchange settings for message routing. Leave empty to use default exchange.
              </AlertDescription>
            </Alert>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="exchangeName">Exchange Name</Label>
                <Input
                  id="exchangeName"
                  value={localConfig.exchangeName}
                  onChange={(e) => handleChange('exchangeName', e.target.value)}
                  placeholder="my-exchange"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="exchangeType">Exchange Type</Label>
                <Select
                  value={localConfig.exchangeType}
                  onValueChange={(value) => handleChange('exchangeType', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="direct">Direct</SelectItem>
                    <SelectItem value="topic">Topic</SelectItem>
                    <SelectItem value="fanout">Fanout</SelectItem>
                    <SelectItem value="headers">Headers</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="routingKey">Routing Key</Label>
                <Input
                  id="routingKey"
                  value={localConfig.routingKey}
                  onChange={(e) => handleChange('routingKey', e.target.value)}
                  placeholder="routing.key"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="bindingKey">Binding Key</Label>
                <Input
                  id="bindingKey"
                  value={localConfig.bindingKey}
                  onChange={(e) => handleChange('bindingKey', e.target.value)}
                  placeholder="binding.key"
                />
              </div>
            </div>

            <div className="space-y-4">
              <div className="flex items-center space-x-2">
                <Switch
                  id="exchangeDurable"
                  checked={localConfig.exchangeDurable}
                  onCheckedChange={(checked) => handleChange('exchangeDurable', checked)}
                />
                <Label htmlFor="exchangeDurable">Durable Exchange</Label>
              </div>

              <div className="flex items-center space-x-2">
                <Switch
                  id="exchangeAutoDelete"
                  checked={localConfig.exchangeAutoDelete}
                  onCheckedChange={(checked) => handleChange('exchangeAutoDelete', checked)}
                />
                <Label htmlFor="exchangeAutoDelete">Auto Delete Exchange</Label>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="queue" className="space-y-4">
            <Alert>
              <Info className="h-4 w-4" />
              <AlertDescription>
                Configure queue settings for message consumption. Required for inbound adapters.
              </AlertDescription>
            </Alert>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2 col-span-2">
                <Label htmlFor="queueName">Queue Name</Label>
                <Input
                  id="queueName"
                  value={localConfig.queueName}
                  onChange={(e) => handleChange('queueName', e.target.value)}
                  placeholder="my-queue"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="prefetchCount">Prefetch Count</Label>
                <div className="flex items-center space-x-2">
                  <Slider
                    id="prefetchCount"
                    min={1}
                    max={1000}
                    step={1}
                    value={[localConfig.prefetchCount]}
                    onValueChange={(value) => handleChange('prefetchCount', value[0])}
                    className="flex-1"
                  />
                  <span className="w-12 text-sm text-muted-foreground">
                    {localConfig.prefetchCount}
                  </span>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <div className="flex items-center space-x-2">
                <Switch
                  id="queueDurable"
                  checked={localConfig.queueDurable}
                  onCheckedChange={(checked) => handleChange('queueDurable', checked)}
                />
                <Label htmlFor="queueDurable">Durable Queue</Label>
              </div>

              <div className="flex items-center space-x-2">
                <Switch
                  id="queueExclusive"
                  checked={localConfig.queueExclusive}
                  onCheckedChange={(checked) => handleChange('queueExclusive', checked)}
                />
                <Label htmlFor="queueExclusive">Exclusive Queue</Label>
              </div>

              <div className="flex items-center space-x-2">
                <Switch
                  id="queueAutoDelete"
                  checked={localConfig.queueAutoDelete}
                  onCheckedChange={(checked) => handleChange('queueAutoDelete', checked)}
                />
                <Label htmlFor="queueAutoDelete">Auto Delete Queue</Label>
              </div>

              <div className="flex items-center space-x-2">
                <Switch
                  id="autoAck"
                  checked={localConfig.autoAck}
                  onCheckedChange={(checked) => handleChange('autoAck', checked)}
                />
                <Label htmlFor="autoAck">Auto Acknowledge Messages</Label>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="advanced" className="space-y-4">
            <div className="space-y-6">
              <div>
                <h4 className="text-sm font-medium mb-3">Publisher Settings</h4>
                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <Switch
                      id="publisherConfirms"
                      checked={localConfig.publisherConfirms}
                      onCheckedChange={(checked) => handleChange('publisherConfirms', checked)}
                    />
                    <Label htmlFor="publisherConfirms">Enable Publisher Confirms</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="publisherReturns"
                      checked={localConfig.publisherReturns}
                      onCheckedChange={(checked) => handleChange('publisherReturns', checked)}
                    />
                    <Label htmlFor="publisherReturns">Enable Publisher Returns</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="mandatory"
                      checked={localConfig.mandatory}
                      onCheckedChange={(checked) => handleChange('mandatory', checked)}
                    />
                    <Label htmlFor="mandatory">Mandatory Publishing</Label>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="deliveryMode">Delivery Mode</Label>
                    <Select
                      value={localConfig.deliveryMode}
                      onValueChange={(value) => handleChange('deliveryMode', value)}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="non-persistent">Non-Persistent</SelectItem>
                        <SelectItem value="persistent">Persistent</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>

              <Separator />

              <div>
                <h4 className="text-sm font-medium mb-3">Error Handling</h4>
                <div className="space-y-4">
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

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableDeadLetterExchange"
                      checked={localConfig.enableDeadLetterExchange}
                      onCheckedChange={(checked) => handleChange('enableDeadLetterExchange', checked)}
                    />
                    <Label htmlFor="enableDeadLetterExchange">Enable Dead Letter Exchange</Label>
                  </div>

                  {localConfig.enableDeadLetterExchange && (
                    <div className="grid grid-cols-2 gap-4 pl-6">
                      <div className="space-y-2">
                        <Label htmlFor="deadLetterExchangeName">Dead Letter Exchange</Label>
                        <Input
                          id="deadLetterExchangeName"
                          value={localConfig.deadLetterExchangeName}
                          onChange={(e) => handleChange('deadLetterExchangeName', e.target.value)}
                          placeholder="dlx.exchange"
                        />
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="deadLetterRoutingKey">Dead Letter Routing Key</Label>
                        <Input
                          id="deadLetterRoutingKey"
                          value={localConfig.deadLetterRoutingKey}
                          onChange={(e) => handleChange('deadLetterRoutingKey', e.target.value)}
                          placeholder="dlx.routing.key"
                        />
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <Separator />

              <div>
                <h4 className="text-sm font-medium mb-3">Performance & Features</h4>
                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableBatching"
                      checked={localConfig.enableBatching}
                      onCheckedChange={(checked) => handleChange('enableBatching', checked)}
                    />
                    <Label htmlFor="enableBatching">Enable Message Batching</Label>
                  </div>

                  {localConfig.enableBatching && (
                    <div className="space-y-2 pl-6">
                      <Label htmlFor="batchSize">Batch Size</Label>
                      <Input
                        id="batchSize"
                        type="number"
                        value={localConfig.batchSize}
                        onChange={(e) => handleNumberChange('batchSize', e.target.value)}
                        placeholder="100"
                      />
                    </div>
                  )}

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableTransactions"
                      checked={localConfig.enableTransactions}
                      onCheckedChange={(checked) => handleChange('enableTransactions', checked)}
                    />
                    <Label htmlFor="enableTransactions">Enable Transactions</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableMetrics"
                      checked={localConfig.enableMetrics}
                      onCheckedChange={(checked) => handleChange('enableMetrics', checked)}
                    />
                    <Label htmlFor="enableMetrics">Enable Metrics Collection</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableTracing"
                      checked={localConfig.enableTracing}
                      onCheckedChange={(checked) => handleChange('enableTracing', checked)}
                    />
                    <Label htmlFor="enableTracing">Enable Distributed Tracing</Label>
                  </div>
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="security" className="space-y-4">
            <div className="flex items-center space-x-2 mb-4">
              <Switch
                id="sslEnabled"
                checked={localConfig.sslEnabled}
                onCheckedChange={(checked) => handleChange('sslEnabled', checked)}
              />
              <Label htmlFor="sslEnabled">Enable SSL/TLS</Label>
            </div>

            {localConfig.sslEnabled && (
              <div className="space-y-4">
                <Alert>
                  <Shield className="h-4 w-4" />
                  <AlertDescription>
                    Configure SSL/TLS settings for secure communication with RabbitMQ
                  </AlertDescription>
                </Alert>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="trustStore">Trust Store Path</Label>
                    <Input
                      id="trustStore"
                      value={localConfig.trustStore}
                      onChange={(e) => handleChange('trustStore', e.target.value)}
                      placeholder="/path/to/truststore.jks"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="trustStorePassword">Trust Store Password</Label>
                    <Input
                      id="trustStorePassword"
                      type="password"
                      value={localConfig.trustStorePassword}
                      onChange={(e) => handleChange('trustStorePassword', e.target.value)}
                      placeholder="Password"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="keyStore">Key Store Path (Client Certificate)</Label>
                    <Input
                      id="keyStore"
                      value={localConfig.keyStore}
                      onChange={(e) => handleChange('keyStore', e.target.value)}
                      placeholder="/path/to/keystore.jks"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="keyStorePassword">Key Store Password</Label>
                    <Input
                      id="keyStorePassword"
                      type="password"
                      value={localConfig.keyStorePassword}
                      onChange={(e) => handleChange('keyStorePassword', e.target.value)}
                      placeholder="Password"
                    />
                  </div>

                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="sslProtocol">SSL Protocol</Label>
                    <Select
                      value={localConfig.sslProtocol}
                      onValueChange={(value) => handleChange('sslProtocol', value)}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="TLSv1.2">TLS v1.2</SelectItem>
                        <SelectItem value="TLSv1.3">TLS v1.3</SelectItem>
                        <SelectItem value="TLS">TLS (Auto)</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>
            )}
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
              RabbitMQ adapter configuration
            </div>
            <div className="flex gap-2">
              <Badge variant="outline">
                <Server className="h-3 w-3 mr-1" />
                {localConfig.host}:{localConfig.port}
              </Badge>
              {localConfig.sslEnabled && (
                <Badge variant="outline">
                  <Lock className="h-3 w-3 mr-1" />
                  SSL/TLS
                </Badge>
              )}
              {localConfig.clusterEnabled && (
                <Badge variant="outline">
                  <Network className="h-3 w-3 mr-1" />
                  Cluster
                </Badge>
              )}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};