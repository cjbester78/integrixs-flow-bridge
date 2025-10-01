import { useEffect, useState } from 'react';
import {
 Dialog,
 DialogContent,
 DialogDescription,
 DialogHeader,
 DialogTitle,
} from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { Loader2, Copy, ExternalLink, Globe, FileCode, Server } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { deploymentService } from '@/services/deploymentService';
import { DeploymentInfo } from '@/types/deployment';
import { useToast } from '@/hooks/use-toast';

interface DeploymentDetailsDialogProps {
 flowId: string | null;
 flowName?: string;
 open: boolean;
 onOpenChange: (open: boolean) => void;
}

export function DeploymentDetailsDialog({
 flowId,
 flowName,
 open,
 onOpenChange
}: DeploymentDetailsDialogProps) {
 const [loading, setLoading] = useState(false);
 const [deploymentInfo, setDeploymentInfo] = useState<DeploymentInfo | null>(null);
 const { toast } = useToast();

 useEffect(() => {
 if (open && flowId) {
 fetchDeploymentInfo();
 }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, flowId]);

 const fetchDeploymentInfo = async () => {
 if (!flowId) return;

 setLoading(true);
 try {
 const response = await deploymentService.getDeploymentInfo(flowId);
 if (response.success && response.data) {
 setDeploymentInfo(response.data);
 } else {
 toast({
 variant: 'destructive',
 title: 'Error',
 description: response.error || 'Failed to fetch deployment information',
 });
 }
 } catch (error) {
 toast({
 variant: 'destructive',
 title: 'Error',
 description: 'Failed to fetch deployment information',
 });
 } finally {
 setLoading(false);
 }
 };

 const copyToClipboard = (text: string, label: string) => {
 navigator.clipboard.writeText(text);
 toast({
 title: 'Copied!',
 description: `${label} copied to clipboard`,
 });
 };

 const formatDate = (dateString: string) => {
 return new Date(dateString).toLocaleString();
 };

 if (!open) return null;

 return (
 <Dialog open={open} onOpenChange={onOpenChange}>
 <DialogContent className="max-w-2xl">
 <DialogHeader>
 <DialogTitle className="flex items-center gap-2">
 <Server className="h-5 w-5" />
 Deployment Details
 </DialogTitle>
 <DialogDescription>
 {flowName ? `Deployment information for ${flowName}` : 'Flow deployment information'}
 </DialogDescription>
 </DialogHeader>

 {loading ? (
 <div className="flex justify-center py-8">
 <Loader2 className="h-8 w-8 animate-spin" />
 </div>
 ) : deploymentInfo ? (
 <div className="space-y-6">
 {/* Main Endpoint */}
 <div className="space-y-2">
 <h3 className="text-sm font-medium flex items-center gap-2">
 <Globe className="h-4 w-4" />
 Primary Endpoint
 </h3>
 <div className="flex items-center gap-2">
 <code className="flex-1 p-2 bg-muted rounded text-sm">
 {deploymentInfo.endpoint}
 </code>
 <Button
 size="sm"
 variant="outline"
 onClick={() => copyToClipboard(deploymentInfo.endpoint, 'Endpoint')}
 >
 <Copy className="h-4 w-4" />
 </Button>
 {deploymentInfo.endpoint.startsWith('http') && (
 <Button
 size="sm"
 variant="outline"
 onClick={() => window.open(deploymentInfo.endpoint, '_blank')}
 >
 <ExternalLink className="h-4 w-4" />
 </Button>
 )}
 </div>
 </div>

 {/* WSDL URL for SOAP */}
 {deploymentInfo.metadata?.wsdlUrl && (
 <div className="space-y-2">
 <h3 className="text-sm font-medium flex items-center gap-2">
 <FileCode className="h-4 w-4" />
 WSDL URL (Planned)
 </h3>
 <div className="space-y-2">
 <div className="flex items-center gap-2">
 <code className="flex-1 p-2 bg-muted rounded text-sm">
 {deploymentInfo.metadata?.wsdlUrl || 'N/A'}
 </code>
 <Button
 size="sm"
 variant="outline"
 onClick={() => copyToClipboard(deploymentInfo.metadata?.wsdlUrl || '', 'WSDL URL')}
 disabled={!deploymentInfo.metadata?.wsdlUrl}
 >
 <Copy className="h-4 w-4" />
 </Button>
 </div>
 <p className="text-xs text-muted-foreground">
 Note: The actual SOAP endpoint implementation is pending. This URL shows where the WSDL will be available once the endpoint is fully deployed.
 </p>
 </div>
 </div>
 )}

 {/* API Docs for REST */}
 {deploymentInfo.metadata?.apiDocsUrl && (
 <div className="space-y-2">
 <h3 className="text-sm font-medium">API Documentation</h3>
 <div className="flex items-center gap-2">
 <code className="flex-1 p-2 bg-muted rounded text-sm">
 {deploymentInfo.metadata?.apiDocsUrl || 'N/A'}
 </code>
 <Button
 size="sm"
 variant="outline"
 onClick={() => copyToClipboard(deploymentInfo.metadata?.apiDocsUrl || '', 'API Docs URL')}
 disabled={!deploymentInfo.metadata?.apiDocsUrl}
 >
 <Copy className="h-4 w-4" />
 </Button>
 <Button
 size="sm"
 variant="outline"
 onClick={() => window.open(deploymentInfo.metadata?.apiDocsUrl || '', '_blank')}
 disabled={!deploymentInfo.metadata?.apiDocsUrl}
 >
 <ExternalLink className="h-4 w-4" />
 </Button>
 </div>
 </div>
 )}

 {/* OpenAPI URL for REST */}
 {deploymentInfo.metadata?.openApiUrl && (
 <div className="space-y-2">
 <h3 className="text-sm font-medium">OpenAPI Specification</h3>
 <div className="flex items-center gap-2">
 <code className="flex-1 p-2 bg-muted rounded text-sm">
 {deploymentInfo.metadata?.openApiUrl || 'N/A'}
 </code>
 <Button
 size="sm"
 variant="outline"
 onClick={() => copyToClipboard(deploymentInfo.metadata?.openApiUrl || '', 'OpenAPI URL')}
 disabled={!deploymentInfo.metadata?.openApiUrl}
 >
 <Copy className="h-4 w-4" />
 </Button>
 </div>
 </div>
 )}

 {/* Metadata */}
 <div className="space-y-2">
 <h3 className="text-sm font-medium">Deployment Metadata</h3>
 <div className="grid grid-cols-2 gap-4 text-sm">
 <div>
 <span className="text-muted-foreground">Adapter Type:</span>
 <Badge variant="outline" className="ml-2">
 {deploymentInfo.metadata?.adapterType || 'Unknown'}
 </Badge>
 </div>
 <div>
 <span className="text-muted-foreground">Adapter Mode:</span>
 <Badge variant="outline" className="ml-2">
 {deploymentInfo.metadata?.adapterMode || 'Unknown'}
 </Badge>
 </div>
 <div>
 <span className="text-muted-foreground">Deployed At:</span>
 <span className="ml-2">{formatDate(deploymentInfo.deployedAt)}</span>
 </div>
 {deploymentInfo.deployedBy && (
 <div>
 <span className="text-muted-foreground">Deployed By:</span>
 <span className="ml-2">{deploymentInfo.deployedBy}</span>
 </div>
 )}
 </div>
 </div>

 {/* Additional Metadata */}
 {deploymentInfo.metadata && Object.keys(deploymentInfo.metadata).length > 0 && (
 <div className="space-y-2">
 <h3 className="text-sm font-medium">Additional Information</h3>
 <div className="space-y-1 text-sm">
 {deploymentInfo.metadata.httpMethods && (
 <div>
 <span className="text-muted-foreground">HTTP Methods:</span>
 <span className="ml-2">{deploymentInfo.metadata.httpMethods}</span>
 </div>
 )}
 {deploymentInfo.metadata.contentType && (
 <div>
 <span className="text-muted-foreground">Content Type:</span>
 <span className="ml-2">{deploymentInfo.metadata.contentType}</span>
 </div>
 )}
 {deploymentInfo.metadata.soapVersion && (
 <div>
 <span className="text-muted-foreground">SOAP Version:</span>
 <span className="ml-2">{deploymentInfo.metadata.soapVersion}</span>
 </div>
 )}
 {deploymentInfo.metadata.pollingEnabled !== undefined && (
 <div>
 <span className="text-muted-foreground">Polling:</span>
 <span className="ml-2">
 {deploymentInfo.metadata.pollingEnabled ? 'Enabled' : 'Disabled'}
 </span>
 </div>
 )}
 {deploymentInfo.metadata.filePattern && (
 <div>
 <span className="text-muted-foreground">File Pattern:</span>
 <span className="ml-2">{deploymentInfo.metadata.filePattern}</span>
 </div>
 )}
 </div>
 </div>
 )}
 </div>
 ) : (
 <div className="text-center py-8 text-muted-foreground">
 No deployment information available
 </div>
 )}
 </DialogContent>
 </Dialog>
 );
}
