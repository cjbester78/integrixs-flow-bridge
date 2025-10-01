import React, { useState, useCallback } from 'react';
import ReactFlow, { 
  Node, 
  Edge, 
  addEdge,
  Connection,
  useNodesState,
  useEdgesState,
  Handle,
  Position,
  NodeProps,
  ReactFlowProvider
} from 'reactflow';
import 'reactflow/dist/style.css';
import { 
  Zap, 
  Radio,
  MessageSquare,
  Database,
  Bell,
  Webhook,
  AlertTriangle,
  CheckCircle,
  Plus,
  Settings,
  Save,
  Eye,
  Filter
} from 'lucide-react';

interface EventDrivenProcessBuilderProps {
  flowId: string;
  onEventConfigured?: (eventConfig: EventConfiguration) => void;
}

interface EventConfiguration {
  id: string;
  name: string;
  type: EventType;
  source: EventSource;
  filters?: EventFilter[];
  transformation?: EventTransformation;
  targets: EventTarget[];
  errorHandling?: ErrorHandling;
  enabled: boolean;
}

type EventType = 'message' | 'signal' | 'webhook' | 'database' | 'file' | 'timer' | 'error';

interface EventSource {
  type: string;
  config: Record<string, any>;
}

interface EventFilter {
  field: string;
  operator: 'equals' | 'contains' | 'startsWith' | 'endsWith' | 'regex' | 'exists';
  value: any;
}

interface EventTransformation {
  type: 'jmespath' | 'jsonata' | 'javascript';
  expression: string;
}

interface EventTarget {
  id: string;
  type: 'process' | 'task' | 'webhook' | 'queue' | 'topic';
  config: Record<string, any>;
}

interface ErrorHandling {
  strategy: 'retry' | 'dlq' | 'ignore' | 'compensate';
  maxRetries?: number;
  retryDelay?: number;
  dlqTarget?: string;
}

// Custom node components
const EventSourceNode: React.FC<NodeProps> = ({ data }) => {
  const getIcon = () => {
    switch (data.eventType) {
      case 'message': return <MessageSquare className="w-4 h-4" />;
      case 'signal': return <Radio className="w-4 h-4" />;
      case 'webhook': return <Webhook className="w-4 h-4" />;
      case 'database': return <Database className="w-4 h-4" />;
      case 'timer': return <Bell className="w-4 h-4" />;
      default: return <Zap className="w-4 h-4" />;
    }
  };

  return (
    <div className="px-4 py-3 bg-gradient-to-r from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-lg border-2 border-purple-300 dark:border-purple-700">
      <div className="flex items-center gap-2">
        {getIcon()}
        <div>
          <div className="font-medium text-sm">{data.label}</div>
          <div className="text-xs text-purple-600 dark:text-purple-400">{data.eventType}</div>
        </div>
      </div>
      <Handle type="source" position={Position.Right} className="!bg-purple-500" />
    </div>
  );
};

const EventProcessorNode: React.FC<NodeProps> = ({ data }) => {
  return (
    <div className="px-4 py-3 bg-gradient-to-r from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-lg border-2 border-blue-300 dark:border-blue-700">
      <Handle type="target" position={Position.Left} className="!bg-blue-500" />
      <div className="flex items-center gap-2">
        <Settings className="w-4 h-4" />
        <div>
          <div className="font-medium text-sm">{data.label}</div>
          <div className="text-xs text-blue-600 dark:text-blue-400">{data.processorType}</div>
        </div>
      </div>
      <Handle type="source" position={Position.Right} className="!bg-blue-500" />
    </div>
  );
};

const EventTargetNode: React.FC<NodeProps> = ({ data }) => {
  return (
    <div className="px-4 py-3 bg-gradient-to-r from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-lg border-2 border-green-300 dark:border-green-700">
      <Handle type="target" position={Position.Left} className="!bg-green-500" />
      <div className="flex items-center gap-2">
        <CheckCircle className="w-4 h-4" />
        <div>
          <div className="font-medium text-sm">{data.label}</div>
          <div className="text-xs text-green-600 dark:text-green-400">{data.targetType}</div>
        </div>
      </div>
    </div>
  );
};

const nodeTypes = {
  eventSource: EventSourceNode,
  eventProcessor: EventProcessorNode,
  eventTarget: EventTargetNode
};

const EventDrivenProcessBuilderContent: React.FC<EventDrivenProcessBuilderProps> = ({
  flowId,
  onEventConfigured
}) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [selectedEventType, setSelectedEventType] = useState<EventType>('message');
  const [showConfiguration, setShowConfiguration] = useState(false);
  const [eventConfigurations, setEventConfigurations] = useState<EventConfiguration[]>([]);
  
  // Form state
  const [eventName, setEventName] = useState('');
  const [eventSource, setEventSource] = useState<EventSource>({
    type: 'queue',
    config: { queueName: '' }
  });
  const [filters, setFilters] = useState<EventFilter[]>([]);
  const [transformation, setTransformation] = useState<EventTransformation | undefined>();
  const [errorStrategy, setErrorStrategy] = useState<ErrorHandling['strategy']>('retry');

  const onConnect = useCallback(
    (params: Edge | Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  const addEventSource = (type: EventType) => {
    const newNode: Node = {
      id: `source_${nodes.length}`,
      type: 'eventSource',
      position: { x: 100, y: 100 + nodes.length * 100 },
      data: {
        label: `${type.charAt(0).toUpperCase() + type.slice(1)} Event`,
        eventType: type
      }
    };
    setNodes([...nodes, newNode]);
  };

  const addEventProcessor = (type: string) => {
    const newNode: Node = {
      id: `processor_${nodes.length}`,
      type: 'eventProcessor',
      position: { x: 400, y: 100 + nodes.length * 100 },
      data: {
        label: type,
        processorType: type
      }
    };
    setNodes([...nodes, newNode]);
  };

  const addEventTarget = (type: string) => {
    const newNode: Node = {
      id: `target_${nodes.length}`,
      type: 'eventTarget',
      position: { x: 700, y: 100 + nodes.length * 100 },
      data: {
        label: `${type} Target`,
        targetType: type
      }
    };
    setNodes([...nodes, newNode]);
  };

  const addFilter = () => {
    setFilters([...filters, { field: '', operator: 'equals', value: '' }]);
  };

  const updateFilter = (index: number, filter: EventFilter) => {
    const newFilters = [...filters];
    newFilters[index] = filter;
    setFilters(newFilters);
  };

  const removeFilter = (index: number) => {
    setFilters(filters.filter((_, i) => i !== index));
  };

  const saveEventConfiguration = () => {
    const config: EventConfiguration = {
      id: `event_${Date.now()}`,
      name: eventName,
      type: selectedEventType,
      source: eventSource,
      filters: filters.filter(f => f.field && f.value),
      transformation,
      targets: [{ 
        id: flowId,
        type: 'process',
        config: { processId: flowId }
      }],
      errorHandling: {
        strategy: errorStrategy,
        maxRetries: errorStrategy === 'retry' ? 3 : undefined,
        retryDelay: errorStrategy === 'retry' ? 60000 : undefined
      },
      enabled: true
    };

    setEventConfigurations([...eventConfigurations, config]);
    
    if (onEventConfigured) {
      onEventConfigured(config);
    }

    // Reset form
    setEventName('');
    setFilters([]);
    setTransformation(undefined);
    setShowConfiguration(false);
  };

  const eventTypes = [
    { type: 'message', icon: MessageSquare, label: 'Message Event', color: 'purple' },
    { type: 'signal', icon: Radio, label: 'Signal Event', color: 'blue' },
    { type: 'webhook', icon: Webhook, label: 'Webhook Event', color: 'green' },
    { type: 'database', icon: Database, label: 'Database Event', color: 'orange' },
    { type: 'timer', icon: Bell, label: 'Timer Event', color: 'yellow' },
    { type: 'error', icon: AlertTriangle, label: 'Error Event', color: 'red' }
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <div className="flex items-center gap-3 mb-4">
          <Zap className="w-6 h-6 text-purple-500" />
          <h3 className="text-lg font-semibold">Event-Driven Process Builder</h3>
        </div>
        <p className="text-gray-600 dark:text-gray-400">
          Design event-driven processes that respond to messages, signals, webhooks, and other triggers.
        </p>
      </div>

      {/* Event Type Selection */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h4 className="font-medium mb-4">Choose Event Type</h4>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
          {eventTypes.map(({ type, icon: Icon, label, color }) => (
            <button
              key={type}
              onClick={() => {
                setSelectedEventType(type as EventType);
                addEventSource(type as EventType);
              }}
              className={`
                flex items-center gap-3 p-4 border-2 rounded-lg transition-all
                hover:border-${color}-500 hover:bg-${color}-50 dark:hover:bg-${color}-900/20
                ${selectedEventType === type ? `border-${color}-500 bg-${color}-50 dark:bg-${color}-900/20` : 'border-gray-200 dark:border-gray-700'}
              `}
            >
              <div className={`p-2 bg-${color}-100 dark:bg-${color}-900/30 rounded`}>
                <Icon className="w-5 h-5" />
              </div>
              <span className="font-medium">{label}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Visual Flow Designer */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <h4 className="font-medium">Event Flow Design</h4>
          <div className="flex gap-2">
            <button
              onClick={() => addEventProcessor('Filter')}
              className="px-3 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <Filter className="w-4 h-4 inline mr-1" />
              Add Filter
            </button>
            <button
              onClick={() => addEventProcessor('Transform')}
              className="px-3 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <Settings className="w-4 h-4 inline mr-1" />
              Add Transform
            </button>
            <button
              onClick={() => addEventTarget('Process')}
              className="px-3 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <CheckCircle className="w-4 h-4 inline mr-1" />
              Add Target
            </button>
          </div>
        </div>
        
        <div className="h-[400px] border border-gray-200 dark:border-gray-700 rounded-lg">
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            nodeTypes={nodeTypes}
            fitView
          >
          </ReactFlow>
        </div>
      </div>

      {/* Event Configuration */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <h4 className="font-medium">Event Configuration</h4>
          <button
            onClick={() => setShowConfiguration(!showConfiguration)}
            className="text-blue-600 hover:text-blue-700 text-sm"
          >
            {showConfiguration ? 'Hide' : 'Show'} Configuration
          </button>
        </div>

        {showConfiguration && (
          <div className="space-y-4">
            {/* Basic Info */}
            <div>
              <label className="block text-sm font-medium mb-2">Event Name</label>
              <input
                type="text"
                value={eventName}
                onChange={(e) => setEventName(e.target.value)}
                placeholder="e.g., Order Received Event"
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
              />
            </div>

            {/* Event Source Configuration */}
            <div>
              <h5 className="font-medium mb-2">Event Source</h5>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm mb-1">Source Type</label>
                  <select
                    value={eventSource.type}
                    onChange={(e) => setEventSource({ ...eventSource, type: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                  >
                    <option value="queue">Message Queue</option>
                    <option value="topic">Topic/Channel</option>
                    <option value="webhook">Webhook Endpoint</option>
                    <option value="database">Database Table</option>
                    <option value="file">File System</option>
                  </select>
                </div>
                
                {eventSource.type === 'queue' && (
                  <div>
                    <label className="block text-sm mb-1">Queue Name</label>
                    <input
                      type="text"
                      value={eventSource.config.queueName || ''}
                      onChange={(e) => setEventSource({
                        ...eventSource,
                        config: { ...eventSource.config, queueName: e.target.value }
                      })}
                      placeholder="orders.received"
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                    />
                  </div>
                )}
              </div>
            </div>

            {/* Event Filters */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <h5 className="font-medium">Event Filters</h5>
                <button
                  onClick={addFilter}
                  className="text-sm text-blue-600 hover:text-blue-700"
                >
                  <Plus className="w-4 h-4 inline" /> Add Filter
                </button>
              </div>
              
              <div className="space-y-2">
                {filters.map((filter, index) => (
                  <div key={index} className="flex gap-2">
                    <input
                      type="text"
                      value={filter.field}
                      onChange={(e) => updateFilter(index, { ...filter, field: e.target.value })}
                      placeholder="Field path"
                      className="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                    />
                    <select
                      value={filter.operator}
                      onChange={(e) => updateFilter(index, { ...filter, operator: e.target.value as any })}
                      className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                    >
                      <option value="equals">Equals</option>
                      <option value="contains">Contains</option>
                      <option value="startsWith">Starts With</option>
                      <option value="endsWith">Ends With</option>
                      <option value="regex">Regex</option>
                      <option value="exists">Exists</option>
                    </select>
                    <input
                      type="text"
                      value={filter.value}
                      onChange={(e) => updateFilter(index, { ...filter, value: e.target.value })}
                      placeholder="Value"
                      className="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                    />
                    <button
                      onClick={() => removeFilter(index)}
                      className="p-2 text-red-500 hover:bg-red-50 rounded"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                ))}
              </div>
            </div>

            {/* Transformation */}
            <div>
              <h5 className="font-medium mb-2">Data Transformation</h5>
              <div className="space-y-2">
                <select
                  value={transformation?.type || ''}
                  onChange={(e) => setTransformation(
                    e.target.value ? { type: e.target.value as any, expression: '' } : undefined
                  )}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                >
                  <option value="">No transformation</option>
                  <option value="jmespath">JMESPath</option>
                  <option value="jsonata">JSONata</option>
                  <option value="javascript">JavaScript</option>
                </select>
                
                {transformation && (
                  <textarea
                    value={transformation.expression}
                    onChange={(e) => setTransformation({ ...transformation, expression: e.target.value })}
                    placeholder={
                      transformation.type === 'jmespath' ? 'data.items[?status==`active`]' :
                      transformation.type === 'jsonata' ? '$.items[status="active"]' :
                      'return { ...event, timestamp: new Date() }'
                    }
                    rows={3}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg font-mono text-sm"
                  />
                )}
              </div>
            </div>

            {/* Error Handling */}
            <div>
              <h5 className="font-medium mb-2">Error Handling</h5>
              <select
                value={errorStrategy}
                onChange={(e) => setErrorStrategy(e.target.value as any)}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
              >
                <option value="retry">Retry with backoff</option>
                <option value="dlq">Send to Dead Letter Queue</option>
                <option value="ignore">Ignore and continue</option>
                <option value="compensate">Trigger compensation</option>
              </select>
            </div>

            {/* Save Button */}
            <div className="flex gap-2 pt-4">
              <button
                onClick={saveEventConfiguration}
                disabled={!eventName}
                className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                <Save className="w-4 h-4" />
                Save Event Configuration
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Configured Events */}
      {eventConfigurations.length > 0 && (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <h4 className="font-medium mb-4">Configured Events</h4>
          <div className="space-y-3">
            {eventConfigurations.map(config => (
              <div
                key={config.id}
                className="border border-gray-200 dark:border-gray-700 rounded-lg p-4"
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h5 className="font-medium">{config.name}</h5>
                    <div className="flex items-center gap-4 mt-2 text-sm text-gray-600 dark:text-gray-400">
                      <span>Type: {config.type}</span>
                      <span>Source: {config.source.type}</span>
                      {config.filters && config.filters.length > 0 && (
                        <span>Filters: {config.filters.length}</span>
                      )}
                      {config.transformation && (
                        <span>Transform: {config.transformation.type}</span>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    {config.enabled ? (
                      <span className="text-xs text-green-600 bg-green-50 px-2 py-1 rounded-full">
                        Active
                      </span>
                    ) : (
                      <span className="text-xs text-gray-600 bg-gray-50 px-2 py-1 rounded-full">
                        Inactive
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Examples */}
      <div className="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
        <h4 className="font-medium mb-3 flex items-center gap-2">
          <Eye className="w-5 h-5 text-blue-600" />
          Event Pattern Examples
        </h4>
        <div className="space-y-3 text-sm">
          <div>
            <p className="font-medium text-blue-700 dark:text-blue-300">Order Processing:</p>
            <p className="text-blue-600 dark:text-blue-400">
              Message Event → Filter (status=new) → Transform (extract data) → Start Order Process
            </p>
          </div>
          <div>
            <p className="font-medium text-blue-700 dark:text-blue-300">Webhook Integration:</p>
            <p className="text-blue-600 dark:text-blue-400">
              Webhook Event → Validate Signature → Transform Payload → Trigger Process
            </p>
          </div>
          <div>
            <p className="font-medium text-blue-700 dark:text-blue-300">Database Change Detection:</p>
            <p className="text-blue-600 dark:text-blue-400">
              Database Event → Filter (table=customers) → Extract Changes → Update CRM Process
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export const EventDrivenProcessBuilder: React.FC<EventDrivenProcessBuilderProps> = (props) => {
  return (
    <ReactFlowProvider>
      <EventDrivenProcessBuilderContent {...props} />
    </ReactFlowProvider>
  );
};

// Add missing import
import { X } from 'lucide-react';