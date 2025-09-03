import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { DataTable } from '@/components/ui/data-table';
import { Badge } from '@/components/ui/badge';
import {
 Plus,
 RefreshCw,
 Shield,
 Key,
 UserCheck,
 ShieldAlert,
 MoreVertical,
 Edit,
 Trash2,
 ToggleLeft,
 ToggleRight,
 TestTube
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { externalAuthService } from '@/services/externalAuthService';
import { AuthType, ExternalAuthConfig } from '@/types/externalAuth';
import { CreateExternalAuthDialog } from './CreateExternalAuthDialog';
import { EditExternalAuthDialog } from './EditExternalAuthDialog';
import { AuthAttemptLogsDialog } from './AuthAttemptLogsDialog';
import { format } from 'date-fns';
import { DataTableColumn, DataTableAction } from '@/components/ui/data-table';
import { Skeleton } from '@/components/ui/skeleton';
import { logger, LogCategory } from '@/lib/logger';

interface ExternalAuthManagementProps {
 authConfigs: ExternalAuthConfig[];
 isLoading: boolean;
 onRefresh: () => void;
}

export function ExternalAuthManagement({ authConfigs, isLoading, onRefresh }: ExternalAuthManagementProps) {
 const { toast } = useToast();
 const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
 const [editingConfig, setEditingConfig] = useState<ExternalAuthConfig | null>(null);
 const [viewingLogsFor, setViewingLogsFor] = useState<string | null>(null);
 const [isDeleting, setIsDeleting] = useState<string | null>(null);

 const getAuthTypeIcon = (authType: AuthType) => {
 switch (authType) {
 case AuthType.BASIC:
 return <UserCheck className="h-4 w-4" />;
 case AuthType.API_KEY:
 return <Key className="h-4 w-4" />;
 case AuthType.OAUTH2:
 return <Shield className="h-4 w-4" />;
 case AuthType.OAUTH1:
 return <ShieldAlert className="h-4 w-4" />;
 default:
 return <Shield className="h-4 w-4" />;
 }
 };

 const getAuthTypeBadgeVariant = (authType: AuthType): "default" | "secondary" | "destructive" | "outline" => {
 switch (authType) {
 case AuthType.BASIC:
 return "secondary";
 case AuthType.API_KEY:
 return "outline";
 case AuthType.OAUTH2:
 return "default";
 case AuthType.OAUTH1:
 return "destructive";
 default:
 return "default";
 }
 };

 const handleToggleStatus = async (config: ExternalAuthConfig) => {
 try {
 const response = await externalAuthService.updateAuthConfig(config.id, {
 isActive: !config.isActive
 });

 if (response && typeof response === 'object' && 'success' in response && response.success) {
 toast({
 title: "Success",
 description: `Authentication configuration ${config.isActive ? 'deactivated' : 'activated'} successfully`
 });
 onRefresh();
 } else {
 throw new Error((response && typeof response === 'object' && 'message' in response ? response.message as string : undefined) || 'Failed to update configuration');}
} catch (error) {
 logger.error(LogCategory.AUTH, 'Error toggling auth config status', { error: error });
 toast({
 title: "Error",
 description: "Failed to update authentication status",
 variant: "destructive"
 });
 }
 };

 const handleDelete = async (config: ExternalAuthConfig) => {
 if (!confirm(`Are you sure you want to delete "${config.name}"? This action cannot be undone.`)) {
 return;
 }

 setIsDeleting(config.id);
 try {
 const response = await externalAuthService.deleteAuthConfig(config.id);
 if (response && typeof response === 'object' && 'success' in response && response.success) {
 toast({
 title: "Success",
 description: "Authentication configuration deleted successfully"
 });
 onRefresh();
 } else {
 throw new Error((response && typeof response === 'object' && 'message' in response ? response.message as string : undefined) || 'Failed to delete configuration');}
} catch (error) {
 logger.error(LogCategory.AUTH, 'Error deleting auth config', { error: error });
 toast({
 title: "Error",
 description: "Failed to delete authentication configuration",
 variant: "destructive"
 });
 } finally {
 setIsDeleting(null);
 }
 };

 const handleTest = async (config: ExternalAuthConfig) => {
 try {
 const response = await externalAuthService.testAuthConfig(config.id);
 if (response && typeof response === 'object' && 'success' in response && response.success) {
 toast({
 title: "Success",
 description: "Authentication test completed successfully"
 });
 } else {
 throw new Error((response && typeof response === 'object' && 'message' in response ? response.message as string : undefined) || 'Test failed');}
} catch (error) {
 logger.error(LogCategory.AUTH, 'Error testing auth config', { error: error });
 toast({
 title: "Error",
 description: "Authentication test failed",
 variant: "destructive"
 });
 }
 };

 const columns: DataTableColumn<ExternalAuthConfig>[] = [
 {
 key: 'name',
 header: 'Name',
 cell: (config) => (
 <div className="flex items-center gap-2">
 {getAuthTypeIcon(config.authType)}
 <span className="font-medium">{config.name}</span>
 </div>
 ),
 },
 {
 key: 'authType',
 header: 'Type',
 cell: (config) => (
 <Badge variant={getAuthTypeBadgeVariant(config.authType)}>
 {config.authType}
 </Badge>
 ),
 },
 {
 key: 'description',
 header: 'Description',
 cell: (config) => (
 <span className="text-sm text-muted-foreground">
 {config.description || '-'}
 </span>
 ),
 },
 {
 key: 'isActive',
 header: 'Status',
 cell: (config) => (
 <Badge variant={config.isActive ? 'default' : 'secondary'}>
 {config.isActive ? 'Active' : 'Inactive'}
 </Badge>
 ),
 },
 {
 key: 'createdAt',
 header: 'Created',
 cell: (config) => (
 <span className="text-sm text-muted-foreground">
 {format(new Date(config.createdAt), 'MMM d, yyyy')}
 </span>
 ),
 },
 ];

 const actions: DataTableAction<ExternalAuthConfig>[] = [
 {
 label: 'Edit',
 icon: Edit,
 onClick: (config) => setEditingConfig(config),
 },
 {
 label: 'Toggle Status',
 icon: ToggleRight,
 onClick: (config) => handleToggleStatus(config),
 },
 {
 label: 'Test Connection',
 icon: TestTube,
 onClick: (config) => handleTest(config),
 },
 {
 label: 'View Attempts',
 icon: Shield,
 onClick: (config) => setViewingLogsFor(config.id),
 },
 {
 label: 'Delete',
 icon: Trash2,
 onClick: (config) => handleDelete(config),
 variant: 'destructive' as const,
 },
 ];

 if (isLoading) {
 return (
 <Card>
 <CardHeader>
 <CardTitle>External Authentication</CardTitle>
 <CardDescription>Loading authentication configurations...</CardDescription>
 </CardHeader>
 <CardContent>
 <div className="space-y-4">
 <Skeleton className="h-10 w-full" />
 <Skeleton className="h-32 w-full" />
 </div>
 </CardContent>
 </Card>
 );
 }

 return (
 <>
 <Card>
 <CardHeader>
 <div className="flex items-center justify-between">
 <div>
 <CardTitle>External Authentication</CardTitle>
 <CardDescription>
 Manage authentication configurations for HTTP/S adapters
 </CardDescription>
 </div>
 <div className="flex items-center gap-2">
 <Button variant="outline" size="sm" onClick={onRefresh}>
 <RefreshCw className="h-4 w-4 mr-2" />
 Refresh
 </Button>
 <Button size="sm" onClick={() => setIsCreateDialogOpen(true)}>
 <Plus className="h-4 w-4 mr-2" />
 Add Authentication
 </Button>
 </div>
 </div>
 </CardHeader>
 <CardContent>
 <DataTable
 columns={columns}
 data={authConfigs}
 actions={actions}
 keyField="id"
 searchable={true}
 searchPlaceholder="Search configurations..."
 />
 </CardContent>
 </Card>

 <CreateExternalAuthDialog
 open={isCreateDialogOpen}
 onOpenChange={setIsCreateDialogOpen}
 onCreated={onRefresh}
 />

 {editingConfig && (
 <EditExternalAuthDialog
 config={editingConfig}
 open={!!editingConfig}
 onOpenChange={(open) => !open && setEditingConfig(null)}
 onUpdated={onRefresh}
 />
 )}

 {viewingLogsFor && (
 <AuthAttemptLogsDialog
 configId={viewingLogsFor}
 open={!!viewingLogsFor}
 onOpenChange={(open) => !open && setViewingLogsFor(null)}
 />
 )}
 </>
 );
}