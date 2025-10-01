import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Separator } from '@/components/ui/separator';
import {
 Search,
 Filter,
 Download,
 RefreshCw,
 Clock,
 AlertCircle,
 Info,
 AlertTriangle,
 Bug,
 ChevronDown,
 ChevronRight
} from 'lucide-react';
import { AdapterLog, LogLevel } from '@/services/adapterMonitoringService';
import { formatDistanceToNow } from 'date-fns';
import { logger, LogCategory } from '@/lib/logger';

interface AdapterLogViewerProps {
 adapterId: string;
 adapterName: string;
 logs: AdapterLog[];
 loading?: boolean;
 onRefresh?: () => void;
 onExport?: () => void;
 realTimeEnabled?: boolean;
}

export const AdapterLogViewer = ({
 adapterId: _adapterId,
 adapterName: _adapterName,
 logs,
 loading = false,
 onRefresh,
 onExport,
 realTimeEnabled = false
}: AdapterLogViewerProps) => {
 const [searchTerm, setSearchTerm] = useState('');
 const [levelFilter, setLevelFilter] = useState<string>('all');
 const [expandedLogs, setExpandedLogs] = useState<Set<string>>(new Set());
 const [filteredLogs, setFilteredLogs] = useState<AdapterLog[]>([]);

 logger.info(LogCategory.UI, `[AdapterLogViewer] Props: { adapterId: ${_adapterId}, adapterName: ${_adapterName}, logs, loading }`);
 useEffect(() => {
 logger.info(LogCategory.UI, `[AdapterLogViewer] Effect running - logs: { data: logs, searchTerm, levelFilter }`);
 let filtered = logs;
;
 // Filter by search term
 if (searchTerm) {
 filtered = filtered.filter(log =>
 log.message.toLowerCase().includes(searchTerm.toLowerCase()) ||
 log.adapterName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
 log.correlationId?.toLowerCase().includes(searchTerm.toLowerCase())
 );
 }

 // Filter by level
 if (levelFilter !== 'all') {
 filtered = filtered.filter(log => log.level === levelFilter);
 }

 logger.info(LogCategory.UI, '[AdapterLogViewer] Filtered logs:', { data: filtered });
 setFilteredLogs(filtered);
 }, [logs, searchTerm, levelFilter]);

 const getLogIcon = (level: LogLevel) => {
 switch (level) {
 case 'error':;
 return <AlertCircle className="h-4 w-4 text-destructive" />;
 case 'warn':
 return <AlertTriangle className="h-4 w-4 text-warning" />;
 case 'info':
 return <Info className="h-4 w-4 text-info" />;
 case 'debug':
 return <Bug className="h-4 w-4 text-muted-foreground" />;
 default:
 return <Info className="h-4 w-4 text-info" />
 }
};

 const getLogBadgeVariant = (level: LogLevel) => {
 switch (level) {
 case 'error':;
 return 'destructive' as const;
 case 'warn':
 return 'secondary' as const;
 case 'info':
 return 'default' as const;
 case 'debug':
 return 'outline' as const;
 default:
 return 'default' as const
 }
};

 const toggleLogExpansion = (logId: string) => {
 setExpandedLogs(prev => {
 const newSet = new Set(prev);
 if (newSet.has(logId)) {
 newSet.delete(logId);
 } else {
 newSet.add(logId);
 }
 return newSet;
 })
 };

 const formatLogTime = (timestamp: string) => {
 const date = new Date(timestamp);
 return {
 time: date.toLocaleTimeString(),
 relative: formatDistanceToNow(date, { addSuffix: true })
 }
};

 const getLogLevelCounts = () => {
 const counts = { error: 0, warn: 0, info: 0, debug: 0 };
 logs.forEach(log => counts[log.level as keyof typeof counts]++);
 return counts;
 };

 const levelCounts = getLogLevelCounts();
;
 return (
 <Card className="bg-gradient-secondary border-border/50">
 <CardHeader>
 <div className="flex items-center justify-between">
 <div>
 <CardTitle className="flex items-center gap-2">
 <Clock className="h-5 w-5" />
 Channel Logs;
 {realTimeEnabled && (
 <Badge variant="secondary" className="animate-pulse">
 Live
 </Badge>
 )}
 </CardTitle>
 <CardDescription>
 Real-time activity and execution logs for {_adapterName || 'adapter'}
 </CardDescription>
 </div>
 <div className="flex items-center gap-2">
 {onExport && (
 <Button variant="outline" size="sm" onClick={onExport}>
 <Download className="h-4 w-4 mr-2" />
 Export
 </Button>
 )}
 {onRefresh && (
 <Button variant="outline" size="sm" onClick={onRefresh} disabled={loading}>
 <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
 Refresh
 </Button>
 )}
 </div>
 </div>

 {/* Log Level Summary */}
 <div className="flex items-center gap-4 pt-2">
 <div className="flex items-center gap-2">
 <Badge variant="destructive" className="text-xs">
 {levelCounts.error} Errors
 </Badge>
 <Badge variant="secondary" className="text-xs">
 {levelCounts.warn} Warnings
 </Badge>
 <Badge variant="default" className="text-xs">
 {levelCounts.info} Info
 </Badge>
 <Badge variant="outline" className="text-xs">
 {levelCounts.debug} Debug
 </Badge>
 </div>
 </div>

 {/* Filters */}
 <div className="flex items-center gap-4 pt-4">
 <div className="flex-1">
 <div className="relative">
 <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
 <Input
 placeholder="Search logs..."
 value={searchTerm}
 onChange={(e) => setSearchTerm(e.target.value)}
 className="pl-10"
 />
 </div>
 </div>
 <Select value={levelFilter} onValueChange={(value) => setLevelFilter(value as LogLevel | 'all')}>
 <SelectTrigger className="w-32">
 <Filter className="h-4 w-4 mr-2" />
 <SelectValue />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="all">All Levels</SelectItem>
 <SelectItem value="error">Error</SelectItem>
 <SelectItem value="warn">Warning</SelectItem>
 <SelectItem value="info">Info</SelectItem>
 <SelectItem value="debug">Debug</SelectItem>
 </SelectContent>
 </Select>
 </div>
 </CardHeader>

 <CardContent>
 <ScrollArea className="h-96">
 <div className="space-y-2">
 {filteredLogs.length === 0 ? (
 <div className="text-center py-8 text-muted-foreground">
 {searchTerm || levelFilter !== 'all'
 ? 'No logs match the current filters'
 : 'No logs available'
 }
 </div>
 ) : (
 filteredLogs.map((log) => {
 const isExpanded = expandedLogs.has(log.id);
 const timeInfo = formatLogTime(log.timestamp);
;
 return (
 <div key={log.id} className="border border-border/50 rounded-lg p-3 bg-background/50">
 <div className="flex items-start justify-between">
 <div className="flex items-start gap-3 flex-1">
 {getLogIcon(log.level)}
 <div className="flex-1 min-w-0">
 <div className="flex items-center gap-2 mb-1">
 <Badge variant={getLogBadgeVariant(log.level)} className="text-xs">
 {(log.level || 'info').toUpperCase()}
 </Badge>
 {log.adapterName && (
 <Badge variant="outline" className="text-xs">
 {log.adapterName}
 </Badge>
 )}
 {log.correlationId && (
 <code className="text-xs text-muted-foreground bg-muted px-1 rounded">
 {log.correlationId}
 </code>
 )}
 {log.duration && (
 <span className="text-xs text-muted-foreground">
 {log.duration}ms
 </span>
 )}
 </div>
 <p className="text-sm text-foreground mb-1">
 {log.message}
 </p>
 <div className="flex items-center gap-4 text-xs text-muted-foreground">
 <span>{timeInfo.time}</span>
 <span>{timeInfo.relative}</span>
 </div>
 </div>
 </div>
 {log.details && (
 <Button
 variant="ghost"
 size="sm"
 onClick={() => toggleLogExpansion(log.id)}
 className="ml-2"
 >
 {isExpanded ? (
 <ChevronDown className="h-4 w-4" />
 ) : (
 <ChevronRight className="h-4 w-4" />
 )}
 </Button>
 )}
 </div>

 {isExpanded && log.details && (
 <>
 <Separator className="my-2" />
 <div className="bg-muted/50 rounded p-2">
 {log.details.processingSteps && Array.isArray(log.details.processingSteps) ? (
 <div className="space-y-2">
 <div className="text-xs font-medium text-muted-foreground mb-2">Processing Steps:</div>
 {log.details.processingSteps.map((step: any, index: number) => (
 <div key={index} className="border-l-2 border-border/50 pl-3 py-1">
 <div className="flex items-start gap-2">
 {step.level === 'ERROR' && <AlertCircle className="h-3 w-3 text-destructive mt-0.5" />}
 {step.level === 'WARN' && <AlertTriangle className="h-3 w-3 text-warning mt-0.5" />}
 {step.level === 'INFO' && <Info className="h-3 w-3 text-info mt-0.5" />}
 <div className="flex-1">
 <div className="text-xs font-medium">{step.step}</div>
 {step.details && (
 <div className="text-xs text-muted-foreground mt-0.5">{step.details}</div>
 )}
 {step.timestamp && (
 <div className="text-xs text-muted-foreground mt-0.5">
 {new Date(step.timestamp).toLocaleTimeString()}
 </div>
 )}
 </div>
 </div>
 </div>
 ))}
 </div>
 ) : (
 <pre className="text-xs overflow-x-auto">
 {typeof log.details === 'string'
 ? log.details
 : JSON.stringify(log.details, null, 2)
 }
 </pre>
 )}
 </div>
 </>
 )}
 </div>
 );
 })
 )}
 </div>
 </ScrollArea>
 </CardContent>
 </Card>
 );
};