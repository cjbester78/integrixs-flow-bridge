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
  console.log('[MessageList] Received messages:', messages);
  console.log('[MessageList] Messages type:', typeof messages);
  console.log('[MessageList] Is array:', Array.isArray(messages));
  if (messages && messages.length > 0) {
    console.log('[MessageList] First message:', messages[0]);
    console.log('[MessageList] First message keys:', Object.keys(messages[0]));
    console.log('[MessageList] First message logs:', messages[0].logs);
    if (messages[0].logs && messages[0].logs.length > 0) {
      console.log('[MessageList] First log entry:', messages[0].logs[0]);
      console.log('[MessageList] First log type:', typeof messages[0].logs[0]);
      console.log('[MessageList] First log keys:', Object.keys(messages[0].logs[0]));
      console.log('[MessageList] Log timestamp:', messages[0].logs[0].timestamp);
      console.log('[MessageList] Log timestamp type:', typeof messages[0].logs[0].timestamp);
      console.log('[MessageList] Log level:', messages[0].logs[0].level);
      console.log('[MessageList] Log message:', messages[0].logs[0].message);
    }
  }

  // Apply filters with safe messages array
  let filteredMessages = filterMessagesByTime(messages || [], timeFilter);
  
  if (statusFilter) {
    filteredMessages = filteredMessages.filter(msg => msg.status === statusFilter);
  }

  // Sort by timestamp (newest first)
  filteredMessages.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
  
  console.log('[MessageList] Filtered messages:', filteredMessages);
  console.log('[MessageList] Filtered messages length:', filteredMessages.length);

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
            {console.log('[MessageList] TimeFilter value:', timeFilter, 'Type:', typeof timeFilter)}
          </div>
          <p className="text-sm text-muted-foreground">
            {(() => {
              const desc = getFilterDescription(timeFilter, filteredMessages.length);
              console.log('[MessageList] Filter description:', desc, 'Type:', typeof desc);
              return desc;
            })()}
          </p>
        </CardHeader>
      </Card>

      {/* Message List */}
      <div className="space-y-3">
        {console.log('[MessageList] About to render messages, loading:', loading, 'filteredMessages.length:', filteredMessages.length)}
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
            console.log('[MessageList] Rendering message:', message.id, 'Type:', typeof message);
            return <MessageCard key={message.id} message={message} />;
          })
        )}
      </div>
    </div>
  );
};