import { useState, useEffect, useCallback } from 'react';
import { Plus, Package, Edit, Trash2, Play, CheckCircle, FileDown, FileUp } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Spinner } from '@/components/loading-states';
import { useToast } from '@/hooks/use-toast';
import { packageService } from '@/services/packageService';
import type { IntegrationPackage } from '@/types/package';
import PackageCreationWizard from './PackageCreationWizard';
import { logger, LogCategory } from '@/lib/logger';
import {
 Table,
 TableBody,
 TableCell,
 TableHead,
 TableHeader,
 TableRow,
} from '@/components/ui/table';
import {
 DropdownMenu,
 DropdownMenuContent,
 DropdownMenuItem,
 DropdownMenuLabel,
 DropdownMenuSeparator,
 DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { MoreHorizontal } from 'lucide-react';

export default function PackageManagement() {
 const [packages, setPackages] = useState<IntegrationPackage[]>([]);
 const [isLoading, setIsLoading] = useState(true);
 const [showCreationWizard, setShowCreationWizard] = useState(false);
 const [showImportDialog, setShowImportDialog] = useState(false);
 const [selectedPackage, setSelectedPackage] = useState<IntegrationPackage | null>(null);
 const { toast } = useToast();

 const loadPackages = useCallback(async () => {
    try {
        setIsLoading(true);
        const response = await packageService.getAllPackages();
        if (response && typeof response === 'object' && 'success' in response && response.success && 'data' in response && response.data) {
            setPackages((response.data as any).content || []);
        }
    } catch (error) {
 logger.error(LogCategory.UI, 'Error loading packages', { error: error });
 toast({
 title: 'Error',
 description: 'Failed to load packages',
 variant: 'destructive',
 });
 } finally {
 setIsLoading(false);
 }
 }, [toast]);

 useEffect(() => {
 loadPackages();
 }, [loadPackages]);

 const handleValidatePackage = async (packageId: string) => {
 try {
 const response = await packageService.validatePackage(packageId);
 if (response.isValid) {
 toast({
 title: 'Package Valid',
 description: 'The package passed all validation checks',
 });
 } else {
 toast({
 title: 'Validation Failed',
 description: response.errors.join(', '),
 variant: 'destructive',
 });
 }
 } catch (error) {
 logger.error(LogCategory.UI, 'Error validating package', { error: error });
 toast({
 title: 'Error',
 description: 'Failed to validate package',
 variant: 'destructive',
 });
 }
 };

 const handleDeployPackage = async (packageId: string) => {
 try {
 const response = await packageService.deployPackage(packageId, 'DEVELOPMENT');
 if (response && typeof response === 'object' && 'success' in response && response.success) {
 toast({
 title: 'Deployment Started',
 description: 'Package deployment has been initiated',
 });
 await loadPackages();
 }
 } catch (error) {
 logger.error(LogCategory.UI, 'Error deploying package', { error: error });
 toast({
 title: 'Error',
 description: 'Failed to deploy package',
 variant: 'destructive',
 });
 }
 };

 const handleDeletePackage = async (packageId: string) => {
 if (!confirm('Are you sure you want to delete this package?')) {
 return;
 }

 try {
 const response = await packageService.deletePackage(packageId);
 if (response && typeof response === 'object' && 'success' in response && response.success) {
 toast({
 title: 'Success',
 description: 'Package deleted successfully',
 });
 await loadPackages();
 }
 } catch (error) {
 logger.error(LogCategory.UI, 'Error deleting package', { error: error });
 toast({
 title: 'Error',
 description: 'Failed to delete package',
 variant: 'destructive',
 });
 }
 };

 const handleExportPackage = async (packageId: string) => {
 try {
 await packageService.exportPackage(packageId);
 toast({
 title: 'Success',
 description: 'Package exported successfully',
 });
 } catch (error) {
 logger.error(LogCategory.UI, 'Error exporting package', { error: error });
 toast({
 title: 'Error',
 description: 'Failed to export package',
 variant: 'destructive',
 });
 }
 };

 const handleImportPackage = async (file: File) => {
 try {
 const response = await packageService.importPackage(file);
 if (response && typeof response === 'object' && 'success' in response && response.success) {
 toast({
 title: 'Success',
 description: 'Package imported successfully',
 });
 setShowImportDialog(false);
 await loadPackages();
 }
 } catch (error) {
 logger.error(LogCategory.UI, 'Error importing package', { error: error });
 toast({
 title: 'Error',
 description: error instanceof Error ? error.message : 'Failed to import package',
 variant: 'destructive',
 });
 }
 };

 const getStatusBadge = (status: IntegrationPackage['status']) => {
 const variants: Record<IntegrationPackage['status'], { variant: 'default' | 'secondary' | 'destructive' | 'outline'; label: string }> = {
 DRAFT: { variant: 'secondary', label: 'Draft' },
 CONFIGURED: { variant: 'default', label: 'Configured' },
 DEPLOYED: { variant: 'default', label: 'Deployed' },
 INACTIVE: { variant: 'outline', label: 'Inactive' },
 };

 const config = variants[status];
 return <Badge variant={config.variant}>{config.label}</Badge>;
 };

 const getSyncTypeBadge = (syncType: IntegrationPackage['syncType']) => {
 return (
 <Badge variant="outline">
 {syncType === 'SYNCHRONOUS' ? 'Sync' : 'Async'}
 </Badge>
 );
 };

 if (isLoading) {
 return (
 <div className="flex items-center justify-center h-96">
 <Spinner size="lg" />
 </div>
 );
 }

 return (
 <div className="space-y-6">
 <div className="flex justify-between items-center">
 <div>
 <h2 className="text-3xl font-bold tracking-tight">Integration Packages</h2>
 <p className="text-muted-foreground">
 Manage integration packages with all their components in one place
 </p>
 </div>
 <div className="space-x-2">
 <Button variant="outline" onClick={() => setShowImportDialog(true)}>
 <FileUp className="mr-2 h-4 w-4" />
 Import
 </Button>
 <Button onClick={() => setShowCreationWizard(true)}>
 <Plus className="mr-2 h-4 w-4" />
 Create Package
 </Button>
 </div>
 </div>

 <Card>
 <CardHeader>
 <CardTitle>Packages</CardTitle>
 <CardDescription>
 View and manage all integration packages
 </CardDescription>
 </CardHeader>
 <CardContent>
 <Table>
 <TableHeader>
 <TableRow>
 <TableHead>Name</TableHead>
 <TableHead>Description</TableHead>
 <TableHead>Type</TableHead>
 <TableHead>Transformation</TableHead>
 <TableHead>Status</TableHead>
 <TableHead>Components</TableHead>
 <TableHead>Created</TableHead>
 <TableHead className="text-right">Actions</TableHead>
 </TableRow>
 </TableHeader>
 <TableBody>
 {packages.length === 0 ? (
 <TableRow>
 <TableCell colSpan={8} className="text-center py-8">
 <Package className="mx-auto h-12 w-12 text-muted-foreground mb-4" />
 <p className="text-muted-foreground">No packages found</p>
 <Button
 variant="outline"
 size="sm"
 className="mt-4"
 onClick={() => setShowCreationWizard(true)}
 >
 Create your first package
 </Button>
 </TableCell>
 </TableRow>
 ) : (
 packages.map((pkg) => (
 <TableRow key={pkg.id}>
 <TableCell className="font-medium">{pkg.name}</TableCell>
 <TableCell className="max-w-xs truncate">
 {pkg.description || '-'}
 </TableCell>
 <TableCell>{getSyncTypeBadge(pkg.syncType)}</TableCell>
 <TableCell>
 {pkg.transformationRequired ? (
 <Badge variant="default">Required</Badge>
 ) : (
 <Badge variant="secondary">Not Required</Badge>
 )}
 </TableCell>
 <TableCell>{getStatusBadge(pkg.status)}</TableCell>
 <TableCell>
 <Badge variant="outline">
 {pkg.componentCount || 0} components
 </Badge>
 </TableCell>
 <TableCell>
 {new Date(pkg.createdAt).toLocaleDateString()}
 </TableCell>
 <TableCell className="text-right">
 <DropdownMenu>
 <DropdownMenuTrigger asChild>
 <Button variant="ghost" className="h-8 w-8 p-0">
 <span className="sr-only">Open menu</span>
 <MoreHorizontal className="h-4 w-4" />
 </Button>
 </DropdownMenuTrigger>
 <DropdownMenuContent align="end">
 <DropdownMenuLabel>Actions</DropdownMenuLabel>
 <DropdownMenuItem
 onClick={() => {
 setSelectedPackage(pkg);
 setShowCreationWizard(true);
 }}
 >
 <Edit className="mr-2 h-4 w-4" />
 Edit
 </DropdownMenuItem>
 <DropdownMenuItem
 onClick={() => handleValidatePackage(pkg.id)}
 >
 <CheckCircle className="mr-2 h-4 w-4" />
 Validate
 </DropdownMenuItem>
 <DropdownMenuItem
 onClick={() => handleDeployPackage(pkg.id)}
 disabled={pkg.status === 'DEPLOYED'}
 >
 <Play className="mr-2 h-4 w-4" />
 Deploy
 </DropdownMenuItem>
 <DropdownMenuSeparator />
 <DropdownMenuItem
 onClick={() => handleExportPackage(pkg.id)}
 >
 <FileDown className="mr-2 h-4 w-4" />
 Export
 </DropdownMenuItem>
 <DropdownMenuSeparator />
 <DropdownMenuItem
 onClick={() => handleDeletePackage(pkg.id)}
 className="text-destructive"
 disabled={pkg.status === 'DEPLOYED'}
 >
 <Trash2 className="mr-2 h-4 w-4" />
 Delete
 </DropdownMenuItem>
 </DropdownMenuContent>
 </DropdownMenu>
 </TableCell>
 </TableRow>
 ))
 )}
 </TableBody>
 </Table>
 </CardContent>
 </Card>

 {showCreationWizard && (
 <PackageCreationWizard
 isOpen={showCreationWizard}
 onClose={() => {
 setShowCreationWizard(false);
 setSelectedPackage(null);
 }}
 onSuccess={() => {
 setShowCreationWizard(false);
 setSelectedPackage(null);
 loadPackages();
 }}
 existingPackage={selectedPackage}
 />
 )}

 {showImportDialog && (
 <ImportPackageDialog
 isOpen={showImportDialog}
 onClose={() => setShowImportDialog(false)}
 onImport={handleImportPackage}
 />
 )}
 </div>
 );
}

// Import dialog component
function ImportPackageDialog({
 isOpen,
 onClose,
 onImport
}: {
 isOpen: boolean;
 onClose: () => void;
 onImport: (file: File) => Promise<void>;
}) {
 const [selectedFile, setSelectedFile] = useState<File | null>(null);
 const [isImporting, setIsImporting] = useState(false);

 const handleImport = async () => {
 if (!selectedFile) return;

 setIsImporting(true);
 try {
 await onImport(selectedFile);
 setSelectedFile(null);
 } finally {
 setIsImporting(false);
 }
 };

 return (
 <Dialog open={isOpen} onOpenChange={onClose}>
 <DialogContent>
 <DialogHeader>
 <DialogTitle>Import Package</DialogTitle>
 </DialogHeader>

 <div className="space-y-4 py-4">
 <div className="space-y-2">
 <Label htmlFor="file">Select Package File</Label>
 <Input
 id="file"
 type="file"
 accept=".json"
 onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
 disabled={isImporting}
 />
 <p className="text-sm text-muted-foreground">
 Select a JSON file exported from another package
 </p>
 </div>

 {selectedFile && (
 <div className="bg-muted p-3 rounded-md">
 <p className="text-sm">
 <strong>Selected file:</strong> {selectedFile.name}
 </p>
 <p className="text-sm text-muted-foreground">
 Size: {(selectedFile.size / 1024).toFixed(2)} KB
 </p>
 </div>
 )}
 </div>

 <div className="flex justify-end space-x-2">
 <Button variant="outline" onClick={onClose} disabled={isImporting}>
 Cancel
 </Button>
 <Button
 onClick={handleImport}
 disabled={!selectedFile || isImporting}
 >
 {isImporting ? (
 <>
 <Spinner size="sm" className="mr-2" />
 Importing...
 </>
 ) : (
 <>
 <FileUp className="mr-2 h-4 w-4" />
 Import Package
 </>
 )}
 </Button>
 </div>
 </DialogContent>
 </Dialog>
 );
}