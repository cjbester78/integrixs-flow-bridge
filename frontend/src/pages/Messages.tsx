import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { PageContainer } from '@/components/ui/page-container';
import { PageHeader } from '@/components/common/PageHeader';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';
import { useMessageMonitoring } from '@/hooks/useMessageMonitoring';
import { BusinessComponent } from '@/types/businessComponent';
import { MessageSquare, RefreshCw, Wifi, WifiOff } from 'lucide-react';
import { MessageStats } from '@/components/messages/MessageStats';
import { MessageList } from '@/components/messages/MessageList';
import { BusinessComponentFilter } from '@/components/adapters/BusinessComponentFilter';
import { useDocumentTitle } from '@/hooks/useDocumentTitle';
import { useMetaDescription } from '@/hooks/useMetaDescription';

export const Messages = () => {
 useDocumentTitle('Messages');
 useMetaDescription('Track and monitor all integration messages, view processing status, and analyze message flow through your Integrix Flow Bridge system.');
 const [selectedBusinessComponent, setSelectedBusinessComponent] = useState<BusinessComponent | null>(null);
 const [statusFilter, setStatusFilter] = useState<string | null>(null);
 const [timeFilter, setTimeFilter] = useState<string>('today');
 const { businessComponents, loading } = useBusinessComponentAdapters();
 const {
 messages,
 stats,
 loading: messagesLoading,
 connected,
 refreshData,
 loadStats
 } = useMessageMonitoring(selectedBusinessComponent?.id);

 // Messages are already filtered by the backend based on our filters
 const displayMessages = messages;
 const handleRefresh = () => {
 const filters: any = {};

 // Add status filter
 if (statusFilter) {
 filters.status = [statusFilter];
 }

 // Add time filter as date range
 const dateRange = getDateRangeFromTimeFilter(timeFilter);
 if (dateRange.dateFrom) {
 filters.dateFrom = dateRange.dateFrom;
 filters.dateTo = dateRange.dateTo;
 }

 refreshData(filters);
 };

 const handleStatusFilter = (status: string) => {
 setStatusFilter(statusFilter === status ? null : status);
 };

 const handleBusinessComponentChange = (businessComponent: BusinessComponent | null) => {
 setSelectedBusinessComponent(businessComponent);
 setStatusFilter(null); // Reset status filter when changing business component
 };

 // Convert time filter to date range
 const getDateRangeFromTimeFilter = (filter: string) => {
 const now = new Date();
 const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
 switch (filter) {
 case 'today':
 return { dateFrom: today.toISOString(), dateTo: now.toISOString() };
 case 'yesterday': {
 const yesterday = new Date(today);
 yesterday.setDate(yesterday.getDate() - 1);
 return { dateFrom: yesterday.toISOString(), dateTo: today.toISOString() };
 }
 case 'last7days': {
 const last7days = new Date(today);
 last7days.setDate(last7days.getDate() - 7);
 return { dateFrom: last7days.toISOString(), dateTo: now.toISOString() };
 }
 case 'last30days': {
 const last30days = new Date(today);
 last30days.setDate(last30days.getDate() - 30);
 return { dateFrom: last30days.toISOString(), dateTo: now.toISOString() };
 }
 case 'all':
 default:
 return {};
 }
 };

 // Reload data when filters change
 useEffect(() => {
 const filters: any = {};

 // Add status filter
 if (statusFilter) {
 filters.status = [statusFilter];
 }

 // Add time filter as date range
 const dateRange = getDateRangeFromTimeFilter(timeFilter);
 if (dateRange.dateFrom) {
 filters.dateFrom = dateRange.dateFrom;
 filters.dateTo = dateRange.dateTo;
 }

 // Refresh both messages and stats with the same filters
 refreshData(filters);
 }, [statusFilter, timeFilter, refreshData]);

 return (
 <PageContainer>
 <PageHeader
 title="Message Monitor"
 description="Track and analyze integration message flows"
 icon={<MessageSquare />}
 actions={
 <div className="flex items-center gap-3">
 <Badge variant={connected ? "success" : "destructive"} className="flex items-center gap-1">
 {connected ? <Wifi className="h-3 w-3" /> : <WifiOff className="h-3 w-3" />}
 {connected ? 'Live' : 'Disconnected'}
 </Badge>
 <Button
 variant="outline"
 size="sm"
 onClick={handleRefresh}
 disabled={messagesLoading}
 >
 <RefreshCw className={`h-4 w-4 mr-2 ${messagesLoading ? 'animate-spin' : ''}`} />
 Refresh
 </Button>
 </div>
 }
 />

 {/* Overview Stats */}
 <MessageStats
 messages={displayMessages}
 stats={stats}
 isBusinessComponentSelected={!!selectedBusinessComponent}
 onStatusFilter={handleStatusFilter}
 statusFilter={statusFilter}
 loading={messagesLoading}
 />

 {/* Business Component Selection */}
 <BusinessComponentFilter
 selectedBusinessComponent={selectedBusinessComponent}
 businessComponents={businessComponents}
 loading={loading}
 onBusinessComponentChange={handleBusinessComponentChange}
 />

 {/* Message List */}
 <MessageList
 messages={displayMessages}
 isBusinessComponentSelected={!!selectedBusinessComponent}
 statusFilter={statusFilter}
 loading={messagesLoading}
 onTimeFilterChange={setTimeFilter}
 />
 </PageContainer>
 );
}