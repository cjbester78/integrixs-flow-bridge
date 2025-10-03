import { useState } from 'react';
import { IntegrationFlow } from '@/services/integrationFlowService';
import { Card, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { EmptyState } from '@/components/ui/empty-state';
import { FileText, Database } from 'lucide-react';
import { TimeFilter } from './types/timeFilter';
import { filterIntegrationFlowsByTime, getFilterDescription } from './utils/timeFilters';
import { TimeFilterSelect } from './components/TimeFilterSelect';
import { IntegrationFlowCard } from './components/IntegrationFlowCard';
import { logger, LogCategory } from '@/lib/logger';

interface IntegrationFlowListProps {
 integrationFlows: IntegrationFlow[];
 isBusinessComponentSelected: boolean;
 statusFilter?: string | null;
 loading?: boolean;
 onTimeFilterChange?: (filter: TimeFilter) => void;
}

export const IntegrationFlowList = ({ integrationFlows, isBusinessComponentSelected, statusFilter, loading = false, onTimeFilterChange }: IntegrationFlowListProps) => {
 const [timeFilter, setTimeFilter] = useState<TimeFilter>('today');

 // Handle time filter change
 const handleTimeFilterChange = (newFilter: TimeFilter) => {
 setTimeFilter(newFilter);
 onTimeFilterChange?.(newFilter);
 };

 // Debug logging
 logger.info(LogCategory.UI, '[IntegrationFlowList] Received integration flows:', { data: integrationFlows });
 logger.info(LogCategory.UI, '[IntegrationFlowList] Integration flows type:', { data: typeof integrationFlows });
 logger.info(LogCategory.UI, '[IntegrationFlowList] Is array:', { data: Array.isArray(integrationFlows) });
 if (integrationFlows && integrationFlows.length > 0) {
 logger.info(LogCategory.UI, '[IntegrationFlowList] First integration flow:', { data: integrationFlows[0] });
 logger.info(LogCategory.UI, '[IntegrationFlowList] First integration flow keys:', { data: Object.keys(integrationFlows[0]) });
 logger.info(LogCategory.UI, '[IntegrationFlowList] First integration flow logs:', { data: integrationFlows[0].logs });
 if (integrationFlows[0].logs && integrationFlows[0].logs.length > 0) {
 logger.info(LogCategory.UI, '[IntegrationFlowList] First log entry:', { data: integrationFlows[0].logs[0] });
 logger.info(LogCategory.UI, '[IntegrationFlowList] First log type:', { data: typeof integrationFlows[0].logs[0] });
 logger.info(LogCategory.UI, '[IntegrationFlowList] First log keys:', { data: Object.keys(integrationFlows[0].logs[0]) });
 logger.info(LogCategory.UI, '[IntegrationFlowList] Log timestamp:', { data: integrationFlows[0].logs[0].timestamp });
 logger.info(LogCategory.UI, '[IntegrationFlowList] Log timestamp type:', { data: typeof integrationFlows[0].logs[0].timestamp });
 logger.info(LogCategory.UI, '[IntegrationFlowList] Log level:', { data: integrationFlows[0].logs[0].level });
 logger.info(LogCategory.UI, '[IntegrationFlowList] Log message:', { data: integrationFlows[0].logs[0].message });
 }
 }

 // Apply filters with safe messages array
 let filteredFlows = filterIntegrationFlowsByTime(integrationFlows || [], timeFilter);
 if (statusFilter) {
 filteredFlows = filteredFlows.filter(flow => flow.status === statusFilter);
 }

 // Sort by timestamp (newest first)
 filteredFlows.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

 logger.info(LogCategory.UI, '[IntegrationFlowList] Filtered flows:', { data: filteredFlows });
 logger.info(LogCategory.UI, '[IntegrationFlowList] Filtered flows length:', { data: filteredFlows.length });
 return (
 <div className="space-y-4">
 {/* Filter Controls */}
 <Card className="bg-gradient-secondary border-border/50">
 <CardHeader className="pb-3">
 <div className="flex items-center justify-between">
 <CardTitle className="text-lg font-semibold flex items-center gap-2">
 <FileText className="h-5 w-5" />
 Integration Flow History
 </CardTitle>
 <TimeFilterSelect value={timeFilter} onValueChange={handleTimeFilterChange} />
 </div>
 <p className="text-sm text-muted-foreground">
 {getFilterDescription(timeFilter, filteredFlows.length)}
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
 ) : filteredFlows.length === 0 ? (
 <EmptyState
 icon={Database}
 title="No Integration Flows Found"
 description={statusFilter
 ? `No ${String(statusFilter)} integration flows found for the selected time period.`
 : 'No integration flows found for the selected time period.'
 }
 />
 ) : (
 filteredFlows.map((integrationFlow) => {
 logger.info(LogCategory.UI, '[IntegrationFlowList] Rendering flow:', { flowId: integrationFlow.id, flowType: typeof integrationFlow });
 return <IntegrationFlowCard key={integrationFlow.id} integrationFlow={integrationFlow} />;
 })
 )}
 </div>
 </div>
 );
};
