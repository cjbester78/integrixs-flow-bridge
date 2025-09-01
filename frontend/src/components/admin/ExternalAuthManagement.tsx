import { useState, useEffect } from 'react';
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { useToast } from '@/hooks/use-toast';
import { externalAuthService } from '@/services/externalAuthService';
import { AuthType, ExternalAuthConfig } from '@/types/externalAuth';
import { CreateExternalAuthDialog } from './CreateExternalAuthDialog';
import { EditExternalAuthDialog } from './EditExternalAuthDialog';
import { AuthAttemptLogsDialog } from './AuthAttemptLogsDialog';
import { format } from 'date-fns';
import { ColumnDef } from '@tanstack/react-table';
import { Skeleton } from '@/components/ui/skeleton';

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

      if (response.success) {
        toast({
          title: "Success",
          description: `Authentication configuration ${config.isActive ? 'deactivated' : 'activated'} successfully`
        });
        onRefresh();
      } else {
        throw new Error(response.message || 'Failed to update configuration');
      }
    } catch (error) {
      console.error('Error toggling auth config status:', error);
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

      if (response.success) {
        toast({
          title: "Success",
          description: "Authentication configuration deleted successfully"
        });
        onRefresh();
      } else {
        throw new Error(response.message || 'Failed to delete configuration');
      }
    } catch (error) {
      console.error('Error deleting auth config:', error);
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

      if (response.success) {
        toast({
          title: "Success",
          description: "Authentication test completed successfully"
        });
      } else {
        throw new Error(response.message || 'Test failed');
      }
    } catch (error) {
      console.error('Error testing auth config:', error);
      toast({
        title: "Error",
        description: "Authentication test failed",
        variant: "destructive"
      });
    }
  };

  const columns: ColumnDef<ExternalAuthConfig>[] = [
    {
      accessorKey: 'name',
      header: 'Name',
      cell: ({ row }) => (
        <div className="flex items-center gap-2">
          {getAuthTypeIcon(row.original.authType)}
          <span className="font-medium">{row.getValue('name')}</span>
        </div>
      ),
    },
    {
      accessorKey: 'authType',
      header: 'Type',
      cell: ({ row }) => (
        <Badge variant={getAuthTypeBadgeVariant(row.original.authType)}>
          {row.getValue('authType')}
        </Badge>
      ),
    },
    {
      accessorKey: 'description',
      header: 'Description',
      cell: ({ row }) => (
        <span className="text-sm text-muted-foreground">
          {row.getValue('description') || '-'}
        </span>
      ),
    },
    {
      accessorKey: 'isActive',
      header: 'Status',
      cell: ({ row }) => (
        <Badge variant={row.original.isActive ? 'default' : 'secondary'}>
          {row.original.isActive ? 'Active' : 'Inactive'}
        </Badge>
      ),
    },
    {
      accessorKey: 'createdAt',
      header: 'Created',
      cell: ({ row }) => (
        <span className="text-sm text-muted-foreground">
          {format(new Date(row.getValue('createdAt')), 'MMM d, yyyy')}
        </span>
      ),
    },
    {
      id: 'actions',
      cell: ({ row }) => {
        const config = row.original;

        return (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="h-8 w-8 p-0">
                <span className="sr-only">Open menu</span>
                <MoreVertical className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => setEditingConfig(config)}>
                <Edit className="mr-2 h-4 w-4" />
                Edit
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleToggleStatus(config)}>
                {config.isActive ? (
                  <>
                    <ToggleLeft className="mr-2 h-4 w-4" />
                    Deactivate
                  </>
                ) : (
                  <>
                    <ToggleRight className="mr-2 h-4 w-4" />
                    Activate
                  </>
                )}
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleTest(config)}>
                <TestTube className="mr-2 h-4 w-4" />
                Test Connection
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setViewingLogsFor(config.id)}>
                <Shield className="mr-2 h-4 w-4" />
                View Attempts
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem 
                onClick={() => handleDelete(config)}
                className="text-destructive"
                disabled={isDeleting === config.id}
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        );
      },
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
            searchKey="name"
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