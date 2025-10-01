import React, { useState, useEffect, useCallback } from 'react';
import { 
  BarChart3, 
  TrendingUp, 
  Clock,
  Activity,
  AlertCircle,
  CheckCircle,
  Zap,
  Target,
  Layers,
  Filter
} from 'lucide-react';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
  Treemap
} from 'recharts';
import { apiClient } from '@/lib/api-client';
import { logger } from '@/lib/logger';

interface ProcessPerformanceAnalyticsProps {
  processDefinitionId: string;
  timeRange?: '1h' | '24h' | '7d' | '30d';
}

interface PerformanceMetrics {
  avgExecutionTime: number;
  minExecutionTime: number;
  maxExecutionTime: number;
  throughput: number;
  successRate: number;
  errorRate: number;
  activeInstances: number;
  completedInstances: number;
  failedInstances: number;
}

interface StepMetrics {
  stepName: string;
  avgDuration: number;
  minDuration: number;
  maxDuration: number;
  executionCount: number;
  errorCount: number;
  successRate: number;
}

interface TimeSeriesData {
  timestamp: string;
  instanceCount: number;
  avgDuration: number;
  successCount: number;
  failureCount: number;
}

const COLORS = {
  success: '#10b981',
  error: '#ef4444',
  warning: '#f59e0b',
  info: '#3b82f6',
  primary: '#6366f1',
  secondary: '#8b5cf6'
};

export const ProcessPerformanceAnalytics: React.FC<ProcessPerformanceAnalyticsProps> = ({
  processDefinitionId,
  timeRange = '24h'
}) => {
  const [metrics, setMetrics] = useState<PerformanceMetrics | null>(null);
  const [stepMetrics, setStepMetrics] = useState<StepMetrics[]>([]);
  const [timeSeriesData, setTimeSeriesData] = useState<TimeSeriesData[]>([]);
  const [selectedTimeRange, setSelectedTimeRange] = useState(timeRange);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedMetric, setSelectedMetric] = useState<'duration' | 'throughput' | 'success'>('duration');

  const loadAnalytics = useCallback(async () => {
    setIsLoading(true);
    try {
      // In a real implementation, these would be separate API calls
      const [metricsResponse, instancesResponse] = await Promise.all([
        apiClient.get(`/api/process-engine/stats/${processDefinitionId}`),
        apiClient.get('/api/process-engine/instances')
      ]);

      // Calculate metrics
      const instances = instancesResponse.data.filter(
        (i: any) => i.processDefinitionId === processDefinitionId
      );

      const completedInstances = instances.filter((i: any) => i.status === 'COMPLETED');
      const failedInstances = instances.filter((i: any) => i.status === 'FAILED');
      const activeInstances = instances.filter((i: any) => i.status === 'RUNNING');

      // Calculate execution times
      const executionTimes = completedInstances
        .map((i: any) => {
          if (i.endTime) {
            return new Date(i.endTime).getTime() - new Date(i.startTime).getTime();
          }
          return null;
        })
        .filter(Boolean) as number[];

      const avgExecutionTime = executionTimes.length > 0
        ? executionTimes.reduce((a, b) => a + b, 0) / executionTimes.length
        : 0;

      const metrics: PerformanceMetrics = {
        avgExecutionTime,
        minExecutionTime: Math.min(...executionTimes) || 0,
        maxExecutionTime: Math.max(...executionTimes) || 0,
        throughput: calculateThroughput(instances, selectedTimeRange),
        successRate: instances.length > 0 
          ? (completedInstances.length / instances.length) * 100 
          : 0,
        errorRate: instances.length > 0 
          ? (failedInstances.length / instances.length) * 100 
          : 0,
        activeInstances: activeInstances.length,
        completedInstances: completedInstances.length,
        failedInstances: failedInstances.length
      };

      setMetrics(metrics);

      // Generate step metrics (mock data for demo)
      const mockStepMetrics: StepMetrics[] = [
        { stepName: 'Start', avgDuration: 50, minDuration: 20, maxDuration: 100, executionCount: 100, errorCount: 0, successRate: 100 },
        { stepName: 'Validate Input', avgDuration: 150, minDuration: 100, maxDuration: 300, executionCount: 100, errorCount: 5, successRate: 95 },
        { stepName: 'Process Data', avgDuration: 500, minDuration: 300, maxDuration: 1200, executionCount: 95, errorCount: 3, successRate: 97 },
        { stepName: 'Call External API', avgDuration: 800, minDuration: 400, maxDuration: 2000, executionCount: 92, errorCount: 8, successRate: 91 },
        { stepName: 'Transform Response', avgDuration: 200, minDuration: 150, maxDuration: 400, executionCount: 84, errorCount: 2, successRate: 98 },
        { stepName: 'Save Results', avgDuration: 300, minDuration: 200, maxDuration: 600, executionCount: 82, errorCount: 1, successRate: 99 },
        { stepName: 'End', avgDuration: 50, minDuration: 20, maxDuration: 100, executionCount: 81, errorCount: 0, successRate: 100 }
      ];
      setStepMetrics(mockStepMetrics);

      // Generate time series data
      const timeSeriesData = generateTimeSeriesData(instances, selectedTimeRange);
      setTimeSeriesData(timeSeriesData);

    } catch (error) {
      logger.error('Failed to load analytics:', error);
    } finally {
      setIsLoading(false);
    }
  }, [processDefinitionId, selectedTimeRange]);

  useEffect(() => {
    loadAnalytics();
    const interval = setInterval(loadAnalytics, 30000); // Refresh every 30s
    return () => clearInterval(interval);
  }, [loadAnalytics]);

  const calculateThroughput = (instances: any[], range: string): number => {
    const now = Date.now();
    const rangeMs = {
      '1h': 3600000,
      '24h': 86400000,
      '7d': 604800000,
      '30d': 2592000000
    }[range] || 86400000;

    const recentInstances = instances.filter(i => {
      const startTime = new Date(i.startTime).getTime();
      return now - startTime <= rangeMs;
    });

    return recentInstances.length / (rangeMs / 3600000); // per hour
  };

  const generateTimeSeriesData = (instances: any[], range: string): TimeSeriesData[] => {
    // Mock time series data
    const points = range === '1h' ? 12 : range === '24h' ? 24 : range === '7d' ? 7 : 30;
    const data: TimeSeriesData[] = [];

    for (let i = 0; i < points; i++) {
      const successCount = Math.floor(Math.random() * 20) + 10;
      const failureCount = Math.floor(Math.random() * 5);
      
      data.push({
        timestamp: range === '1h' ? `${i * 5}m` : 
                   range === '24h' ? `${i}h` : 
                   `Day ${i + 1}`,
        instanceCount: successCount + failureCount,
        avgDuration: Math.floor(Math.random() * 500) + 1000,
        successCount,
        failureCount
      });
    }

    return data;
  };

  const formatDuration = (ms: number): string => {
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
    return `${(ms / 60000).toFixed(1)}m`;
  };

  const statusDistribution = metrics ? [
    { name: 'Completed', value: metrics.completedInstances, color: COLORS.success },
    { name: 'Failed', value: metrics.failedInstances, color: COLORS.error },
    { name: 'Active', value: metrics.activeInstances, color: COLORS.info }
  ] : [];

  const stepPerformanceData = stepMetrics.map(step => ({
    name: step.stepName.length > 15 ? step.stepName.substring(0, 15) + '...' : step.stepName,
    avgDuration: step.avgDuration,
    errorRate: (step.errorCount / step.executionCount) * 100
  }));

  const radarData = stepMetrics.map(step => ({
    step: step.stepName,
    duration: (step.avgDuration / 1000) * 10, // Normalize
    reliability: step.successRate,
    throughput: step.executionCount / 10 // Normalize
  }));

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <BarChart3 className="w-6 h-6 text-purple-500" />
            <h3 className="text-lg font-semibold">Process Performance Analytics</h3>
          </div>
          
          {/* Time Range Selector */}
          <div className="flex items-center gap-2">
            <Filter className="w-4 h-4 text-gray-400" />
            <select
              value={selectedTimeRange}
              onChange={(e) => setSelectedTimeRange(e.target.value as any)}
              className="px-3 py-1 border border-gray-300 dark:border-gray-600 rounded-lg text-sm"
            >
              <option value="1h">Last Hour</option>
              <option value="24h">Last 24 Hours</option>
              <option value="7d">Last 7 Days</option>
              <option value="30d">Last 30 Days</option>
            </select>
          </div>
        </div>

        {/* Key Metrics */}
        {metrics && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-gradient-to-r from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 p-4 rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                <Clock className="w-4 h-4 text-blue-600" />
                <p className="text-sm text-blue-600 dark:text-blue-400">Avg Duration</p>
              </div>
              <p className="text-2xl font-bold text-blue-700 dark:text-blue-300">
                {formatDuration(metrics.avgExecutionTime)}
              </p>
              <p className="text-xs text-blue-600 dark:text-blue-400 mt-1">
                Min: {formatDuration(metrics.minExecutionTime)} | Max: {formatDuration(metrics.maxExecutionTime)}
              </p>
            </div>

            <div className="bg-gradient-to-r from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 p-4 rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                <CheckCircle className="w-4 h-4 text-green-600" />
                <p className="text-sm text-green-600 dark:text-green-400">Success Rate</p>
              </div>
              <p className="text-2xl font-bold text-green-700 dark:text-green-300">
                {metrics.successRate.toFixed(1)}%
              </p>
              <p className="text-xs text-green-600 dark:text-green-400 mt-1">
                {metrics.completedInstances} completed
              </p>
            </div>

            <div className="bg-gradient-to-r from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 p-4 rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                <Zap className="w-4 h-4 text-purple-600" />
                <p className="text-sm text-purple-600 dark:text-purple-400">Throughput</p>
              </div>
              <p className="text-2xl font-bold text-purple-700 dark:text-purple-300">
                {metrics.throughput.toFixed(1)}
              </p>
              <p className="text-xs text-purple-600 dark:text-purple-400 mt-1">
                instances/hour
              </p>
            </div>

            <div className="bg-gradient-to-r from-red-50 to-red-100 dark:from-red-900/20 dark:to-red-800/20 p-4 rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                <AlertCircle className="w-4 h-4 text-red-600" />
                <p className="text-sm text-red-600 dark:text-red-400">Error Rate</p>
              </div>
              <p className="text-2xl font-bold text-red-700 dark:text-red-300">
                {metrics.errorRate.toFixed(1)}%
              </p>
              <p className="text-xs text-red-600 dark:text-red-400 mt-1">
                {metrics.failedInstances} failed
              </p>
            </div>
          </div>
        )}
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Execution Trend */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <h4 className="font-medium mb-4 flex items-center gap-2">
            <TrendingUp className="w-4 h-4" />
            Execution Trend
          </h4>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={timeSeriesData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="timestamp" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Area 
                type="monotone" 
                dataKey="successCount" 
                stackId="1"
                stroke={COLORS.success} 
                fill={COLORS.success}
                fillOpacity={0.6}
                name="Success"
              />
              <Area 
                type="monotone" 
                dataKey="failureCount" 
                stackId="1"
                stroke={COLORS.error} 
                fill={COLORS.error}
                fillOpacity={0.6}
                name="Failure"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Status Distribution */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <h4 className="font-medium mb-4 flex items-center gap-2">
            <Activity className="w-4 h-4" />
            Status Distribution
          </h4>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={statusDistribution}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
              >
                {statusDistribution.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Step Performance */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <h4 className="font-medium mb-4 flex items-center gap-2">
            <Layers className="w-4 h-4" />
            Step Performance
          </h4>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={stepPerformanceData} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" />
              <YAxis dataKey="name" type="category" width={100} />
              <Tooltip />
              <Legend />
              <Bar dataKey="avgDuration" fill={COLORS.primary} name="Avg Duration (ms)" />
              <Bar dataKey="errorRate" fill={COLORS.error} name="Error Rate (%)" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Performance Radar */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <h4 className="font-medium mb-4 flex items-center gap-2">
            <Target className="w-4 h-4" />
            Step Analysis Radar
          </h4>
          <ResponsiveContainer width="100%" height={300}>
            <RadarChart data={radarData}>
              <PolarGrid />
              <PolarAngleAxis dataKey="step" />
              <PolarRadiusAxis angle={90} domain={[0, 100]} />
              <Radar name="Duration" dataKey="duration" stroke={COLORS.primary} fill={COLORS.primary} fillOpacity={0.3} />
              <Radar name="Reliability" dataKey="reliability" stroke={COLORS.success} fill={COLORS.success} fillOpacity={0.3} />
              <Radar name="Throughput" dataKey="throughput" stroke={COLORS.info} fill={COLORS.info} fillOpacity={0.3} />
              <Tooltip />
              <Legend />
            </RadarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Duration Distribution */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h4 className="font-medium mb-4">Duration Distribution Over Time</h4>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={timeSeriesData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="timestamp" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Line type="monotone" dataKey="avgDuration" stroke={COLORS.primary} name="Avg Duration (ms)" strokeWidth={2} />
            <Line type="monotone" dataKey="instanceCount" stroke={COLORS.secondary} name="Instance Count" strokeWidth={2} />
          </LineChart>
        </ResponsiveContainer>
      </div>

      {/* Step Details Table */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h4 className="font-medium mb-4">Detailed Step Metrics</h4>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200 dark:border-gray-700">
                <th className="text-left py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">Step</th>
                <th className="text-right py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">Executions</th>
                <th className="text-right py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">Avg Duration</th>
                <th className="text-right py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">Min/Max</th>
                <th className="text-right py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">Success Rate</th>
                <th className="text-right py-2 px-3 text-sm font-medium text-gray-700 dark:text-gray-300">Errors</th>
              </tr>
            </thead>
            <tbody>
              {stepMetrics.map((step, index) => (
                <tr key={index} className="border-b border-gray-100 dark:border-gray-700">
                  <td className="py-2 px-3 text-sm">{step.stepName}</td>
                  <td className="py-2 px-3 text-sm text-right">{step.executionCount}</td>
                  <td className="py-2 px-3 text-sm text-right">{formatDuration(step.avgDuration)}</td>
                  <td className="py-2 px-3 text-sm text-right">
                    {formatDuration(step.minDuration)} / {formatDuration(step.maxDuration)}
                  </td>
                  <td className="py-2 px-3 text-sm text-right">
                    <span className={`font-medium ${step.successRate >= 95 ? 'text-green-600' : step.successRate >= 90 ? 'text-yellow-600' : 'text-red-600'}`}>
                      {step.successRate.toFixed(1)}%
                    </span>
                  </td>
                  <td className="py-2 px-3 text-sm text-right">
                    {step.errorCount > 0 && (
                      <span className="text-red-600">{step.errorCount}</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};