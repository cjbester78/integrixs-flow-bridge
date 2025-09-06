import { useState } from 'react';
import { Message } from '@/services/messageService';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ChevronDown, ChevronRight, FileText, RefreshCw } from 'lucide-react';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { getStatusIcon, getLogLevelIcon } from './MessageIcons';
import { useToast } from '@/hooks/use-toast';
import { apiClient } from '@/lib/api-client';
import { logger, LogCategory } from '@/lib/logger';

interface MessageCardProps {
 message: Message;
}

export const MessageCard = ({ message }: MessageCardProps) => {
 const [isExpanded, setIsExpanded] = useState(false);
 const [isReprocessing, setIsReprocessing] = useState(false);
 const { toast } = useToast();

 // Debug logging to identify React error source
 logger.debug(LogCategory.UI, '[MessageCard] Rendering message', { 
 messageId: message.id, 
 hasLogs: !!message.logs,
 logsCount: message.logs?.length || 0
 });

 const handleReprocess = async (e: React.MouseEvent) => {
 e.stopPropagation(); // Prevent expanding/collapsing
 setIsReprocessing(true);

 try {
 const response = await apiClient.post(`/messages/${message.id}/reprocess`);
 if (response) {
 toast({
 title: "Success",
 description: "Message reprocessing initiated",
 });
 } else {
 toast({
 title: "Error",
 description: (response && typeof response === 'object' && 'error' in response ? (response as any).error : undefined) || "Failed to reprocess message",
 variant: "destructive",
 });
    }
  } catch (error) {
 toast({
 title: "Error",
 description: "Failed to reprocess message",
 variant: "destructive",
    });
 } finally {
 setIsReprocessing(false);
 }
 };

 return (
 <Card className="bg-gradient-secondary border-border/50 hover-scale">
 <Collapsible open={isExpanded} onOpenChange={setIsExpanded}>
 <CollapsibleTrigger asChild>
 <Button variant="ghost" className="w-full p-0 h-auto hover:bg-transparent">
 <CardHeader className="w-full">
 <div className="flex items-center justify-between">
 <div className="flex items-center gap-3">
 {getStatusIcon(message.status)}
 <div className="text-left">
 <div className="flex items-center gap-2">
 <span className="font-medium text-foreground">{message.id}</span>
 <Badge variant="outline" className="text-xs">
 {message.type}
 </Badge>
 </div>
 <div className="text-sm text-muted-foreground">
 {message.source} → {message.target}
 </div>
 </div>
 </div>
 <div className="flex items-center gap-3 text-right">
 <div className="text-sm">
 <div className="text-muted-foreground">
 {(() => {
 if (typeof message.timestamp === 'string') {
 return message.timestamp;
 } else if (message.timestamp && typeof message.timestamp === 'object') {
 // Handle LocalDateTime object format from backend
 try {
 if (Array.isArray(message.timestamp)) {
 // [year, month, day, hour, minute, second, nano]
 const [year, month, day, hour, minute, second] = message.timestamp as number[];
 return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`;
 }
 return JSON.stringify(message.timestamp);
        } catch (e) {
 logger.error(LogCategory.UI, 'Error formatting timestamp:', { data: message.timestamp, extra: e });
 return 'Invalid timestamp';
 }
 }
 return 'Unknown time';
 })()}
 </div>
 <div className="text-xs text-muted-foreground">
 {message.processingTime} • {message.size}
 </div>
 </div>
 {isExpanded ? (
 <ChevronDown className="h-4 w-4 text-muted-foreground" />
 ) : (
 <ChevronRight className="h-4 w-4 text-muted-foreground" />
 )}
 </div>
 </div>
 </CardHeader>
 </Button>
 </CollapsibleTrigger>

 <CollapsibleContent>
 <CardContent className="pt-0">
 <div className="border-t border-border/50 pt-4">
 <div className="flex items-center justify-between mb-3">
 <h4 className="text-sm font-medium text-foreground flex items-center gap-2">
 <FileText className="h-4 w-4" />
 Processing Steps ({message.logs ? message.logs.length : 0})
 </h4>
 {message.status === 'failed' && (
 <Button
 size="sm"
 variant="outline"
 onClick={handleReprocess}
 disabled={isReprocessing}
 >
 <RefreshCw className={`h-4 w-4 mr-2 ${isReprocessing ? 'animate-spin' : ''}`} />
 {isReprocessing ? 'Reprocessing...' : 'Retry Message'}
 </Button>
 )}
 </div>
 <div className="space-y-2 max-h-64 overflow-y-auto">
 {message.logs && message.logs.length > 0 ? (
 message.logs.map((log, index) => {
 logger.debug(LogCategory.UI, '[MessageCard] Rendering log', { index, logLevel: log.level });
 return (
 <div key={index} className="flex items-start gap-3 text-xs">
 {getLogLevelIcon(log.level)}
 <span className="text-muted-foreground min-w-[120px] font-mono">
 {(() => {
 try {
 if (!log.timestamp) {
                              return 'No timestamp';
                            }
 if (typeof log.timestamp === 'string') {
 if (log.timestamp.includes(' ')) {
 return log.timestamp.split(' ')[1];
 } else if (log.timestamp.includes('T')) {
 return new Date(log.timestamp).toLocaleTimeString();
 }
 return log.timestamp;
 } else if (typeof log.timestamp === 'object' && log.timestamp !== null && log.timestamp instanceof Date) {
 return log.timestamp.toLocaleTimeString();
 } else if (log.timestamp && typeof log.timestamp === 'object') {
 // Handle cases where timestamp might be a complex object
 return JSON.stringify(log.timestamp);
 }
 return 'Invalid timestamp';
        } catch (e) {
 logger.error(LogCategory.UI, 'Error formatting log timestamp:', { data: log.timestamp, extra: e });
 return 'Invalid timestamp';
 }
 })()}
 </span>
 <span className="text-foreground">{String(log.message || '')}</span>
 </div>
 );
 })
 ) : (
 <div className="text-sm text-muted-foreground">No processing steps available</div>
 )}
 </div>
 </div>
 </CardContent>
 </CollapsibleContent>
 </Collapsible>
 </Card>
 );
};
