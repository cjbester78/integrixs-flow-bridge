import { useState } from 'react';
import { Message } from '@/services/messageService';
import { Card, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { EmptyState } from '@/components/ui/empty-state';
import { FileText, Database } from 'lucide-react';
import { TimeFilter } from './types/timeFilter';
import { filterMessagesByTime, getFilterDescription } from './utils/timeFilters';
import { TimeFilterSelect } from './components/TimeFilterSelect';
import { MessageCard } from './components/MessageCard';
import { logger, LogCategory } from '@/lib/logger';

interface MessageListProps {
 messages: Message[];
 isBusinessComponentSelected: boolean;
 statusFilter?: string | null;
 loading?: boolean;
 onTimeFilterChange?: (filter: TimeFilter) => void;
}

export const MessageList = ({ messages, isBusinessComponentSelected, statusFilter, loading = false, onTimeFilterChange }: MessageListProps) => {
 const [timeFilter, setTimeFilter] = useState<TimeFilter>('today');

 // Handle time filter change
 const handleTimeFilterChange = (newFilter: TimeFilter) => {
 setTimeFilter(newFilter);
 onTimeFilterChange?.(newFilter);
 };

 // Debug logging
 logger.info(LogCategory.UI, '[MessageList] Received messages:', { data: messages });
 logger.info(LogCategory.UI, '[MessageList] Messages type:', { data: typeof messages });
 logger.info(LogCategory.UI, '[MessageList] Is array:', { data: Array.isArray(messages) });
 if (messages && messages.length > 0) {
 logger.info(LogCategory.UI, '[MessageList] First message:', { data: messages[0] });
 logger.info(LogCategory.UI, '[MessageList] First message keys:', { data: Object.keys(messages[0]) });
 logger.info(LogCategory.UI, '[MessageList] First message logs:', { data: messages[0].logs });
 if (messages[0].logs && messages[0].logs.length > 0) {
 logger.info(LogCategory.UI, '[MessageList] First log entry:', { data: messages[0].logs[0] });
 logger.info(LogCategory.UI, '[MessageList] First log type:', { data: typeof messages[0].logs[0] });
 logger.info(LogCategory.UI, '[MessageList] First log keys:', { data: Object.keys(messages[0].logs[0]) });
 logger.info(LogCategory.UI, '[MessageList] Log timestamp:', { data: messages[0].logs[0].timestamp });
 logger.info(LogCategory.UI, '[MessageList] Log timestamp type:', { data: typeof messages[0].logs[0].timestamp });
 logger.info(LogCategory.UI, '[MessageList] Log level:', { data: messages[0].logs[0].level });
 logger.info(LogCategory.UI, '[MessageList] Log message:', { data: messages[0].logs[0].message });
 }
 }

 // Apply filters with safe messages array
 let filteredMessages = filterMessagesByTime(messages || [], timeFilter);
 if (statusFilter) {
 filteredMessages = filteredMessages.filter(msg => msg.status === statusFilter);
 }

 // Sort by timestamp (newest first)
 filteredMessages.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

 logger.info(LogCategory.UI, '[MessageList] Filtered messages:', { data: filteredMessages });
 logger.info(LogCategory.UI, '[MessageList] Filtered messages length:', { data: filteredMessages.length });
 return (
 <div className="space-y-4">
 {/* Filter Controls */}
 <Card className="bg-gradient-secondary border-border/50">
 <CardHeader className="pb-3">
 <div className="flex items-center justify-between">
 <CardTitle className="text-lg font-semibold flex items-center gap-2">
 <FileText className="h-5 w-5" />
 Message History
 </CardTitle>
 <TimeFilterSelect value={timeFilter} onValueChange={handleTimeFilterChange} />
 </div>
 <p className="text-sm text-muted-foreground">
 {getFilterDescription(timeFilter, filteredMessages.length)}
 </p>
 </CardHeader>
 </Card>

 {/* Message List */}
 <div className="space-y-3">
 {loading ? (
 // Loading skeleton
 Array.from({ length: 5 }).map((_, i) => (
 <Card key={i} className="bg-gradient-secondary border-border/50">
 <CardHeader>
 <div className="flex items-center justify-between">
 <div className="flex items-center gap-3">
 <Skeleton className="h-6 w-6 rounded-full" />
 <div>
 <Skeleton className="h-4 w-32 mb-2" />
 <Skeleton className="h-3 w-48" />
 </div>
 </div>
 <div>
 <Skeleton className="h-3 w-24 mb-1" />
 <Skeleton className="h-3 w-16" />
 </div>
 </div>
 </CardHeader>
 </Card>
 ))
 ) : filteredMessages.length === 0 ? (
 <EmptyState
 icon={Database}
 title="No Messages Found"
 description={statusFilter
 ? `No ${String(statusFilter)} messages found for the selected time period.`
 : 'No messages found for the selected time period.'
 }
 />
 ) : (
 filteredMessages.map((message) => {
 logger.info(LogCategory.UI, '[MessageList] Rendering message:', { messageId: message.id, messageType: typeof message });
 return <MessageCard key={message.id} message={message} />;
 })
 )}
 </div>
 </div>
 );
};
