import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { 
  LineChart, Line, AreaChart, Area, BarChart, Bar, 
  PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, 
  Tooltip, Legend, ResponsiveContainer 
} from 'recharts';
import { 
  Activity, TrendingUp, AlertCircle, Clock, 
  Zap, Package, CheckCircle, XCircle 
} from 'lucide-react';
import { pluginApi } from '../api/plugin';
import { MetricCard } from '../components/analytics/MetricCard';
import { PerformanceChart } from '../components/analytics/PerformanceChart';
import { ErrorSummary } from '../components/analytics/ErrorSummary';

export const PluginAnalytics: React.FC = () => {
  const [selectedPlugin, setSelectedPlugin] = useState<string | null>(null);
  const [timeRange, setTimeRange] = useState<'5m' | '1h' | '24h' | '7d'>('1h');

  // Fetch available plugins
  const { data: plugins } = useQuery({
    queryKey: ['plugins'],
    queryFn: () => pluginApi.getAllPlugins(),
  });

  // Fetch plugin metrics
  const { data: metrics, isLoading: metricsLoading } = useQuery({
    queryKey: ['plugin-metrics', selectedPlugin],
    queryFn: () => selectedPlugin ? pluginApi.getMetrics(selectedPlugin) : null,
    enabled: !!selectedPlugin,
    refetchInterval: 5000, // Refresh every 5 seconds
  });

  // Fetch performance report
  const { data: report, isLoading: reportLoading } = useQuery({
    queryKey: ['plugin-performance', selectedPlugin, timeRange],
    queryFn: () => selectedPlugin ? pluginApi.getPerformanceReport(selectedPlugin) : null,
    enabled: !!selectedPlugin,
  });

  const formatNumber = (num: number) => {
    if (num >= 1000000) return `${(num / 1000000).toFixed(1)}M`;
    if (num >= 1000) return `${(num / 1000).toFixed(1)}K`;
    return num.toString();
  };

  const formatDuration = (ms: number) => {
    if (ms >= 1000) return `${(ms / 1000).toFixed(2)}s`;
    return `${ms}ms`;
  };

  return (
    <div className="container mx-auto px-6 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Plugin Analytics</h1>
        <p className="text-gray-600 mt-1">
          Monitor performance and health of your plugins
        </p>
      </div>

      {/* Plugin Selector */}
      <div className="mb-6 flex gap-4">
        <select
          value={selectedPlugin || ''}
          onChange={(e) => setSelectedPlugin(e.target.value || null)}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="">Select a plugin</option>
          {plugins?.map(plugin => (
            <option key={plugin.id} value={plugin.id}>
              {plugin.name} ({plugin.version})
            </option>
          ))}
        </select>

        <select
          value={timeRange}
          onChange={(e) => setTimeRange(e.target.value as any)}
          className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="5m">Last 5 minutes</option>
          <option value="1h">Last hour</option>
          <option value="24h">Last 24 hours</option>
          <option value="7d">Last 7 days</option>
        </select>
      </div>

      {selectedPlugin ? (
        <>
          {/* Overview Metrics */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <MetricCard
              title="Messages Processed"
              value={formatNumber(metrics?.messagesProcessed || 0)}
              icon={<Activity className="w-5 h-5" />}
              trend={metrics?.trend?.messages || 0}
              loading={metricsLoading}
            />
            <MetricCard
              title="Success Rate"
              value={`${(metrics?.successRate || 0).toFixed(1)}%`}
              icon={<CheckCircle className="w-5 h-5" />}
              trend={metrics?.trend?.successRate || 0}
              loading={metricsLoading}
              valueColor={
                metrics?.successRate >= 95 ? 'text-green-600' :
                metrics?.successRate >= 90 ? 'text-yellow-600' :
                'text-red-600'
              }
            />
            <MetricCard
              title="Avg Response Time"
              value={formatDuration(metrics?.averageProcessingTime || 0)}
              icon={<Clock className="w-5 h-5" />}
              trend={metrics?.trend?.responseTime || 0}
              loading={metricsLoading}
            />
            <MetricCard
              title="Errors"
              value={formatNumber(metrics?.errors || 0)}
              icon={<AlertCircle className="w-5 h-5" />}
              trend={metrics?.trend?.errors || 0}
              loading={metricsLoading}
              valueColor="text-red-600"
            />
          </div>

          {/* Performance Charts */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
            {/* Throughput Chart */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Message Throughput</h3>
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={report?.throughputData || []}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="time" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Area 
                    type="monotone" 
                    dataKey="messages" 
                    stroke="#3b82f6" 
                    fill="#93bbfc" 
                    name="Messages/min"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>

            {/* Response Time Chart */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Response Time</h3>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={report?.responseTimeData || []}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="time" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="p50" 
                    stroke="#10b981" 
                    name="P50" 
                    strokeWidth={2}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="p95" 
                    stroke="#f59e0b" 
                    name="P95" 
                    strokeWidth={2}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="p99" 
                    stroke="#ef4444" 
                    name="P99" 
                    strokeWidth={2}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Error Distribution and Resource Usage */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Error Distribution */}
            {metrics?.errorSummary && Object.keys(metrics.errorSummary).length > 0 && (
              <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Error Distribution</h3>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={Object.entries(metrics.errorSummary).map(([type, count]) => ({
                        name: type,
                        value: count,
                      }))}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                      outerRadius={80}
                      fill="#8884d8"
                    >
                      {Object.entries(metrics.errorSummary).map((_, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            )}

            {/* Resource Usage */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Resource Usage</h3>
              <div className="space-y-4">
                <div>
                  <div className="flex justify-between text-sm text-gray-600 mb-1">
                    <span>Memory Usage</span>
                    <span>{(report?.resourceUsage?.currentMemory || 0) / 1024 / 1024}MB</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-blue-600 h-2 rounded-full"
                      style={{ 
                        width: `${Math.min(100, (report?.resourceUsage?.currentMemory || 0) / 
                          (report?.resourceUsage?.peakMemory || 1) * 100)}%` 
                      }}
                    />
                  </div>
                  <p className="text-xs text-gray-500 mt-1">
                    Peak: {(report?.resourceUsage?.peakMemory || 0) / 1024 / 1024}MB
                  </p>
                </div>

                <div>
                  <div className="flex justify-between text-sm text-gray-600 mb-1">
                    <span>CPU Usage</span>
                    <span>{(report?.resourceUsage?.currentCpu || 0).toFixed(1)}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-green-600 h-2 rounded-full"
                      style={{ width: `${report?.resourceUsage?.currentCpu || 0}%` }}
                    />
                  </div>
                  <p className="text-xs text-gray-500 mt-1">
                    Peak: {(report?.resourceUsage?.peakCpu || 0).toFixed(1)}%
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Performance Summary Table */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mt-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Performance Summary</h3>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Time Range
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Messages
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Success Rate
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Avg Time
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      P95 Time
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {['last5Minutes', 'lastHour', 'last24Hours'].map((period) => {
                    const stats = report?.[period];
                    if (!stats) return null;
                    
                    return (
                      <tr key={period}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {period === 'last5Minutes' ? 'Last 5 min' :
                           period === 'lastHour' ? 'Last hour' :
                           'Last 24h'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatNumber(stats.sampleCount)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          <span className={
                            stats.successRate >= 95 ? 'text-green-600' :
                            stats.successRate >= 90 ? 'text-yellow-600' :
                            'text-red-600'
                          }>
                            {stats.successRate.toFixed(1)}%
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDuration(stats.averageProcessingTime)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDuration(stats.p95ProcessingTime)}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </>
      ) : (
        <div className="text-center py-12">
          <Package className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No Plugin Selected</h3>
          <p className="text-gray-600">
            Select a plugin from the dropdown to view its analytics
          </p>
        </div>
      )}
    </div>
  );
};

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];