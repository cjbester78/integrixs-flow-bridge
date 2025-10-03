import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { IntegrationFlow } from '@/services/integrationFlowService';
import { IntegrationFlowStats as IntegrationFlowStatsType } from '@/services/integrationFlowService';

interface IntegrationFlowStatsProps {
 integrationFlows: IntegrationFlow[];
 stats?: IntegrationFlowStatsType | null;
 isBusinessComponentSelected: boolean;
 onStatusFilter?: (status: string) => void;
 statusFilter?: string | null;
 loading?: boolean;
}

export const IntegrationFlowStats = ({
 integrationFlows,
 stats,
 isBusinessComponentSelected,
 onStatusFilter,
 statusFilter,
 loading = false
}: IntegrationFlowStatsProps) => {
 // Ensure messages is an array
 const safeIntegrationFlows = integrationFlows || [];
;
 // Use API stats if available, otherwise calculate from messages
 const successfulFlows = stats?.successful ?? safeIntegrationFlows.filter(flow => flow.status === 'success').length;
 const processingFlows = stats?.processing ?? safeIntegrationFlows.filter(flow => flow.status === 'processing').length;
 const failedFlows = stats?.failed ?? safeIntegrationFlows.filter(flow => flow.status === 'failed').length;
 const totalFlows = stats?.total ?? safeIntegrationFlows.length;
 const successRate = stats?.successRate ?? (totalFlows > 0 ? ((successfulFlows / totalFlows) * 100) : 0);
 const avgProcessingTime = stats?.avgProcessingTime ?? (() => {
 const completedFlows = safeIntegrationFlows.filter(flow => flow.status !== 'processing' && flow.processingTime !== '-');
 return completedFlows.length > 0
 ? Math.round(completedFlows.reduce((sum, flow) => {
 const time = parseFloat(flow.processingTime.replace(/[^\d.]/g, ''));
 return sum + (isNaN(time) ? 0 : time);
 }, 0) / completedFlows.length)
 : 0;
 })();

 const getSubtext = () =>
 isBusinessComponentSelected
 ? `of ${totalFlows} business component flows`
 : 'across all integration flows';

 const handleCardClick = (status: string) => {
 if (onStatusFilter) {
 onStatusFilter(status);
 }
 };

 if (loading) {
 return (
 <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
 {Array.from({ length: 5 }).map((_, i) => (
 <Card key={i} className="bg-gradient-secondary border-border/50">
 <CardHeader className="pb-2">
 <Skeleton className="h-4 w-32" />
 </CardHeader>
 <CardContent>
 <Skeleton className="h-8 w-16 mb-2" />
 <Skeleton className="h-3 w-24" />
 </CardContent>
 </Card>
 ))}
 </div>
 );
 }

 return (
 <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
 <Card
 className={`bg-gradient-secondary border-border/50 cursor-pointer transition-all hover-scale animate-scale-in ${
 statusFilter === 'success' ? 'ring-2 ring-success' : ''
 }`}
 onClick={() => handleCardClick('success')}
 >
 <CardHeader className="pb-2">
 <CardTitle className="text-sm font-medium text-muted-foreground">Successful Flows</CardTitle>
 </CardHeader>
 <CardContent>
 <div className="text-2xl font-bold text-success">{String(successfulFlows)}</div>
 <p className="text-xs text-muted-foreground">{getSubtext()}</p>
 </CardContent>
 </Card>

 <Card
 className={`bg-gradient-secondary border-border/50 cursor-pointer transition-all hover-scale animate-scale-in ${
 statusFilter === 'processing' ? 'ring-2 ring-warning' : ''
 }`}
 onClick={() => handleCardClick('processing')}
 >
 <CardHeader className="pb-2">
 <CardTitle className="text-sm font-medium text-muted-foreground">Processing Flows</CardTitle>
 </CardHeader>
 <CardContent>
 <div className="text-2xl font-bold text-warning">{String(processingFlows)}</div>
 <p className="text-xs text-muted-foreground">{getSubtext()}</p>
 </CardContent>
 </Card>

 <Card
 className={`bg-gradient-secondary border-border/50 cursor-pointer transition-all hover-scale animate-scale-in ${
 statusFilter === 'failed' ? 'ring-2 ring-destructive' : ''
 }`}
 onClick={() => handleCardClick('failed')}
 >
 <CardHeader className="pb-2">
 <CardTitle className="text-sm font-medium text-muted-foreground">Failed Flows</CardTitle>
 </CardHeader>
 <CardContent>
 <div className="text-2xl font-bold text-destructive">{String(failedFlows)}</div>
 <p className="text-xs text-muted-foreground">{getSubtext()}</p>
 </CardContent>
 </Card>

 <Card className="bg-gradient-secondary border-border/50 animate-scale-in">
 <CardHeader className="pb-2">
 <CardTitle className="text-sm font-medium text-muted-foreground">Success Rate</CardTitle>
 </CardHeader>
 <CardContent>
 <div className="text-2xl font-bold text-success">{successRate ? successRate.toFixed(1) : '0'}%</div>
 <p className="text-xs text-muted-foreground">{getSubtext()}</p>
 </CardContent>
 </Card>

 <Card className="bg-gradient-secondary border-border/50 animate-scale-in">
 <CardHeader className="pb-2">
 <CardTitle className="text-sm font-medium text-muted-foreground">Avg Processing</CardTitle>
 </CardHeader>
 <CardContent>
 <div className="text-2xl font-bold text-info">{String(avgProcessingTime)}ms</div>
 <p className="text-xs text-muted-foreground">{getSubtext()}</p>
 </CardContent>
 </Card>
 </div>
 );
};
