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
import {
  ArrowRight,
  RotateCcw,
  AlertCircle,
  CheckCircle2,
  Plus,
  Trash2,
  Link2,
  Unlink,
  GitBranch,
  Layers,
  Shield,
  Clock,
  RefreshCw,
  Activity,
  Eye,
  EyeOff,
  ChevronRight,
  ChevronDown,
  Zap,
  Save,
  FileJson,
  Copy,
  Download,
  Upload
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
  addEdge,
  Connection,
  NodeChange,
  EdgeChange,
  ReactFlowProvider,
  Handle,
  Position,
  MarkerType,
  NodeProps,
  BackgroundVariant
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

interface CompensationFlowDesignerProps {
  sagaSteps?: SagaStep[];
  onSave?: (compensationFlow: CompensationFlow) => void;
  initialFlow?: CompensationFlow;
  className?: string;
}

interface SagaStep {
  id: string;
  name: string;
  type: string;
  serviceName?: string;
  operation?: string;
  compensationOperation?: string;
}

interface CompensationStep {
  id: string;
  stepId: string;
  compensationType: 'UNDO' | 'COMPENSATE' | 'IGNORE' | 'CUSTOM';
  operation?: string;
  serviceName?: string;
  description?: string;
  dependsOn: string[];
  retryable: boolean;
  timeout?: number;
  customLogic?: string;
}

interface CompensationFlow {
  id?: string;
  name: string;
  description?: string;
  strategy: 'BACKWARD' | 'FORWARD' | 'DEPENDENCY_BASED' | 'CUSTOM';
  steps: CompensationStep[];
  globalTimeout?: number;
  continueOnError: boolean;
  notifications?: {
    onStart?: string;
    onSuccess?: string;
    onFailure?: string;
  };
}

interface FlowNodeData {
  label: string;
  step?: SagaStep;
  compensationStep?: CompensationStep;
  nodeType: 'forward' | 'compensation' | 'link';
  hasCompensation?: boolean;
  isLinked?: boolean;
}

const ForwardStepNode = ({ data, selected }: NodeProps<FlowNodeData>) => {
  return (
    <div className={cn(
      "px-4 py-3 rounded-lg border-2 bg-background min-w-[200px]",
      selected ? "border-primary" : "border-border"
    )}>
      <Handle
        type="source"
        position={Position.Bottom}
        className="w-2 h-2"
      />
      <div className="space-y-1">
        <div className="flex items-center justify-between">
          <span className="font-medium text-sm">{data.label}</span>
          {data.hasCompensation ? (
            <CheckCircle2 className="h-4 w-4 text-green-500" />
          ) : (
            <AlertCircle className="h-4 w-4 text-yellow-500" />
          )}
        </div>
        {data.step?.serviceName && (
          <p className="text-xs text-muted-foreground">{data.step.serviceName}</p>
        )}
        {data.step?.operation && (
          <p className="text-xs text-muted-foreground">{data.step.operation}</p>
        )}
      </div>
    </div>
  );
};

const CompensationStepNode = ({ data, selected }: NodeProps<FlowNodeData>) => {
  const getCompensationIcon = (type?: string) => {
    switch (type) {
      case 'UNDO': return <RotateCcw className="h-3 w-3" />;
      case 'COMPENSATE': return <RefreshCw className="h-3 w-3" />;
      case 'IGNORE': return <EyeOff className="h-3 w-3" />;
      case 'CUSTOM': return <Zap className="h-3 w-3" />;
      default: return <Shield className="h-3 w-3" />;
    }
  };

  return (
    <div className={cn(
      "px-4 py-3 rounded-lg border-2 bg-background min-w-[200px]",
      selected ? "border-primary" : "border-orange-500/50",
      "bg-orange-50 dark:bg-orange-950/20"
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
      <div className="space-y-1">
        <div className="flex items-center gap-2">
          {getCompensationIcon(data.compensationStep?.compensationType)}
          <span className="font-medium text-sm">{data.label}</span>
        </div>
        {data.compensationStep?.serviceName && (
          <p className="text-xs text-muted-foreground">{data.compensationStep.serviceName}</p>
        )}
        {data.compensationStep?.operation && (
          <p className="text-xs text-muted-foreground">{data.compensationStep.operation}</p>
        )}
        {data.compensationStep?.compensationType && (
          <Badge variant="outline" className="text-xs">
            {data.compensationStep.compensationType}
          </Badge>
        )}
      </div>
    </div>
  );
};

const nodeTypes = {
  forwardStep: ForwardStepNode,
  compensationStep: CompensationStepNode
};

const DEFAULT_FLOW: CompensationFlow = {
  name: '',
  strategy: 'BACKWARD',
  steps: [],
  continueOnError: false
};

export function CompensationFlowDesigner({
  sagaSteps = [],
  onSave,
  initialFlow = DEFAULT_FLOW,
  className
}: CompensationFlowDesignerProps) {
  const { toast } = useToast();
  const [flow, setFlow] = useState<CompensationFlow>(initialFlow);
  const [selectedStep, setSelectedStep] = useState<CompensationStep | null>(null);
  const [showStepEditor, setShowStepEditor] = useState(false);
  const [viewMode, setViewMode] = useState<'visual' | 'table'>('visual');

  // Create initial nodes and edges from saga steps and compensation flow
  const initialNodes = useMemo(() => {
    const nodes: Node<FlowNodeData>[] = [];
    const forwardY = 100;
    const compensationY = 300;
    
    // Create forward step nodes
    sagaSteps.forEach((step, index) => {
      const hasCompensation = flow.steps.some(cs => cs.stepId === step.id);
      nodes.push({
        id: `forward-${step.id}`,
        type: 'forwardStep',
        position: { x: 100 + index * 250, y: forwardY },
        data: {
          label: step.name,
          step,
          nodeType: 'forward',
          hasCompensation,
          isLinked: hasCompensation
        }
      });
    });

    // Create compensation step nodes
    flow.steps.forEach((compStep, index) => {
      const sagaStep = sagaSteps.find(s => s.id === compStep.stepId);
      const sagaIndex = sagaSteps.findIndex(s => s.id === compStep.stepId);
      const xPosition = sagaIndex >= 0 ? 100 + sagaIndex * 250 : 100 + index * 250;
      
      nodes.push({
        id: `compensation-${compStep.id}`,
        type: 'compensationStep',
        position: { x: xPosition, y: compensationY },
        data: {
          label: compStep.operation || `Compensate ${sagaStep?.name || 'Step'}`,
          compensationStep: compStep,
          nodeType: 'compensation'
        }
      });
    });

    return nodes;
  }, [sagaSteps, flow.steps]);

  const initialEdges = useMemo(() => {
    const edges: Edge[] = [];

    // Create links between forward and compensation steps
    flow.steps.forEach((compStep) => {
      edges.push({
        id: `link-${compStep.stepId}-${compStep.id}`,
        source: `forward-${compStep.stepId}`,
        target: `compensation-${compStep.id}`,
        type: 'smoothstep',
        animated: true,
        style: { stroke: '#f97316', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed }
      });
    });

    // Create dependency edges between compensation steps
    flow.steps.forEach((compStep) => {
      compStep.dependsOn.forEach((depId) => {
        edges.push({
          id: `dep-${depId}-${compStep.id}`,
          source: `compensation-${depId}`,
          target: `compensation-${compStep.id}`,
          type: 'smoothstep',
          style: { stroke: '#6b7280', strokeWidth: 1, strokeDasharray: '5,5' },
          markerEnd: { type: MarkerType.ArrowClosed }
        });
      });
    });

    return edges;
  }, [flow.steps]);

  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  const handleFlowChange = useCallback((field: keyof CompensationFlow, value: any) => {
    setFlow(prev => ({
      ...prev,
      [field]: value
    }));
  }, []);

  const handleNotificationChange = useCallback((field: string, value: string) => {
    setFlow(prev => ({
      ...prev,
      notifications: {
        ...prev.notifications,
        [field]: value
      }
    }));
  }, []);

  const addCompensationStep = useCallback((sagaStepId: string) => {
    const sagaStep = sagaSteps.find(s => s.id === sagaStepId);
    if (!sagaStep) return;

    const newStep: CompensationStep = {
      id: `comp_${Date.now()}`,
      stepId: sagaStepId,
      compensationType: sagaStep.compensationOperation ? 'COMPENSATE' : 'UNDO',
      operation: sagaStep.compensationOperation || `Undo ${sagaStep.name}`,
      serviceName: sagaStep.serviceName,
      dependsOn: [],
      retryable: true,
      timeout: 30000
    };

    setSelectedStep(newStep);
    setShowStepEditor(true);
  }, [sagaSteps]);

  const updateCompensationStep = useCallback((stepId: string, updates: Partial<CompensationStep>) => {
    setFlow(prev => ({
      ...prev,
      steps: prev.steps.map(step =>
        step.id === stepId ? { ...step, ...updates } : step
      )
    }));

    // Update nodes and edges to reflect changes
    const updatedFlow = {
      ...flow,
      steps: flow.steps.map(step =>
        step.id === stepId ? { ...step, ...updates } : step
      )
    };
    setNodes(initialNodes);
    setEdges(initialEdges);
  }, [flow, initialNodes, initialEdges, setNodes, setEdges]);

  const deleteCompensationStep = useCallback((stepId: string) => {
    setFlow(prev => ({
      ...prev,
      steps: prev.steps.filter(step => step.id !== stepId)
    }));
  }, []);

  const handleStepSave = useCallback(() => {
    if (!selectedStep) return;

    const existingIndex = flow.steps.findIndex(s => s.id === selectedStep.id);
    
    if (existingIndex === -1) {
      // Add new step
      setFlow(prev => ({
        ...prev,
        steps: [...prev.steps, selectedStep]
      }));
    } else {
      // Update existing step
      updateCompensationStep(selectedStep.id, selectedStep);
    }

    setShowStepEditor(false);
    setSelectedStep(null);
  }, [selectedStep, flow.steps, updateCompensationStep]);

  const handleNodeClick = useCallback((event: React.MouseEvent, node: Node<FlowNodeData>) => {
    if (node.data.nodeType === 'forward' && node.data.step) {
      // Check if compensation exists
      const hasCompensation = flow.steps.some(cs => cs.stepId === node.data.step!.id);
      if (!hasCompensation) {
        addCompensationStep(node.data.step.id);
      }
    } else if (node.data.nodeType === 'compensation' && node.data.compensationStep) {
      setSelectedStep(node.data.compensationStep);
      setShowStepEditor(true);
    }
  }, [flow.steps, addCompensationStep]);

  const handleConnect = useCallback((params: Connection) => {
    if (!params.source || !params.target) return;

    // Only allow connections between compensation steps
    if (params.source.startsWith('compensation-') && params.target.startsWith('compensation-')) {
      const sourceId = params.source.replace('compensation-', '');
      const targetId = params.target.replace('compensation-', '');
      
      setFlow(prev => ({
        ...prev,
        steps: prev.steps.map(step =>
          step.id === targetId
            ? { ...step, dependsOn: [...new Set([...step.dependsOn, sourceId])] }
            : step
        )
      }));
    }
  }, []);

  const exportFlow = useCallback(() => {
    const data = JSON.stringify(flow, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `compensation-flow-${flow.name || 'unnamed'}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }, [flow]);

  const importFlow = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const data = JSON.parse(e.target?.result as string);
        setFlow(data);
        toast({
          title: 'Success',
          description: 'Compensation flow imported successfully'
        });
      } catch (error) {
        toast({
          title: 'Error',
          description: 'Failed to import compensation flow',
          variant: 'destructive'
        });
      }
    };
    reader.readAsText(file);
  }, [toast]);

  const handleSave = useCallback(() => {
    if (!flow.name) {
      toast({
        title: 'Validation Error',
        description: 'Please provide a name for the compensation flow',
        variant: 'destructive'
      });
      return;
    }

    onSave?.(flow);
    toast({
      title: 'Success',
      description: 'Compensation flow saved successfully'
    });
  }, [flow, onSave, toast]);

  const unlinkedSteps = useMemo(() => {
    return sagaSteps.filter(step => !flow.steps.some(cs => cs.stepId === step.id));
  }, [sagaSteps, flow.steps]);

  return (
    <div className={cn("space-y-6", className)}>
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <RotateCcw className="h-5 w-5" />
            Compensation Flow Designer
          </CardTitle>
          <CardDescription>
            Design compensation flows for transaction rollback and recovery
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="design">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="design">Design</TabsTrigger>
              <TabsTrigger value="configuration">Configuration</TabsTrigger>
              <TabsTrigger value="preview">Preview</TabsTrigger>
            </TabsList>

            <TabsContent value="design" className="space-y-4">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h4 className="font-medium">Flow Design</h4>
                  <p className="text-sm text-muted-foreground">
                    Click on forward steps to add compensation, drag to create dependencies
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setViewMode(viewMode === 'visual' ? 'table' : 'visual')}
                  >
                    {viewMode === 'visual' ? (
                      <>
                        <Eye className="mr-2 h-4 w-4" />
                        Visual View
                      </>
                    ) : (
                      <>
                        <Layers className="mr-2 h-4 w-4" />
                        Table View
                      </>
                    )}
                  </Button>
                </div>
              </div>

              {viewMode === 'visual' ? (
                <div className="h-[600px] border rounded-lg">
                  <ReactFlowProvider>
                    <ReactFlow
                      nodes={nodes}
                      edges={edges}
                      onNodesChange={onNodesChange}
                      onEdgesChange={onEdgesChange}
                      onConnect={handleConnect}
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
                  {/* Table view of compensation steps */}
                  <div className="border rounded-lg overflow-hidden">
                    <table className="w-full">
                      <thead>
                        <tr className="bg-muted">
                          <th className="px-4 py-2 text-left text-sm font-medium">Forward Step</th>
                          <th className="px-4 py-2 text-left text-sm font-medium">Compensation</th>
                          <th className="px-4 py-2 text-left text-sm font-medium">Type</th>
                          <th className="px-4 py-2 text-left text-sm font-medium">Dependencies</th>
                          <th className="px-4 py-2 text-left text-sm font-medium">Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {sagaSteps.map((step) => {
                          const compStep = flow.steps.find(cs => cs.stepId === step.id);
                          return (
                            <tr key={step.id} className="border-t">
                              <td className="px-4 py-2">
                                <div>
                                  <p className="font-medium">{step.name}</p>
                                  {step.serviceName && (
                                    <p className="text-xs text-muted-foreground">{step.serviceName}</p>
                                  )}
                                </div>
                              </td>
                              <td className="px-4 py-2">
                                {compStep ? (
                                  <div>
                                    <p>{compStep.operation}</p>
                                    {compStep.description && (
                                      <p className="text-xs text-muted-foreground">{compStep.description}</p>
                                    )}
                                  </div>
                                ) : (
                                  <span className="text-muted-foreground">Not configured</span>
                                )}
                              </td>
                              <td className="px-4 py-2">
                                {compStep && (
                                  <Badge variant="outline">
                                    {compStep.compensationType}
                                  </Badge>
                                )}
                              </td>
                              <td className="px-4 py-2">
                                {compStep && compStep.dependsOn.length > 0 ? (
                                  <div className="flex flex-wrap gap-1">
                                    {compStep.dependsOn.map((depId) => {
                                      const depStep = flow.steps.find(s => s.id === depId);
                                      return depStep ? (
                                        <Badge key={depId} variant="secondary" className="text-xs">
                                          {depStep.operation}
                                        </Badge>
                                      ) : null;
                                    })}
                                  </div>
                                ) : (
                                  <span className="text-xs text-muted-foreground">None</span>
                                )}
                              </td>
                              <td className="px-4 py-2">
                                <div className="flex items-center gap-2">
                                  {compStep ? (
                                    <>
                                      <Button
                                        size="icon"
                                        variant="ghost"
                                        onClick={() => {
                                          setSelectedStep(compStep);
                                          setShowStepEditor(true);
                                        }}
                                      >
                                        <Eye className="h-4 w-4" />
                                      </Button>
                                      <Button
                                        size="icon"
                                        variant="ghost"
                                        onClick={() => deleteCompensationStep(compStep.id)}
                                      >
                                        <Trash2 className="h-4 w-4" />
                                      </Button>
                                    </>
                                  ) : (
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      onClick={() => addCompensationStep(step.id)}
                                    >
                                      <Plus className="mr-2 h-4 w-4" />
                                      Add
                                    </Button>
                                  )}
                                </div>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {unlinkedSteps.length > 0 && (
                <Alert>
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Unlinked Steps</AlertTitle>
                  <AlertDescription>
                    The following steps do not have compensation configured:
                    <div className="mt-2 flex flex-wrap gap-2">
                      {unlinkedSteps.map((step) => (
                        <Badge key={step.id} variant="outline">
                          {step.name}
                        </Badge>
                      ))}
                    </div>
                  </AlertDescription>
                </Alert>
              )}
            </TabsContent>

            <TabsContent value="configuration" className="space-y-6">
              <div className="grid grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="flowName">Flow Name *</Label>
                    <Input
                      id="flowName"
                      value={flow.name}
                      onChange={(e) => handleFlowChange('name', e.target.value)}
                      placeholder="Enter compensation flow name"
                      className="mt-1"
                    />
                  </div>

                  <div>
                    <Label htmlFor="flowDescription">Description</Label>
                    <Textarea
                      id="flowDescription"
                      value={flow.description || ''}
                      onChange={(e) => handleFlowChange('description', e.target.value)}
                      placeholder="Describe the compensation flow"
                      className="mt-1"
                      rows={3}
                    />
                  </div>

                  <div>
                    <Label htmlFor="strategy">Compensation Strategy</Label>
                    <Select
                      value={flow.strategy}
                      onValueChange={(value) => handleFlowChange('strategy', value)}
                    >
                      <SelectTrigger id="strategy" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="BACKWARD">Backward (Reverse Order)</SelectItem>
                        <SelectItem value="FORWARD">Forward (Same Order)</SelectItem>
                        <SelectItem value="DEPENDENCY_BASED">Dependency Based</SelectItem>
                        <SelectItem value="CUSTOM">Custom Order</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-4">
                  <div>
                    <Label htmlFor="globalTimeout">Global Timeout (ms)</Label>
                    <Input
                      id="globalTimeout"
                      type="number"
                      value={flow.globalTimeout || ''}
                      onChange={(e) => handleFlowChange('globalTimeout', parseInt(e.target.value) || undefined)}
                      placeholder="Optional global timeout"
                      min={1000}
                      className="mt-1"
                    />
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="continueOnError"
                      checked={flow.continueOnError}
                      onCheckedChange={(checked) => handleFlowChange('continueOnError', checked)}
                    />
                    <Label htmlFor="continueOnError">Continue on Error</Label>
                  </div>

                  <div className="space-y-2">
                    <h5 className="text-sm font-medium">Notifications</h5>
                    <div className="space-y-2">
                      <div>
                        <Label htmlFor="onStart" className="text-xs">On Start</Label>
                        <Input
                          id="onStart"
                          value={flow.notifications?.onStart || ''}
                          onChange={(e) => handleNotificationChange('onStart', e.target.value)}
                          placeholder="e.g., compensation.started"
                          className="mt-1"
                        />
                      </div>
                      <div>
                        <Label htmlFor="onSuccess" className="text-xs">On Success</Label>
                        <Input
                          id="onSuccess"
                          value={flow.notifications?.onSuccess || ''}
                          onChange={(e) => handleNotificationChange('onSuccess', e.target.value)}
                          placeholder="e.g., compensation.completed"
                          className="mt-1"
                        />
                      </div>
                      <div>
                        <Label htmlFor="onFailure" className="text-xs">On Failure</Label>
                        <Input
                          id="onFailure"
                          value={flow.notifications?.onFailure || ''}
                          onChange={(e) => handleNotificationChange('onFailure', e.target.value)}
                          placeholder="e.g., compensation.failed"
                          className="mt-1"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="preview" className="space-y-4">
              <div className="flex items-center justify-between mb-4">
                <h4 className="font-medium">Compensation Flow Preview</h4>
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => {
                      navigator.clipboard.writeText(JSON.stringify(flow, null, 2));
                      toast({
                        title: 'Copied',
                        description: 'Flow configuration copied to clipboard'
                      });
                    }}
                  >
                    <Copy className="mr-2 h-4 w-4" />
                    Copy
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={exportFlow}
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
                      onChange={importFlow}
                      className="hidden"
                    />
                  </Label>
                </div>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Execution Order</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {flow.strategy === 'DEPENDENCY_BASED' ? (
                      <div className="space-y-1">
                        <p className="text-sm text-muted-foreground mb-2">
                          Steps will execute based on dependency graph
                        </p>
                        {flow.steps.map((step, index) => {
                          const sagaStep = sagaSteps.find(s => s.id === step.stepId);
                          return (
                            <div key={step.id} className="flex items-center gap-2 p-2 border rounded">
                              <Badge variant="outline" className="w-8">{index + 1}</Badge>
                              <span className="text-sm flex-1">
                                {step.operation || sagaStep?.name || 'Unknown Step'}
                              </span>
                              {step.dependsOn.length > 0 && (
                                <span className="text-xs text-muted-foreground">
                                  depends on: {step.dependsOn.length} step(s)
                                </span>
                              )}
                            </div>
                          );
                        })}
                      </div>
                    ) : (
                      <div className="space-y-1">
                        <p className="text-sm text-muted-foreground mb-2">
                          Steps will execute in {flow.strategy.toLowerCase()} order
                        </p>
                        {(flow.strategy === 'BACKWARD' ? [...flow.steps].reverse() : flow.steps).map((step, index) => {
                          const sagaStep = sagaSteps.find(s => s.id === step.stepId);
                          return (
                            <div key={step.id} className="flex items-center gap-2 p-2 border rounded">
                              <Badge variant="outline" className="w-8">{index + 1}</Badge>
                              <ArrowRight className="h-4 w-4 text-muted-foreground" />
                              <span className="text-sm">
                                {step.operation || sagaStep?.name || 'Unknown Step'}
                              </span>
                            </div>
                          );
                        })}
                      </div>
                    )}
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
                      {JSON.stringify(flow, null, 2)}
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
              Save Flow
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Step Editor Dialog */}
      {showStepEditor && selectedStep && (
        <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50">
          <div className="fixed inset-4 md:inset-auto md:left-1/2 md:top-1/2 md:-translate-x-1/2 md:-translate-y-1/2 md:w-[600px] bg-background border rounded-lg shadow-lg">
            <div className="p-6 space-y-4">
              <h3 className="text-lg font-semibold">
                Edit Compensation Step
              </h3>
              
              <div className="space-y-4">
                <div>
                  <Label htmlFor="compensationType">Compensation Type</Label>
                  <Select
                    value={selectedStep.compensationType}
                    onValueChange={(value) => setSelectedStep({ ...selectedStep, compensationType: value as any })}
                  >
                    <SelectTrigger id="compensationType" className="mt-1">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="UNDO">Undo Operation</SelectItem>
                      <SelectItem value="COMPENSATE">Compensate</SelectItem>
                      <SelectItem value="IGNORE">Ignore (No Action)</SelectItem>
                      <SelectItem value="CUSTOM">Custom Logic</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <Label htmlFor="operation">Operation Name *</Label>
                  <Input
                    id="operation"
                    value={selectedStep.operation || ''}
                    onChange={(e) => setSelectedStep({ ...selectedStep, operation: e.target.value })}
                    placeholder="e.g., Cancel Order, Rollback Transaction"
                    className="mt-1"
                  />
                </div>

                <div>
                  <Label htmlFor="serviceName">Service Name</Label>
                  <Input
                    id="serviceName"
                    value={selectedStep.serviceName || ''}
                    onChange={(e) => setSelectedStep({ ...selectedStep, serviceName: e.target.value })}
                    className="mt-1"
                  />
                </div>

                <div>
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    value={selectedStep.description || ''}
                    onChange={(e) => setSelectedStep({ ...selectedStep, description: e.target.value })}
                    placeholder="Describe the compensation logic"
                    className="mt-1"
                    rows={2}
                  />
                </div>

                <div>
                  <Label htmlFor="timeout">Timeout (ms)</Label>
                  <Input
                    id="timeout"
                    type="number"
                    value={selectedStep.timeout || 30000}
                    onChange={(e) => setSelectedStep({ ...selectedStep, timeout: parseInt(e.target.value) })}
                    min={1000}
                    className="mt-1"
                  />
                </div>

                <div className="flex items-center space-x-2">
                  <Switch
                    id="retryable"
                    checked={selectedStep.retryable}
                    onCheckedChange={(checked) => setSelectedStep({ ...selectedStep, retryable: checked })}
                  />
                  <Label htmlFor="retryable">Allow Retry on Failure</Label>
                </div>

                {selectedStep.compensationType === 'CUSTOM' && (
                  <div>
                    <Label htmlFor="customLogic">Custom Logic</Label>
                    <Textarea
                      id="customLogic"
                      value={selectedStep.customLogic || ''}
                      onChange={(e) => setSelectedStep({ ...selectedStep, customLogic: e.target.value })}
                      placeholder="Enter custom compensation logic (e.g., script or expression)"
                      className="mt-1 font-mono text-xs"
                      rows={4}
                    />
                  </div>
                )}

                <div>
                  <Label>Dependencies</Label>
                  <p className="text-xs text-muted-foreground mb-2">
                    Select steps that must complete before this compensation
                  </p>
                  <div className="border rounded-lg p-2 space-y-1 max-h-32 overflow-y-auto">
                    {flow.steps
                      .filter(s => s.id !== selectedStep.id)
                      .map((step) => {
                        const sagaStep = sagaSteps.find(s => s.id === step.stepId);
                        const isSelected = selectedStep.dependsOn.includes(step.id);
                        return (
                          <div
                            key={step.id}
                            className={cn(
                              "flex items-center space-x-2 p-1 rounded cursor-pointer hover:bg-muted",
                              isSelected && "bg-muted"
                            )}
                            onClick={() => {
                              if (isSelected) {
                                setSelectedStep({
                                  ...selectedStep,
                                  dependsOn: selectedStep.dependsOn.filter(id => id !== step.id)
                                });
                              } else {
                                setSelectedStep({
                                  ...selectedStep,
                                  dependsOn: [...selectedStep.dependsOn, step.id]
                                });
                              }
                            }}
                          >
                            <div className={cn(
                              "w-4 h-4 border rounded",
                              isSelected && "bg-primary border-primary"
                            )}>
                              {isSelected && <CheckCircle2 className="h-4 w-4 text-primary-foreground" />}
                            </div>
                            <span className="text-sm">
                              {step.operation || sagaStep?.name || 'Unknown Step'}
                            </span>
                          </div>
                        );
                      })}
                    {flow.steps.filter(s => s.id !== selectedStep.id).length === 0 && (
                      <p className="text-xs text-muted-foreground text-center py-2">
                        No other compensation steps available
                      </p>
                    )}
                  </div>
                </div>
              </div>

              <div className="flex justify-end gap-2 mt-6">
                <Button
                  variant="outline"
                  onClick={() => {
                    setShowStepEditor(false);
                    setSelectedStep(null);
                  }}
                >
                  Cancel
                </Button>
                <Button onClick={handleStepSave}>
                  <Save className="mr-2 h-4 w-4" />
                  Save Step
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}