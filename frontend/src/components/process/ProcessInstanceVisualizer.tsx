import React, { useState, useEffect, useCallback } from 'react';
import ReactFlow, { 
  Node, 
  Edge, 
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  Handle,
  Position,
  NodeProps,
  ReactFlowProvider
} from 'reactflow';
import 'reactflow/dist/style.css';
import { 
  Play, 
  CheckCircle, 
  AlertCircle, 
  Clock,
  Loader2,
  Activity,
  Maximize2,
  RotateCcw
} from 'lucide-react';
import { apiClient } from '@/lib/api-client';
import { logger } from '@/lib/logger';

interface ProcessInstanceVisualizerProps {
  processInstanceId: string;
  processDefinitionId: string;
  autoRefresh?: boolean;
  refreshInterval?: number;
}

interface ProcessStep {
  id: string;
  name: string;
  type: string;
  status: 'pending' | 'running' | 'completed' | 'failed' | 'skipped';
  startTime?: string;
  endTime?: string;
  duration?: number;
  error?: string;
}

// Custom node component
const ProcessNode: React.FC<NodeProps> = ({ data }) => {
  const getNodeStyle = () => {
    switch (data.status) {
      case 'completed':
        return 'border-green-500 bg-green-50 dark:bg-green-900/20';
      case 'running':
        return 'border-blue-500 bg-blue-50 dark:bg-blue-900/20 animate-pulse';
      case 'failed':
        return 'border-red-500 bg-red-50 dark:bg-red-900/20';
      case 'skipped':
        return 'border-gray-400 bg-gray-100 dark:bg-gray-700 opacity-50';
      default:
        return 'border-gray-300 bg-white dark:bg-gray-800';
    }
  };

  const getIcon = () => {
    switch (data.status) {
      case 'completed':
        return <CheckCircle className="w-4 h-4 text-green-500" />;
      case 'running':
        return <Loader2 className="w-4 h-4 text-blue-500 animate-spin" />;
      case 'failed':
        return <AlertCircle className="w-4 h-4 text-red-500" />;
      case 'skipped':
        return <Clock className="w-4 h-4 text-gray-400" />;
      default:
        return <Clock className="w-4 h-4 text-gray-400" />;
    }
  };

  return (
    <div className={`px-4 py-3 rounded-lg border-2 min-w-[200px] transition-all ${getNodeStyle()}`}>
      <Handle type="target" position={Position.Left} className="!bg-gray-400" />
      
      <div className="flex items-start gap-3">
        <div className="mt-0.5">{getIcon()}</div>
        <div className="flex-1">
          <div className="font-medium text-sm">{data.label}</div>
          <div className="text-xs text-gray-600 dark:text-gray-400 mt-1">
            {data.type}
          </div>
          {data.duration && (
            <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              {data.duration}ms
            </div>
          )}
          {data.error && (
            <div className="text-xs text-red-600 dark:text-red-400 mt-2 p-2 bg-red-100 dark:bg-red-900/30 rounded">
              {data.error}
            </div>
          )}
        </div>
      </div>
      
      <Handle type="source" position={Position.Right} className="!bg-gray-400" />
    </div>
  );
};

const nodeTypes = {
  process: ProcessNode
};

const ProcessInstanceVisualizerContent: React.FC<ProcessInstanceVisualizerProps> = ({
  processInstanceId,
  processDefinitionId,
  autoRefresh = true,
  refreshInterval = 2000
}) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [lastUpdate, setLastUpdate] = useState<Date>(new Date());
  const [processInstance, setProcessInstance] = useState<any>(null);

  // Fetch process instance and build flow
  const fetchProcessInstance = useCallback(async () => {
    try {
      const response = await apiClient.get(`/api/process-engine/instance/${processInstanceId}`);
      const instance = response.data;
      setProcessInstance(instance);

      // Build nodes from instance
      const buildNodesFromInstance = (instance: any): Node[] => {
        const nodes: Node[] = [];
        const executionLog = instance.executionLog || [];
        const currentStep = instance.currentStep;

        // Parse execution log to build step status
        const stepStatuses = new Map<string, ProcessStep>();
        
        // Start node
        nodes.push({
          id: 'start',
          type: 'process',
          position: { x: 100, y: 200 },
          data: {
            label: 'Start',
            type: 'startEvent',
            status: 'completed'
          }
        });

        // Extract steps from log
        let x = 300;
        const y = 200;
        const xGap = 250;

        executionLog.forEach((log: string) => {
          const stepMatch = log.match(/Executing step: (.+)/);
          const completedMatch = log.match(/Step completed: (.+)/);
          const failedMatch = log.match(/Step failed: (.+) - (.+)/);

          if (stepMatch) {
            const stepName = stepMatch[1];
            const stepId = `step_${nodes.length}`;
            
            nodes.push({
              id: stepId,
              type: 'process',
              position: { x, y },
              data: {
                label: stepName,
                type: 'task',
                status: currentStep === stepName ? 'running' : 'pending'
              }
            });
            
            stepStatuses.set(stepName, {
              id: stepId,
              name: stepName,
              type: 'task',
              status: 'running'
            });
            
            x += xGap;
          } else if (completedMatch) {
            const stepName = completedMatch[1];
            const step = stepStatuses.get(stepName);
            if (step) {
              step.status = 'completed';
              const node = nodes.find(n => n.data.label === stepName);
              if (node) {
                node.data.status = 'completed';
              }
            }
          } else if (failedMatch) {
            const stepName = failedMatch[1];
            const error = failedMatch[2];
            const step = stepStatuses.get(stepName);
            if (step) {
              step.status = 'failed';
              step.error = error;
              const node = nodes.find(n => n.data.label === stepName);
              if (node) {
                node.data.status = 'failed';
                node.data.error = error;
              }
            }
          }
        });

        // End node
        nodes.push({
          id: 'end',
          type: 'process',
          position: { x, y },
          data: {
            label: 'End',
            type: 'endEvent',
            status: instance.status === 'COMPLETED' ? 'completed' : 
                    instance.status === 'FAILED' ? 'failed' : 'pending'
          }
        });

        return nodes;
      };

      // Build edges from instance
      const buildEdgesFromInstance = (instance: any): Edge[] => {
        const edges: Edge[] = [];
        const nodes = buildNodesFromInstance(instance);

        // Connect nodes sequentially
        for (let i = 0; i < nodes.length - 1; i++) {
          const animated = nodes[i].data.status === 'completed' && 
                          nodes[i + 1].data.status === 'running';

          edges.push({
            id: `e${i}`,
            source: nodes[i].id,
            target: nodes[i + 1].id,
            type: 'smoothstep',
            animated,
            style: {
              stroke: nodes[i].data.status === 'completed' ? '#10b981' : '#e5e7eb',
              strokeWidth: 2
            }
          });
        }

        return edges;
      };

      // Build nodes and edges from execution data
      setNodes(buildNodesFromInstance(instance));
      setEdges(buildEdgesFromInstance(instance));
      setLastUpdate(new Date());
    } catch (error) {
      logger.error('Failed to fetch process instance:', error);
    }
  }, [processInstanceId, setNodes, setEdges]);

  // Auto-refresh
  useEffect(() => {
    fetchProcessInstance();

    if (autoRefresh && processInstance?.status === 'RUNNING') {
      const interval = setInterval(fetchProcessInstance, refreshInterval);
      return () => clearInterval(interval);
    }
  }, [fetchProcessInstance, autoRefresh, refreshInterval, processInstance?.status]);

  const toggleFullscreen = () => {
    setIsFullscreen(!isFullscreen);
  };

  return (
    <div className={`relative ${isFullscreen ? 'fixed inset-0 z-50 bg-white dark:bg-gray-900' : 'h-[600px]'}`}>
      {/* Header */}
      <div className="absolute top-0 left-0 right-0 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 p-4 z-10">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Activity className="w-5 h-5 text-blue-500" />
            <div>
              <h3 className="font-semibold">Process Instance Visualization</h3>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Instance: {processInstanceId.substring(0, 8)}...
              </p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-xs text-gray-500">
              Last update: {lastUpdate.toLocaleTimeString()}
            </span>
            <button
              onClick={fetchProcessInstance}
              className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
              title="Refresh"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
            <button
              onClick={toggleFullscreen}
              className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
              title={isFullscreen ? 'Exit fullscreen' : 'Fullscreen'}
            >
              <Maximize2 className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Status bar */}
        {processInstance && (
          <div className="mt-3 flex items-center gap-6 text-sm">
            <div className="flex items-center gap-2">
              <span className="text-gray-500">Status:</span>
              <span className={`font-medium ${
                processInstance.status === 'RUNNING' ? 'text-blue-600' :
                processInstance.status === 'COMPLETED' ? 'text-green-600' :
                processInstance.status === 'FAILED' ? 'text-red-600' :
                'text-gray-600'
              }`}>
                {processInstance.status}
              </span>
            </div>
            {processInstance.currentStep && (
              <div className="flex items-center gap-2">
                <span className="text-gray-500">Current:</span>
                <span className="font-medium">{processInstance.currentStep}</span>
              </div>
            )}
            <div className="flex items-center gap-2">
              <span className="text-gray-500">Duration:</span>
              <span className="font-medium">
                {processInstance.endTime
                  ? `${Math.round(
                      (new Date(processInstance.endTime).getTime() -
                        new Date(processInstance.startTime).getTime()) / 1000
                    )}s`
                  : 'Running...'}
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Flow diagram */}
      <div className="absolute inset-0 top-24">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          nodeTypes={nodeTypes}
          fitView
          attributionPosition="bottom-right"
        >
          <Background variant="dots" gap={16} size={1} />
          <Controls />
          <MiniMap 
            nodeStrokeWidth={3}
            pannable
            zoomable
            className="!bg-gray-50 dark:!bg-gray-800"
          />
        </ReactFlow>
      </div>

      {/* Legend */}
      <div className="absolute bottom-4 left-4 bg-white dark:bg-gray-800 rounded-lg shadow-lg p-4 z-10">
        <h4 className="font-medium text-sm mb-2">Legend</h4>
        <div className="space-y-2 text-xs">
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded border-2 border-gray-300 bg-white"></div>
            <span>Pending</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded border-2 border-blue-500 bg-blue-50 animate-pulse"></div>
            <span>Running</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded border-2 border-green-500 bg-green-50"></div>
            <span>Completed</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded border-2 border-red-500 bg-red-50"></div>
            <span>Failed</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export const ProcessInstanceVisualizer: React.FC<ProcessInstanceVisualizerProps> = (props) => {
  return (
    <ReactFlowProvider>
      <ProcessInstanceVisualizerContent {...props} />
    </ReactFlowProvider>
  );
};