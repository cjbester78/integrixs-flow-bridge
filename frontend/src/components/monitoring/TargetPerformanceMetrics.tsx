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
import { Slider } from '@/components/ui/slider';
import {
  Activity,
  AlertCircle,
  CheckCircle2,
  Clock,
  RefreshCw,
  Download,
  Filter,
  TrendingUp,
  TrendingDown,
  Server,
  Cpu,
  HardDrive,
  Network,
  Zap,
  BarChart3,
  Timer,
  Target,
  ArrowUp,
  ArrowDown,
  Minus,
  Info,
  Database,
  Globe,
  Cloud,
  Shield,
  AlertTriangle
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { apiClient } from '@/lib/api-client';
import { 
  LineChart, 
  Line, 
  AreaChart, 
  Area, 
  BarChart, 
  Bar, 
  RadarChart,
  Radar,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer, 
  Legend,
  Cell,
  PieChart,
  Pie
} from 'recharts';

interface TargetPerformanceMetricsProps {
  flowId?: string;
  targetId?: string;
  timeRange?: '1h' | '6h' | '24h' | '7d' | '30d';
  autoRefresh?: boolean;
  refreshInterval?: number;
  className?: string;
}

interface TargetMetrics {
  targetId: string;
  targetName: string;
  adapterType: string;
  endpoint: string;
  status: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY' | 'OFFLINE';
  metrics: {
    requests: {
      total: number;
      successful: number;
      failed: number;
      successRate: number;
      trend: 'up' | 'down' | 'stable';
    };
    performance: {
      avgResponseTime: number;
      p50ResponseTime: number;
      p95ResponseTime: number;
      p99ResponseTime: number;
      minResponseTime: number;
      maxResponseTime: number;
      trend: 'improving' | 'degrading' | 'stable';
    };
    throughput: {
      current: number;
      average: number;
      peak: number;
      trend: 'up' | 'down' | 'stable';
    };
    errors: {
      total: number;
      rate: number;
      topErrors: ErrorDetail[];
    };
    resources: {
      cpuUsage: number;
      memoryUsage: number;
      connectionPoolUsage: number;
      queueDepth: number;
    };
  };
  timeSeries: TimeSeriesData[];
  sla: {
    availability: number;
    target: number;
    compliant: boolean;
    violations: number;
  };
}

interface ErrorDetail {
  type: string;
  message: string;
  count: number;
  lastOccurrence: string;
}

interface TimeSeriesData {
  timestamp: string;
  requests: number;
  successRate: number;
  avgResponseTime: number;
  throughput: number;
  errorRate: number;
  cpuUsage: number;
  memoryUsage: number;
}

interface AggregatedMetrics {
  totalTargets: number;
  healthyTargets: number;
  degradedTargets: number;
  unhealthyTargets: number;
  overallSuccessRate: number;
  overallAvgResponseTime: number;
  totalThroughput: number;
  topPerformers: TargetMetrics[];
  bottomPerformers: TargetMetrics[];
}

const COLORS = ['#10b981', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

export function TargetPerformanceMetrics({
  flowId,
  targetId,
  timeRange = '1h',
  autoRefresh = true,
  refreshInterval = 30000,
  className
}: TargetPerformanceMetricsProps) {
  const { toast } = useToast();
  const [targets, setTargets] = useState<TargetMetrics[]>([]);
  const [selectedTarget, setSelectedTarget] = useState<TargetMetrics | null>(null);
  const [aggregated, setAggregated] = useState<AggregatedMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTimeRange, setSelectedTimeRange] = useState(timeRange);
  const [viewMode, setViewMode] = useState<'overview' | 'detailed'>('overview');
  const [comparisonMode, setComparisonMode] = useState(false);
  const [selectedTargetsForComparison, setSelectedTargetsForComparison] = useState<string[]>([]);

  // Fetch metrics data
  const fetchMetrics = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const params = new URLSearchParams();
      if (flowId) params.append('flowId', flowId);
      if (targetId) params.append('targetId', targetId);
      params.append('timeRange', selectedTimeRange);

      const response = await apiClient.get(`/api/metrics/targets?${params}`);
      
      setTargets(response.data.targets);
      setAggregated(response.data.aggregated);
      
      // Auto-select target if targetId provided
      if (targetId && response.data.targets.length > 0) {
        const target = response.data.targets.find((t: TargetMetrics) => t.targetId === targetId);
        if (target) setSelectedTarget(target);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to fetch metrics data');
      console.error('Error fetching metrics:', err);
    } finally {
      setLoading(false);
    }
  }, [flowId, targetId, selectedTimeRange]);

  // Auto-refresh logic
  useEffect(() => {
    fetchMetrics();

    if (autoRefresh) {
      const interval = setInterval(fetchMetrics, refreshInterval);
      return () => clearInterval(interval);
    }
  }, [fetchMetrics, autoRefresh, refreshInterval]);

  const getStatusColor = (status: TargetMetrics['status']) => {
    switch (status) {
      case 'HEALTHY': return 'text-green-500';
      case 'DEGRADED': return 'text-yellow-500';
      case 'UNHEALTHY': return 'text-red-500';
      case 'OFFLINE': return 'text-gray-500';
      default: return 'text-gray-500';
    }
  };

  const getStatusIcon = (status: TargetMetrics['status']) => {
    switch (status) {
      case 'HEALTHY': return <CheckCircle2 className="h-4 w-4" />;
      case 'DEGRADED': return <AlertTriangle className="h-4 w-4" />;
      case 'UNHEALTHY': return <AlertCircle className="h-4 w-4" />;
      case 'OFFLINE': return <XCircle className="h-4 w-4" />;
      default: return <Activity className="h-4 w-4" />;
    }
  };

  const getTrendIcon = (trend: 'up' | 'down' | 'stable' | 'improving' | 'degrading') => {
    if (trend === 'up' || trend === 'improving') return <ArrowUp className="h-3 w-3 text-green-500" />;
    if (trend === 'down' || trend === 'degrading') return <ArrowDown className="h-3 w-3 text-red-500" />;
    return <Minus className="h-3 w-3 text-gray-500" />;
  };

  const getAdapterIcon = (type: string) => {
    switch (type.toLowerCase()) {
      case 'rest': return <Globe className="h-4 w-4" />;
      case 'soap': return <Cloud className="h-4 w-4" />;
      case 'database': return <Database className="h-4 w-4" />;
      case 'jms': return <Network className="h-4 w-4" />;
      default: return <Server className="h-4 w-4" />;
    }
  };

  const formatValue = (value: number, type: 'time' | 'percentage' | 'count' | 'throughput') => {
    switch (type) {
      case 'time':
        if (value < 1000) return `${value.toFixed(0)}ms`;
        return `${(value / 1000).toFixed(2)}s`;
      case 'percentage':
        return `${value.toFixed(1)}%`;
      case 'count':
        if (value > 1000000) return `${(value / 1000000).toFixed(2)}M`;
        if (value > 1000) return `${(value / 1000).toFixed(1)}K`;
        return value.toFixed(0);
      case 'throughput':
        return `${value.toFixed(1)}/s`;
      default:
        return value.toFixed(2);
    }
  };

  const exportMetrics = useCallback(() => {
    const data = {
      timeRange: selectedTimeRange,
      exportedAt: new Date().toISOString(),
      targets: comparisonMode ? 
        targets.filter(t => selectedTargetsForComparison.includes(t.targetId)) : 
        targets,
      aggregated
    };
    
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `target-metrics-${selectedTimeRange}-${new Date().toISOString()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }, [targets, aggregated, selectedTimeRange, comparisonMode, selectedTargetsForComparison]);

  // Prepare comparison data
  const comparisonData = useMemo(() => {
    if (!comparisonMode || selectedTargetsForComparison.length === 0) return null;

    const selectedTargets = targets.filter(t => selectedTargetsForComparison.includes(t.targetId));
    
    return {
      performance: selectedTargets.map(t => ({
        name: t.targetName,
        avgResponseTime: t.metrics.performance.avgResponseTime,
        p95ResponseTime: t.metrics.performance.p95ResponseTime,
        successRate: t.metrics.requests.successRate
      })),
      throughput: selectedTargets.map(t => ({
        name: t.targetName,
        current: t.metrics.throughput.current,
        average: t.metrics.throughput.average,
        peak: t.metrics.throughput.peak
      })),
      resources: selectedTargets.map(t => ({
        name: t.targetName,
        cpu: t.metrics.resources.cpuUsage,
        memory: t.metrics.resources.memoryUsage,
        connections: t.metrics.resources.connectionPoolUsage
      }))
    };
  }, [targets, selectedTargetsForComparison, comparisonMode]);

  if (loading && !targets.length) {
    return (
      <Card className={className}>
        <CardHeader>
          <Skeleton className="h-6 w-48" />
          <Skeleton className="h-4 w-72 mt-2" />
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="grid grid-cols-4 gap-4">
              {[1, 2, 3, 4].map(i => (
                <Skeleton key={i} className="h-32" />
              ))}
            </div>
            <Skeleton className="h-[400px]" />
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error && !targets.length) {
    return (
      <Card className={className}>
        <CardContent className="pt-6">
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
          <Button onClick={fetchMetrics} className="mt-4">
            <RefreshCw className="mr-2 h-4 w-4" />
            Retry
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className={cn("space-y-6", className)}>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <Target className="h-5 w-5" />
                Target Performance Metrics
              </CardTitle>
              <CardDescription>
                Monitor performance metrics for orchestration targets
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <Select value={selectedTimeRange} onValueChange={setSelectedTimeRange}>
                <SelectTrigger className="w-[120px]">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1h">Last Hour</SelectItem>
                  <SelectItem value="6h">Last 6 Hours</SelectItem>
                  <SelectItem value="24h">Last 24 Hours</SelectItem>
                  <SelectItem value="7d">Last 7 Days</SelectItem>
                  <SelectItem value="30d">Last 30 Days</SelectItem>
                </SelectContent>
              </Select>
              <Button
                size="sm"
                variant={comparisonMode ? "default" : "outline"}
                onClick={() => {
                  setComparisonMode(!comparisonMode);
                  setSelectedTargetsForComparison([]);
                }}
              >
                <BarChart3 className="mr-2 h-4 w-4" />
                Compare
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={fetchMetrics}
              >
                <RefreshCw className="h-4 w-4" />
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={exportMetrics}
              >
                <Download className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {/* Overview Cards */}
          {aggregated && (
            <div className="grid grid-cols-4 gap-4 mb-6">
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Total Targets</p>
                      <p className="text-2xl font-bold">{aggregated.totalTargets}</p>
                      <div className="flex gap-2 mt-2">
                        <Badge variant="default" className="text-xs">
                          {aggregated.healthyTargets} Healthy
                        </Badge>
                        {aggregated.degradedTargets > 0 && (
                          <Badge variant="secondary" className="text-xs">
                            {aggregated.degradedTargets} Degraded
                          </Badge>
                        )}
                        {aggregated.unhealthyTargets > 0 && (
                          <Badge variant="destructive" className="text-xs">
                            {aggregated.unhealthyTargets} Unhealthy
                          </Badge>
                        )}
                      </div>
                    </div>
                    <Server className="h-8 w-8 text-muted-foreground" />
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Overall Success Rate</p>
                      <p className="text-2xl font-bold">{formatValue(aggregated.overallSuccessRate, 'percentage')}</p>
                      <Progress value={aggregated.overallSuccessRate} className="mt-2" />
                    </div>
                    <CheckCircle2 className="h-8 w-8 text-green-500" />
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Avg Response Time</p>
                      <p className="text-2xl font-bold">{formatValue(aggregated.overallAvgResponseTime, 'time')}</p>
                    </div>
                    <Timer className="h-8 w-8 text-muted-foreground" />
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Total Throughput</p>
                      <p className="text-2xl font-bold">{formatValue(aggregated.totalThroughput, 'throughput')}</p>
                    </div>
                    <Zap className="h-8 w-8 text-yellow-500" />
                  </div>
                </CardContent>
              </Card>
            </div>
          )}

          <Tabs defaultValue="targets">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="targets">Targets</TabsTrigger>
              <TabsTrigger value="performance">Performance</TabsTrigger>
              <TabsTrigger value="comparison" disabled={!comparisonMode}>Comparison</TabsTrigger>
              <TabsTrigger value="sla">SLA Compliance</TabsTrigger>
            </TabsList>

            <TabsContent value="targets" className="space-y-4">
              <ScrollArea className="h-[600px]">
                <div className="space-y-4">
                  {targets.map((target) => (
                    <Card
                      key={target.targetId}
                      className={cn(
                        "cursor-pointer transition-colors hover:border-primary",
                        selectedTarget?.targetId === target.targetId && "border-primary",
                        comparisonMode && selectedTargetsForComparison.includes(target.targetId) && "bg-primary/5"
                      )}
                      onClick={() => {
                        if (comparisonMode) {
                          setSelectedTargetsForComparison(prev =>
                            prev.includes(target.targetId)
                              ? prev.filter(id => id !== target.targetId)
                              : [...prev, target.targetId]
                          );
                        } else {
                          setSelectedTarget(target);
                        }
                      }}
                    >
                      <CardHeader className="pb-3">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            {getAdapterIcon(target.adapterType)}
                            <div>
                              <p className="font-medium">{target.targetName}</p>
                              <p className="text-sm text-muted-foreground">{target.endpoint}</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <Badge className={cn("gap-1", getStatusColor(target.status))}>
                              {getStatusIcon(target.status)}
                              {target.status}
                            </Badge>
                            <Badge variant="outline">{target.adapterType}</Badge>
                          </div>
                        </div>
                      </CardHeader>
                      <CardContent>
                        <div className="grid grid-cols-4 gap-4">
                          <div>
                            <p className="text-sm text-muted-foreground">Success Rate</p>
                            <div className="flex items-center gap-1">
                              <p className="font-medium">{formatValue(target.metrics.requests.successRate, 'percentage')}</p>
                              {getTrendIcon(target.metrics.requests.trend)}
                            </div>
                            <Progress value={target.metrics.requests.successRate} className="mt-1 h-1" />
                          </div>
                          <div>
                            <p className="text-sm text-muted-foreground">Avg Response</p>
                            <div className="flex items-center gap-1">
                              <p className="font-medium">{formatValue(target.metrics.performance.avgResponseTime, 'time')}</p>
                              {getTrendIcon(target.metrics.performance.trend)}
                            </div>
                          </div>
                          <div>
                            <p className="text-sm text-muted-foreground">Throughput</p>
                            <div className="flex items-center gap-1">
                              <p className="font-medium">{formatValue(target.metrics.throughput.current, 'throughput')}</p>
                              {getTrendIcon(target.metrics.throughput.trend)}
                            </div>
                          </div>
                          <div>
                            <p className="text-sm text-muted-foreground">Error Rate</p>
                            <p className="font-medium text-red-600">{formatValue(target.metrics.errors.rate, 'percentage')}</p>
                          </div>
                        </div>

                        <Separator className="my-4" />

                        <div className="grid grid-cols-4 gap-4 text-sm">
                          <div className="flex items-center gap-2">
                            <Cpu className="h-4 w-4 text-muted-foreground" />
                            <span className="text-muted-foreground">CPU:</span>
                            <span className="font-medium">{formatValue(target.metrics.resources.cpuUsage, 'percentage')}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <HardDrive className="h-4 w-4 text-muted-foreground" />
                            <span className="text-muted-foreground">Memory:</span>
                            <span className="font-medium">{formatValue(target.metrics.resources.memoryUsage, 'percentage')}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <Network className="h-4 w-4 text-muted-foreground" />
                            <span className="text-muted-foreground">Connections:</span>
                            <span className="font-medium">{formatValue(target.metrics.resources.connectionPoolUsage, 'percentage')}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <Database className="h-4 w-4 text-muted-foreground" />
                            <span className="text-muted-foreground">Queue:</span>
                            <span className="font-medium">{target.metrics.resources.queueDepth}</span>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              </ScrollArea>
            </TabsContent>

            <TabsContent value="performance" className="space-y-4">
              {selectedTarget ? (
                <>
                  <Card>
                    <CardHeader>
                      <div className="flex items-center justify-between">
                        <CardTitle className="text-base">
                          Performance Metrics - {selectedTarget.targetName}
                        </CardTitle>
                        <Badge className={cn(getStatusColor(selectedTarget.status))}>
                          {selectedTarget.status}
                        </Badge>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-6">
                      <div>
                        <h4 className="text-sm font-medium mb-4">Response Time Distribution</h4>
                        <ResponsiveContainer width="100%" height={200}>
                          <BarChart data={[
                            { name: 'Min', value: selectedTarget.metrics.performance.minResponseTime },
                            { name: 'P50', value: selectedTarget.metrics.performance.p50ResponseTime },
                            { name: 'Avg', value: selectedTarget.metrics.performance.avgResponseTime },
                            { name: 'P95', value: selectedTarget.metrics.performance.p95ResponseTime },
                            { name: 'P99', value: selectedTarget.metrics.performance.p99ResponseTime },
                            { name: 'Max', value: selectedTarget.metrics.performance.maxResponseTime }
                          ]}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="name" />
                            <YAxis />
                            <Tooltip formatter={(value: any) => formatValue(value, 'time')} />
                            <Bar dataKey="value" fill="#3b82f6" />
                          </BarChart>
                        </ResponsiveContainer>
                      </div>

                      <Separator />

                      <div>
                        <h4 className="text-sm font-medium mb-4">Time Series Performance</h4>
                        <ResponsiveContainer width="100%" height={300}>
                          <LineChart data={selectedTarget.timeSeries}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis 
                              dataKey="timestamp" 
                              tickFormatter={(value) => new Date(value).toLocaleTimeString()}
                            />
                            <YAxis yAxisId="left" />
                            <YAxis yAxisId="right" orientation="right" />
                            <Tooltip 
                              labelFormatter={(value) => new Date(value).toLocaleString()}
                              formatter={(value: any, name: string) => {
                                if (name.includes('Rate')) return formatValue(value, 'percentage');
                                if (name.includes('Time')) return formatValue(value, 'time');
                                if (name.includes('Throughput')) return formatValue(value, 'throughput');
                                return value;
                              }}
                            />
                            <Legend />
                            <Line
                              yAxisId="left"
                              type="monotone"
                              dataKey="avgResponseTime"
                              stroke="#3b82f6"
                              name="Avg Response Time"
                              strokeWidth={2}
                              dot={false}
                            />
                            <Line
                              yAxisId="right"
                              type="monotone"
                              dataKey="successRate"
                              stroke="#10b981"
                              name="Success Rate"
                              strokeWidth={2}
                              dot={false}
                            />
                            <Line
                              yAxisId="right"
                              type="monotone"
                              dataKey="throughput"
                              stroke="#f59e0b"
                              name="Throughput"
                              strokeWidth={2}
                              dot={false}
                            />
                          </LineChart>
                        </ResponsiveContainer>
                      </div>

                      <Separator />

                      <div className="grid grid-cols-2 gap-6">
                        <div>
                          <h4 className="text-sm font-medium mb-4">Request Statistics</h4>
                          <div className="space-y-3">
                            <div className="flex justify-between">
                              <span className="text-sm text-muted-foreground">Total Requests</span>
                              <span className="font-medium">{formatValue(selectedTarget.metrics.requests.total, 'count')}</span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-sm text-muted-foreground">Successful</span>
                              <span className="font-medium text-green-600">{formatValue(selectedTarget.metrics.requests.successful, 'count')}</span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-sm text-muted-foreground">Failed</span>
                              <span className="font-medium text-red-600">{formatValue(selectedTarget.metrics.requests.failed, 'count')}</span>
                            </div>
                            <Separator />
                            <div className="flex justify-between">
                              <span className="text-sm text-muted-foreground">Success Rate</span>
                              <span className="font-medium">{formatValue(selectedTarget.metrics.requests.successRate, 'percentage')}</span>
                            </div>
                          </div>
                        </div>

                        <div>
                          <h4 className="text-sm font-medium mb-4">Top Errors</h4>
                          {selectedTarget.metrics.errors.topErrors.length > 0 ? (
                            <div className="space-y-2">
                              {selectedTarget.metrics.errors.topErrors.slice(0, 5).map((error, idx) => (
                                <div key={idx} className="text-sm">
                                  <div className="flex justify-between">
                                    <span className="text-muted-foreground truncate flex-1">{error.type}</span>
                                    <Badge variant="destructive" className="ml-2">{error.count}</Badge>
                                  </div>
                                  <p className="text-xs text-muted-foreground truncate">{error.message}</p>
                                </div>
                              ))}
                            </div>
                          ) : (
                            <p className="text-sm text-muted-foreground">No errors recorded</p>
                          )}
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="text-base">Resource Utilization</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ResponsiveContainer width="100%" height={300}>
                        <AreaChart data={selectedTarget.timeSeries}>
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis 
                            dataKey="timestamp" 
                            tickFormatter={(value) => new Date(value).toLocaleTimeString()}
                          />
                          <YAxis />
                          <Tooltip 
                            labelFormatter={(value) => new Date(value).toLocaleString()}
                            formatter={(value: any) => formatValue(value, 'percentage')}
                          />
                          <Legend />
                          <Area
                            type="monotone"
                            dataKey="cpuUsage"
                            stackId="1"
                            stroke="#3b82f6"
                            fill="#3b82f6"
                            name="CPU Usage"
                          />
                          <Area
                            type="monotone"
                            dataKey="memoryUsage"
                            stackId="1"
                            stroke="#10b981"
                            fill="#10b981"
                            name="Memory Usage"
                          />
                        </AreaChart>
                      </ResponsiveContainer>
                    </CardContent>
                  </Card>
                </>
              ) : (
                <Card>
                  <CardContent className="pt-6">
                    <div className="text-center py-12">
                      <Target className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground">Select a target to view detailed performance metrics</p>
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value="comparison" className="space-y-4">
              {comparisonData ? (
                <>
                  <Card>
                    <CardHeader>
                      <CardTitle className="text-base">Performance Comparison</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={comparisonData.performance}>
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis dataKey="name" />
                          <YAxis yAxisId="left" />
                          <YAxis yAxisId="right" orientation="right" />
                          <Tooltip 
                            formatter={(value: any, name: string) => {
                              if (name.includes('Time')) return formatValue(value, 'time');
                              if (name.includes('Rate')) return formatValue(value, 'percentage');
                              return value;
                            }}
                          />
                          <Legend />
                          <Bar yAxisId="left" dataKey="avgResponseTime" fill="#3b82f6" name="Avg Response Time" />
                          <Bar yAxisId="left" dataKey="p95ResponseTime" fill="#8b5cf6" name="P95 Response Time" />
                          <Bar yAxisId="right" dataKey="successRate" fill="#10b981" name="Success Rate" />
                        </BarChart>
                      </ResponsiveContainer>
                    </CardContent>
                  </Card>

                  <div className="grid grid-cols-2 gap-4">
                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base">Throughput Comparison</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <ResponsiveContainer width="100%" height={200}>
                          <RadarChart data={comparisonData.throughput}>
                            <PolarGrid />
                            <PolarAngleAxis dataKey="name" />
                            <PolarRadiusAxis />
                            <Radar name="Current" dataKey="current" stroke="#3b82f6" fill="#3b82f6" fillOpacity={0.6} />
                            <Radar name="Average" dataKey="average" stroke="#10b981" fill="#10b981" fillOpacity={0.6} />
                            <Radar name="Peak" dataKey="peak" stroke="#f59e0b" fill="#f59e0b" fillOpacity={0.6} />
                            <Legend />
                          </RadarChart>
                        </ResponsiveContainer>
                      </CardContent>
                    </Card>

                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base">Resource Usage</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <ResponsiveContainer width="100%" height={200}>
                          <BarChart data={comparisonData.resources} layout="vertical">
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis type="number" />
                            <YAxis type="category" dataKey="name" />
                            <Tooltip formatter={(value: any) => formatValue(value, 'percentage')} />
                            <Legend />
                            <Bar dataKey="cpu" fill="#ef4444" name="CPU" />
                            <Bar dataKey="memory" fill="#f59e0b" name="Memory" />
                            <Bar dataKey="connections" fill="#10b981" name="Connections" />
                          </BarChart>
                        </ResponsiveContainer>
                      </CardContent>
                    </Card>
                  </div>
                </>
              ) : (
                <Card>
                  <CardContent className="pt-6">
                    <div className="text-center py-12">
                      <BarChart3 className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground">
                        Select targets from the Targets tab to compare their performance
                      </p>
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value="sla" className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                {targets.map((target) => (
                  <Card key={target.targetId}>
                    <CardHeader>
                      <div className="flex items-center justify-between">
                        <CardTitle className="text-base">{target.targetName}</CardTitle>
                        <Badge
                          variant={target.sla.compliant ? "default" : "destructive"}
                        >
                          {target.sla.compliant ? "Compliant" : "Violated"}
                        </Badge>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-4">
                        <div>
                          <div className="flex justify-between mb-2">
                            <span className="text-sm text-muted-foreground">Availability</span>
                            <span className="font-medium">{formatValue(target.sla.availability, 'percentage')}</span>
                          </div>
                          <Progress 
                            value={target.sla.availability} 
                            className={cn(
                              "h-2",
                              target.sla.availability >= target.sla.target && "bg-green-100 [&>div]:bg-green-500"
                            )}
                          />
                          <p className="text-xs text-muted-foreground mt-1">
                            Target: {formatValue(target.sla.target, 'percentage')}
                          </p>
                        </div>
                        <Separator />
                        <div className="flex justify-between">
                          <span className="text-sm text-muted-foreground">SLA Violations</span>
                          <Badge variant={target.sla.violations > 0 ? "destructive" : "secondary"}>
                            {target.sla.violations}
                          </Badge>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>

              {aggregated && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">Overall SLA Summary</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      <Alert>
                        <Shield className="h-4 w-4" />
                        <AlertTitle>SLA Compliance Status</AlertTitle>
                        <AlertDescription>
                          {targets.filter(t => t.sla.compliant).length} out of {targets.length} targets 
                          are meeting their SLA requirements
                        </AlertDescription>
                      </Alert>
                      
                      <ResponsiveContainer width="100%" height={200}>
                        <PieChart>
                          <Pie
                            data={[
                              { name: 'Compliant', value: targets.filter(t => t.sla.compliant).length, fill: '#10b981' },
                              { name: 'Non-Compliant', value: targets.filter(t => !t.sla.compliant).length, fill: '#ef4444' }
                            ]}
                            cx="50%"
                            cy="50%"
                            labelLine={false}
                            label={({ name, value }) => `${name}: ${value}`}
                            outerRadius={80}
                          >
                            {[0, 1].map((entry, index) => (
                              <Cell key={`cell-${index}`} />
                            ))}
                          </Pie>
                          <Tooltip />
                        </PieChart>
                      </ResponsiveContainer>
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}