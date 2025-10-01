import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { Progress } from '@/components/ui/progress';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Activity,
  AlertCircle,
  CheckCircle2,
  Clock,
  RefreshCw,
  Play,
  Pause,
  Square,
  SkipForward,
  Zap,
  GitBranch,
  Layers,
  ArrowRight,
  Download,
  Filter,
  Search,
  Eye,
  EyeOff,
  Maximize2,
  Minimize2,
  Info,
  XCircle,
  Timer,
  TrendingUp,
  Database,
  Network,
  Cpu,
  BarChart3
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
  BackgroundVariant,
  Panel
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { apiClient } from '@/lib/api-client';

interface OrchestrationFlowVisualizerProps {
  flowId: string;
  executionId?: string;
  autoRefresh?: boolean;
  refreshInterval?: number;
  className?: string;
}

interface FlowExecution {
  id: string;
  flowId: string;
  status: 'RUNNING' | 'COMPLETED' | 'FAILED' | 'PAUSED' | 'CANCELLED';
  startTime: string;
  endTime?: string;
  currentStep?: string;
  progress: number;
  metrics: ExecutionMetrics;
  steps: StepExecution[];
}

interface StepExecution {
  id: string;
  stepId: string;
  name: string;
  type: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'SKIPPED';
  startTime?: string;
  endTime?: string;
  duration?: number;
  retryCount: number;
  error?: string;
  input?: any;
  output?: any;
  metrics?: StepMetrics;
}

interface ExecutionMetrics {
  totalSteps: number;
  completedSteps: number;
  failedSteps: number;
  averageStepDuration: number;
  totalDuration: number;
  memoryUsage: number;
  cpuUsage: number;
  throughput: number;
}

interface StepMetrics {
  executionTime: number;
  queueTime: number;
  retries: number;
  inputSize: number;
  outputSize: number;
  memoryPeak: number;
}

interface FlowNodeData {
  label: string;
  stepExecution?: StepExecution;
  nodeType: 'start' | 'end' | 'step' | 'router' | 'transformer';
  status?: StepExecution['status'];
  progress?: number;
}

const getStatusColor = (status?: StepExecution['status']) => {
  switch (status) {
    case 'COMPLETED': return '#10b981';
    case 'RUNNING': return '#3b82f6';
    case 'FAILED': return '#ef4444';
    case 'PENDING': return '#6b7280';
    case 'SKIPPED': return '#f59e0b';
    default: return '#6b7280';
  }
};

const getStatusIcon = (status?: StepExecution['status']) => {
  switch (status) {
    case 'COMPLETED': return <CheckCircle2 className="h-4 w-4" />;
    case 'RUNNING': return <RefreshCw className="h-4 w-4 animate-spin" />;
    case 'FAILED': return <XCircle className="h-4 w-4" />;
    case 'PENDING': return <Clock className="h-4 w-4" />;
    case 'SKIPPED': return <SkipForward className="h-4 w-4" />;
    default: return <Activity className="h-4 w-4" />;
  }
};

const FlowStepNode = ({ data, selected }: NodeProps<FlowNodeData>) => {
  const isRunning = data.status === 'RUNNING';
  
  return (
    <div
      className={cn(
        "px-4 py-3 rounded-lg border-2 bg-background min-w-[200px]",
        selected ? "border-primary" : "border-border",
        isRunning && "animate-pulse"
      )}
      style={{
        borderColor: selected ? undefined : getStatusColor(data.status),
        boxShadow: isRunning ? `0 0 20px ${getStatusColor(data.status)}40` : undefined
      }}
    >
      <Handle
        type="target"
        position={Position.Top}
        style={{ background: getStatusColor(data.status) }}
        className="w-3 h-3"
      />
      <Handle
        type="source"
        position={Position.Bottom}
        style={{ background: getStatusColor(data.status) }}
        className="w-3 h-3"
      />
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <span className="font-medium text-sm">{data.label}</span>
          <div className="flex items-center gap-1">
            {getStatusIcon(data.status)}
            {data.stepExecution?.retryCount > 0 && (
              <Badge variant="secondary" className="text-xs">
                Retry {data.stepExecution.retryCount}
              </Badge>
            )}
          </div>
        </div>
        {data.stepExecution && (
          <>
            {data.status === 'RUNNING' && data.progress !== undefined && (
              <Progress value={data.progress} className="h-1" />
            )}
            {data.stepExecution.duration && (
              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                <Timer className="h-3 w-3" />
                {data.stepExecution.duration}ms
              </div>
            )}
            {data.stepExecution.error && (
              <div className="text-xs text-red-500 truncate" title={data.stepExecution.error}>
                {data.stepExecution.error}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

const StartEndNode = ({ data, selected }: NodeProps<FlowNodeData>) => {
  const isStart = data.nodeType === 'start';
  
  return (
    <div
      className={cn(
        "w-12 h-12 rounded-full border-2 flex items-center justify-center bg-background",
        selected ? "border-primary" : isStart ? "border-green-500" : "border-blue-500"
      )}
    >
      {isStart ? (
        <>
          <Play className="h-5 w-5 text-green-500" />
          <Handle
            type="source"
            position={Position.Bottom}
            className="w-3 h-3"
            style={{ background: '#10b981' }}
          />
        </>
      ) : (
        <>
          <Square className="h-5 w-5 text-blue-500" />
          <Handle
            type="target"
            position={Position.Top}
            className="w-3 h-3"
            style={{ background: '#3b82f6' }}
          />
        </>
      )}
    </div>
  );
};

const nodeTypes = {
  flowStep: FlowStepNode,
  start: StartEndNode,
  end: StartEndNode
};

export function OrchestrationFlowVisualizer({
  flowId,
  executionId,
  autoRefresh = true,
  refreshInterval = 5000,
  className
}: OrchestrationFlowVisualizerProps) {
  const { toast } = useToast();
  const [execution, setExecution] = useState<FlowExecution | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedStep, setSelectedStep] = useState<StepExecution | null>(null);
  const [filter, setFilter] = useState<string>('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [showMetrics, setShowMetrics] = useState(true);
  const [isPaused, setIsPaused] = useState(false);

  // Create flow nodes and edges from execution data
  const { nodes, edges } = useMemo(() => {
    if (!execution) return { nodes: [], edges: [] };

    const nodes: Node<FlowNodeData>[] = [];
    const edges: Edge[] = [];

    // Add start node
    nodes.push({
      id: 'start',
      type: 'start',
      position: { x: 250, y: 0 },
      data: { label: 'Start', nodeType: 'start' }
    });

    // Add step nodes
    execution.steps.forEach((step, index) => {
      const row = Math.floor(index / 3);
      const col = index % 3;
      
      nodes.push({
        id: step.id,
        type: 'flowStep',
        position: { x: 100 + col * 250, y: 100 + row * 150 },
        data: {
          label: step.name,
          stepExecution: step,
          nodeType: 'step',
          status: step.status,
          progress: step.status === 'RUNNING' ? 50 : undefined
        }
      });

      // Add edge from previous step or start
      if (index === 0) {
        edges.push({
          id: `start-${step.id}`,
          source: 'start',
          target: step.id,
          animated: step.status === 'RUNNING',
          style: {
            stroke: getStatusColor(step.status),
            strokeWidth: 2
          }
        });
      } else {
        edges.push({
          id: `${execution.steps[index - 1].id}-${step.id}`,
          source: execution.steps[index - 1].id,
          target: step.id,
          animated: step.status === 'RUNNING',
          style: {
            stroke: getStatusColor(step.status),
            strokeWidth: 2
          }
        });
      }
    });

    // Add end node
    if (execution.steps.length > 0) {
      const lastStep = execution.steps[execution.steps.length - 1];
      nodes.push({
        id: 'end',
        type: 'end',
        position: { x: 250, y: 100 + Math.floor((execution.steps.length - 1) / 3 + 1) * 150 },
        data: { label: 'End', nodeType: 'end' }
      });

      edges.push({
        id: `${lastStep.id}-end`,
        source: lastStep.id,
        target: 'end',
        style: {
          stroke: getStatusColor(lastStep.status),
          strokeWidth: 2
        }
      });
    }

    return { nodes, edges };
  }, [execution]);

  const [flowNodes, setFlowNodes, onNodesChange] = useNodesState(nodes);
  const [flowEdges, setFlowEdges, onEdgesChange] = useEdgesState(edges);

  // Update nodes and edges when execution changes
  useEffect(() => {
    setFlowNodes(nodes);
    setFlowEdges(edges);
  }, [nodes, edges, setFlowNodes, setFlowEdges]);

  // Fetch execution data
  const fetchExecution = useCallback(async () => {
    if (!flowId) return;

    try {
      setLoading(true);
      setError(null);
      
      const response = await apiClient.get(`/api/flows/${flowId}/executions/${executionId || 'latest'}`);
      setExecution(response.data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch execution data');
      console.error('Error fetching execution:', err);
    } finally {
      setLoading(false);
    }
  }, [flowId, executionId]);

  // Auto-refresh logic
  useEffect(() => {
    fetchExecution();

    if (autoRefresh && !isPaused) {
      const interval = setInterval(fetchExecution, refreshInterval);
      return () => clearInterval(interval);
    }
  }, [fetchExecution, autoRefresh, refreshInterval, isPaused]);

  const handleNodeClick = useCallback((event: React.MouseEvent, node: Node<FlowNodeData>) => {
    if (node.data.stepExecution) {
      setSelectedStep(node.data.stepExecution);
    }
  }, []);

  const handlePauseExecution = useCallback(async () => {
    try {
      await apiClient.post(`/api/flows/${flowId}/executions/${execution?.id}/pause`);
      toast({
        title: 'Success',
        description: 'Execution paused'
      });
      fetchExecution();
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.message || 'Failed to pause execution',
        variant: 'destructive'
      });
    }
  }, [flowId, execution?.id, fetchExecution, toast]);

  const handleResumeExecution = useCallback(async () => {
    try {
      await apiClient.post(`/api/flows/${flowId}/executions/${execution?.id}/resume`);
      toast({
        title: 'Success',
        description: 'Execution resumed'
      });
      fetchExecution();
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.message || 'Failed to resume execution',
        variant: 'destructive'
      });
    }
  }, [flowId, execution?.id, fetchExecution, toast]);

  const handleCancelExecution = useCallback(async () => {
    try {
      await apiClient.post(`/api/flows/${flowId}/executions/${execution?.id}/cancel`);
      toast({
        title: 'Success',
        description: 'Execution cancelled'
      });
      fetchExecution();
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.message || 'Failed to cancel execution',
        variant: 'destructive'
      });
    }
  }, [flowId, execution?.id, fetchExecution, toast]);

  const exportExecutionData = useCallback(() => {
    if (!execution) return;

    const data = JSON.stringify(execution, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `execution-${execution.id}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }, [execution]);

  const filteredSteps = useMemo(() => {
    if (!execution) return [];
    
    let steps = execution.steps;

    // Apply status filter
    if (filter !== 'all') {
      steps = steps.filter(step => step.status === filter);
    }

    // Apply search filter
    if (searchTerm) {
      steps = steps.filter(step => 
        step.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        step.id.includes(searchTerm) ||
        (step.error && step.error.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    }

    return steps;
  }, [execution, filter, searchTerm]);

  if (loading && !execution) {
    return (
      <Card className={className}>
        <CardHeader>
          <Skeleton className="h-6 w-48" />
          <Skeleton className="h-4 w-72 mt-2" />
        </CardHeader>
        <CardContent>
          <Skeleton className="h-[600px] w-full" />
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className={className}>
        <CardContent className="pt-6">
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
          <Button onClick={fetchExecution} className="mt-4">
            <RefreshCw className="mr-2 h-4 w-4" />
            Retry
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className={cn("space-y-6", className, isFullscreen && "fixed inset-0 z-50 bg-background p-4")}>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <Activity className="h-5 w-5" />
                Orchestration Flow Visualizer
                {execution && (
                  <Badge
                    variant={
                      execution.status === 'COMPLETED' ? 'default' :
                      execution.status === 'FAILED' ? 'destructive' :
                      execution.status === 'RUNNING' ? 'secondary' : 'outline'
                    }
                  >
                    {execution.status}
                  </Badge>
                )}
              </CardTitle>
              <CardDescription>
                {execution ? (
                  <>
                    Execution ID: {execution.id} • Started: {new Date(execution.startTime).toLocaleString()}
                    {execution.endTime && ` • Ended: ${new Date(execution.endTime).toLocaleString()}`}
                  </>
                ) : (
                  'No execution data available'
                )}
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              {execution?.status === 'RUNNING' && (
                <>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={handlePauseExecution}
                  >
                    <Pause className="mr-2 h-4 w-4" />
                    Pause
                  </Button>
                </>
              )}
              {execution?.status === 'PAUSED' && (
                <Button
                  size="sm"
                  variant="outline"
                  onClick={handleResumeExecution}
                >
                  <Play className="mr-2 h-4 w-4" />
                  Resume
                </Button>
              )}
              {execution && ['RUNNING', 'PAUSED'].includes(execution.status) && (
                <Button
                  size="sm"
                  variant="outline"
                  onClick={handleCancelExecution}
                >
                  <Square className="mr-2 h-4 w-4" />
                  Cancel
                </Button>
              )}
              <Button
                size="sm"
                variant="outline"
                onClick={() => setIsPaused(!isPaused)}
              >
                {isPaused ? (
                  <>
                    <Play className="mr-2 h-4 w-4" />
                    Resume Refresh
                  </>
                ) : (
                  <>
                    <Pause className="mr-2 h-4 w-4" />
                    Pause Refresh
                  </>
                )}
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={fetchExecution}
              >
                <RefreshCw className="h-4 w-4" />
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={exportExecutionData}
                disabled={!execution}
              >
                <Download className="h-4 w-4" />
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={() => setIsFullscreen(!isFullscreen)}
              >
                {isFullscreen ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {execution ? (
            <Tabs defaultValue="visual">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="visual">Visual Flow</TabsTrigger>
                <TabsTrigger value="steps">Step Details</TabsTrigger>
                <TabsTrigger value="metrics">Metrics</TabsTrigger>
              </TabsList>

              <TabsContent value="visual" className="space-y-4">
                {showMetrics && execution.metrics && (
                  <div className="grid grid-cols-4 gap-4 mb-4">
                    <Card>
                      <CardContent className="pt-6">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-sm text-muted-foreground">Progress</p>
                            <p className="text-2xl font-bold">{execution.progress}%</p>
                          </div>
                          <Progress value={execution.progress} className="w-16 h-16 rounded-full" />
                        </div>
                      </CardContent>
                    </Card>
                    <Card>
                      <CardContent className="pt-6">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-sm text-muted-foreground">Steps</p>
                            <p className="text-2xl font-bold">
                              {execution.metrics.completedSteps}/{execution.metrics.totalSteps}
                            </p>
                          </div>
                          <Layers className="h-8 w-8 text-muted-foreground" />
                        </div>
                      </CardContent>
                    </Card>
                    <Card>
                      <CardContent className="pt-6">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-sm text-muted-foreground">Duration</p>
                            <p className="text-2xl font-bold">
                              {(execution.metrics.totalDuration / 1000).toFixed(2)}s
                            </p>
                          </div>
                          <Clock className="h-8 w-8 text-muted-foreground" />
                        </div>
                      </CardContent>
                    </Card>
                    <Card>
                      <CardContent className="pt-6">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-sm text-muted-foreground">Throughput</p>
                            <p className="text-2xl font-bold">
                              {execution.metrics.throughput.toFixed(0)}/s
                            </p>
                          </div>
                          <TrendingUp className="h-8 w-8 text-muted-foreground" />
                        </div>
                      </CardContent>
                    </Card>
                  </div>
                )}

                <div className="h-[600px] border rounded-lg relative">
                  <Button
                    size="sm"
                    variant="outline"
                    className="absolute top-4 right-4 z-10"
                    onClick={() => setShowMetrics(!showMetrics)}
                  >
                    {showMetrics ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </Button>
                  <ReactFlowProvider>
                    <ReactFlow
                      nodes={flowNodes}
                      edges={flowEdges}
                      onNodesChange={onNodesChange}
                      onEdgesChange={onEdgesChange}
                      onNodeClick={handleNodeClick}
                      nodeTypes={nodeTypes}
                      fitView
                      attributionPosition="bottom-left"
                    >
                      <Background variant={BackgroundVariant.Dots} />
                      <Controls />
                      <MiniMap />
                      <Panel position="top-left">
                        <div className="bg-background/80 backdrop-blur-sm p-2 rounded-lg space-y-1">
                          <div className="flex items-center gap-2 text-xs">
                            <div className="w-3 h-3 rounded-full bg-green-500" />
                            <span>Completed</span>
                          </div>
                          <div className="flex items-center gap-2 text-xs">
                            <div className="w-3 h-3 rounded-full bg-blue-500" />
                            <span>Running</span>
                          </div>
                          <div className="flex items-center gap-2 text-xs">
                            <div className="w-3 h-3 rounded-full bg-red-500" />
                            <span>Failed</span>
                          </div>
                          <div className="flex items-center gap-2 text-xs">
                            <div className="w-3 h-3 rounded-full bg-gray-500" />
                            <span>Pending</span>
                          </div>
                          <div className="flex items-center gap-2 text-xs">
                            <div className="w-3 h-3 rounded-full bg-yellow-500" />
                            <span>Skipped</span>
                          </div>
                        </div>
                      </Panel>
                    </ReactFlow>
                  </ReactFlowProvider>
                </div>
              </TabsContent>

              <TabsContent value="steps" className="space-y-4">
                <div className="flex items-center gap-4 mb-4">
                  <div className="flex-1">
                    <div className="relative">
                      <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                      <Input
                        placeholder="Search steps..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="pl-8"
                      />
                    </div>
                  </div>
                  <Select value={filter} onValueChange={setFilter}>
                    <SelectTrigger className="w-[180px]">
                      <SelectValue placeholder="Filter by status" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Status</SelectItem>
                      <SelectItem value="COMPLETED">Completed</SelectItem>
                      <SelectItem value="RUNNING">Running</SelectItem>
                      <SelectItem value="FAILED">Failed</SelectItem>
                      <SelectItem value="PENDING">Pending</SelectItem>
                      <SelectItem value="SKIPPED">Skipped</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <ScrollArea className="h-[500px]">
                  <div className="space-y-2">
                    {filteredSteps.map((step) => (
                      <Card
                        key={step.id}
                        className={cn(
                          "cursor-pointer transition-colors",
                          selectedStep?.id === step.id && "border-primary"
                        )}
                        onClick={() => setSelectedStep(step)}
                      >
                        <CardHeader className="pb-3">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-2">
                              {getStatusIcon(step.status)}
                              <span className="font-medium">{step.name}</span>
                              <Badge variant="outline">{step.type}</Badge>
                            </div>
                            <Badge
                              variant={
                                step.status === 'COMPLETED' ? 'default' :
                                step.status === 'FAILED' ? 'destructive' :
                                step.status === 'RUNNING' ? 'secondary' : 'outline'
                              }
                            >
                              {step.status}
                            </Badge>
                          </div>
                        </CardHeader>
                        <CardContent>
                          <div className="grid grid-cols-4 gap-4 text-sm">
                            {step.startTime && (
                              <div>
                                <p className="text-muted-foreground">Started</p>
                                <p className="font-medium">
                                  {new Date(step.startTime).toLocaleTimeString()}
                                </p>
                              </div>
                            )}
                            {step.duration !== undefined && (
                              <div>
                                <p className="text-muted-foreground">Duration</p>
                                <p className="font-medium">{step.duration}ms</p>
                              </div>
                            )}
                            {step.retryCount > 0 && (
                              <div>
                                <p className="text-muted-foreground">Retries</p>
                                <p className="font-medium">{step.retryCount}</p>
                              </div>
                            )}
                            {step.metrics && (
                              <div>
                                <p className="text-muted-foreground">Memory</p>
                                <p className="font-medium">
                                  {(step.metrics.memoryPeak / 1024 / 1024).toFixed(2)} MB
                                </p>
                              </div>
                            )}
                          </div>
                          {step.error && (
                            <Alert variant="destructive" className="mt-4">
                              <AlertCircle className="h-4 w-4" />
                              <AlertDescription className="text-xs">
                                {step.error}
                              </AlertDescription>
                            </Alert>
                          )}
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                </ScrollArea>
              </TabsContent>

              <TabsContent value="metrics" className="space-y-4">
                {execution.metrics && (
                  <div className="grid grid-cols-2 gap-6">
                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base flex items-center gap-2">
                          <BarChart3 className="h-4 w-4" />
                          Execution Metrics
                        </CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <p className="text-sm text-muted-foreground">Total Steps</p>
                            <p className="text-xl font-bold">{execution.metrics.totalSteps}</p>
                          </div>
                          <div>
                            <p className="text-sm text-muted-foreground">Completed</p>
                            <p className="text-xl font-bold text-green-600">
                              {execution.metrics.completedSteps}
                            </p>
                          </div>
                          <div>
                            <p className="text-sm text-muted-foreground">Failed</p>
                            <p className="text-xl font-bold text-red-600">
                              {execution.metrics.failedSteps}
                            </p>
                          </div>
                          <div>
                            <p className="text-sm text-muted-foreground">Success Rate</p>
                            <p className="text-xl font-bold">
                              {execution.metrics.totalSteps > 0
                                ? ((execution.metrics.completedSteps / execution.metrics.totalSteps) * 100).toFixed(1)
                                : 0}%
                            </p>
                          </div>
                        </div>
                        <Separator />
                        <div className="space-y-2">
                          <div className="flex justify-between">
                            <span className="text-sm text-muted-foreground">Avg Step Duration</span>
                            <span className="font-medium">
                              {execution.metrics.averageStepDuration.toFixed(2)}ms
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-sm text-muted-foreground">Total Duration</span>
                            <span className="font-medium">
                              {(execution.metrics.totalDuration / 1000).toFixed(2)}s
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-sm text-muted-foreground">Throughput</span>
                            <span className="font-medium">
                              {execution.metrics.throughput.toFixed(2)} ops/sec
                            </span>
                          </div>
                        </div>
                      </CardContent>
                    </Card>

                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base flex items-center gap-2">
                          <Cpu className="h-4 w-4" />
                          Resource Usage
                        </CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="space-y-4">
                          <div>
                            <div className="flex justify-between mb-2">
                              <span className="text-sm text-muted-foreground">CPU Usage</span>
                              <span className="font-medium">{execution.metrics.cpuUsage.toFixed(1)}%</span>
                            </div>
                            <Progress value={execution.metrics.cpuUsage} />
                          </div>
                          <div>
                            <div className="flex justify-between mb-2">
                              <span className="text-sm text-muted-foreground">Memory Usage</span>
                              <span className="font-medium">
                                {(execution.metrics.memoryUsage / 1024 / 1024).toFixed(2)} MB
                              </span>
                            </div>
                            <Progress value={Math.min((execution.metrics.memoryUsage / (1024 * 1024 * 1024)) * 100, 100)} />
                          </div>
                        </div>
                        <Separator />
                        <Alert>
                          <Info className="h-4 w-4" />
                          <AlertDescription className="text-xs">
                            Resource metrics are sampled every {refreshInterval / 1000} seconds
                          </AlertDescription>
                        </Alert>
                      </CardContent>
                    </Card>
                  </div>
                )}
              </TabsContent>
            </Tabs>
          ) : (
            <div className="text-center py-12">
              <Activity className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
              <p className="text-muted-foreground">No execution data available</p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Step Details Modal */}
      {selectedStep && (
        <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50" onClick={() => setSelectedStep(null)}>
          <div className="fixed inset-4 md:inset-auto md:left-1/2 md:top-1/2 md:-translate-x-1/2 md:-translate-y-1/2 md:w-[600px] md:max-h-[80vh] overflow-y-auto bg-background border rounded-lg shadow-lg" onClick={(e) => e.stopPropagation()}>
            <div className="p-6 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold flex items-center gap-2">
                  {getStatusIcon(selectedStep.status)}
                  {selectedStep.name}
                </h3>
                <Badge
                  variant={
                    selectedStep.status === 'COMPLETED' ? 'default' :
                    selectedStep.status === 'FAILED' ? 'destructive' :
                    selectedStep.status === 'RUNNING' ? 'secondary' : 'outline'
                  }
                >
                  {selectedStep.status}
                </Badge>
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-muted-foreground">Step ID</Label>
                  <p className="font-mono text-sm">{selectedStep.id}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Type</Label>
                  <p>{selectedStep.type}</p>
                </div>
                {selectedStep.startTime && (
                  <div>
                    <Label className="text-muted-foreground">Start Time</Label>
                    <p>{new Date(selectedStep.startTime).toLocaleString()}</p>
                  </div>
                )}
                {selectedStep.endTime && (
                  <div>
                    <Label className="text-muted-foreground">End Time</Label>
                    <p>{new Date(selectedStep.endTime).toLocaleString()}</p>
                  </div>
                )}
                {selectedStep.duration !== undefined && (
                  <div>
                    <Label className="text-muted-foreground">Duration</Label>
                    <p>{selectedStep.duration}ms</p>
                  </div>
                )}
                {selectedStep.retryCount > 0 && (
                  <div>
                    <Label className="text-muted-foreground">Retry Count</Label>
                    <p>{selectedStep.retryCount}</p>
                  </div>
                )}
              </div>

              {selectedStep.error && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Error</AlertTitle>
                  <AlertDescription>{selectedStep.error}</AlertDescription>
                </Alert>
              )}

              {selectedStep.metrics && (
                <div>
                  <h4 className="font-medium mb-2">Performance Metrics</h4>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div className="space-y-1">
                      <p className="text-muted-foreground">Execution Time</p>
                      <p className="font-medium">{selectedStep.metrics.executionTime}ms</p>
                    </div>
                    <div className="space-y-1">
                      <p className="text-muted-foreground">Queue Time</p>
                      <p className="font-medium">{selectedStep.metrics.queueTime}ms</p>
                    </div>
                    <div className="space-y-1">
                      <p className="text-muted-foreground">Input Size</p>
                      <p className="font-medium">{(selectedStep.metrics.inputSize / 1024).toFixed(2)} KB</p>
                    </div>
                    <div className="space-y-1">
                      <p className="text-muted-foreground">Output Size</p>
                      <p className="font-medium">{(selectedStep.metrics.outputSize / 1024).toFixed(2)} KB</p>
                    </div>
                    <div className="space-y-1">
                      <p className="text-muted-foreground">Memory Peak</p>
                      <p className="font-medium">{(selectedStep.metrics.memoryPeak / 1024 / 1024).toFixed(2)} MB</p>
                    </div>
                    <div className="space-y-1">
                      <p className="text-muted-foreground">Retries</p>
                      <p className="font-medium">{selectedStep.metrics.retries}</p>
                    </div>
                  </div>
                </div>
              )}

              {selectedStep.input && (
                <div>
                  <h4 className="font-medium mb-2">Input Data</h4>
                  <ScrollArea className="h-[200px] w-full border rounded p-2">
                    <pre className="text-xs">{JSON.stringify(selectedStep.input, null, 2)}</pre>
                  </ScrollArea>
                </div>
              )}

              {selectedStep.output && (
                <div>
                  <h4 className="font-medium mb-2">Output Data</h4>
                  <ScrollArea className="h-[200px] w-full border rounded p-2">
                    <pre className="text-xs">{JSON.stringify(selectedStep.output, null, 2)}</pre>
                  </ScrollArea>
                </div>
              )}

              <div className="flex justify-end">
                <Button onClick={() => setSelectedStep(null)}>Close</Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}