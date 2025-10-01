import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Loader2, Save, RefreshCw, Download, Upload, Lock, Unlock, AlertCircle, Settings, Database, Shield, Mail, Bell, Cpu, Globe } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { systemConfigService } from '@/services/systemConfigService';

interface ConfigurationCategory {
  id: string;
  code: string;
  name: string;
  description: string;
  icon: React.ReactNode;
  subcategories?: ConfigurationCategory[];
}

interface Configuration {
  id: string;
  categoryId: string;
  configKey: string;
  configValue: string;
  configType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON' | 'YAML' | 'ENCRYPTED';
  defaultValue?: string;
  description?: string;
  isRequired: boolean;
  isEncrypted: boolean;
  isSensitive: boolean;
  validationRules?: any;
  allowedValues?: string[];
  environment?: string;
  profile?: string;
}

const SystemConfiguration: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = useState<string>('general');
  const [editedConfigs, setEditedConfigs] = useState<Record<string, string>>({});
  const [showSensitive, setShowSensitive] = useState<Record<string, boolean>>({});
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedEnvironment, setSelectedEnvironment] = useState<string>('all');
  
  const { toast } = useToast();
  const queryClient = useQueryClient();

  // Configuration categories with icons
  const categories: ConfigurationCategory[] = [
    { id: 'general', code: 'general', name: 'General', description: 'General application settings', icon: <Settings className="h-4 w-4" /> },
    { id: 'server', code: 'server', name: 'Server', description: 'Server and web configurations', icon: <Globe className="h-4 w-4" /> },
    { id: 'database', code: 'database', name: 'Database', description: 'Database connections and pools', icon: <Database className="h-4 w-4" /> },
    { id: 'messaging', code: 'messaging', name: 'Messaging', description: 'Message broker configurations', icon: <Mail className="h-4 w-4" />,
      subcategories: [
        { id: 'rabbitmq', code: 'rabbitmq', name: 'RabbitMQ', description: 'RabbitMQ settings', icon: null },
        { id: 'kafka', code: 'kafka', name: 'Kafka', description: 'Apache Kafka settings', icon: null },
      ]
    },
    { id: 'security', code: 'security', name: 'Security', description: 'Security and authentication', icon: <Shield className="h-4 w-4" />,
      subcategories: [
        { id: 'jwt', code: 'jwt', name: 'JWT', description: 'JWT authentication', icon: null },
        { id: 'oauth2', code: 'oauth2', name: 'OAuth2', description: 'OAuth2 settings', icon: null },
        { id: 'cors', code: 'cors', name: 'CORS', description: 'CORS settings', icon: null },
      ]
    },
    { id: 'monitoring', code: 'monitoring', name: 'Monitoring', description: 'Monitoring and metrics', icon: <Bell className="h-4 w-4" /> },
    { id: 'integrations', code: 'integrations', name: 'Integrations', description: 'Third-party integrations', icon: <Cpu className="h-4 w-4" />,
      subcategories: [
        { id: 'camunda', code: 'camunda', name: 'Camunda', description: 'Camunda BPM', icon: null },
        { id: 'hazelcast', code: 'hazelcast', name: 'Hazelcast', description: 'Hazelcast cache', icon: null },
      ]
    },
  ];

  // Fetch configurations for selected category
  const { data: configurations, isLoading, error } = useQuery({
    queryKey: ['configurations', selectedCategory, selectedEnvironment],
    queryFn: () => systemConfigService.getConfigurationsByCategory(selectedCategory, selectedEnvironment === 'all' ? undefined : selectedEnvironment),
  });

  // Save configurations mutation
  const saveMutation = useMutation({
    mutationFn: (configs: Record<string, string>) => 
      systemConfigService.updateConfigurations(configs),
    onSuccess: () => {
      toast({
        title: 'Success',
        description: 'Configurations saved successfully',
      });
      queryClient.invalidateQueries({ queryKey: ['configurations'] });
      setEditedConfigs({});
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: 'Failed to save configurations',
        variant: 'destructive',
      });
    },
  });

  // Refresh configurations
  const refreshMutation = useMutation({
    mutationFn: () => systemConfigService.refreshConfigurations(),
    onSuccess: () => {
      toast({
        title: 'Success',
        description: 'Configurations refreshed from database',
      });
      queryClient.invalidateQueries({ queryKey: ['configurations'] });
    },
  });

  // Handle configuration value change
  const handleConfigChange = (key: string, value: string) => {
    setEditedConfigs(prev => ({ ...prev, [key]: value }));
  };

  // Render configuration input based on type
  const renderConfigInput = (config: Configuration) => {
    const currentValue = editedConfigs[config.configKey] ?? config.configValue;
    const isEdited = editedConfigs.hasOwnProperty(config.configKey);

    switch (config.configType) {
      case 'BOOLEAN':
        return (
          <div className="flex items-center space-x-2">
            <Switch
              id={config.configKey}
              checked={currentValue === 'true'}
              onCheckedChange={(checked) => handleConfigChange(config.configKey, checked.toString())}
            />
            <Label htmlFor={config.configKey}>{currentValue === 'true' ? 'Enabled' : 'Disabled'}</Label>
          </div>
        );

      case 'NUMBER':
        return (
          <Input
            type="number"
            value={currentValue}
            onChange={(e) => handleConfigChange(config.configKey, e.target.value)}
            className={isEdited ? 'border-blue-500' : ''}
          />
        );

      case 'JSON':
      case 'YAML':
        return (
          <Textarea
            value={currentValue}
            onChange={(e) => handleConfigChange(config.configKey, e.target.value)}
            className={`font-mono text-sm ${isEdited ? 'border-blue-500' : ''}`}
            rows={4}
          />
        );

      case 'ENCRYPTED':
        return (
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Input
                type={showSensitive[config.configKey] ? 'text' : 'password'}
                value={currentValue}
                onChange={(e) => handleConfigChange(config.configKey, e.target.value)}
                className={isEdited ? 'border-blue-500' : ''}
              />
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={() => setShowSensitive(prev => ({ 
                  ...prev, 
                  [config.configKey]: !prev[config.configKey] 
                }))}
              >
                {showSensitive[config.configKey] ? <Unlock className="h-4 w-4" /> : <Lock className="h-4 w-4" />}
              </Button>
            </div>
            {config.isSensitive && (
              <p className="text-xs text-muted-foreground">
                <AlertCircle className="inline h-3 w-3 mr-1" />
                This value is encrypted and sensitive
              </p>
            )}
          </div>
        );

      default:
        if (config.allowedValues && config.allowedValues.length > 0) {
          return (
            <Select
              value={currentValue}
              onValueChange={(value) => handleConfigChange(config.configKey, value)}
            >
              <SelectTrigger className={isEdited ? 'border-blue-500' : ''}>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {config.allowedValues.map((value) => (
                  <SelectItem key={value} value={value}>{value}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          );
        }

        return (
          <Input
            type="text"
            value={currentValue}
            onChange={(e) => handleConfigChange(config.configKey, e.target.value)}
            className={isEdited ? 'border-blue-500' : ''}
          />
        );
    }
  };

  // Filter configurations based on search
  const filteredConfigurations = configurations?.filter((config: Configuration) =>
    config.configKey.toLowerCase().includes(searchTerm.toLowerCase()) ||
    config.description?.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  // Check if there are unsaved changes
  const hasChanges = Object.keys(editedConfigs).length > 0;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">System Configuration</h1>
          <p className="text-muted-foreground">
            Manage system-wide configurations and settings
          </p>
        </div>
        <div className="flex space-x-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refreshMutation.mutate()}
            disabled={refreshMutation.isPending}
          >
            {refreshMutation.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <RefreshCw className="h-4 w-4" />
            )}
            Refresh
          </Button>
          <Button
            variant="outline"
            size="sm"
          >
            <Download className="h-4 w-4 mr-2" />
            Export
          </Button>
          <Button
            variant="outline"
            size="sm"
          >
            <Upload className="h-4 w-4 mr-2" />
            Import
          </Button>
        </div>
      </div>

      <div className="flex space-x-6">
        {/* Category Navigation */}
        <div className="w-64 space-y-2">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Categories</CardTitle>
            </CardHeader>
            <CardContent className="space-y-1">
              {categories.map((category) => (
                <div key={category.id}>
                  <Button
                    variant={selectedCategory === category.code ? 'secondary' : 'ghost'}
                    className="w-full justify-start"
                    onClick={() => setSelectedCategory(category.code)}
                  >
                    {category.icon}
                    <span className="ml-2">{category.name}</span>
                  </Button>
                  {category.subcategories && selectedCategory === category.code && (
                    <div className="ml-6 mt-1 space-y-1">
                      {category.subcategories.map((sub) => (
                        <Button
                          key={sub.id}
                          variant="ghost"
                          size="sm"
                          className="w-full justify-start text-xs"
                          onClick={() => setSelectedCategory(sub.code)}
                        >
                          {sub.name}
                        </Button>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </CardContent>
          </Card>

          {/* Environment Filter */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm">Environment</CardTitle>
            </CardHeader>
            <CardContent>
              <Select value={selectedEnvironment} onValueChange={setSelectedEnvironment}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Environments</SelectItem>
                  <SelectItem value="dev">Development</SelectItem>
                  <SelectItem value="test">Testing</SelectItem>
                  <SelectItem value="prod">Production</SelectItem>
                </SelectContent>
              </Select>
            </CardContent>
          </Card>
        </div>

        {/* Configuration Editor */}
        <div className="flex-1">
          <Card>
            <CardHeader>
              <div className="flex justify-between items-center">
                <div>
                  <CardTitle>
                    {categories.find(c => c.code === selectedCategory)?.name} Configuration
                  </CardTitle>
                  <CardDescription>
                    {categories.find(c => c.code === selectedCategory)?.description}
                  </CardDescription>
                </div>
                <div className="flex items-center space-x-2">
                  <Input
                    type="search"
                    placeholder="Search configurations..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-64"
                  />
                  {hasChanges && (
                    <>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setEditedConfigs({})}
                      >
                        Cancel
                      </Button>
                      <Button
                        size="sm"
                        onClick={() => saveMutation.mutate(editedConfigs)}
                        disabled={saveMutation.isPending}
                      >
                        {saveMutation.isPending ? (
                          <Loader2 className="h-4 w-4 animate-spin mr-2" />
                        ) : (
                          <Save className="h-4 w-4 mr-2" />
                        )}
                        Save Changes
                      </Button>
                    </>
                  )}
                </div>
              </div>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="flex justify-center py-8">
                  <Loader2 className="h-8 w-8 animate-spin" />
                </div>
              ) : error ? (
                <Alert variant="destructive">
                  <AlertDescription>
                    Failed to load configurations. Please try again.
                  </AlertDescription>
                </Alert>
              ) : filteredConfigurations.length === 0 ? (
                <div className="text-center py-8 text-muted-foreground">
                  No configurations found
                </div>
              ) : (
                <div className="space-y-6">
                  {filteredConfigurations.map((config: Configuration) => (
                    <div key={config.id} className="space-y-2">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-2">
                          <Label htmlFor={config.configKey} className="text-sm font-medium">
                            {config.configKey}
                          </Label>
                          {config.isRequired && (
                            <Badge variant="secondary" className="text-xs">Required</Badge>
                          )}
                          {config.environment && (
                            <Badge variant="outline" className="text-xs">{config.environment}</Badge>
                          )}
                          {config.profile && (
                            <Badge variant="outline" className="text-xs">{config.profile}</Badge>
                          )}
                          {editedConfigs.hasOwnProperty(config.configKey) && (
                            <Badge className="text-xs">Modified</Badge>
                          )}
                        </div>
                        {config.defaultValue && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleConfigChange(config.configKey, config.defaultValue!)}
                          >
                            Reset to default
                          </Button>
                        )}
                      </div>
                      
                      {renderConfigInput(config)}
                      
                      {config.description && (
                        <p className="text-sm text-muted-foreground">{config.description}</p>
                      )}
                      
                      {config.validationRules && (
                        <p className="text-xs text-muted-foreground">
                          Validation: {JSON.stringify(config.validationRules)}
                        </p>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default SystemConfiguration;