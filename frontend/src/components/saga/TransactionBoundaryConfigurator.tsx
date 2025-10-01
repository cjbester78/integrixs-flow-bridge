import React, { useState, useCallback, useMemo } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { Slider } from '@/components/ui/slider';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Shield,
  Lock,
  Unlock,
  AlertCircle,
  CheckCircle2,
  Plus,
  Trash2,
  GitBranch,
  Layers,
  Settings,
  Info,
  Save,
  Copy,
  Download,
  Upload,
  ChevronRight,
  ChevronDown,
  Database,
  Clock,
  RefreshCw,
  Zap,
  FileText,
  Activity,
  ArrowRight,
  ArrowRightLeft,
  Target
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import ReactFlow, {
  Node,
  Edge,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  ReactFlowProvider,
  Handle,
  Position,
  MarkerType,
  NodeProps,
  BackgroundVariant
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

interface TransactionBoundaryConfiguratorProps {
  flowId?: string;
  flowSteps?: FlowStep[];
  onSave?: (configuration: TransactionConfiguration) => void;
  initialConfiguration?: TransactionConfiguration;
  className?: string;
}

interface FlowStep {
  id: string;
  name: string;
  type: 'SERVICE' | 'TRANSFORMATION' | 'VALIDATION' | 'ROUTER' | 'ENRICHER';
  serviceId?: string;
  description?: string;
}

interface TransactionBoundary {
  id: string;
  name: string;
  type: 'REQUIRED' | 'REQUIRES_NEW' | 'SUPPORTS' | 'NOT_SUPPORTED' | 'MANDATORY' | 'NEVER';
  isolationLevel: 'DEFAULT' | 'READ_UNCOMMITTED' | 'READ_COMMITTED' | 'REPEATABLE_READ' | 'SERIALIZABLE';
  timeout: number;
  readOnly: boolean;
  rollbackFor: string[];
  noRollbackFor: string[];
  stepIds: string[];
  savepoints?: SavepointConfig[];
  propagationBehavior?: PropagationBehavior;
}

interface SavepointConfig {
  id: string;
  name: string;
  afterStepId: string;
  condition?: string;
  autoRollbackTo: boolean;
}

interface PropagationBehavior {
  suspendCurrent: boolean;
  isolateResources: boolean;
  shareConnection: boolean;
  inheritTimeout: boolean;
}

interface TransactionConfiguration {
  id?: string;
  name: string;
  description?: string;
  defaultBoundaryType: string;
  defaultIsolationLevel: string;
  defaultTimeout: number;
  boundaries: TransactionBoundary[];
  globalSettings: {
    enableDistributed: boolean;
    transactionManager: 'JTA' | 'SPRING' | 'CUSTOM';
    coordinatorUrl?: string;
    maxRetries: number;
    retryDelay: number;
    enableCompensation: boolean;
    enableSavepoints: boolean;
  };
  resourceSettings: {
    connectionPooling: boolean;
    maxConnections: number;
    connectionTimeout: number;
    statementCaching: boolean;
    batchSize: number;
  };
}

interface BoundaryNodeData {
  label: string;
  boundary?: TransactionBoundary;
  steps?: FlowStep[];
  nodeType: 'boundary' | 'step';
}

const TransactionBoundaryNode = ({ data, selected }: NodeProps<BoundaryNodeData>) => {
  const getTypeColor = (type?: string) => {
    switch (type) {
      case 'REQUIRED': return 'text-green-500';
      case 'REQUIRES_NEW': return 'text-blue-500';
      case 'MANDATORY': return 'text-red-500';
      case 'NOT_SUPPORTED': return 'text-gray-500';
      case 'SUPPORTS': return 'text-yellow-500';
      case 'NEVER': return 'text-purple-500';
      default: return 'text-muted-foreground';
    }
  };

  const getTypeIcon = (type?: string) => {
    switch (type) {
      case 'REQUIRED': return <Shield className="h-4 w-4" />;
      case 'REQUIRES_NEW': return <GitBranch className="h-4 w-4" />;
      case 'MANDATORY': return <Lock className="h-4 w-4" />;
      case 'NOT_SUPPORTED': return <Unlock className="h-4 w-4" />;
      default: return <Database className="h-4 w-4" />;
    }
  };

  return (
    <div className={cn(
      "px-4 py-3 rounded-lg border-2 bg-background min-w-[250px]",
      selected ? "border-primary" : "border-border"
    )}>
      <Handle
        type="target"
        position={Position.Top}
        className="w-2 h-2"
      />
      <Handle
        type="source"
        position={Position.Bottom}
        className="w-2 h-2"
      />
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            {getTypeIcon(data.boundary?.type)}
            <span className="font-medium">{data.label}</span>
          </div>
          <Badge variant="outline" className={getTypeColor(data.boundary?.type)}>
            {data.boundary?.type}
          </Badge>
        </div>
        {data.boundary && (
          <>
            <div className="flex items-center gap-4 text-xs text-muted-foreground">
              <span>Isolation: {data.boundary.isolationLevel}</span>
              <span>Timeout: {data.boundary.timeout}ms</span>
              {data.boundary.readOnly && <Badge variant="secondary" className="text-xs">Read-Only</Badge>}
            </div>
            {data.steps && data.steps.length > 0 && (
              <div className="pt-2 border-t">
                <p className="text-xs font-medium mb-1">Contains {data.steps.length} steps:</p>
                <div className="space-y-1">
                  {data.steps.slice(0, 3).map(step => (
                    <div key={step.id} className="text-xs text-muted-foreground flex items-center gap-1">
                      <ChevronRight className="h-3 w-3" />
                      {step.name}
                    </div>
                  ))}
                  {data.steps.length > 3 && (
                    <div className="text-xs text-muted-foreground italic">
                      ... and {data.steps.length - 3} more
                    </div>
                  )}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

const FlowStepNode = ({ data, selected }: NodeProps<BoundaryNodeData>) => {
  return (
    <div className={cn(
      "px-3 py-2 rounded border bg-background",
      selected ? "border-primary" : "border-border"
    )}>
      <Handle
        type="target"
        position={Position.Top}
        className="w-2 h-2"
      />
      <Handle
        type="source"
        position={Position.Bottom}
        className="w-2 h-2"
      />
      <div className="text-sm font-medium">{data.label}</div>
    </div>
  );
};

const nodeTypes = {
  transactionBoundary: TransactionBoundaryNode,
  flowStep: FlowStepNode
};

const DEFAULT_CONFIGURATION: TransactionConfiguration = {
  name: '',
  defaultBoundaryType: 'REQUIRED',
  defaultIsolationLevel: 'DEFAULT',
  defaultTimeout: 30000,
  boundaries: [],
  globalSettings: {
    enableDistributed: false,
    transactionManager: 'SPRING',
    maxRetries: 3,
    retryDelay: 1000,
    enableCompensation: true,
    enableSavepoints: false
  },
  resourceSettings: {
    connectionPooling: true,
    maxConnections: 10,
    connectionTimeout: 5000,
    statementCaching: true,
    batchSize: 100
  }
};

const PROPAGATION_TYPES = [
  { value: 'REQUIRED', label: 'Required', description: 'Support a current transaction, create a new one if none exists' },
  { value: 'REQUIRES_NEW', label: 'Requires New', description: 'Create a new transaction, suspend the current transaction if one exists' },
  { value: 'MANDATORY', label: 'Mandatory', description: 'Support a current transaction, throw an exception if none exists' },
  { value: 'SUPPORTS', label: 'Supports', description: 'Support a current transaction, execute non-transactionally if none exists' },
  { value: 'NOT_SUPPORTED', label: 'Not Supported', description: 'Execute non-transactionally, suspend the current transaction if one exists' },
  { value: 'NEVER', label: 'Never', description: 'Execute non-transactionally, throw an exception if a transaction exists' }
];

const ISOLATION_LEVELS = [
  { value: 'DEFAULT', label: 'Default', description: 'Use the default isolation level of the underlying datastore' },
  { value: 'READ_UNCOMMITTED', label: 'Read Uncommitted', description: 'Dirty reads, non-repeatable reads and phantom reads can occur' },
  { value: 'READ_COMMITTED', label: 'Read Committed', description: 'Dirty reads are prevented; non-repeatable reads and phantom reads can occur' },
  { value: 'REPEATABLE_READ', label: 'Repeatable Read', description: 'Dirty reads and non-repeatable reads are prevented; phantom reads can occur' },
  { value: 'SERIALIZABLE', label: 'Serializable', description: 'Dirty reads, non-repeatable reads and phantom reads are prevented' }
];

export function TransactionBoundaryConfigurator({
  flowId,
  flowSteps = [],
  onSave,
  initialConfiguration = DEFAULT_CONFIGURATION,
  className
}: TransactionBoundaryConfiguratorProps) {
  const { toast } = useToast();
  const [configuration, setConfiguration] = useState<TransactionConfiguration>(initialConfiguration);
  const [selectedBoundary, setSelectedBoundary] = useState<TransactionBoundary | null>(null);
  const [showBoundaryEditor, setShowBoundaryEditor] = useState(false);
  const [viewMode, setViewMode] = useState<'visual' | 'list'>('visual');
  const [selectedTab, setSelectedTab] = useState('boundaries');

  // Create nodes and edges for visual representation
  const { nodes, edges } = useMemo(() => {
    const nodes: Node<BoundaryNodeData>[] = [];
    const edges: Edge[] = [];
    let yPosition = 100;

    configuration.boundaries.forEach((boundary, index) => {
      const boundarySteps = flowSteps.filter(step => boundary.stepIds.includes(step.id));
      
      nodes.push({
        id: `boundary-${boundary.id}`,
        type: 'transactionBoundary',
        position: { x: 100 + (index % 3) * 300, y: yPosition },
        data: {
          label: boundary.name,
          boundary,
          steps: boundarySteps,
          nodeType: 'boundary'
        }
      });

      if (index > 0 && index % 3 === 0) {
        yPosition += 200;
      }
    });

    // Add unassigned steps
    const assignedStepIds = configuration.boundaries.flatMap(b => b.stepIds);
    const unassignedSteps = flowSteps.filter(step => !assignedStepIds.includes(step.id));
    
    if (unassignedSteps.length > 0) {
      yPosition += 200;
      unassignedSteps.forEach((step, index) => {
        nodes.push({
          id: `step-${step.id}`,
          type: 'flowStep',
          position: { x: 100 + (index % 5) * 150, y: yPosition },
          data: {
            label: step.name,
            nodeType: 'step'
          }
        });
      });
    }

    // Create edges between boundaries (for visual flow)
    for (let i = 0; i < configuration.boundaries.length - 1; i++) {
      edges.push({
        id: `edge-${i}`,
        source: `boundary-${configuration.boundaries[i].id}`,
        target: `boundary-${configuration.boundaries[i + 1].id}`,
        type: 'smoothstep',
        animated: true,
        style: { strokeDasharray: '5,5' }
      });
    }

    return { nodes, edges };
  }, [configuration.boundaries, flowSteps]);

  const [flowNodes, setFlowNodes, onNodesChange] = useNodesState(nodes);
  const [flowEdges, setFlowEdges, onEdgesChange] = useEdgesState(edges);

  const handleConfigurationChange = useCallback((field: keyof TransactionConfiguration, value: any) => {
    setConfiguration(prev => ({
      ...prev,
      [field]: value
    }));
  }, []);

  const handleGlobalSettingChange = useCallback((field: keyof TransactionConfiguration['globalSettings'], value: any) => {
    setConfiguration(prev => ({
      ...prev,
      globalSettings: {
        ...prev.globalSettings,
        [field]: value
      }
    }));
  }, []);

  const handleResourceSettingChange = useCallback((field: keyof TransactionConfiguration['resourceSettings'], value: any) => {
    setConfiguration(prev => ({
      ...prev,
      resourceSettings: {
        ...prev.resourceSettings,
        [field]: value
      }
    }));
  }, []);

  const addBoundary = useCallback(() => {
    const newBoundary: TransactionBoundary = {
      id: `boundary_${Date.now()}`,
      name: `Transaction Boundary ${configuration.boundaries.length + 1}`,
      type: configuration.defaultBoundaryType as any,
      isolationLevel: configuration.defaultIsolationLevel as any,
      timeout: configuration.defaultTimeout,
      readOnly: false,
      rollbackFor: ['Exception'],
      noRollbackFor: [],
      stepIds: []
    };

    setSelectedBoundary(newBoundary);
    setShowBoundaryEditor(true);
  }, [configuration]);

  const updateBoundary = useCallback((boundaryId: string, updates: Partial<TransactionBoundary>) => {
    setConfiguration(prev => ({
      ...prev,
      boundaries: prev.boundaries.map(boundary =>
        boundary.id === boundaryId ? { ...boundary, ...updates } : boundary
      )
    }));
  }, []);

  const deleteBoundary = useCallback((boundaryId: string) => {
    setConfiguration(prev => ({
      ...prev,
      boundaries: prev.boundaries.filter(boundary => boundary.id !== boundaryId)
    }));
  }, []);

  const handleBoundarySave = useCallback(() => {
    if (!selectedBoundary) return;

    const existingIndex = configuration.boundaries.findIndex(b => b.id === selectedBoundary.id);
    
    if (existingIndex === -1) {
      // Add new boundary
      setConfiguration(prev => ({
        ...prev,
        boundaries: [...prev.boundaries, selectedBoundary]
      }));
    } else {
      // Update existing boundary
      updateBoundary(selectedBoundary.id, selectedBoundary);
    }

    setShowBoundaryEditor(false);
    setSelectedBoundary(null);
  }, [selectedBoundary, configuration.boundaries, updateBoundary]);

  const handleNodeClick = useCallback((event: React.MouseEvent, node: Node<BoundaryNodeData>) => {
    if (node.data.nodeType === 'boundary' && node.data.boundary) {
      setSelectedBoundary(node.data.boundary);
      setShowBoundaryEditor(true);
    }
  }, []);

  const exportConfiguration = useCallback(() => {
    const data = JSON.stringify(configuration, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `transaction-config-${configuration.name || 'unnamed'}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }, [configuration]);

  const importConfiguration = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const data = JSON.parse(e.target?.result as string);
        setConfiguration(data);
        toast({
          title: 'Success',
          description: 'Configuration imported successfully'
        });
      } catch (error) {
        toast({
          title: 'Error',
          description: 'Failed to import configuration',
          variant: 'destructive'
        });
      }
    };
    reader.readAsText(file);
  }, [toast]);

  const handleSave = useCallback(() => {
    if (!configuration.name) {
      toast({
        title: 'Validation Error',
        description: 'Please provide a name for the configuration',
        variant: 'destructive'
      });
      return;
    }

    onSave?.(configuration);
    toast({
      title: 'Success',
      description: 'Transaction configuration saved successfully'
    });
  }, [configuration, onSave, toast]);

  const unassignedSteps = useMemo(() => {
    const assignedStepIds = configuration.boundaries.flatMap(b => b.stepIds);
    return flowSteps.filter(step => !assignedStepIds.includes(step.id));
  }, [configuration.boundaries, flowSteps]);

  return (
    <div className={cn("space-y-6", className)}>
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Database className="h-5 w-5" />
            Transaction Boundary Configuration
          </CardTitle>
          <CardDescription>
            Define transaction boundaries and propagation behavior for your integration flow
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs value={selectedTab} onValueChange={setSelectedTab}>
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="boundaries">Boundaries</TabsTrigger>
              <TabsTrigger value="global">Global Settings</TabsTrigger>
              <TabsTrigger value="resources">Resource Settings</TabsTrigger>
              <TabsTrigger value="preview">Preview</TabsTrigger>
            </TabsList>

            <TabsContent value="boundaries" className="space-y-4">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h4 className="font-medium">Transaction Boundaries</h4>
                  <p className="text-sm text-muted-foreground">
                    Define how transactions are managed across different parts of your flow
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setViewMode(viewMode === 'visual' ? 'list' : 'visual')}
                  >
                    {viewMode === 'visual' ? 'Visual' : 'List'} View
                  </Button>
                  <Button onClick={addBoundary}>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Boundary
                  </Button>
                </div>
              </div>

              {viewMode === 'visual' ? (
                <div className="h-[600px] border rounded-lg">
                  <ReactFlowProvider>
                    <ReactFlow
                      nodes={flowNodes}
                      edges={flowEdges}
                      onNodesChange={onNodesChange}
                      onEdgesChange={onEdgesChange}
                      onNodeClick={handleNodeClick}
                      nodeTypes={nodeTypes}
                      fitView
                    >
                      <Background variant={BackgroundVariant.Dots} />
                      <Controls />
                      <MiniMap />
                    </ReactFlow>
                  </ReactFlowProvider>
                </div>
              ) : (
                <div className="space-y-4">
                  {configuration.boundaries.map((boundary) => (
                    <Card key={boundary.id}>
                      <CardHeader className="pb-3">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <h5 className="font-medium">{boundary.name}</h5>
                            <Badge variant="outline">{boundary.type}</Badge>
                            <Badge variant="secondary">{boundary.isolationLevel}</Badge>
                          </div>
                          <div className="flex items-center gap-2">
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={() => {
                                setSelectedBoundary(boundary);
                                setShowBoundaryEditor(true);
                              }}
                            >
                              <Settings className="h-4 w-4" />
                            </Button>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={() => deleteBoundary(boundary.id)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                      </CardHeader>
                      <CardContent>
                        <div className="grid grid-cols-3 gap-4 text-sm">
                          <div>
                            <p className="text-muted-foreground">Timeout</p>
                            <p className="font-medium">{boundary.timeout}ms</p>
                          </div>
                          <div>
                            <p className="text-muted-foreground">Read-Only</p>
                            <p className="font-medium">{boundary.readOnly ? 'Yes' : 'No'}</p>
                          </div>
                          <div>
                            <p className="text-muted-foreground">Steps</p>
                            <p className="font-medium">{boundary.stepIds.length} assigned</p>
                          </div>
                        </div>
                        {boundary.stepIds.length > 0 && (
                          <div className="mt-4 pt-4 border-t">
                            <p className="text-sm font-medium mb-2">Assigned Steps:</p>
                            <div className="flex flex-wrap gap-2">
                              {boundary.stepIds.map(stepId => {
                                const step = flowSteps.find(s => s.id === stepId);
                                return step ? (
                                  <Badge key={stepId} variant="secondary">
                                    {step.name}
                                  </Badge>
                                ) : null;
                              })}
                            </div>
                          </div>
                        )}
                      </CardContent>
                    </Card>
                  ))}
                  {configuration.boundaries.length === 0 && (
                    <Card>
                      <CardContent className="pt-6">
                        <div className="text-center space-y-4">
                          <Database className="w-12 h-12 mx-auto text-muted-foreground" />
                          <div>
                            <h3 className="font-medium">No Transaction Boundaries</h3>
                            <p className="text-sm text-muted-foreground mt-1">
                              Create boundaries to control transaction behavior
                            </p>
                          </div>
                          <Button onClick={addBoundary}>
                            <Plus className="mr-2 h-4 w-4" />
                            Create First Boundary
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  )}
                </div>
              )}

              {unassignedSteps.length > 0 && (
                <Alert>
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Unassigned Steps</AlertTitle>
                  <AlertDescription>
                    The following steps are not assigned to any transaction boundary:
                    <div className="mt-2 flex flex-wrap gap-2">
                      {unassignedSteps.map((step) => (
                        <Badge key={step.id} variant="outline">
                          {step.name}
                        </Badge>
                      ))}
                    </div>
                  </AlertDescription>
                </Alert>
              )}
            </TabsContent>

            <TabsContent value="global" className="space-y-6">
              <div className="grid grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="configName">Configuration Name *</Label>
                    <Input
                      id="configName"
                      value={configuration.name}
                      onChange={(e) => handleConfigurationChange('name', e.target.value)}
                      placeholder="Enter configuration name"
                      className="mt-1"
                    />
                  </div>

                  <div>
                    <Label htmlFor="configDescription">Description</Label>
                    <Textarea
                      id="configDescription"
                      value={configuration.description || ''}
                      onChange={(e) => handleConfigurationChange('description', e.target.value)}
                      placeholder="Describe the configuration"
                      className="mt-1"
                      rows={3}
                    />
                  </div>

                  <div>
                    <Label htmlFor="transactionManager">Transaction Manager</Label>
                    <Select
                      value={configuration.globalSettings.transactionManager}
                      onValueChange={(value) => handleGlobalSettingChange('transactionManager', value)}
                    >
                      <SelectTrigger id="transactionManager" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="SPRING">Spring Transaction Manager</SelectItem>
                        <SelectItem value="JTA">JTA (Java Transaction API)</SelectItem>
                        <SelectItem value="CUSTOM">Custom Transaction Manager</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  {configuration.globalSettings.transactionManager === 'JTA' && (
                    <div>
                      <Label htmlFor="coordinatorUrl">Coordinator URL</Label>
                      <Input
                        id="coordinatorUrl"
                        value={configuration.globalSettings.coordinatorUrl || ''}
                        onChange={(e) => handleGlobalSettingChange('coordinatorUrl', e.target.value)}
                        placeholder="e.g., http://coordinator:8080"
                        className="mt-1"
                      />
                    </div>
                  )}
                </div>

                <div className="space-y-4">
                  <div>
                    <Label htmlFor="defaultBoundaryType">Default Boundary Type</Label>
                    <Select
                      value={configuration.defaultBoundaryType}
                      onValueChange={(value) => handleConfigurationChange('defaultBoundaryType', value)}
                    >
                      <SelectTrigger id="defaultBoundaryType" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {PROPAGATION_TYPES.map(type => (
                          <SelectItem key={type.value} value={type.value}>
                            {type.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="defaultIsolationLevel">Default Isolation Level</Label>
                    <Select
                      value={configuration.defaultIsolationLevel}
                      onValueChange={(value) => handleConfigurationChange('defaultIsolationLevel', value)}
                    >
                      <SelectTrigger id="defaultIsolationLevel" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {ISOLATION_LEVELS.map(level => (
                          <SelectItem key={level.value} value={level.value}>
                            {level.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="defaultTimeout">Default Timeout (ms)</Label>
                    <Input
                      id="defaultTimeout"
                      type="number"
                      value={configuration.defaultTimeout}
                      onChange={(e) => handleConfigurationChange('defaultTimeout', parseInt(e.target.value))}
                      min={1000}
                      className="mt-1"
                    />
                  </div>
                </div>
              </div>

              <Separator />

              <div className="space-y-4">
                <h4 className="font-medium">Advanced Settings</h4>
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <Switch
                        id="enableDistributed"
                        checked={configuration.globalSettings.enableDistributed}
                        onCheckedChange={(checked) => handleGlobalSettingChange('enableDistributed', checked)}
                      />
                      <Label htmlFor="enableDistributed">Enable Distributed Transactions</Label>
                    </div>

                    <div className="flex items-center space-x-2">
                      <Switch
                        id="enableCompensation"
                        checked={configuration.globalSettings.enableCompensation}
                        onCheckedChange={(checked) => handleGlobalSettingChange('enableCompensation', checked)}
                      />
                      <Label htmlFor="enableCompensation">Enable Compensation</Label>
                    </div>

                    <div className="flex items-center space-x-2">
                      <Switch
                        id="enableSavepoints"
                        checked={configuration.globalSettings.enableSavepoints}
                        onCheckedChange={(checked) => handleGlobalSettingChange('enableSavepoints', checked)}
                      />
                      <Label htmlFor="enableSavepoints">Enable Savepoints</Label>
                    </div>
                  </div>

                  <div className="space-y-4">
                    <div>
                      <Label htmlFor="maxRetries">Max Retries</Label>
                      <Input
                        id="maxRetries"
                        type="number"
                        value={configuration.globalSettings.maxRetries}
                        onChange={(e) => handleGlobalSettingChange('maxRetries', parseInt(e.target.value))}
                        min={0}
                        max={10}
                        className="mt-1"
                      />
                    </div>

                    <div>
                      <Label htmlFor="retryDelay">Retry Delay (ms)</Label>
                      <Input
                        id="retryDelay"
                        type="number"
                        value={configuration.globalSettings.retryDelay}
                        onChange={(e) => handleGlobalSettingChange('retryDelay', parseInt(e.target.value))}
                        min={100}
                        className="mt-1"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="resources" className="space-y-6">
              <div className="space-y-4">
                <h4 className="font-medium">Connection Pool Settings</h4>
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <Switch
                        id="connectionPooling"
                        checked={configuration.resourceSettings.connectionPooling}
                        onCheckedChange={(checked) => handleResourceSettingChange('connectionPooling', checked)}
                      />
                      <Label htmlFor="connectionPooling">Enable Connection Pooling</Label>
                    </div>

                    <div>
                      <Label htmlFor="maxConnections">Max Connections</Label>
                      <div className="mt-2">
                        <Slider
                          id="maxConnections"
                          min={1}
                          max={100}
                          step={1}
                          value={[configuration.resourceSettings.maxConnections]}
                          onValueChange={(value) => handleResourceSettingChange('maxConnections', value[0])}
                          className="mb-2"
                        />
                        <div className="flex justify-between text-xs text-muted-foreground">
                          <span>1</span>
                          <span className="font-medium">{configuration.resourceSettings.maxConnections}</span>
                          <span>100</span>
                        </div>
                      </div>
                    </div>

                    <div>
                      <Label htmlFor="connectionTimeout">Connection Timeout (ms)</Label>
                      <Input
                        id="connectionTimeout"
                        type="number"
                        value={configuration.resourceSettings.connectionTimeout}
                        onChange={(e) => handleResourceSettingChange('connectionTimeout', parseInt(e.target.value))}
                        min={1000}
                        className="mt-1"
                      />
                    </div>
                  </div>

                  <div className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <Switch
                        id="statementCaching"
                        checked={configuration.resourceSettings.statementCaching}
                        onCheckedChange={(checked) => handleResourceSettingChange('statementCaching', checked)}
                      />
                      <Label htmlFor="statementCaching">Enable Statement Caching</Label>
                    </div>

                    <div>
                      <Label htmlFor="batchSize">Batch Size</Label>
                      <div className="mt-2">
                        <Slider
                          id="batchSize"
                          min={1}
                          max={1000}
                          step={10}
                          value={[configuration.resourceSettings.batchSize]}
                          onValueChange={(value) => handleResourceSettingChange('batchSize', value[0])}
                          className="mb-2"
                        />
                        <div className="flex justify-between text-xs text-muted-foreground">
                          <span>1</span>
                          <span className="font-medium">{configuration.resourceSettings.batchSize}</span>
                          <span>1000</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <Separator />

              <Alert>
                <Info className="h-4 w-4" />
                <AlertTitle>Resource Optimization</AlertTitle>
                <AlertDescription>
                  These settings control how database connections and statements are managed.
                  Adjust based on your expected load and available resources.
                </AlertDescription>
              </Alert>
            </TabsContent>

            <TabsContent value="preview" className="space-y-4">
              <div className="flex items-center justify-between mb-4">
                <h4 className="font-medium">Configuration Preview</h4>
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => {
                      navigator.clipboard.writeText(JSON.stringify(configuration, null, 2));
                      toast({
                        title: 'Copied',
                        description: 'Configuration copied to clipboard'
                      });
                    }}
                  >
                    <Copy className="mr-2 h-4 w-4" />
                    Copy
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={exportConfiguration}
                  >
                    <Download className="mr-2 h-4 w-4" />
                    Export
                  </Button>
                  <Label htmlFor="import" className="cursor-pointer">
                    <Button
                      variant="outline"
                      size="sm"
                      asChild
                    >
                      <span>
                        <Upload className="mr-2 h-4 w-4" />
                        Import
                      </span>
                    </Button>
                    <Input
                      id="import"
                      type="file"
                      accept=".json"
                      onChange={importConfiguration}
                      className="hidden"
                    />
                  </Label>
                </div>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Configuration Summary</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm text-muted-foreground">Transaction Manager</p>
                        <p className="font-medium">{configuration.globalSettings.transactionManager}</p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Total Boundaries</p>
                        <p className="font-medium">{configuration.boundaries.length}</p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Distributed Transactions</p>
                        <p className="font-medium">{configuration.globalSettings.enableDistributed ? 'Enabled' : 'Disabled'}</p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Connection Pooling</p>
                        <p className="font-medium">{configuration.resourceSettings.connectionPooling ? 'Enabled' : 'Disabled'}</p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="text-base">JSON Configuration</CardTitle>
                </CardHeader>
                <CardContent>
                  <ScrollArea className="h-[400px] w-full">
                    <pre className="text-xs">
                      {JSON.stringify(configuration, null, 2)}
                    </pre>
                  </ScrollArea>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>

          <div className="flex justify-end gap-2 mt-6">
            <Button variant="outline">
              Cancel
            </Button>
            <Button onClick={handleSave}>
              <Save className="mr-2 h-4 w-4" />
              Save Configuration
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Boundary Editor Dialog */}
      {showBoundaryEditor && selectedBoundary && (
        <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50">
          <div className="fixed inset-4 md:inset-auto md:left-1/2 md:top-1/2 md:-translate-x-1/2 md:-translate-y-1/2 md:w-[700px] md:max-h-[90vh] overflow-y-auto bg-background border rounded-lg shadow-lg">
            <div className="p-6 space-y-4">
              <h3 className="text-lg font-semibold">
                {selectedBoundary.id ? 'Edit' : 'Add'} Transaction Boundary
              </h3>
              
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="boundaryName">Boundary Name *</Label>
                    <Input
                      id="boundaryName"
                      value={selectedBoundary.name}
                      onChange={(e) => setSelectedBoundary({ ...selectedBoundary, name: e.target.value })}
                      className="mt-1"
                    />
                  </div>

                  <div>
                    <Label htmlFor="boundaryType">Propagation Type</Label>
                    <Select
                      value={selectedBoundary.type}
                      onValueChange={(value) => setSelectedBoundary({ ...selectedBoundary, type: value as any })}
                    >
                      <SelectTrigger id="boundaryType" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {PROPAGATION_TYPES.map(type => (
                          <SelectItem key={type.value} value={type.value}>
                            <div>
                              <div>{type.label}</div>
                              <div className="text-xs text-muted-foreground">{type.description}</div>
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="isolationLevel">Isolation Level</Label>
                    <Select
                      value={selectedBoundary.isolationLevel}
                      onValueChange={(value) => setSelectedBoundary({ ...selectedBoundary, isolationLevel: value as any })}
                    >
                      <SelectTrigger id="isolationLevel" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {ISOLATION_LEVELS.map(level => (
                          <SelectItem key={level.value} value={level.value}>
                            <div>
                              <div>{level.label}</div>
                              <div className="text-xs text-muted-foreground">{level.description}</div>
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="timeout">Timeout (ms)</Label>
                    <Input
                      id="timeout"
                      type="number"
                      value={selectedBoundary.timeout}
                      onChange={(e) => setSelectedBoundary({ ...selectedBoundary, timeout: parseInt(e.target.value) })}
                      min={1000}
                      className="mt-1"
                    />
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <Switch
                    id="readOnly"
                    checked={selectedBoundary.readOnly}
                    onCheckedChange={(checked) => setSelectedBoundary({ ...selectedBoundary, readOnly: checked })}
                  />
                  <Label htmlFor="readOnly">Read-Only Transaction</Label>
                </div>

                <Separator />

                <div>
                  <Label>Assign Steps to Boundary</Label>
                  <p className="text-xs text-muted-foreground mb-2">
                    Select which flow steps should be executed within this transaction boundary
                  </p>
                  <div className="border rounded-lg p-2 space-y-1 max-h-48 overflow-y-auto">
                    {flowSteps.map((step) => {
                      const isSelected = selectedBoundary.stepIds.includes(step.id);
                      const isAssignedElsewhere = !isSelected && 
                        configuration.boundaries.some(b => b.id !== selectedBoundary.id && b.stepIds.includes(step.id));
                      
                      return (
                        <div
                          key={step.id}
                          className={cn(
                            "flex items-center space-x-2 p-2 rounded cursor-pointer",
                            isSelected && "bg-primary/10",
                            isAssignedElsewhere && "opacity-50 cursor-not-allowed",
                            !isSelected && !isAssignedElsewhere && "hover:bg-muted"
                          )}
                          onClick={() => {
                            if (isAssignedElsewhere) return;
                            
                            if (isSelected) {
                              setSelectedBoundary({
                                ...selectedBoundary,
                                stepIds: selectedBoundary.stepIds.filter(id => id !== step.id)
                              });
                            } else {
                              setSelectedBoundary({
                                ...selectedBoundary,
                                stepIds: [...selectedBoundary.stepIds, step.id]
                              });
                            }
                          }}
                        >
                          <Checkbox
                            checked={isSelected}
                            disabled={isAssignedElsewhere}
                          />
                          <div className="flex-1">
                            <p className="text-sm font-medium">{step.name}</p>
                            <p className="text-xs text-muted-foreground">{step.type}</p>
                          </div>
                          {isAssignedElsewhere && (
                            <Badge variant="secondary" className="text-xs">
                              Assigned
                            </Badge>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>

                <Separator />

                <div className="space-y-4">
                  <div>
                    <Label>Rollback For (Exception Classes)</Label>
                    <div className="space-y-2">
                      {selectedBoundary.rollbackFor.map((exception, index) => (
                        <div key={index} className="flex items-center gap-2">
                          <Input
                            value={exception}
                            onChange={(e) => {
                              const newRollbackFor = [...selectedBoundary.rollbackFor];
                              newRollbackFor[index] = e.target.value;
                              setSelectedBoundary({ ...selectedBoundary, rollbackFor: newRollbackFor });
                            }}
                            placeholder="e.g., Exception, RuntimeException"
                          />
                          <Button
                            size="icon"
                            variant="ghost"
                            onClick={() => {
                              setSelectedBoundary({
                                ...selectedBoundary,
                                rollbackFor: selectedBoundary.rollbackFor.filter((_, i) => i !== index)
                              });
                            }}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      ))}
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setSelectedBoundary({
                            ...selectedBoundary,
                            rollbackFor: [...selectedBoundary.rollbackFor, '']
                          });
                        }}
                      >
                        <Plus className="mr-2 h-4 w-4" />
                        Add Exception
                      </Button>
                    </div>
                  </div>

                  <div>
                    <Label>No Rollback For (Exception Classes)</Label>
                    <div className="space-y-2">
                      {selectedBoundary.noRollbackFor.map((exception, index) => (
                        <div key={index} className="flex items-center gap-2">
                          <Input
                            value={exception}
                            onChange={(e) => {
                              const newNoRollbackFor = [...selectedBoundary.noRollbackFor];
                              newNoRollbackFor[index] = e.target.value;
                              setSelectedBoundary({ ...selectedBoundary, noRollbackFor: newNoRollbackFor });
                            }}
                            placeholder="e.g., BusinessException"
                          />
                          <Button
                            size="icon"
                            variant="ghost"
                            onClick={() => {
                              setSelectedBoundary({
                                ...selectedBoundary,
                                noRollbackFor: selectedBoundary.noRollbackFor.filter((_, i) => i !== index)
                              });
                            }}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      ))}
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setSelectedBoundary({
                            ...selectedBoundary,
                            noRollbackFor: [...selectedBoundary.noRollbackFor, '']
                          });
                        }}
                      >
                        <Plus className="mr-2 h-4 w-4" />
                        Add Exception
                      </Button>
                    </div>
                  </div>
                </div>
              </div>

              <div className="flex justify-end gap-2 mt-6">
                <Button
                  variant="outline"
                  onClick={() => {
                    setShowBoundaryEditor(false);
                    setSelectedBoundary(null);
                  }}
                >
                  Cancel
                </Button>
                <Button onClick={handleBoundarySave}>
                  <Save className="mr-2 h-4 w-4" />
                  Save Boundary
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}