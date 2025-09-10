import React, { useState, useCallback } from 'react';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { ConnectionTester } from './ConnectionTester';
import {
  Save,
  X,
  AlertCircle,
  Settings,
  Wifi,
  Shield,
  Key
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import type { AdapterType, CommunicationAdapter } from '@/types/communicationAdapter';

interface AdapterConfigurationDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (adapter: Partial<CommunicationAdapter>) => void;
  adapter?: CommunicationAdapter;
  mode: 'create' | 'edit';
}

const ADAPTER_TYPES: { value: AdapterType; label: string }[] = [
  { value: 'REST', label: 'REST API' },
  { value: 'SOAP', label: 'SOAP Web Service' },
  { value: 'DATABASE', label: 'Database' },
  { value: 'FILE', label: 'File System' },
  { value: 'IBMMQ', label: 'IBM MQ' },
  { value: 'RABBITMQ', label: 'RabbitMQ' },
  { value: 'KAFKA', label: 'Kafka' },
  { value: 'SFTP', label: 'SFTP' },
  { value: 'EMAIL', label: 'Email' }
];

export function AdapterConfigurationDialog({
  isOpen,
  onClose,
  onSave,
  adapter,
  mode
}: AdapterConfigurationDialogProps) {
  const { toast } = useToast();
  const [formData, setFormData] = useState<Partial<CommunicationAdapter>>({
    name: adapter?.name || '',
    type: adapter?.type || 'REST',
    configuration: adapter?.configuration || {},
    active: adapter?.active ?? true,
    ...adapter
  });
  
  const [connectionTestPassed, setConnectionTestPassed] = useState(false);
  const [activeTab, setActiveTab] = useState('configuration');

  const handleFieldChange = useCallback((field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  }, []);

  const handleConfigChange = useCallback((key: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      configuration: {
        ...prev.configuration,
        [key]: value
      }
    }));
  }, []);

  const handleSave = useCallback(() => {
    if (!formData.name || !formData.type) {
      toast({
        title: 'Validation Error',
        description: 'Please provide adapter name and type',
        variant: 'destructive'
      });
      return;
    }

    if (!connectionTestPassed && mode === 'create') {
      toast({
        title: 'Connection Not Tested',
        description: 'It is recommended to test the connection before saving',
        variant: 'warning'
      });
    }

    onSave(formData);
  }, [formData, connectionTestPassed, mode, onSave, toast]);

  const renderConfigurationFields = () => {
    switch (formData.type) {
      case 'REST':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="endpoint">Endpoint URL *</Label>
              <Input
                id="endpoint"
                value={formData.configuration?.endpoint || ''}
                onChange={(e) => handleConfigChange('endpoint', e.target.value)}
                placeholder="https://api.example.com"
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="method">HTTP Method</Label>
              <Select
                value={formData.configuration?.method || 'GET'}
                onValueChange={(value) => handleConfigChange('method', value)}
              >
                <SelectTrigger id="method" className="mt-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="GET">GET</SelectItem>
                  <SelectItem value="POST">POST</SelectItem>
                  <SelectItem value="PUT">PUT</SelectItem>
                  <SelectItem value="DELETE">DELETE</SelectItem>
                  <SelectItem value="PATCH">PATCH</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="authType">Authentication Type</Label>
              <Select
                value={formData.configuration?.authType || 'NONE'}
                onValueChange={(value) => handleConfigChange('authType', value)}
              >
                <SelectTrigger id="authType" className="mt-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="NONE">None</SelectItem>
                  <SelectItem value="BASIC">Basic Auth</SelectItem>
                  <SelectItem value="BEARER">Bearer Token</SelectItem>
                  <SelectItem value="API_KEY">API Key</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {formData.configuration?.authType === 'BASIC' && (
              <>
                <div>
                  <Label htmlFor="username">Username</Label>
                  <Input
                    id="username"
                    value={formData.configuration?.username || ''}
                    onChange={(e) => handleConfigChange('username', e.target.value)}
                    className="mt-1"
                  />
                </div>
                <div>
                  <Label htmlFor="password">Password</Label>
                  <Input
                    id="password"
                    type="password"
                    value={formData.configuration?.password || ''}
                    onChange={(e) => handleConfigChange('password', e.target.value)}
                    className="mt-1"
                  />
                </div>
              </>
            )}
            {formData.configuration?.authType === 'BEARER' && (
              <div>
                <Label htmlFor="token">Bearer Token</Label>
                <Textarea
                  id="token"
                  value={formData.configuration?.token || ''}
                  onChange={(e) => handleConfigChange('token', e.target.value)}
                  placeholder="Enter bearer token"
                  className="mt-1"
                  rows={3}
                />
              </div>
            )}
          </div>
        );
        
      case 'SOAP':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="wsdlUrl">WSDL URL *</Label>
              <Input
                id="wsdlUrl"
                value={formData.configuration?.wsdlUrl || ''}
                onChange={(e) => handleConfigChange('wsdlUrl', e.target.value)}
                placeholder="https://example.com/service?wsdl"
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="soapEndpoint">SOAP Endpoint</Label>
              <Input
                id="soapEndpoint"
                value={formData.configuration?.endpoint || ''}
                onChange={(e) => handleConfigChange('endpoint', e.target.value)}
                placeholder="https://example.com/service"
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="soapAction">SOAP Action</Label>
              <Input
                id="soapAction"
                value={formData.configuration?.soapAction || ''}
                onChange={(e) => handleConfigChange('soapAction', e.target.value)}
                placeholder="http://example.com/ServiceAction"
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="soapVersion">SOAP Version</Label>
              <Select
                value={formData.configuration?.soapVersion || '1.1'}
                onValueChange={(value) => handleConfigChange('soapVersion', value)}
              >
                <SelectTrigger id="soapVersion" className="mt-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1.1">SOAP 1.1</SelectItem>
                  <SelectItem value="1.2">SOAP 1.2</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        );
        
      case 'DATABASE':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="jdbcUrl">JDBC URL *</Label>
              <Input
                id="jdbcUrl"
                value={formData.configuration?.jdbcUrl || ''}
                onChange={(e) => handleConfigChange('jdbcUrl', e.target.value)}
                placeholder="jdbc:postgresql://localhost:5432/mydb"
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="driverClassName">Driver Class Name</Label>
              <Select
                value={formData.configuration?.driverClassName || ''}
                onValueChange={(value) => handleConfigChange('driverClassName', value)}
              >
                <SelectTrigger id="driverClassName" className="mt-1">
                  <SelectValue placeholder="Select driver" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="org.postgresql.Driver">PostgreSQL</SelectItem>
                  <SelectItem value="com.mysql.cj.jdbc.Driver">MySQL</SelectItem>
                  <SelectItem value="oracle.jdbc.OracleDriver">Oracle</SelectItem>
                  <SelectItem value="com.microsoft.sqlserver.jdbc.SQLServerDriver">SQL Server</SelectItem>
                  <SelectItem value="org.h2.Driver">H2</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="dbUsername">Username</Label>
              <Input
                id="dbUsername"
                value={formData.configuration?.username || ''}
                onChange={(e) => handleConfigChange('username', e.target.value)}
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="dbPassword">Password</Label>
              <Input
                id="dbPassword"
                type="password"
                value={formData.configuration?.password || ''}
                onChange={(e) => handleConfigChange('password', e.target.value)}
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="connectionPool">Enable Connection Pooling</Label>
              <div className="flex items-center space-x-2 mt-1">
                <Switch
                  id="connectionPool"
                  checked={formData.configuration?.enablePooling || false}
                  onCheckedChange={(checked) => handleConfigChange('enablePooling', checked)}
                />
                <Label htmlFor="connectionPool" className="text-sm text-muted-foreground">
                  Use connection pooling for better performance
                </Label>
              </div>
            </div>
          </div>
        );
        
      case 'IBMMQ':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="brokerUrl">Broker URL *</Label>
              <Input
                id="brokerUrl"
                value={formData.configuration?.brokerUrl || ''}
                onChange={(e) => handleConfigChange('brokerUrl', e.target.value)}
                placeholder="tcp://localhost:61616"
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="destinationType">Destination Type</Label>
              <Select
                value={formData.configuration?.destinationType || 'QUEUE'}
                onValueChange={(value) => handleConfigChange('destinationType', value)}
              >
                <SelectTrigger id="destinationType" className="mt-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="QUEUE">Queue</SelectItem>
                  <SelectItem value="TOPIC">Topic</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="destinationName">Destination Name *</Label>
              <Input
                id="destinationName"
                value={formData.configuration?.destinationName || ''}
                onChange={(e) => handleConfigChange('destinationName', e.target.value)}
                placeholder="myQueue"
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="jmsUsername">Username</Label>
              <Input
                id="jmsUsername"
                value={formData.configuration?.username || ''}
                onChange={(e) => handleConfigChange('username', e.target.value)}
                className="mt-1"
              />
            </div>
            <div>
              <Label htmlFor="jmsPassword">Password</Label>
              <Input
                id="jmsPassword"
                type="password"
                value={formData.configuration?.password || ''}
                onChange={(e) => handleConfigChange('password', e.target.value)}
                className="mt-1"
              />
            </div>
          </div>
        );
        
      default:
        return (
          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              Configuration for {formData.type} adapter type is not yet implemented
            </AlertDescription>
          </Alert>
        );
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle>
            {mode === 'create' ? 'Create New Adapter' : 'Edit Adapter'}
          </DialogTitle>
          <DialogDescription>
            Configure the adapter settings and test the connection
          </DialogDescription>
        </DialogHeader>
        
        <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 overflow-hidden">
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="configuration" className="gap-2">
              <Settings className="h-4 w-4" />
              Configuration
            </TabsTrigger>
            <TabsTrigger value="authentication" className="gap-2">
              <Shield className="h-4 w-4" />
              Security
            </TabsTrigger>
            <TabsTrigger value="testing" className="gap-2">
              <Wifi className="h-4 w-4" />
              Connection Test
            </TabsTrigger>
          </TabsList>
          
          <div className="flex-1 overflow-y-auto">
            <TabsContent value="configuration" className="space-y-6 p-4">
              <Card>
                <CardHeader>
                  <CardTitle>Basic Information</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <Label htmlFor="adapterName">Adapter Name *</Label>
                    <Input
                      id="adapterName"
                      value={formData.name}
                      onChange={(e) => handleFieldChange('name', e.target.value)}
                      placeholder="Enter adapter name"
                      className="mt-1"
                    />
                  </div>
                  
                  <div>
                    <Label htmlFor="adapterType">Adapter Type *</Label>
                    <Select
                      value={formData.type}
                      onValueChange={(value) => handleFieldChange('type', value as AdapterType)}
                      disabled={mode === 'edit'}
                    >
                      <SelectTrigger id="adapterType" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {ADAPTER_TYPES.map(type => (
                          <SelectItem key={type.value} value={type.value}>
                            {type.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="flex items-center space-x-2">
                    <Switch
                      id="active"
                      checked={formData.active}
                      onCheckedChange={(checked) => handleFieldChange('active', checked)}
                    />
                    <Label htmlFor="active">Active</Label>
                  </div>
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader>
                  <CardTitle>Connection Settings</CardTitle>
                  <CardDescription>
                    Configure the connection parameters for {formData.type} adapter
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {renderConfigurationFields()}
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="authentication" className="space-y-6 p-4">
              <Card>
                <CardHeader>
                  <CardTitle>Security Configuration</CardTitle>
                  <CardDescription>
                    Configure authentication and security settings
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <Switch
                        id="useSsl"
                        checked={formData.configuration?.useSsl || false}
                        onCheckedChange={(checked) => handleConfigChange('useSsl', checked)}
                      />
                      <Label htmlFor="useSsl">Use SSL/TLS</Label>
                    </div>
                    
                    <div className="flex items-center space-x-2">
                      <Switch
                        id="validateCertificate"
                        checked={formData.configuration?.validateCertificate ?? true}
                        onCheckedChange={(checked) => handleConfigChange('validateCertificate', checked)}
                      />
                      <Label htmlFor="validateCertificate">Validate SSL Certificate</Label>
                    </div>
                    
                    <div>
                      <Label htmlFor="timeout">Connection Timeout (seconds)</Label>
                      <Input
                        id="timeout"
                        type="number"
                        value={formData.configuration?.timeout || 30}
                        onChange={(e) => handleConfigChange('timeout', parseInt(e.target.value))}
                        min={1}
                        max={300}
                        className="mt-1"
                      />
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="testing" className="space-y-6 p-4">
              <ConnectionTester
                adapterType={formData.type!}
                adapterName={formData.name || 'Unnamed Adapter'}
                configuration={formData.configuration || {}}
                onTestComplete={(result) => {
                  setConnectionTestPassed(result.success);
                }}
              />
            </TabsContent>
          </div>
        </Tabs>
        
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            <X className="mr-2 h-4 w-4" />
            Cancel
          </Button>
          <Button onClick={handleSave} disabled={!formData.name || !formData.type}>
            <Save className="mr-2 h-4 w-4" />
            {mode === 'create' ? 'Create' : 'Update'} Adapter
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}