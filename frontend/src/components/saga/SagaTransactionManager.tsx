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
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import {
  GitBranch,
  Plus,
  Trash2,
  ChevronDown,
  ChevronRight,
  Save,
  AlertCircle,
  CheckCircle2,
  Clock,
  RefreshCw,
  Settings,
  Play,
  Pause,
  RotateCcw,
  Zap,
  ArrowRight,
  ArrowDown,
  Copy,
  Layers,
  Shield
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { DndContext, DragEndEvent, closestCorners } from '@dnd-kit/core';
import { SortableContext, arrayMove, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

interface SagaTransactionManagerProps {
  flowId?: string;
  onSave?: (config: SagaConfiguration) => void;
  initialConfig?: SagaConfiguration;
  className?: string;
}

interface SagaStep {
  id: string;
  name: string;
  type: 'SERVICE_CALL' | 'TRANSFORMATION' | 'VALIDATION' | 'SPLIT' | 'JOIN' | 'CUSTOM';
  description?: string;
  serviceName?: string;
  operation?: string;
  compensationOperation?: string;
  timeout?: number;
  retryPolicy?: {
    enabled: boolean;
    maxAttempts: number;
    backoffMultiplier: number;
    initialInterval: number;
  };
  executionMode?: 'SEQUENTIAL' | 'PARALLEL';
  condition?: string;
  metadata?: Record<string, any>;
}

interface SagaStepGroup {
  id: string;
  name: string;
  steps: SagaStep[];
  executionMode: 'SEQUENTIAL' | 'PARALLEL';
  condition?: string;
}

interface SagaConfiguration {
  id?: string;
  name: string;
  description?: string;
  enabled: boolean;
  transactionBoundary: 'FLOW' | 'STEP' | 'GROUP';
  isolationLevel: 'READ_UNCOMMITTED' | 'READ_COMMITTED' | 'REPEATABLE_READ' | 'SERIALIZABLE';
  timeout: number;
  stepGroups: SagaStepGroup[];
  compensationStrategy: 'BACKWARD' | 'FORWARD' | 'CUSTOM';
  errorHandling: {
    onStepFailure: 'COMPENSATE' | 'RETRY' | 'CONTINUE' | 'FAIL_FAST';
    onCompensationFailure: 'CONTINUE' | 'HALT' | 'ALERT';
    deadLetterQueue?: string;
  };
}

const DEFAULT_STEP: SagaStep = {
  id: '',
  name: '',
  type: 'SERVICE_CALL',
  timeout: 30000,
  retryPolicy: {
    enabled: true,
    maxAttempts: 3,
    backoffMultiplier: 2,
    initialInterval: 1000
  }
};

const DEFAULT_CONFIG: SagaConfiguration = {
  name: '',
  enabled: true,
  transactionBoundary: 'FLOW',
  isolationLevel: 'READ_COMMITTED',
  timeout: 300000, // 5 minutes
  stepGroups: [],
  compensationStrategy: 'BACKWARD',
  errorHandling: {
    onStepFailure: 'COMPENSATE',
    onCompensationFailure: 'HALT'
  }
};

function SortableStepItem({ step, onEdit, onDelete }: { step: SagaStep; onEdit: () => void; onDelete: () => void }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: step.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={cn(
        "border rounded-lg p-3 bg-background",
        isDragging && "opacity-50"
      )}
      {...attributes}
      {...listeners}
    >
      <div className="flex items-start justify-between">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <Badge variant="outline">{step.type}</Badge>
            <span className="font-medium">{step.name}</span>
          </div>
          {step.description && (
            <p className="text-sm text-muted-foreground">{step.description}</p>
          )}
          <div className="flex items-center gap-4 text-xs text-muted-foreground">
            {step.serviceName && (
              <span>Service: {step.serviceName}</span>
            )}
            {step.timeout && (
              <span>Timeout: {step.timeout}ms</span>
            )}
            {step.retryPolicy?.enabled && (
              <span>Retry: {step.retryPolicy.maxAttempts}x</span>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button size="icon" variant="ghost" onClick={onEdit}>
            <Settings className="h-4 w-4" />
          </Button>
          <Button size="icon" variant="ghost" onClick={onDelete}>
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}

export function SagaTransactionManager({
  flowId,
  onSave,
  initialConfig = DEFAULT_CONFIG,
  className
}: SagaTransactionManagerProps) {
  const { toast } = useToast();
  const [config, setConfig] = useState<SagaConfiguration>(initialConfig);
  const [selectedGroup, setSelectedGroup] = useState<string | null>(null);
  const [selectedStep, setSelectedStep] = useState<SagaStep | null>(null);
  const [showStepEditor, setShowStepEditor] = useState(false);
  const [expandedGroups, setExpandedGroups] = useState<Record<string, boolean>>({});

  const handleConfigChange = useCallback((field: keyof SagaConfiguration, value: any) => {
    setConfig(prev => ({
      ...prev,
      [field]: value
    }));
  }, []);

  const handleErrorHandlingChange = useCallback((field: keyof SagaConfiguration['errorHandling'], value: any) => {
    setConfig(prev => ({
      ...prev,
      errorHandling: {
        ...prev.errorHandling,
        [field]: value
      }
    }));
  }, []);

  const addStepGroup = useCallback(() => {
    const newGroup: SagaStepGroup = {
      id: `group_${Date.now()}`,
      name: `Step Group ${config.stepGroups.length + 1}`,
      steps: [],
      executionMode: 'SEQUENTIAL'
    };

    setConfig(prev => ({
      ...prev,
      stepGroups: [...prev.stepGroups, newGroup]
    }));
    
    setExpandedGroups(prev => ({
      ...prev,
      [newGroup.id]: true
    }));
  }, [config.stepGroups.length]);

  const updateStepGroup = useCallback((groupId: string, updates: Partial<SagaStepGroup>) => {
    setConfig(prev => ({
      ...prev,
      stepGroups: prev.stepGroups.map(group => 
        group.id === groupId ? { ...group, ...updates } : group
      )
    }));
  }, []);

  const deleteStepGroup = useCallback((groupId: string) => {
    setConfig(prev => ({
      ...prev,
      stepGroups: prev.stepGroups.filter(group => group.id !== groupId)
    }));
  }, []);

  const addStep = useCallback((groupId: string) => {
    const newStep: SagaStep = {
      ...DEFAULT_STEP,
      id: `step_${Date.now()}`,
      name: `New Step`
    };

    setSelectedStep(newStep);
    setSelectedGroup(groupId);
    setShowStepEditor(true);
  }, []);

  const updateStep = useCallback((groupId: string, stepId: string, updates: Partial<SagaStep>) => {
    setConfig(prev => ({
      ...prev,
      stepGroups: prev.stepGroups.map(group => {
        if (group.id === groupId) {
          return {
            ...group,
            steps: group.steps.map(step =>
              step.id === stepId ? { ...step, ...updates } : step
            )
          };
        }
        return group;
      })
    }));
  }, []);

  const deleteStep = useCallback((groupId: string, stepId: string) => {
    setConfig(prev => ({
      ...prev,
      stepGroups: prev.stepGroups.map(group => {
        if (group.id === groupId) {
          return {
            ...group,
            steps: group.steps.filter(step => step.id !== stepId)
          };
        }
        return group;
      })
    }));
  }, []);

  const handleStepSave = useCallback(() => {
    if (!selectedStep || !selectedGroup) return;

    const groupIndex = config.stepGroups.findIndex(g => g.id === selectedGroup);
    if (groupIndex === -1) return;

    const existingStepIndex = config.stepGroups[groupIndex].steps.findIndex(s => s.id === selectedStep.id);
    
    if (existingStepIndex === -1) {
      // Add new step
      updateStepGroup(selectedGroup, {
        steps: [...config.stepGroups[groupIndex].steps, selectedStep]
      });
    } else {
      // Update existing step
      updateStep(selectedGroup, selectedStep.id, selectedStep);
    }

    setShowStepEditor(false);
    setSelectedStep(null);
    setSelectedGroup(null);
  }, [selectedStep, selectedGroup, config.stepGroups, updateStepGroup, updateStep]);

  const handleDragEnd = useCallback((event: DragEndEvent, groupId: string) => {
    const { active, over } = event;
    
    if (over && active.id !== over.id) {
      setConfig(prev => {
        const group = prev.stepGroups.find(g => g.id === groupId);
        if (!group) return prev;

        const oldIndex = group.steps.findIndex(s => s.id === active.id);
        const newIndex = group.steps.findIndex(s => s.id === over.id);
        
        const newSteps = arrayMove(group.steps, oldIndex, newIndex);
        
        return {
          ...prev,
          stepGroups: prev.stepGroups.map(g => 
            g.id === groupId ? { ...g, steps: newSteps } : g
          )
        };
      });
    }
  }, []);

  const toggleGroup = useCallback((groupId: string) => {
    setExpandedGroups(prev => ({
      ...prev,
      [groupId]: !prev[groupId]
    }));
  }, []);

  const handleSave = useCallback(() => {
    if (!config.name) {
      toast({
        title: 'Validation Error',
        description: 'Please provide a name for the saga configuration',
        variant: 'destructive'
      });
      return;
    }

    onSave?.(config);
    toast({
      title: 'Success',
      description: 'Saga configuration saved successfully'
    });
  }, [config, onSave, toast]);

  return (
    <div className={cn("space-y-6", className)}>
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <GitBranch className="h-5 w-5" />
            Transaction Management
          </CardTitle>
          <CardDescription>
            Configure saga pattern for distributed transactions and compensation flows
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="configuration">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="configuration">Configuration</TabsTrigger>
              <TabsTrigger value="steps">Transaction Steps</TabsTrigger>
              <TabsTrigger value="compensation">Compensation</TabsTrigger>
              <TabsTrigger value="monitoring">Monitoring</TabsTrigger>
            </TabsList>

            <TabsContent value="configuration" className="space-y-6">
              <div className="grid grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="name">Transaction Name *</Label>
                    <Input
                      id="name"
                      value={config.name}
                      onChange={(e) => handleConfigChange('name', e.target.value)}
                      placeholder="Enter transaction name"
                      className="mt-1"
                    />
                  </div>

                  <div>
                    <Label htmlFor="description">Description</Label>
                    <Textarea
                      id="description"
                      value={config.description || ''}
                      onChange={(e) => handleConfigChange('description', e.target.value)}
                      placeholder="Describe the transaction flow"
                      className="mt-1"
                      rows={3}
                    />
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="enabled"
                      checked={config.enabled}
                      onCheckedChange={(checked) => handleConfigChange('enabled', checked)}
                    />
                    <Label htmlFor="enabled">Enable Transaction Management</Label>
                  </div>
                </div>

                <div className="space-y-4">
                  <div>
                    <Label htmlFor="transactionBoundary">Transaction Boundary</Label>
                    <Select
                      value={config.transactionBoundary}
                      onValueChange={(value) => handleConfigChange('transactionBoundary', value)}
                    >
                      <SelectTrigger id="transactionBoundary" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="FLOW">Entire Flow</SelectItem>
                        <SelectItem value="GROUP">Step Group</SelectItem>
                        <SelectItem value="STEP">Individual Step</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="isolationLevel">Isolation Level</Label>
                    <Select
                      value={config.isolationLevel}
                      onValueChange={(value) => handleConfigChange('isolationLevel', value)}
                    >
                      <SelectTrigger id="isolationLevel" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="READ_UNCOMMITTED">Read Uncommitted</SelectItem>
                        <SelectItem value="READ_COMMITTED">Read Committed</SelectItem>
                        <SelectItem value="REPEATABLE_READ">Repeatable Read</SelectItem>
                        <SelectItem value="SERIALIZABLE">Serializable</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="timeout">Global Timeout (ms)</Label>
                    <Input
                      id="timeout"
                      type="number"
                      value={config.timeout}
                      onChange={(e) => handleConfigChange('timeout', parseInt(e.target.value))}
                      min={1000}
                      className="mt-1"
                    />
                  </div>
                </div>
              </div>

              <Separator />

              <div>
                <h4 className="font-medium mb-4">Error Handling</h4>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="onStepFailure">On Step Failure</Label>
                    <Select
                      value={config.errorHandling.onStepFailure}
                      onValueChange={(value) => handleErrorHandlingChange('onStepFailure', value)}
                    >
                      <SelectTrigger id="onStepFailure" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="COMPENSATE">Compensate (Rollback)</SelectItem>
                        <SelectItem value="RETRY">Retry Step</SelectItem>
                        <SelectItem value="CONTINUE">Continue Execution</SelectItem>
                        <SelectItem value="FAIL_FAST">Fail Fast</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div>
                    <Label htmlFor="onCompensationFailure">On Compensation Failure</Label>
                    <Select
                      value={config.errorHandling.onCompensationFailure}
                      onValueChange={(value) => handleErrorHandlingChange('onCompensationFailure', value)}
                    >
                      <SelectTrigger id="onCompensationFailure" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="CONTINUE">Continue Compensation</SelectItem>
                        <SelectItem value="HALT">Halt Process</SelectItem>
                        <SelectItem value="ALERT">Alert & Halt</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="steps" className="space-y-4">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h4 className="font-medium">Transaction Steps</h4>
                  <p className="text-sm text-muted-foreground">
                    Define the steps in your transaction flow
                  </p>
                </div>
                <Button onClick={addStepGroup}>
                  <Plus className="mr-2 h-4 w-4" />
                  Add Step Group
                </Button>
              </div>

              <ScrollArea className="h-[500px]">
                <div className="space-y-4">
                  {config.stepGroups.map((group) => (
                    <Card key={group.id}>
                      <CardHeader className="pb-3">
                        <Collapsible
                          open={expandedGroups[group.id] !== false}
                          onOpenChange={() => toggleGroup(group.id)}
                        >
                          <div className="flex items-center justify-between">
                            <CollapsibleTrigger className="flex items-center gap-2">
                              {expandedGroups[group.id] !== false ? (
                                <ChevronDown className="h-4 w-4" />
                              ) : (
                                <ChevronRight className="h-4 w-4" />
                              )}
                              <Input
                                value={group.name}
                                onChange={(e) => updateStepGroup(group.id, { name: e.target.value })}
                                onClick={(e) => e.stopPropagation()}
                                className="w-48"
                              />
                              <Badge variant={group.executionMode === 'PARALLEL' ? 'default' : 'secondary'}>
                                {group.executionMode === 'PARALLEL' ? (
                                  <>
                                    <Layers className="mr-1 h-3 w-3" />
                                    Parallel
                                  </>
                                ) : (
                                  <>
                                    <ArrowDown className="mr-1 h-3 w-3" />
                                    Sequential
                                  </>
                                )}
                              </Badge>
                            </CollapsibleTrigger>
                            <div className="flex items-center gap-2">
                              <Select
                                value={group.executionMode}
                                onValueChange={(value) => updateStepGroup(group.id, { executionMode: value as any })}
                              >
                                <SelectTrigger className="w-32">
                                  <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem value="SEQUENTIAL">Sequential</SelectItem>
                                  <SelectItem value="PARALLEL">Parallel</SelectItem>
                                </SelectContent>
                              </Select>
                              <Button
                                size="sm"
                                variant="ghost"
                                onClick={() => addStep(group.id)}
                              >
                                <Plus className="h-4 w-4" />
                              </Button>
                              <Button
                                size="icon"
                                variant="ghost"
                                onClick={() => deleteStepGroup(group.id)}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </div>
                          </div>
                          <CollapsibleContent className="mt-4">
                            <DndContext
                              collisionDetection={closestCorners}
                              onDragEnd={(event) => handleDragEnd(event, group.id)}
                            >
                              <SortableContext
                                items={group.steps.map(s => s.id)}
                                strategy={verticalListSortingStrategy}
                              >
                                <div className="space-y-2">
                                  {group.steps.map((step) => (
                                    <SortableStepItem
                                      key={step.id}
                                      step={step}
                                      onEdit={() => {
                                        setSelectedStep(step);
                                        setSelectedGroup(group.id);
                                        setShowStepEditor(true);
                                      }}
                                      onDelete={() => deleteStep(group.id, step.id)}
                                    />
                                  ))}
                                  {group.steps.length === 0 && (
                                    <div className="text-center py-8 text-muted-foreground">
                                      <Zap className="h-8 w-8 mx-auto mb-2" />
                                      <p className="text-sm">No steps defined</p>
                                      <Button
                                        size="sm"
                                        variant="ghost"
                                        onClick={() => addStep(group.id)}
                                        className="mt-2"
                                      >
                                        <Plus className="mr-2 h-4 w-4" />
                                        Add First Step
                                      </Button>
                                    </div>
                                  )}
                                </div>
                              </SortableContext>
                            </DndContext>
                          </CollapsibleContent>
                        </Collapsible>
                      </CardHeader>
                    </Card>
                  ))}
                  
                  {config.stepGroups.length === 0 && (
                    <Card>
                      <CardContent className="pt-6">
                        <div className="text-center space-y-4">
                          <GitBranch className="w-12 h-12 mx-auto text-muted-foreground" />
                          <div>
                            <h3 className="font-medium">No Transaction Steps</h3>
                            <p className="text-sm text-muted-foreground mt-1">
                              Create step groups to define your transaction flow
                            </p>
                          </div>
                          <Button onClick={addStepGroup}>
                            <Plus className="mr-2 h-4 w-4" />
                            Add First Step Group
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  )}
                </div>
              </ScrollArea>
            </TabsContent>

            <TabsContent value="compensation" className="space-y-6">
              <div>
                <h4 className="font-medium mb-4">Compensation Strategy</h4>
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="compensationStrategy">Compensation Order</Label>
                    <Select
                      value={config.compensationStrategy}
                      onValueChange={(value) => handleConfigChange('compensationStrategy', value)}
                    >
                      <SelectTrigger id="compensationStrategy" className="mt-1">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="BACKWARD">Backward (Reverse Order)</SelectItem>
                        <SelectItem value="FORWARD">Forward (Same Order)</SelectItem>
                        <SelectItem value="CUSTOM">Custom Order</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <Alert>
                    <Shield className="h-4 w-4" />
                    <AlertTitle>Compensation Flow</AlertTitle>
                    <AlertDescription>
                      Each step should define its compensation operation. When a transaction fails,
                      the compensation flow will execute based on the selected strategy.
                    </AlertDescription>
                  </Alert>

                  <div className="space-y-2">
                    <h5 className="text-sm font-medium">Compensation Preview</h5>
                    <div className="border rounded-lg p-4 space-y-2">
                      {config.stepGroups.flatMap(group => 
                        group.steps.filter(step => step.compensationOperation)
                      ).map((step, index) => (
                        <div key={step.id} className="flex items-center gap-2">
                          <Badge variant="outline" className="w-8">{index + 1}</Badge>
                          <span className="text-sm">{step.name}</span>
                          <ArrowRight className="h-4 w-4 text-muted-foreground" />
                          <span className="text-sm text-muted-foreground">
                            {step.compensationOperation || 'No compensation defined'}
                          </span>
                        </div>
                      ))}
                      {config.stepGroups.every(g => g.steps.every(s => !s.compensationOperation)) && (
                        <p className="text-sm text-muted-foreground text-center py-4">
                          No compensation operations defined
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="monitoring" className="space-y-6">
              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>Transaction Monitoring</AlertTitle>
                <AlertDescription>
                  Monitor running transactions, view execution history, and analyze performance metrics
                  from the Monitoring Dashboard.
                </AlertDescription>
              </Alert>

              <div className="grid grid-cols-2 gap-6">
                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">Monitoring Settings</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex items-center space-x-2">
                      <Switch id="enableMetrics" defaultChecked />
                      <Label htmlFor="enableMetrics">Collect Performance Metrics</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Switch id="enableTracing" defaultChecked />
                      <Label htmlFor="enableTracing">Enable Distributed Tracing</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Switch id="enableAlerting" />
                      <Label htmlFor="enableAlerting">Enable Transaction Alerts</Label>
                    </div>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">Dead Letter Queue</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div>
                      <Label htmlFor="dlq">Failed Transaction Queue</Label>
                      <Input
                        id="dlq"
                        value={config.errorHandling.deadLetterQueue || ''}
                        onChange={(e) => handleErrorHandlingChange('deadLetterQueue', e.target.value)}
                        placeholder="e.g., failed-transactions-queue"
                        className="mt-1"
                      />
                      <p className="text-xs text-muted-foreground mt-2">
                        Failed transactions will be sent to this queue for manual processing
                      </p>
                    </div>
                  </CardContent>
                </Card>
              </div>
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

      {/* Step Editor Dialog */}
      {showStepEditor && selectedStep && (
        <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50">
          <div className="fixed inset-4 md:inset-auto md:left-1/2 md:top-1/2 md:-translate-x-1/2 md:-translate-y-1/2 md:w-[600px] bg-background border rounded-lg shadow-lg">
            <div className="p-6 space-y-4">
              <h3 className="text-lg font-semibold">
                {selectedStep.id ? 'Edit Step' : 'Add Step'}
              </h3>
              
              <div className="space-y-4">
                <div>
                  <Label htmlFor="stepName">Step Name *</Label>
                  <Input
                    id="stepName"
                    value={selectedStep.name}
                    onChange={(e) => setSelectedStep({ ...selectedStep, name: e.target.value })}
                    className="mt-1"
                  />
                </div>

                <div>
                  <Label htmlFor="stepType">Step Type</Label>
                  <Select
                    value={selectedStep.type}
                    onValueChange={(value) => setSelectedStep({ ...selectedStep, type: value as any })}
                  >
                    <SelectTrigger id="stepType" className="mt-1">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="SERVICE_CALL">Service Call</SelectItem>
                      <SelectItem value="TRANSFORMATION">Transformation</SelectItem>
                      <SelectItem value="VALIDATION">Validation</SelectItem>
                      <SelectItem value="SPLIT">Split</SelectItem>
                      <SelectItem value="JOIN">Join</SelectItem>
                      <SelectItem value="CUSTOM">Custom</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <Label htmlFor="stepDescription">Description</Label>
                  <Textarea
                    id="stepDescription"
                    value={selectedStep.description || ''}
                    onChange={(e) => setSelectedStep({ ...selectedStep, description: e.target.value })}
                    className="mt-1"
                    rows={2}
                  />
                </div>

                {selectedStep.type === 'SERVICE_CALL' && (
                  <>
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
                      <Label htmlFor="operation">Operation</Label>
                      <Input
                        id="operation"
                        value={selectedStep.operation || ''}
                        onChange={(e) => setSelectedStep({ ...selectedStep, operation: e.target.value })}
                        className="mt-1"
                      />
                    </div>

                    <div>
                      <Label htmlFor="compensationOperation">Compensation Operation</Label>
                      <Input
                        id="compensationOperation"
                        value={selectedStep.compensationOperation || ''}
                        onChange={(e) => setSelectedStep({ ...selectedStep, compensationOperation: e.target.value })}
                        placeholder="e.g., rollback, cancel, delete"
                        className="mt-1"
                      />
                    </div>
                  </>
                )}

                <div>
                  <Label htmlFor="stepTimeout">Timeout (ms)</Label>
                  <Input
                    id="stepTimeout"
                    type="number"
                    value={selectedStep.timeout || 30000}
                    onChange={(e) => setSelectedStep({ ...selectedStep, timeout: parseInt(e.target.value) })}
                    min={1000}
                    className="mt-1"
                  />
                </div>

                <div className="space-y-2">
                  <div className="flex items-center space-x-2">
                    <Switch
                      id="retryEnabled"
                      checked={selectedStep.retryPolicy?.enabled || false}
                      onCheckedChange={(checked) => setSelectedStep({
                        ...selectedStep,
                        retryPolicy: {
                          ...selectedStep.retryPolicy!,
                          enabled: checked
                        }
                      })}
                    />
                    <Label htmlFor="retryEnabled">Enable Retry</Label>
                  </div>
                  
                  {selectedStep.retryPolicy?.enabled && (
                    <div className="grid grid-cols-3 gap-2 ml-6">
                      <div>
                        <Label htmlFor="maxAttempts" className="text-xs">Max Attempts</Label>
                        <Input
                          id="maxAttempts"
                          type="number"
                          value={selectedStep.retryPolicy.maxAttempts}
                          onChange={(e) => setSelectedStep({
                            ...selectedStep,
                            retryPolicy: {
                              ...selectedStep.retryPolicy!,
                              maxAttempts: parseInt(e.target.value)
                            }
                          })}
                          min={1}
                          max={10}
                          className="mt-1"
                        />
                      </div>
                      <div>
                        <Label htmlFor="initialInterval" className="text-xs">Initial Interval (ms)</Label>
                        <Input
                          id="initialInterval"
                          type="number"
                          value={selectedStep.retryPolicy.initialInterval}
                          onChange={(e) => setSelectedStep({
                            ...selectedStep,
                            retryPolicy: {
                              ...selectedStep.retryPolicy!,
                              initialInterval: parseInt(e.target.value)
                            }
                          })}
                          min={100}
                          className="mt-1"
                        />
                      </div>
                      <div>
                        <Label htmlFor="backoffMultiplier" className="text-xs">Backoff Multiplier</Label>
                        <Input
                          id="backoffMultiplier"
                          type="number"
                          value={selectedStep.retryPolicy.backoffMultiplier}
                          onChange={(e) => setSelectedStep({
                            ...selectedStep,
                            retryPolicy: {
                              ...selectedStep.retryPolicy!,
                              backoffMultiplier: parseFloat(e.target.value)
                            }
                          })}
                          min={1}
                          step={0.5}
                          className="mt-1"
                        />
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <div className="flex justify-end gap-2 mt-6">
                <Button
                  variant="outline"
                  onClick={() => {
                    setShowStepEditor(false);
                    setSelectedStep(null);
                    setSelectedGroup(null);
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