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
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import {
  Activity,
  AlertCircle,
  CheckCircle2,
  Clock,
  RefreshCw,
  Play,
  Pause,
  Square,
  Filter,
  Search,
  TrendingUp,
  TrendingDown,
  ChevronRight,
  MoreVertical,
  Download,
  Eye,
  Calendar as CalendarIcon,
  BarChart3,
  Timer,
  Zap,
  XCircle,
  ArrowUp,
  ArrowDown,
  Minus
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { format } from 'date-fns';
import { apiClient } from '@/lib/api-client';
import { LineChart, Line, AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';

interface ExecutionTrackerProps {
  flowId?: string;
  autoRefresh?: boolean;
  refreshInterval?: number;
  maxExecutions?: number;
  className?: string;
}

interface ExecutionSummary {
  id: string;
  flowId: string;
  flowName: string;
  status: 'RUNNING' | 'COMPLETED' | 'FAILED' | 'PAUSED' | 'CANCELLED';
  startTime: string;
  endTime?: string;
  duration?: number;
  progress: number;
  currentStep?: string;
  totalSteps: number;
  completedSteps: number;
  failedSteps: number;
  errorMessage?: string;
}

interface ExecutionStats {
  totalExecutions: number;
  runningExecutions: number;
  completedExecutions: number;
  failedExecutions: number;
  averageDuration: number;
  successRate: number;
  throughput: number;
  trendsData: TrendData[];
}

interface TrendData {
  timestamp: string;
  executions: number;
  successes: number;
  failures: number;
  avgDuration: number;
}

interface FilterOptions {
  status?: string;
  flowId?: string;
  dateRange?: {
    from: Date;
    to: Date;
  };
  searchTerm?: string;
}

export function ExecutionTracker({
  flowId,
  autoRefresh = true,
  refreshInterval = 5000,
  maxExecutions = 50,
  className
}: ExecutionTrackerProps) {
  const { toast } = useToast();
  const [executions, setExecutions] = useState<ExecutionSummary[]>([]);
  const [stats, setStats] = useState<ExecutionStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isPaused, setIsPaused] = useState(false);
  const [selectedExecution, setSelectedExecution] = useState<ExecutionSummary | null>(null);
  const [filters, setFilters] = useState<FilterOptions>({});
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');

  // Fetch executions data
  const fetchExecutions = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const params = new URLSearchParams();
      if (flowId) params.append('flowId', flowId);
      if (filters.status && filters.status !== 'all') params.append('status', filters.status);
      if (filters.dateRange?.from) params.append('startDate', filters.dateRange.from.toISOString());
      if (filters.dateRange?.to) params.append('endDate', filters.dateRange.to.toISOString());
      params.append('limit', maxExecutions.toString());

      const [executionsRes, statsRes] = await Promise.all([
        apiClient.get(`/api/executions?${params}`),
        apiClient.get(`/api/executions/stats?${params}`)
      ]);

      setExecutions(executionsRes.data);
      setStats(statsRes.data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch execution data');
      console.error('Error fetching executions:', err);
    } finally {
      setLoading(false);
    }
  }, [flowId, filters, maxExecutions]);

  // Auto-refresh logic
  useEffect(() => {
    fetchExecutions();

    if (autoRefresh && !isPaused) {
      const interval = setInterval(fetchExecutions, refreshInterval);
      return () => clearInterval(interval);
    }
  }, [fetchExecutions, autoRefresh, refreshInterval, isPaused]);

  // Filter executions based on search term
  const filteredExecutions = useMemo(() => {
    if (!filters.searchTerm) return executions;

    return executions.filter(exec =>
      exec.id.includes(filters.searchTerm!) ||
      exec.flowName.toLowerCase().includes(filters.searchTerm!.toLowerCase()) ||
      (exec.currentStep && exec.currentStep.toLowerCase().includes(filters.searchTerm!.toLowerCase()))
    );
  }, [executions, filters.searchTerm]);

  const handleExecutionAction = useCallback(async (executionId: string, action: 'pause' | 'resume' | 'cancel') => {
    try {
      await apiClient.post(`/api/executions/${executionId}/${action}`);
      toast({
        title: 'Success',
        description: `Execution ${action}ed successfully`
      });
      fetchExecutions();
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.message || `Failed to ${action} execution`,
        variant: 'destructive'
      });
    }
  }, [fetchExecutions, toast]);

  const exportExecutions = useCallback(() => {
    const data = JSON.stringify(filteredExecutions, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `executions-${new Date().toISOString()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }, [filteredExecutions]);

  const getStatusIcon = (status: ExecutionSummary['status']) => {
    switch (status) {
      case 'COMPLETED': return <CheckCircle2 className="h-4 w-4 text-green-500" />;
      case 'RUNNING': return <RefreshCw className="h-4 w-4 text-blue-500 animate-spin" />;
      case 'FAILED': return <XCircle className="h-4 w-4 text-red-500" />;
      case 'PAUSED': return <Pause className="h-4 w-4 text-yellow-500" />;
      case 'CANCELLED': return <Square className="h-4 w-4 text-gray-500" />;
      default: return <Activity className="h-4 w-4" />;
    }
  };

  const formatDuration = (ms?: number) => {
    if (!ms) return '-';
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
    return `${(ms / 60000).toFixed(1)}m`;
  };

  const getTrendIcon = (current: number, previous: number) => {
    if (current > previous) return <ArrowUp className="h-3 w-3 text-green-500" />;
    if (current < previous) return <ArrowDown className="h-3 w-3 text-red-500" />;
    return <Minus className="h-3 w-3 text-gray-500" />;
  };

  if (loading && !executions.length) {
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
                <Skeleton key={i} className="h-24" />
              ))}
            </div>
            <Skeleton className="h-[400px]" />
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error && !executions.length) {
    return (
      <Card className={className}>
        <CardContent className="pt-6">
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
          <Button onClick={fetchExecutions} className="mt-4">
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
                <Activity className="h-5 w-5" />
                Execution Tracker
              </CardTitle>
              <CardDescription>
                Monitor and manage orchestration flow executions in real-time
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <Button
                size="sm"
                variant="outline"
                onClick={() => setIsPaused(!isPaused)}
              >
                {isPaused ? (
                  <>
                    <Play className="mr-2 h-4 w-4" />
                    Resume
                  </>
                ) : (
                  <>
                    <Pause className="mr-2 h-4 w-4" />
                    Pause
                  </>
                )}
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={fetchExecutions}
              >
                <RefreshCw className="h-4 w-4" />
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={exportExecutions}
              >
                <Download className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {/* Statistics Cards */}
          {stats && (
            <div className="grid grid-cols-4 gap-4 mb-6">
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Total Executions</p>
                      <p className="text-2xl font-bold">{stats.totalExecutions}</p>
                    </div>
                    <BarChart3 className="h-8 w-8 text-muted-foreground" />
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Running</p>
                      <p className="text-2xl font-bold text-blue-600">{stats.runningExecutions}</p>
                    </div>
                    <RefreshCw className="h-8 w-8 text-blue-600 animate-spin" />
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Success Rate</p>
                      <div className="flex items-center gap-1">
                        <p className="text-2xl font-bold">{stats.successRate.toFixed(1)}%</p>
                        {stats.trendsData.length > 1 && 
                          getTrendIcon(
                            stats.successRate,
                            stats.trendsData[stats.trendsData.length - 2].successes / stats.trendsData[stats.trendsData.length - 2].executions * 100
                          )
                        }
                      </div>
                    </div>
                    <TrendingUp className="h-8 w-8 text-green-600" />
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">Avg Duration</p>
                      <p className="text-2xl font-bold">{formatDuration(stats.averageDuration)}</p>
                    </div>
                    <Timer className="h-8 w-8 text-muted-foreground" />
                  </div>
                </CardContent>
              </Card>
            </div>
          )}

          <Tabs defaultValue="executions">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="executions">Executions</TabsTrigger>
              <TabsTrigger value="trends">Trends</TabsTrigger>
              <TabsTrigger value="performance">Performance</TabsTrigger>
            </TabsList>

            <TabsContent value="executions" className="space-y-4">
              {/* Filters */}
              <div className="flex items-center gap-4">
                <div className="flex-1">
                  <div className="relative">
                    <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="Search executions..."
                      value={filters.searchTerm || ''}
                      onChange={(e) => setFilters({ ...filters, searchTerm: e.target.value })}
                      className="pl-8"
                    />
                  </div>
                </div>
                <Select
                  value={filters.status || 'all'}
                  onValueChange={(value) => setFilters({ ...filters, status: value })}
                >
                  <SelectTrigger className="w-[180px]">
                    <SelectValue placeholder="Filter by status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Status</SelectItem>
                    <SelectItem value="RUNNING">Running</SelectItem>
                    <SelectItem value="COMPLETED">Completed</SelectItem>
                    <SelectItem value="FAILED">Failed</SelectItem>
                    <SelectItem value="PAUSED">Paused</SelectItem>
                    <SelectItem value="CANCELLED">Cancelled</SelectItem>
                  </SelectContent>
                </Select>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button
                      variant="outline"
                      className={cn(
                        "w-[200px] justify-start text-left font-normal",
                        !filters.dateRange && "text-muted-foreground"
                      )}
                    >
                      <CalendarIcon className="mr-2 h-4 w-4" />
                      {filters.dateRange?.from ? (
                        filters.dateRange.to ? (
                          <>
                            {format(filters.dateRange.from, "LLL dd")} -{" "}
                            {format(filters.dateRange.to, "LLL dd")}
                          </>
                        ) : (
                          format(filters.dateRange.from, "PPP")
                        )
                      ) : (
                        <span>Pick date range</span>
                      )}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      initialFocus
                      mode="range"
                      selected={{
                        from: filters.dateRange?.from,
                        to: filters.dateRange?.to
                      }}
                      onSelect={(range) => {
                        if (range?.from) {
                          setFilters({
                            ...filters,
                            dateRange: {
                              from: range.from,
                              to: range.to || range.from
                            }
                          });
                        }
                      }}
                    />
                  </PopoverContent>
                </Popover>
                <Button
                  variant="outline"
                  size="icon"
                  onClick={() => setViewMode(viewMode === 'list' ? 'grid' : 'list')}
                >
                  {viewMode === 'list' ? (
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
                    </svg>
                  ) : (
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                  )}
                </Button>
              </div>

              {/* Executions List/Grid */}
              <ScrollArea className="h-[500px]">
                {viewMode === 'list' ? (
                  <div className="space-y-2">
                    {filteredExecutions.map((execution) => (
                      <Card
                        key={execution.id}
                        className={cn(
                          "cursor-pointer transition-colors hover:border-primary",
                          selectedExecution?.id === execution.id && "border-primary"
                        )}
                        onClick={() => setSelectedExecution(execution)}
                      >
                        <CardContent className="p-4">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-4">
                              {getStatusIcon(execution.status)}
                              <div>
                                <p className="font-medium">{execution.flowName}</p>
                                <p className="text-sm text-muted-foreground">
                                  ID: {execution.id} • Started: {new Date(execution.startTime).toLocaleString()}
                                </p>
                              </div>
                            </div>
                            <div className="flex items-center gap-4">
                              <div className="text-right">
                                <p className="text-sm">
                                  {execution.completedSteps}/{execution.totalSteps} steps
                                </p>
                                <Progress
                                  value={execution.progress}
                                  className="w-24 h-2 mt-1"
                                />
                              </div>
                              <Badge
                                variant={
                                  execution.status === 'COMPLETED' ? 'default' :
                                  execution.status === 'FAILED' ? 'destructive' :
                                  execution.status === 'RUNNING' ? 'secondary' : 'outline'
                                }
                              >
                                {execution.status}
                              </Badge>
                              {execution.duration && (
                                <span className="text-sm text-muted-foreground">
                                  {formatDuration(execution.duration)}
                                </span>
                              )}
                              {execution.status === 'RUNNING' && (
                                <div className="flex items-center gap-1">
                                  <Button
                                    size="icon"
                                    variant="ghost"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleExecutionAction(execution.id, 'pause');
                                    }}
                                  >
                                    <Pause className="h-4 w-4" />
                                  </Button>
                                  <Button
                                    size="icon"
                                    variant="ghost"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleExecutionAction(execution.id, 'cancel');
                                    }}
                                  >
                                    <Square className="h-4 w-4" />
                                  </Button>
                                </div>
                              )}
                              {execution.status === 'PAUSED' && (
                                <Button
                                  size="icon"
                                  variant="ghost"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleExecutionAction(execution.id, 'resume');
                                  }}
                                >
                                  <Play className="h-4 w-4" />
                                </Button>
                              )}
                            </div>
                          </div>
                          {execution.currentStep && execution.status === 'RUNNING' && (
                            <div className="mt-2 flex items-center gap-2 text-sm text-muted-foreground">
                              <RefreshCw className="h-3 w-3 animate-spin" />
                              Current step: {execution.currentStep}
                            </div>
                          )}
                          {execution.errorMessage && (
                            <Alert variant="destructive" className="mt-2">
                              <AlertCircle className="h-4 w-4" />
                              <AlertDescription className="text-xs">
                                {execution.errorMessage}
                              </AlertDescription>
                            </Alert>
                          )}
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                ) : (
                  <div className="grid grid-cols-3 gap-4">
                    {filteredExecutions.map((execution) => (
                      <Card
                        key={execution.id}
                        className={cn(
                          "cursor-pointer transition-colors hover:border-primary",
                          selectedExecution?.id === execution.id && "border-primary"
                        )}
                        onClick={() => setSelectedExecution(execution)}
                      >
                        <CardHeader className="pb-3">
                          <div className="flex items-center justify-between">
                            {getStatusIcon(execution.status)}
                            <Badge
                              variant={
                                execution.status === 'COMPLETED' ? 'default' :
                                execution.status === 'FAILED' ? 'destructive' :
                                execution.status === 'RUNNING' ? 'secondary' : 'outline'
                              }
                            >
                              {execution.status}
                            </Badge>
                          </div>
                        </CardHeader>
                        <CardContent>
                          <p className="font-medium truncate">{execution.flowName}</p>
                          <p className="text-xs text-muted-foreground truncate">ID: {execution.id}</p>
                          <div className="mt-4 space-y-2">
                            <div className="flex justify-between text-sm">
                              <span className="text-muted-foreground">Progress</span>
                              <span>{execution.progress}%</span>
                            </div>
                            <Progress value={execution.progress} className="h-2" />
                            <div className="flex justify-between text-xs">
                              <span className="text-muted-foreground">Steps</span>
                              <span>{execution.completedSteps}/{execution.totalSteps}</span>
                            </div>
                            {execution.duration && (
                              <div className="flex justify-between text-xs">
                                <span className="text-muted-foreground">Duration</span>
                                <span>{formatDuration(execution.duration)}</span>
                              </div>
                            )}
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                )}
              </ScrollArea>
            </TabsContent>

            <TabsContent value="trends" className="space-y-4">
              {stats && stats.trendsData.length > 0 ? (
                <>
                  <Card>
                    <CardHeader>
                      <CardTitle className="text-base">Execution Trends</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ResponsiveContainer width="100%" height={300}>
                        <AreaChart data={stats.trendsData}>
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis
                            dataKey="timestamp"
                            tickFormatter={(value) => new Date(value).toLocaleTimeString()}
                          />
                          <YAxis />
                          <Tooltip
                            labelFormatter={(value) => new Date(value).toLocaleString()}
                          />
                          <Legend />
                          <Area
                            type="monotone"
                            dataKey="executions"
                            stackId="1"
                            stroke="#3b82f6"
                            fill="#3b82f6"
                            fillOpacity={0.6}
                            name="Total"
                          />
                          <Area
                            type="monotone"
                            dataKey="successes"
                            stackId="2"
                            stroke="#10b981"
                            fill="#10b981"
                            fillOpacity={0.6}
                            name="Success"
                          />
                          <Area
                            type="monotone"
                            dataKey="failures"
                            stackId="2"
                            stroke="#ef4444"
                            fill="#ef4444"
                            fillOpacity={0.6}
                            name="Failed"
                          />
                        </AreaChart>
                      </ResponsiveContainer>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="text-base">Success Rate Over Time</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ResponsiveContainer width="100%" height={200}>
                        <LineChart data={stats.trendsData.map(d => ({
                          ...d,
                          successRate: d.executions > 0 ? (d.successes / d.executions) * 100 : 0
                        }))}>
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis
                            dataKey="timestamp"
                            tickFormatter={(value) => new Date(value).toLocaleTimeString()}
                          />
                          <YAxis domain={[0, 100]} />
                          <Tooltip
                            labelFormatter={(value) => new Date(value).toLocaleString()}
                            formatter={(value: any) => `${value.toFixed(1)}%`}
                          />
                          <Line
                            type="monotone"
                            dataKey="successRate"
                            stroke="#10b981"
                            strokeWidth={2}
                            dot={false}
                            name="Success Rate"
                          />
                        </LineChart>
                      </ResponsiveContainer>
                    </CardContent>
                  </Card>
                </>
              ) : (
                <Card>
                  <CardContent className="pt-6">
                    <div className="text-center py-12">
                      <BarChart3 className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground">No trend data available</p>
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value="performance" className="space-y-4">
              {stats && stats.trendsData.length > 0 ? (
                <>
                  <Card>
                    <CardHeader>
                      <CardTitle className="text-base">Average Duration Trends</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={stats.trendsData}>
                          <CartesianGrid strokeDasharray="3 3" />
                          <XAxis
                            dataKey="timestamp"
                            tickFormatter={(value) => new Date(value).toLocaleTimeString()}
                          />
                          <YAxis />
                          <Tooltip
                            labelFormatter={(value) => new Date(value).toLocaleString()}
                            formatter={(value: any) => `${value}ms`}
                          />
                          <Bar
                            dataKey="avgDuration"
                            fill="#8b5cf6"
                            name="Avg Duration (ms)"
                          />
                        </BarChart>
                      </ResponsiveContainer>
                    </CardContent>
                  </Card>

                  <div className="grid grid-cols-2 gap-4">
                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base">Performance Summary</CardTitle>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div>
                          <div className="flex justify-between mb-2">
                            <span className="text-sm text-muted-foreground">Throughput</span>
                            <span className="font-medium">{stats.throughput.toFixed(2)} exec/min</span>
                          </div>
                          <Progress value={Math.min((stats.throughput / 10) * 100, 100)} />
                        </div>
                        <Separator />
                        <div className="space-y-2">
                          <div className="flex justify-between text-sm">
                            <span className="text-muted-foreground">Fastest Execution</span>
                            <span className="font-medium">
                              {formatDuration(Math.min(...stats.trendsData.map(d => d.avgDuration)))}
                            </span>
                          </div>
                          <div className="flex justify-between text-sm">
                            <span className="text-muted-foreground">Slowest Execution</span>
                            <span className="font-medium">
                              {formatDuration(Math.max(...stats.trendsData.map(d => d.avgDuration)))}
                            </span>
                          </div>
                          <div className="flex justify-between text-sm">
                            <span className="text-muted-foreground">Average</span>
                            <span className="font-medium">{formatDuration(stats.averageDuration)}</span>
                          </div>
                        </div>
                      </CardContent>
                    </Card>

                    <Card>
                      <CardHeader>
                        <CardTitle className="text-base">Execution Distribution</CardTitle>
                      </CardHeader>
                      <CardContent>
                        <ResponsiveContainer width="100%" height={200}>
                          <BarChart
                            data={[
                              { status: 'Completed', count: stats.completedExecutions, fill: '#10b981' },
                              { status: 'Failed', count: stats.failedExecutions, fill: '#ef4444' },
                              { status: 'Running', count: stats.runningExecutions, fill: '#3b82f6' }
                            ]}
                            layout="vertical"
                          >
                            <XAxis type="number" />
                            <YAxis type="category" dataKey="status" />
                            <Tooltip />
                            <Bar dataKey="count" />
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
                      <Timer className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                      <p className="text-muted-foreground">No performance data available</p>
                    </div>
                  </CardContent>
                </Card>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      {/* Execution Details Modal */}
      {selectedExecution && (
        <div
          className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50"
          onClick={() => setSelectedExecution(null)}
        >
          <div
            className="fixed inset-4 md:inset-auto md:left-1/2 md:top-1/2 md:-translate-x-1/2 md:-translate-y-1/2 md:w-[600px] bg-background border rounded-lg shadow-lg"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="p-6 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold flex items-center gap-2">
                  {getStatusIcon(selectedExecution.status)}
                  Execution Details
                </h3>
                <Badge
                  variant={
                    selectedExecution.status === 'COMPLETED' ? 'default' :
                    selectedExecution.status === 'FAILED' ? 'destructive' :
                    selectedExecution.status === 'RUNNING' ? 'secondary' : 'outline'
                  }
                >
                  {selectedExecution.status}
                </Badge>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-muted-foreground">Execution ID</Label>
                  <p className="font-mono text-sm">{selectedExecution.id}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Flow</Label>
                  <p>{selectedExecution.flowName}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Start Time</Label>
                  <p>{new Date(selectedExecution.startTime).toLocaleString()}</p>
                </div>
                {selectedExecution.endTime && (
                  <div>
                    <Label className="text-muted-foreground">End Time</Label>
                    <p>{new Date(selectedExecution.endTime).toLocaleString()}</p>
                  </div>
                )}
                <div>
                  <Label className="text-muted-foreground">Duration</Label>
                  <p>{formatDuration(selectedExecution.duration)}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Progress</Label>
                  <div className="flex items-center gap-2">
                    <Progress value={selectedExecution.progress} className="flex-1" />
                    <span className="text-sm">{selectedExecution.progress}%</span>
                  </div>
                </div>
              </div>

              <Separator />

              <div>
                <h4 className="font-medium mb-2">Step Progress</h4>
                <div className="grid grid-cols-3 gap-4 text-sm">
                  <div className="text-center">
                    <p className="text-2xl font-bold">{selectedExecution.completedSteps}</p>
                    <p className="text-muted-foreground">Completed</p>
                  </div>
                  <div className="text-center">
                    <p className="text-2xl font-bold">
                      {selectedExecution.totalSteps - selectedExecution.completedSteps - selectedExecution.failedSteps}
                    </p>
                    <p className="text-muted-foreground">Remaining</p>
                  </div>
                  <div className="text-center">
                    <p className="text-2xl font-bold text-red-600">{selectedExecution.failedSteps}</p>
                    <p className="text-muted-foreground">Failed</p>
                  </div>
                </div>
              </div>

              {selectedExecution.currentStep && selectedExecution.status === 'RUNNING' && (
                <>
                  <Separator />
                  <Alert>
                    <RefreshCw className="h-4 w-4 animate-spin" />
                    <AlertTitle>Current Step</AlertTitle>
                    <AlertDescription>{selectedExecution.currentStep}</AlertDescription>
                  </Alert>
                </>
              )}

              {selectedExecution.errorMessage && (
                <>
                  <Separator />
                  <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertTitle>Error</AlertTitle>
                    <AlertDescription>{selectedExecution.errorMessage}</AlertDescription>
                  </Alert>
                </>
              )}

              <div className="flex justify-between pt-4">
                <div className="flex gap-2">
                  {selectedExecution.status === 'RUNNING' && (
                    <>
                      <Button
                        variant="outline"
                        onClick={() => handleExecutionAction(selectedExecution.id, 'pause')}
                      >
                        <Pause className="mr-2 h-4 w-4" />
                        Pause
                      </Button>
                      <Button
                        variant="outline"
                        onClick={() => handleExecutionAction(selectedExecution.id, 'cancel')}
                      >
                        <Square className="mr-2 h-4 w-4" />
                        Cancel
                      </Button>
                    </>
                  )}
                  {selectedExecution.status === 'PAUSED' && (
                    <Button
                      variant="outline"
                      onClick={() => handleExecutionAction(selectedExecution.id, 'resume')}
                    >
                      <Play className="mr-2 h-4 w-4" />
                      Resume
                    </Button>
                  )}
                </div>
                <Button onClick={() => setSelectedExecution(null)}>Close</Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}