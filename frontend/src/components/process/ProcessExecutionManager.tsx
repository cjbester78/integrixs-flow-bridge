import React, { useState, useEffect, useCallback } from 'react';
import { 
  Play, 
  Pause, 
  Square, 
  RefreshCw,
  AlertCircle,
  CheckCircle,
  Clock,
  Loader2,
  List,
  BarChart3,
  Activity,
  User
} from 'lucide-react';
import { apiClient } from '@/lib/api-client';
import { logger } from '@/lib/logger';

interface ProcessExecutionManagerProps {
  processDefinitionId: string;
  flowName: string;
  onInstanceCreated?: (instanceId: string) => void;
}

interface ProcessInstance {
  id: string;
  processDefinitionId: string;
  status: 'RUNNING' | 'SUSPENDED' | 'COMPLETED' | 'FAILED' | 'TERMINATED';
  startTime: string;
  endTime?: string;
  currentStep?: string;
  error?: string;
  variables: Record<string, any>;
  executionLog: string[];
  userTaskIds: string[];
}

interface ProcessStats {
  processDefinitionId: string;
  totalInstances: number;
  runningInstances: number;
  completedInstances: number;
  failedInstances: number;
  successRate: number;
}

interface StartVariables {
  [key: string]: any;
}

export const ProcessExecutionManager: React.FC<ProcessExecutionManagerProps> = ({
  processDefinitionId,
  flowName,
  onInstanceCreated
}) => {
  const [instances, setInstances] = useState<ProcessInstance[]>([]);
  const [selectedInstance, setSelectedInstance] = useState<ProcessInstance | null>(null);
  const [stats, setStats] = useState<ProcessStats | null>(null);
  const [startVariables, setStartVariables] = useState<StartVariables>({});
  const [showVariablesInput, setShowVariablesInput] = useState(false);
  const [isStarting, setIsStarting] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(true);

  const fetchInstanceDetails = useCallback(async (instanceId: string) => {
    try {
      const response = await apiClient.get(`/api/process-engine/instance/${instanceId}`);
      setSelectedInstance(response.data);
    } catch (error) {
      logger.error('Failed to fetch instance details:', error);
    }
  }, []);

  const fetchInstances = useCallback(async () => {
    try {
      const response = await apiClient.get('/api/process-engine/instances');
      const processInstances = response.data.filter(
        (instance: any) => instance.processDefinitionId === processDefinitionId
      );
      setInstances(processInstances);

      // Update selected instance if it exists
      if (selectedInstance) {
        const updated = processInstances.find((i: any) => i.id === selectedInstance.id);
        if (updated) {
          fetchInstanceDetails(updated.id);
        }
      }
    } catch (error) {
      logger.error('Failed to fetch process instances:', error);
    }
  }, [processDefinitionId, selectedInstance, fetchInstanceDetails]);

  const fetchStats = useCallback(async () => {
    try {
      const response = await apiClient.get(`/api/process-engine/stats/${processDefinitionId}`);
      setStats(response.data);
    } catch (error) {
      logger.error('Failed to fetch process stats:', error);
    }
  }, [processDefinitionId]);

  // Fetch instances and stats
  useEffect(() => {
    fetchInstances();
    fetchStats();

    if (autoRefresh) {
      const interval = setInterval(() => {
        fetchInstances();
        fetchStats();
      }, 2000);

      return () => clearInterval(interval);
    }
  }, [processDefinitionId, autoRefresh, fetchInstances, fetchStats]);

  const startProcessInstance = async () => {
    setIsStarting(true);
    try {
      const response = await apiClient.post(
        `/api/process-engine/start/${processDefinitionId}`,
        startVariables
      );

      if (response.data.success) {
        const { instance } = response.data;
        logger.info('Process instance started:', instance.id);
        
        if (onInstanceCreated) {
          onInstanceCreated(instance.id);
        }

        // Refresh instances
        await fetchInstances();
        await fetchStats();

        // Select the new instance
        await fetchInstanceDetails(instance.id);

        // Reset variables
        setStartVariables({});
        setShowVariablesInput(false);
      } else {
        throw new Error(response.data.error);
      }
    } catch (error) {
      logger.error('Failed to start process instance:', error);
    } finally {
      setIsStarting(false);
    }
  };

  const suspendInstance = async (instanceId: string) => {
    try {
      const response = await apiClient.post(
        `/api/process-engine/instance/${instanceId}/suspend`
      );

      if (response.data.success) {
        logger.info('Process instance suspended:', instanceId);
        await fetchInstances();
        await fetchInstanceDetails(instanceId);
      }
    } catch (error) {
      logger.error('Failed to suspend instance:', error);
    }
  };

  const resumeInstance = async (instanceId: string) => {
    try {
      const response = await apiClient.post(
        `/api/process-engine/instance/${instanceId}/resume`
      );

      if (response.data.success) {
        logger.info('Process instance resumed:', instanceId);
        await fetchInstances();
        await fetchInstanceDetails(instanceId);
      }
    } catch (error) {
      logger.error('Failed to resume instance:', error);
    }
  };

  const terminateInstance = async (instanceId: string) => {
    try {
      const response = await apiClient.post(
        `/api/process-engine/instance/${instanceId}/terminate`
      );

      if (response.data.success) {
        logger.info('Process instance terminated:', instanceId);
        await fetchInstances();
        await fetchInstanceDetails(instanceId);
      }
    } catch (error) {
      logger.error('Failed to terminate instance:', error);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'RUNNING':
        return <Activity className="w-4 h-4 text-blue-500 animate-pulse" />;
      case 'COMPLETED':
        return <CheckCircle className="w-4 h-4 text-green-500" />;
      case 'FAILED':
        return <AlertCircle className="w-4 h-4 text-red-500" />;
      case 'SUSPENDED':
        return <Pause className="w-4 h-4 text-yellow-500" />;
      case 'TERMINATED':
        return <Square className="w-4 h-4 text-gray-500" />;
      default:
        return <Clock className="w-4 h-4 text-gray-400" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'RUNNING': return 'text-blue-600 bg-blue-50';
      case 'COMPLETED': return 'text-green-600 bg-green-50';
      case 'FAILED': return 'text-red-600 bg-red-50';
      case 'SUSPENDED': return 'text-yellow-600 bg-yellow-50';
      case 'TERMINATED': return 'text-gray-600 bg-gray-50';
      default: return 'text-gray-600 bg-gray-50';
    }
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm">
      <div className="p-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <Activity className="w-6 h-6 text-blue-500" />
            <h3 className="text-lg font-semibold">Process Execution Manager</h3>
          </div>
          <div className="flex items-center gap-3">
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                checked={autoRefresh}
                onChange={(e) => setAutoRefresh(e.target.checked)}
                className="rounded"
              />
              Auto-refresh
            </label>
            <button
              onClick={() => {
                setIsRefreshing(true);
                Promise.all([fetchInstances(), fetchStats()]).finally(() => {
                  setIsRefreshing(false);
                });
              }}
              className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
            >
              <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin' : ''}`} />
            </button>
          </div>
        </div>

        {/* Statistics */}
        {stats && (
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
            <div className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
              <p className="text-sm text-gray-500 dark:text-gray-400">Total</p>
              <p className="text-2xl font-semibold">{stats.totalInstances}</p>
            </div>
            <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-lg">
              <p className="text-sm text-blue-600 dark:text-blue-400">Running</p>
              <p className="text-2xl font-semibold text-blue-700 dark:text-blue-300">
                {stats.runningInstances}
              </p>
            </div>
            <div className="bg-green-50 dark:bg-green-900/20 p-4 rounded-lg">
              <p className="text-sm text-green-600 dark:text-green-400">Completed</p>
              <p className="text-2xl font-semibold text-green-700 dark:text-green-300">
                {stats.completedInstances}
              </p>
            </div>
            <div className="bg-red-50 dark:bg-red-900/20 p-4 rounded-lg">
              <p className="text-sm text-red-600 dark:text-red-400">Failed</p>
              <p className="text-2xl font-semibold text-red-700 dark:text-red-300">
                {stats.failedInstances}
              </p>
            </div>
            <div className="bg-purple-50 dark:bg-purple-900/20 p-4 rounded-lg">
              <p className="text-sm text-purple-600 dark:text-purple-400">Success Rate</p>
              <p className="text-2xl font-semibold text-purple-700 dark:text-purple-300">
                {stats.successRate.toFixed(1)}%
              </p>
            </div>
          </div>
        )}

        {/* Start Process */}
        <div className="mb-6">
          <button
            onClick={() => setShowVariablesInput(!showVariablesInput)}
            className="w-full flex items-center justify-center gap-2 px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Play className="w-5 h-5" />
            Start New Process Instance
          </button>

          {showVariablesInput && (
            <div className="mt-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <h4 className="font-medium mb-3">Process Variables (JSON)</h4>
              <textarea
                value={JSON.stringify(startVariables, null, 2)}
                onChange={(e) => {
                  try {
                    setStartVariables(JSON.parse(e.target.value));
                  } catch (err) {
                    // Invalid JSON, ignore
                  }
                }}
                className="w-full h-32 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 font-mono text-sm"
                placeholder='{"userId": "123", "orderAmount": 1000}'
              />
              <div className="mt-3 flex gap-2">
                <button
                  onClick={startProcessInstance}
                  disabled={isStarting}
                  className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50"
                >
                  {isStarting ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                  ) : (
                    <Play className="w-4 h-4" />
                  )}
                  {isStarting ? 'Starting...' : 'Start Process'}
                </button>
                <button
                  onClick={() => {
                    setShowVariablesInput(false);
                    setStartVariables({});
                  }}
                  className="px-4 py-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-600 rounded-lg transition-colors"
                >
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Instances Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Instance List */}
          <div>
            <h4 className="font-medium mb-3 flex items-center gap-2">
              <List className="w-4 h-4" />
              Process Instances
            </h4>
            <div className="space-y-2 max-h-96 overflow-y-auto">
              {instances.length === 0 ? (
                <p className="text-gray-500 dark:text-gray-400 text-center py-8">
                  No process instances yet
                </p>
              ) : (
                instances.map((instance) => (
                  <div
                    key={instance.id}
                    onClick={() => fetchInstanceDetails(instance.id)}
                    className={`p-4 rounded-lg border cursor-pointer transition-all ${
                      selectedInstance?.id === instance.id
                        ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                        : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        {getStatusIcon(instance.status)}
                        <div>
                          <p className="font-medium text-sm">
                            {instance.id.substring(0, 8)}...
                          </p>
                          <p className="text-xs text-gray-500 dark:text-gray-400">
                            {new Date(instance.startTime).toLocaleString()}
                          </p>
                        </div>
                      </div>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(instance.status)}`}>
                        {instance.status}
                      </span>
                    </div>
                    {instance.currentStep && (
                      <p className="mt-2 text-xs text-gray-600 dark:text-gray-400">
                        Current: {instance.currentStep}
                      </p>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Instance Details */}
          {selectedInstance && (
            <div className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
              <div className="flex items-center justify-between mb-4">
                <h4 className="font-medium">Instance Details</h4>
                <div className="flex gap-2">
                  {selectedInstance.status === 'RUNNING' && (
                    <button
                      onClick={() => suspendInstance(selectedInstance.id)}
                      className="p-2 text-yellow-600 hover:bg-yellow-50 dark:hover:bg-yellow-900/20 rounded transition-colors"
                      title="Suspend"
                    >
                      <Pause className="w-4 h-4" />
                    </button>
                  )}
                  {selectedInstance.status === 'SUSPENDED' && (
                    <button
                      onClick={() => resumeInstance(selectedInstance.id)}
                      className="p-2 text-green-600 hover:bg-green-50 dark:hover:bg-green-900/20 rounded transition-colors"
                      title="Resume"
                    >
                      <Play className="w-4 h-4" />
                    </button>
                  )}
                  {['RUNNING', 'SUSPENDED'].includes(selectedInstance.status) && (
                    <button
                      onClick={() => terminateInstance(selectedInstance.id)}
                      className="p-2 text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded transition-colors"
                      title="Terminate"
                    >
                      <Square className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>

              <div className="space-y-3">
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Instance ID</p>
                  <p className="font-mono text-sm">{selectedInstance.id}</p>
                </div>

                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Status</p>
                  <div className="flex items-center gap-2">
                    {getStatusIcon(selectedInstance.status)}
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(selectedInstance.status)}`}>
                      {selectedInstance.status}
                    </span>
                  </div>
                </div>

                {selectedInstance.currentStep && (
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400">Current Step</p>
                    <p className="text-sm">{selectedInstance.currentStep}</p>
                  </div>
                )}

                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Duration</p>
                  <p className="text-sm">
                    {selectedInstance.endTime
                      ? `${Math.round(
                          (new Date(selectedInstance.endTime).getTime() -
                            new Date(selectedInstance.startTime).getTime()) /
                            1000
                        )}s`
                      : 'Running...'}
                  </p>
                </div>

                {selectedInstance.error && (
                  <div>
                    <p className="text-xs text-red-600 dark:text-red-400">Error</p>
                    <p className="text-sm text-red-700 dark:text-red-300">
                      {selectedInstance.error}
                    </p>
                  </div>
                )}

                {/* User Tasks */}
                {selectedInstance.userTaskIds.length > 0 && (
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-2">
                      User Tasks ({selectedInstance.userTaskIds.length})
                    </p>
                    <div className="space-y-1">
                      {selectedInstance.userTaskIds.map((taskId) => (
                        <div
                          key={taskId}
                          className="flex items-center gap-2 p-2 bg-yellow-50 dark:bg-yellow-900/20 rounded text-sm"
                        >
                          <User className="w-3 h-3 text-yellow-600" />
                          <span className="font-mono text-xs">{taskId}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Execution Log */}
                <div>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mb-2">
                    Execution Log
                  </p>
                  <div className="bg-white dark:bg-gray-800 rounded p-3 max-h-48 overflow-y-auto">
                    {selectedInstance.executionLog.map((log, index) => (
                      <p key={index} className="text-xs font-mono text-gray-600 dark:text-gray-400">
                        {log}
                      </p>
                    ))}
                  </div>
                </div>

                {/* Variables */}
                {Object.keys(selectedInstance.variables).length > 0 && (
                  <div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-2">
                      Process Variables
                    </p>
                    <pre className="bg-white dark:bg-gray-800 rounded p-3 text-xs overflow-x-auto">
                      <code>{JSON.stringify(selectedInstance.variables, null, 2)}</code>
                    </pre>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};