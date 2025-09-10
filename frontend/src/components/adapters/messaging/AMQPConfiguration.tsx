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
import { Textarea } from '@/components/ui/textarea';
import { 
  Repeat, 
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
  Network,
  Cloud
} from 'lucide-react';

interface AMQPConfigurationProps {
  configuration: Record<string, any>;
  onConfigurationChange: (config: Record<string, any>) => void;
  mode?: 'create' | 'edit';
}

export const AMQPConfiguration: React.FC<AMQPConfigurationProps> = ({
  configuration,
  onConfigurationChange,
  mode = 'create'
}) => {
  const [localConfig, setLocalConfig] = useState<Record<string, any>>({
    // Connection settings
    host: 'localhost',
    port: 5672,
    username: '',
    password: '',
    connectionUrl: '',
    connectionTimeout: 30000,
    idleTimeout: 60000,
    
    // Container settings
    containerId: '',
    containerName: 'IntegrixsFlowBridge',
    maxFrameSize: 65536,
    channelMax: 65535,
    
    // Link settings
    linkName: '',
    linkRole: 'sender',
    linkCredit: 100,
    
    // Source/Target settings
    sourceAddress: '',
    targetAddress: '',
    distributionMode: 'move',
    
    // Session settings
    sessionWindowSize: 1024,
    sessionTimeout: 30000,
    
    // Transaction support
    enableTransactions: false,
    transactionTimeout: 60000,
    
    // Security settings
    saslMechanism: 'PLAIN',
    useSsl: false,
    trustStore: '',
    trustStorePassword: '',
    keyStore: '',
    keyStorePassword: '',
    sslProtocol: 'TLSv1.2',
    verifyHost: true,
    
    // Flow control
    enableFlowControl: true,
    incomingWindow: 1024,
    outgoingWindow: 1024,
    
    // Message settings
    durable: true,
    priority: 4,
    ttl: 0,
    
    // Error handling
    maxRetries: 3,
    retryDelay: 5000,
    enableDeadLettering: true,
    deadLetterAddress: '',
    
    // Performance tuning
    prefetchSize: 100,
    enableBatching: false,
    batchSize: 100,
    batchTimeout: 1000,
    
    // Advanced features
    enableMessageGrouping: false,
    groupId: '',
    enableLargeMessages: false,
    maxMessageSize: 1048576,
    
    // Protocol-specific settings
    brokerType: 'GENERIC',
    
    // Artemis-specific settings
    artemisAddressPrefix: '',
    artemisAutoCreateQueues: false,
    
    // Azure Service Bus settings
    azureNamespace: '',
    azureSharedAccessKeyName: '',
    azureSharedAccessKey: '',
    azureEntityPath: '',
    
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

  const generateConnectionUrl = () => {
    const protocol = localConfig.useSsl ? 'amqps' : 'amqp';
    const auth = localConfig.username ? `${localConfig.username}:${localConfig.password}@` : '';
    const url = `${protocol}://${auth}${localConfig.host}:${localConfig.port}`;
    handleChange('connectionUrl', url);
  };

  const validateConfiguration = () => {
    const errors: string[] = [];
    
    if (!localConfig.connectionUrl && !localConfig.host) {
      errors.push('Either Connection URL or Host is required');
    }
    
    if (!localConfig.connectionUrl && localConfig.saslMechanism !== 'ANONYMOUS' && !localConfig.username) {
      errors.push('Username is required for selected SASL mechanism');
    }
    
    if (localConfig.useSsl && !localConfig.trustStore) {
      errors.push('Trust Store is required when SSL is enabled');
    }
    
    if (!localConfig.sourceAddress && !localConfig.targetAddress) {
      errors.push('Either Source Address or Target Address must be specified');
    }
    
    return errors;
  };

  const errors = validateConfiguration();

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Repeat className="h-5 w-5" />
          AMQP 1.0 Configuration
        </CardTitle>
        <CardDescription>
          Configure Advanced Message Queuing Protocol settings
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="connection" className="space-y-4">
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="connection">Connection</TabsTrigger>
            <TabsTrigger value="addressing">Addressing</TabsTrigger>
            <TabsTrigger value="broker">Broker</TabsTrigger>
            <TabsTrigger value="performance">Performance</TabsTrigger>
            <TabsTrigger value="security">Security</TabsTrigger>
          </TabsList>

          <TabsContent value="connection" className="space-y-4">
            <Alert>
              <Info className="h-4 w-4" />
              <AlertDescription>
                You can either use a connection URL or specify individual connection parameters.
              </AlertDescription>
            </Alert>

            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="connectionUrl">Connection URL (Optional)</Label>
                <div className="flex gap-2">
                  <Input
                    id="connectionUrl"
                    value={localConfig.connectionUrl}
                    onChange={(e) => handleChange('connectionUrl', e.target.value)}
                    placeholder="amqp://user:pass@host:port"
                    className="flex-1"
                  />
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={generateConnectionUrl}
                  >
                    Generate
                  </Button>
                </div>
                <p className="text-sm text-muted-foreground">
                  If provided, this overrides individual connection settings
                </p>
              </div>

              <Separator />

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="host">Host</Label>
                  <Input
                    id="host"
                    value={localConfig.host}
                    onChange={(e) => handleChange('host', e.target.value)}
                    placeholder="amqp.example.com"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="port">Port</Label>
                  <Input
                    id="port"
                    type="number"
                    value={localConfig.port}
                    onChange={(e) => handleNumberChange('port', e.target.value)}
                    placeholder="5672"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="username">Username</Label>
                  <Input
                    id="username"
                    value={localConfig.username}
                    onChange={(e) => handleChange('username', e.target.value)}
                    placeholder="Username"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="password">Password</Label>
                  <Input
                    id="password"
                    type="password"
                    value={localConfig.password}
                    onChange={(e) => handleChange('password', e.target.value)}
                    placeholder="Password"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="connectionTimeout">Connection Timeout (ms)</Label>
                  <Input
                    id="connectionTimeout"
                    type="number"
                    value={localConfig.connectionTimeout}
                    onChange={(e) => handleNumberChange('connectionTimeout', e.target.value)}
                    placeholder="30000"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="idleTimeout">Idle Timeout (ms)</Label>
                  <Input
                    id="idleTimeout"
                    type="number"
                    value={localConfig.idleTimeout}
                    onChange={(e) => handleNumberChange('idleTimeout', e.target.value)}
                    placeholder="60000"
                  />
                </div>
              </div>

              <Separator />

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="containerId">Container ID</Label>
                  <Input
                    id="containerId"
                    value={localConfig.containerId}
                    onChange={(e) => handleChange('containerId', e.target.value)}
                    placeholder="Auto-generated if empty"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="containerName">Container Name</Label>
                  <Input
                    id="containerName"
                    value={localConfig.containerName}
                    onChange={(e) => handleChange('containerName', e.target.value)}
                    placeholder="IntegrixsFlowBridge"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="maxFrameSize">Max Frame Size</Label>
                  <Input
                    id="maxFrameSize"
                    type="number"
                    value={localConfig.maxFrameSize}
                    onChange={(e) => handleNumberChange('maxFrameSize', e.target.value)}
                    placeholder="65536"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="channelMax">Max Channels</Label>
                  <Input
                    id="channelMax"
                    type="number"
                    value={localConfig.channelMax}
                    onChange={(e) => handleNumberChange('channelMax', e.target.value)}
                    placeholder="65535"
                  />
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="addressing" className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2 col-span-2">
                <Label htmlFor="sourceAddress">Source Address (For Inbound)</Label>
                <Input
                  id="sourceAddress"
                  value={localConfig.sourceAddress}
                  onChange={(e) => handleChange('sourceAddress', e.target.value)}
                  placeholder="queue://my-queue or topic://my-topic"
                />
                <p className="text-sm text-muted-foreground">
                  Address to receive messages from (required for inbound adapters)
                </p>
              </div>

              <div className="space-y-2 col-span-2">
                <Label htmlFor="targetAddress">Target Address (For Outbound)</Label>
                <Input
                  id="targetAddress"
                  value={localConfig.targetAddress}
                  onChange={(e) => handleChange('targetAddress', e.target.value)}
                  placeholder="queue://target-queue or topic://target-topic"
                />
                <p className="text-sm text-muted-foreground">
                  Address to send messages to (required for outbound adapters)
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="linkName">Link Name</Label>
                <Input
                  id="linkName"
                  value={localConfig.linkName}
                  onChange={(e) => handleChange('linkName', e.target.value)}
                  placeholder="Auto-generated if empty"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="linkRole">Link Role</Label>
                <Select
                  value={localConfig.linkRole}
                  onValueChange={(value) => handleChange('linkRole', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="sender">Sender</SelectItem>
                    <SelectItem value="receiver">Receiver</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="distributionMode">Distribution Mode</Label>
                <Select
                  value={localConfig.distributionMode}
                  onValueChange={(value) => handleChange('distributionMode', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="move">Move (consume)</SelectItem>
                    <SelectItem value="copy">Copy (browse)</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="linkCredit">Link Credit</Label>
                <Input
                  id="linkCredit"
                  type="number"
                  value={localConfig.linkCredit}
                  onChange={(e) => handleNumberChange('linkCredit', e.target.value)}
                  placeholder="100"
                />
              </div>
            </div>

            <Separator />

            <div>
              <h4 className="text-sm font-medium mb-3">Dead Letter Settings</h4>
              <div className="space-y-4">
                <div className="flex items-center space-x-2">
                  <Switch
                    id="enableDeadLettering"
                    checked={localConfig.enableDeadLettering}
                    onCheckedChange={(checked) => handleChange('enableDeadLettering', checked)}
                  />
                  <Label htmlFor="enableDeadLettering">Enable Dead Lettering</Label>
                </div>

                {localConfig.enableDeadLettering && (
                  <div className="space-y-2 pl-6">
                    <Label htmlFor="deadLetterAddress">Dead Letter Address</Label>
                    <Input
                      id="deadLetterAddress"
                      value={localConfig.deadLetterAddress}
                      onChange={(e) => handleChange('deadLetterAddress', e.target.value)}
                      placeholder="queue://dead-letter-queue"
                    />
                  </div>
                )}
              </div>
            </div>
          </TabsContent>

          <TabsContent value="broker" className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="brokerType">Broker Type</Label>
              <Select
                value={localConfig.brokerType}
                onValueChange={(value) => handleChange('brokerType', value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="GENERIC">Generic AMQP</SelectItem>
                  <SelectItem value="ACTIVEMQ_ARTEMIS">ActiveMQ Artemis</SelectItem>
                  <SelectItem value="AZURE_SERVICE_BUS">Azure Service Bus</SelectItem>
                  <SelectItem value="QPID">Apache Qpid</SelectItem>
                  <SelectItem value="SOLACE">Solace</SelectItem>
                  <SelectItem value="IBM_MQ_AMQP">IBM MQ AMQP</SelectItem>
                  <SelectItem value="RABBITMQ_AMQP1">RabbitMQ AMQP 1.0</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {localConfig.brokerType === 'ACTIVEMQ_ARTEMIS' && (
              <div className="space-y-4 pt-4 border-t">
                <h4 className="text-sm font-medium">ActiveMQ Artemis Settings</h4>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="artemisAddressPrefix">Address Prefix</Label>
                    <Input
                      id="artemisAddressPrefix"
                      value={localConfig.artemisAddressPrefix}
                      onChange={(e) => handleChange('artemisAddressPrefix', e.target.value)}
                      placeholder="Optional prefix"
                    />
                  </div>

                  <div className="flex items-center space-x-2 self-end">
                    <Switch
                      id="artemisAutoCreateQueues"
                      checked={localConfig.artemisAutoCreateQueues}
                      onCheckedChange={(checked) => handleChange('artemisAutoCreateQueues', checked)}
                    />
                    <Label htmlFor="artemisAutoCreateQueues">Auto Create Queues</Label>
                  </div>
                </div>
              </div>
            )}

            {localConfig.brokerType === 'AZURE_SERVICE_BUS' && (
              <div className="space-y-4 pt-4 border-t">
                <h4 className="text-sm font-medium">Azure Service Bus Settings</h4>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="azureNamespace">Namespace</Label>
                    <Input
                      id="azureNamespace"
                      value={localConfig.azureNamespace}
                      onChange={(e) => handleChange('azureNamespace', e.target.value)}
                      placeholder="your-namespace.servicebus.windows.net"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="azureSharedAccessKeyName">Shared Access Key Name</Label>
                    <Input
                      id="azureSharedAccessKeyName"
                      value={localConfig.azureSharedAccessKeyName}
                      onChange={(e) => handleChange('azureSharedAccessKeyName', e.target.value)}
                      placeholder="RootManageSharedAccessKey"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="azureSharedAccessKey">Shared Access Key</Label>
                    <Input
                      id="azureSharedAccessKey"
                      type="password"
                      value={localConfig.azureSharedAccessKey}
                      onChange={(e) => handleChange('azureSharedAccessKey', e.target.value)}
                      placeholder="Your access key"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="azureEntityPath">Entity Path</Label>
                    <Input
                      id="azureEntityPath"
                      value={localConfig.azureEntityPath}
                      onChange={(e) => handleChange('azureEntityPath', e.target.value)}
                      placeholder="queue-name or topic-name"
                    />
                  </div>
                </div>
              </div>
            )}
          </TabsContent>

          <TabsContent value="performance" className="space-y-4">
            <div className="space-y-6">
              <div>
                <h4 className="text-sm font-medium mb-3">Flow Control</h4>
                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableFlowControl"
                      checked={localConfig.enableFlowControl}
                      onCheckedChange={(checked) => handleChange('enableFlowControl', checked)}
                    />
                    <Label htmlFor="enableFlowControl">Enable Flow Control</Label>
                  </div>

                  {localConfig.enableFlowControl && (
                    <div className="grid grid-cols-2 gap-4 pl-6">
                      <div className="space-y-2">
                        <Label htmlFor="incomingWindow">Incoming Window</Label>
                        <Input
                          id="incomingWindow"
                          type="number"
                          value={localConfig.incomingWindow}
                          onChange={(e) => handleNumberChange('incomingWindow', e.target.value)}
                          placeholder="1024"
                        />
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="outgoingWindow">Outgoing Window</Label>
                        <Input
                          id="outgoingWindow"
                          type="number"
                          value={localConfig.outgoingWindow}
                          onChange={(e) => handleNumberChange('outgoingWindow', e.target.value)}
                          placeholder="1024"
                        />
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <Separator />

              <div>
                <h4 className="text-sm font-medium mb-3">Message Batching</h4>
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
                    <div className="grid grid-cols-2 gap-4 pl-6">
                      <div className="space-y-2">
                        <Label htmlFor="batchSize">Batch Size</Label>
                        <Input
                          id="batchSize"
                          type="number"
                          value={localConfig.batchSize}
                          onChange={(e) => handleNumberChange('batchSize', e.target.value)}
                          placeholder="100"
                        />
                      </div>

                      <div className="space-y-2">
                        <Label htmlFor="batchTimeout">Batch Timeout (ms)</Label>
                        <Input
                          id="batchTimeout"
                          type="number"
                          value={localConfig.batchTimeout}
                          onChange={(e) => handleNumberChange('batchTimeout', e.target.value)}
                          placeholder="1000"
                        />
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <Separator />

              <div>
                <h4 className="text-sm font-medium mb-3">Advanced Features</h4>
                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableTransactions"
                      checked={localConfig.enableTransactions}
                      onCheckedChange={(checked) => handleChange('enableTransactions', checked)}
                    />
                    <Label htmlFor="enableTransactions">Enable Transactions</Label>
                  </div>

                  {localConfig.enableTransactions && (
                    <div className="space-y-2 pl-6">
                      <Label htmlFor="transactionTimeout">Transaction Timeout (ms)</Label>
                      <Input
                        id="transactionTimeout"
                        type="number"
                        value={localConfig.transactionTimeout}
                        onChange={(e) => handleNumberChange('transactionTimeout', e.target.value)}
                        placeholder="60000"
                      />
                    </div>
                  )}

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableMessageGrouping"
                      checked={localConfig.enableMessageGrouping}
                      onCheckedChange={(checked) => handleChange('enableMessageGrouping', checked)}
                    />
                    <Label htmlFor="enableMessageGrouping">Enable Message Grouping</Label>
                  </div>

                  {localConfig.enableMessageGrouping && (
                    <div className="space-y-2 pl-6">
                      <Label htmlFor="groupId">Group ID</Label>
                      <Input
                        id="groupId"
                        value={localConfig.groupId}
                        onChange={(e) => handleChange('groupId', e.target.value)}
                        placeholder="group-identifier"
                      />
                    </div>
                  )}

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enableLargeMessages"
                      checked={localConfig.enableLargeMessages}
                      onCheckedChange={(checked) => handleChange('enableLargeMessages', checked)}
                    />
                    <Label htmlFor="enableLargeMessages">Enable Large Messages</Label>
                  </div>

                  {localConfig.enableLargeMessages && (
                    <div className="space-y-2 pl-6">
                      <Label htmlFor="maxMessageSize">Max Message Size (bytes)</Label>
                      <Input
                        id="maxMessageSize"
                        type="number"
                        value={localConfig.maxMessageSize}
                        onChange={(e) => handleNumberChange('maxMessageSize', e.target.value)}
                        placeholder="1048576"
                      />
                    </div>
                  )}
                </div>
              </div>

              <Separator />

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="prefetchSize">Prefetch Size</Label>
                  <Slider
                    id="prefetchSize"
                    min={0}
                    max={1000}
                    step={10}
                    value={[localConfig.prefetchSize]}
                    onValueChange={(value) => handleChange('prefetchSize', value[0])}
                  />
                  <span className="text-sm text-muted-foreground">
                    {localConfig.prefetchSize} messages
                  </span>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="sessionWindowSize">Session Window Size</Label>
                  <Input
                    id="sessionWindowSize"
                    type="number"
                    value={localConfig.sessionWindowSize}
                    onChange={(e) => handleNumberChange('sessionWindowSize', e.target.value)}
                    placeholder="1024"
                  />
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="security" className="space-y-4">
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="saslMechanism">SASL Mechanism</Label>
                <Select
                  value={localConfig.saslMechanism}
                  onValueChange={(value) => handleChange('saslMechanism', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="PLAIN">PLAIN</SelectItem>
                    <SelectItem value="ANONYMOUS">ANONYMOUS</SelectItem>
                    <SelectItem value="EXTERNAL">EXTERNAL</SelectItem>
                    <SelectItem value="SCRAM-SHA-256">SCRAM-SHA-256</SelectItem>
                    <SelectItem value="SCRAM-SHA-512">SCRAM-SHA-512</SelectItem>
                    <SelectItem value="GSSAPI">GSSAPI</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="flex items-center space-x-2">
                <Switch
                  id="useSsl"
                  checked={localConfig.useSsl}
                  onCheckedChange={(checked) => handleChange('useSsl', checked)}
                />
                <Label htmlFor="useSsl">Enable SSL/TLS</Label>
              </div>

              {localConfig.useSsl && (
                <div className="space-y-4 pl-6">
                  <Alert>
                    <Shield className="h-4 w-4" />
                    <AlertDescription>
                      Configure SSL/TLS settings for secure AMQP communication
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
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
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

                    <div className="flex items-center space-x-2 self-end">
                      <Switch
                        id="verifyHost"
                        checked={localConfig.verifyHost}
                        onCheckedChange={(checked) => handleChange('verifyHost', checked)}
                      />
                      <Label htmlFor="verifyHost">Verify Host</Label>
                    </div>
                  </div>
                </div>
              )}
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
              AMQP 1.0 adapter configuration
            </div>
            <div className="flex gap-2">
              <Badge variant="outline">
                <Server className="h-3 w-3 mr-1" />
                {localConfig.connectionUrl || `${localConfig.host}:${localConfig.port}`}
              </Badge>
              {localConfig.useSsl && (
                <Badge variant="outline">
                  <Lock className="h-3 w-3 mr-1" />
                  SSL/TLS
                </Badge>
              )}
              <Badge variant="outline">
                <Database className="h-3 w-3 mr-1" />
                {localConfig.brokerType}
              </Badge>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};